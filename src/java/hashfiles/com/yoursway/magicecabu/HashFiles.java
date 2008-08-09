package com.yoursway.magicecabu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HashFiles {
    
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
    
    public static void main(String[] args) {
        try {
            Mode mode = Mode.BLOB;
            for (int i = 0; i < args.length; i++)
                if ("--blob".equals(args[i]))
                    mode = Mode.BLOB;
                else if ("--tree".equals(args[i]))
                    mode = Mode.TREE;
            proceed(mode);
        } catch (Exit exit) {
            exit.proceed();
        }
    }
    
    enum Mode {
        
        BLOB {
            public void output(PrintStream out, File file, String sha1, String[] parts) {
                System.out.println("B\t" + sha1 + "\t" + file.length());
            }
        },
        
        TREE {
            public void output(PrintStream out, File file, String sha1, String[] parts) {
                System.out.println("LF\t" + sha1 + "\t" + file.length() + "\t" + file.lastModified() + "\t" + "-" /*(file.isExecutable() ? "E" : "-")*/ + "\t" + parts[1] + "\t" + file.getPath());
            }
        };
        
        public abstract void output(PrintStream out, File file, String sha1, String[] parts);
        
    }
    
    private static void proceed(Mode mode) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("sha-1");
        } catch (NoSuchAlgorithmException e1) {
            System.err.println("hashfiles: SHA1 algorithm not available in this JRE");
            throw new Exit(2);
        }
        BufferedReader listIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            byte[] buf = new byte[10 * 1024 * 1024];
            for (String line = listIn.readLine(); line != null; line = listIn.readLine()) {
                if ((line = line.trim()).length() == 0)
                    continue;
                String[] parts = line.split("\t");
                File inputFile = new File(parts[0]);
                if (!inputFile.isFile()) {
                    System.err.println("hashfiles: file not found: " + inputFile);
                    throw new Exit(3);
                }
                String sha1 = computeHash(digest, buf, inputFile);
                mode.output(System.out, inputFile, sha1, parts);
            }
        } catch (IOException e) {
            System.err.println("hashfiles: I/O error");
            throw new Exit(1);
        }
    }
    
    public static String computeHash(MessageDigest digest, byte[] buf, File inputFile) throws IOException {
        digest.reset();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));
        int len = in.read(buf);
        while (len > 0) {
            digest.update(buf, 0, len);
            len = in.read(buf);
        }
        in.close();
        return asHex(digest.digest());
    }
    
    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', };
    
    /**
     * Turns array of bytes into string representing each byte as unsigned hex
     * number.
     * 
     * Copied from fast_md5 implementation, see
     * http://www.twmacinta.com/myjava/fast_md5.php
     * 
     * @param hash
     *            Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    private static String asHex(byte hash[]) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }
    
}
