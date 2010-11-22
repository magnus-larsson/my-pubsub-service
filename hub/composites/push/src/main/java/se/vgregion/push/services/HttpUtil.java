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
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpUtil {

    private static HttpClient httpclient;
    static {
//        SchemeRegistry schemeRegistry = new SchemeRegistry();
//        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
//        
//        // configure timeouts
//        HttpParams params = new BasicHttpParams();
//        ConnManagerParams.setMaxTotalConnections(params, 100);
//        HttpConnectionParams.setConnectionTimeout(params, 10000);
//        HttpConnectionParams.setSoTimeout(params, 10000);
//
//        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

//        httpclient = new DefaultHttpClient(cm, params);
        httpclient = new DefaultHttpClient();
    }
    
    public static HttpClient getClient() {
        return new DefaultHttpClient();
    }
    
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
