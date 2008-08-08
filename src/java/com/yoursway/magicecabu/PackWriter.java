package com.yoursway.magicecabu;

import static java.util.Collections.indexOfSubList;

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
            proceed();
        } catch (Exit exit) {
            exit.proceed();
        }
    }
    
    private static void proceed() {
        File packTempFile;
        try {
            packTempFile = File.createTempFile("pack", ".zip", new File("."));
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
                File inputFile = new File(line);
                if (!inputFile.isFile()) {
                    System.err.println("file not found: " + inputFile);
                    throw new Exit(3);
                }
                String sha1 = computeHash(digest, buf, inputFile);
                zout.putNextEntry(new ZipEntry(sha1));
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
