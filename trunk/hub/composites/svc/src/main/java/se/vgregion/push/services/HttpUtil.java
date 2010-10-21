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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nu.xom.Document;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;

public class HttpUtil {

    public static boolean successStatus(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status >= 200 && status <300;
    }
    
    public static String readContent(HttpEntity entity) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entity.writeTo(out);
        
        return out.toString("UTF-8");
    }

    public static HttpEntity createEntity(String s) {
        try {
            return new StringEntity(s, "UTF-8");
        } catch (UnsupportedEncodingException shouldNeverHappen) {
            throw new RuntimeException(shouldNeverHappen);
        }
    }

    public static HttpEntity createEntity(Document doc) {
        try {
            return new StringEntity(doc.toXML(), "UTF-8");
        } catch (UnsupportedEncodingException shouldNeverHappen) {
            throw new RuntimeException(shouldNeverHappen);
        }
    }
    
    public static void closeQuitely(HttpResponse response) {
        if(response != null && response.getEntity() != null) {
            try {
                response.getEntity().getContent().close();
            } catch (IOException ignore) { 
            } catch (IllegalStateException ignore) { }
        }
    }
}
