package com.yoursway.magicecabu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LsPack {
    
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
            if (args.length != 1) {
                System.err.println("Usage: java -jar xxxxxx pack.zip");
                throw new Exit(13);
            }
            File pack = new File(args[0]);
            if (!pack.isFile()) {
                System.err.println("invalid file: " + pack.getPath());
                throw new Exit(5);
            }
            proceed(pack);
        } catch (Exit exit) {
            exit.proceed();
        }
    }
    
    private static void proceed(File pack) {
        try {
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(pack), 1024 * 1024));
            ZipEntry entry = zin.getNextEntry();
            byte[] dummy = new byte[1024 * 1024];
            while (entry != null) {
                String sha1 = entry.getName();
                long size = entry.getSize();
                if (size == -1) {
                    size = 0;
                    int segment = zin.read(dummy);
                    while (segment > 0) {
                        size += segment;
                        segment = zin.read(dummy);
                    }
                }
                System.out.println("B\t" + sha1 + "\t" + size);
                entry = zin.getNextEntry();
            }
            zin.close();
        } catch (IOException e) {
            System.err.println("lspack: I/O error");
            throw new Exit(1);
        }
    }
    
}
