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

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;


public class Utils {

    public static HttpEntity createEntity(String s) {
        try {
            return new StringEntity(s);
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
}
