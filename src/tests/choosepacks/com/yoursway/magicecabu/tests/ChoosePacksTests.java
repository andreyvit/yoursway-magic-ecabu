package com.yoursway.magicecabu.tests;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.yoursway.magicecabu.ChoosePacks;

public class ChoosePacksTests {
    
    void check(String in, String expected) {
        StringWriter out = new StringWriter();
        ChoosePacks.proceed(new BufferedReader(new StringReader(in)), new PrintWriter(out), 1000, 0.5);
        assertEquals(expected.trim(), out.toString().trim());
    }
    
    @Test
    public void entirelyCoveredBySinglePack() throws Exception {
        check("B a 100\n" + "P xx\n" + "B a 100\n", "P xx");
    }
    
    @Test
    public void singlePackWithLeftover() throws Exception {
        check("B a 100\n" + "B b 200\n" + "P xx\n" + "B a 100\n", "P xx\n" + "B b 200\n");
    }
    
}
