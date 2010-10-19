/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.push.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;


public class Utils {

    public static HttpEntity createEntity(String s) {
        try {
            return new StringEntity(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void deleteDir(File dir) {
        if(dir.exists()) {
            for(File file : dir.listFiles()) {
                Assert.assertTrue(file.delete());
            }
            Assert.assertTrue(dir.delete());
        }
    }
    
    public static void assertEquals(HttpEntity expected, HttpEntity actual) {
        Assert.assertEquals("ContentLength", expected.getContentLength(), actual.getContentLength());
        
        byte[] expectedBytes = new byte[(int) expected.getContentLength()];
        byte[] actualBytes = new byte[(int) actual.getContentLength()];
        
        Assert.assertArrayEquals(expectedBytes, actualBytes);
    }
    
    public static String readFully(InputStream in) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            StringBuffer sb = new StringBuffer();
            String line = reader.readLine();
            while(line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            return sb.toString();
        } finally {
            in.close();
        }
    }
    
    public static String readFully(File in) throws IOException {
        return readFully(new FileInputStream(in));
    }
}
