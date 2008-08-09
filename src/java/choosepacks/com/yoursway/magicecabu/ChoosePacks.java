package com.yoursway.magicecabu;

import static java.lang.Long.parseLong;
import static java.util.Collections.max;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChoosePacks {
    
    @SuppressWarnings("serial")
    static class Exit extends RuntimeException {
        
        private final int code;
        
        public Exit(int code) {
            this.code = code;
        }
        
        public void proceed() {
            System.exit(code);
        }
        
    }
    
    private static Pattern NUMBER = Pattern.compile("^[0-9]+$");
    private static Pattern NUMBER_AND_MUL = Pattern.compile("^([0-9]+)\\s*([bkmg])$", CASE_INSENSITIVE);
    
    public static long char2mul(char c) {
        switch (Character.toLowerCase(c)) {
        case 'b':
            return 1;
        case 'k':
            return 1024;
        case 'm':
            return 1024 * 1024;
        case 'g':
            return 1024 * 1024 * 1024;
        }
        throw new IllegalArgumentException("Not a multiplier: " + c);
    }
    
    public static long parseSize(String size) {
        size = size.trim();
        if (NUMBER.matcher(size).find())
            return Long.parseLong(size);
        else {
            Matcher matcher = NUMBER_AND_MUL.matcher(size);
            if (matcher.find())
                return Long.parseLong(matcher.group(1)) * char2mul(matcher.group(2).charAt(0));
            throw new IllegalArgumentException("Not a valid size specifier: " + size);
        }
    }
    
    private static Pattern FLOAT_NUMBER = Pattern.compile("^[0-9]+(\\.[0-9]+)?$");
    
    public static double parsePercents(String opercents) {
        String percents = opercents.trim();
        double mul = 1.0;
        if (percents.endsWith("%")) {
            mul = 0.01;
            percents = percents.substring(0, percents.length() - 1).trim();
        }
        if (FLOAT_NUMBER.matcher(percents).find()) {
            double p = Double.parseDouble(percents);
            if (p > 1.0001)
                mul = 0.01;
            return p * mul;
        }
        throw new IllegalArgumentException("Not a valid percent specifier: " + percents);
    }
    
    public static void main(String[] args) {
        try {
            long absThreshold = 1024 * 512;
            double relThreshold = 0.2;
            for (int i = 0; i < args.length; i++) {
                if ("--absthreshold".equals(args[i])) {
                    String value = args[++i];
                    absThreshold = parseSize(value);
                } else if ("--relthreshold".equals(args[i])) {
                    String value = args[++i];
                    relThreshold = parsePercents(value);
                } else {
                    System.err.println("unknown option: " + args[i]);
                    throw new Exit(10);
                }
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
            proceed(in, out, absThreshold, relThreshold);
        } catch (Exit exit) {
            exit.proceed();
        }
    }
    
    static class Blob {
        
        private final String sha1;
        
        private final long size;
        
        private boolean covered = false;
        
        public Blob(String sha1, long size) {
            if (sha1 == null)
                throw new NullPointerException("sha1 is null");
            this.sha1 = sha1;
            this.size = size;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public long getSize() {
            return size;
        }
        
        public boolean isCovered() {
            return covered;
        }
        
        public void markCovered() {
            this.covered = true;
        }
        
    }
    
    static class Pack implements Comparable<Pack> {
        
        private final String sha1;
        
        private final Collection<Blob> usefulBlobs = new ArrayList<Blob>();
        
        private long usefulSize = 0;
        
        private long uselessSize = 0;
        
        private double uselessRatio;
        
        public Pack(String sha1) {
            if (sha1 == null)
                throw new NullPointerException("sha1 is null");
            this.sha1 = sha1;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public void addUseful(Blob blob) {
            if (blob == null)
                throw new NullPointerException("blob is null");
            usefulBlobs.add(blob);
            usefulSize += blob.getSize();
        }
        
        public void addUseless(long size) {
            uselessSize += size;
        }
        
        public void calculateStatistics() {
            for (Blob blob : usefulBlobs)
                if (blob.isCovered()) {
                    long size = blob.getSize();
                    usefulSize -= size;
                    uselessSize += size;
                }
            uselessRatio = uselessSize * 1.0 / (usefulSize + uselessSize);
        }
        
        public boolean matches(long absThreshold, double relThreshold) {
            return (absThreshold > uselessSize) || (relThreshold > uselessRatio);
        }
        
        public int compareTo(Pack o) {
            return Long.signum(usefulSize - o.usefulSize);
        }
        
        public int markBlobsAsCovered() {
            int covered = 0;
            for (Blob blob : usefulBlobs)
                if (!blob.isCovered()) {
                    blob.markCovered();
                    covered++;
                }
            return covered;
        }
        
    }
    
    public static void proceed(BufferedReader in, PrintWriter out, long absThreshold, double relThreshold) {
        Map<String, Blob> blobs = new HashMap<String, Blob>();
        List<Pack> packs = new LinkedList<Pack>();
        readInput(in, blobs, packs);
        
        Collection<Pack> chosen = new ArrayList<Pack>(packs.size());
        int blobsToGo = blobs.size();
        
        while (blobsToGo > 0) {
            for (Iterator<Pack> iterator = packs.iterator(); iterator.hasNext();) {
                Pack pack = (Pack) iterator.next();
                pack.calculateStatistics();
                if (!pack.matches(absThreshold, relThreshold))
                    iterator.remove();
            }
            
            if (packs.isEmpty())
                break;
            Pack maxPack = max(packs);
            chosen.add(maxPack);
            blobsToGo -= maxPack.markBlobsAsCovered();
        }
        
        writeOutput(out, blobs, chosen);
    }
    
    private static void writeOutput(PrintWriter out, Map<String, Blob> blobs, Collection<Pack> chosen) {
        for (Pack pack : chosen)
            out.println("P " + pack.getSha1());
        for (Blob blob : blobs.values())
            if (!blob.isCovered())
                out.println("B " + blob.getSha1() + " " + blob.getSize());
    }
    
    private static void readInput(BufferedReader in, Map<String, Blob> blobs, Collection<Pack> packs) {
        Pack currentPack = null;
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if ((line = line.trim()).length() == 0)
                    continue;
                String[] parts = line.split(" ");
                String cmd = parts[0];
                if ("P".equals(cmd)) {
                    currentPack = new Pack(parts[1]);
                    packs.add(currentPack);
                } else if ("B".equals(cmd)) {
                    String sha1 = parts[1];
                    if (currentPack == null)
                        blobs.put(sha1, new Blob(sha1, parseLong(parts[2])));
                    else {
                        Blob blob = blobs.get(sha1);
                        if (blob == null)
                            currentPack.addUseless(parseLong(parts[2]));
                        else
                            currentPack.addUseful(blob);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("pack writer: I/O error");
            throw new Exit(1);
        }
    }
    
}
