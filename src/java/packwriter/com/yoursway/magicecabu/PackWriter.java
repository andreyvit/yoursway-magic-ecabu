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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackWriter {
    
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
            File packDir = new File(".");
            if (args.length >= 2 && "-d".equals(args[0])) {
                packDir = new File(args[1]);
                if (!packDir.isDirectory()) {
                    System.err.println("-d refers to an invalid directory: " + packDir.getPath());
                    throw new Exit(5);
                }
            }
            proceed(packDir);
        } catch (Exit exit) {
            exit.proceed();
        }
    }
    
    private static void proceed(File packDir) {
        File packTempFile;
        try {
            packTempFile = File.createTempFile("pack", ".zip", packDir);
        } catch (IOException e) {
            System.err.println("pack writer: cannot create a temporary file");
            throw new Exit(1);
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("sha-1");
        } catch (NoSuchAlgorithmException e1) {
            System.err.println("internal error: SHA1 algorithm not available in this JRE");
            throw new Exit(2);
        }
        BufferedReader listIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(packTempFile),
                    1024 * 1024);
            ZipOutputStream zout = new ZipOutputStream(out);
            byte[] buf = new byte[10 * 1024 * 1024];
            for (String line = listIn.readLine(); line != null; line = listIn.readLine()) {
                if ((line = line.trim()).length() == 0)
                    continue;
                String[] parts = line.split("\t");
                
                String sha1;
                long time;
                File inputFile;
                if ("LF".equals(parts[0])) {
                    sha1 = parts[1];
                    time = Long.parseLong(parts[3]);
                    inputFile = new File(parts[6]);
                } else {
                    inputFile = new File(line);
                    if (!inputFile.isFile()) {
                        System.err.println("file not found: " + inputFile);
                        throw new Exit(3);
                    }
                    sha1 = computeHash(digest, buf, inputFile);
                    time = inputFile.lastModified();
                }
                
                ZipEntry entry = new ZipEntry(sha1);
                // could set size here too, but it would be useless, because without knowledge of the compressed
                // size and the CRC32 checksum, the size won't be written to the zip file anyway
                entry.setTime(time);
                zout.putNextEntry(entry);
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));
                try {
                    copyStreamToStream(zout, buf, in);
                } finally {
                    in.close();
                }
            }
            zout.finish();
            zout.close();
            String sha1 = computeHash(digest, buf, packTempFile);
            File packFile = new File(packTempFile.getParentFile(), sha1 + ".zip");
            if (!packTempFile.renameTo(packFile)) {
                System.err.println("cannot rename " + packTempFile + " into " + packFile);
                throw new Exit(4);
            }
            System.out.println(sha1);
        } catch (IOException e) {
            System.err.println("pack writer: I/O error");
            packTempFile.delete();
            throw new Exit(1);
        }
    }

    private static void copyStreamToStream(OutputStream out, byte[] buf, InputStream in)
            throws IOException {
        int len = in.read(buf);
        while (len > 0) {
            out.write(buf, 0, len);
            len = in.read(buf);
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
