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

package se.vgregion.push.inttest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import se.vgregion.push.types.AbstractParser;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;


public class Subscriber {

    private List<PublicationListener> listeners = new ArrayList<PublicationListener>();
    private LocalTestServer server;
    
    public Subscriber() throws Exception {
        this(null);
    }
    
    public Subscriber(final SubscriberResult result) throws Exception {
        server = new LocalTestServer(null, null);
        
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                String challenge = getQueryParamValue(request.getRequestLine().getUri(), "hub.challenge");
                
                if(challenge != null) {
                    // subscription verification, confirm
                    response.setEntity(new StringEntity(challenge));
                } else if(request instanceof HttpEntityEnclosingRequest) {
                    if(result.fail()) {
                        response.setStatusCode(500);
                    }
                    HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
                    publish(entity);
                } else {
                    System.err.println("Unknown request");
                }
            }
        });
        server.start();
    }
    
    private void publish(HttpEntity entity) {
        ContentType contentType = ContentType.fromValue(entity.getContentType().getValue());
        Feed feed;
        try {
            feed = AbstractParser.create(contentType).parse(URI.create("http://example.com"), entity.getContent());
            for(PublicationListener listener : listeners) {
                listener.published(feed);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public void subscribe(URI hub, URI topic) throws URISyntaxException, IOException {
        HttpPost post = new HttpPost(hub);
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("hub.callback", buildTestUrl(server, "/").toString()));
        parameters.add(new BasicNameValuePair("hub.mode", "subscribe"));
        parameters.add(new BasicNameValuePair("hub.topic", topic.toString()));
        parameters.add(new BasicNameValuePair("hub.verify", "sync"));
        
        post.setEntity(new UrlEncodedFormEntity(parameters));
        
        DefaultHttpClient client = new DefaultHttpClient();
        client.execute(post);
    }
    
    public void addListener(PublicationListener listener) {
        this.listeners.add(listener);
    }
    
    private static URI buildTestUrl(LocalTestServer server, String path) throws URISyntaxException {
        return new URI("http://" + server.getServiceHostName() + ":" + server.getServicePort() + path);
    }


    private static String getQueryParamValue(String url, String name) {
        int start = url.indexOf(name);
        if(start > -1) {
            int end = url.indexOf("&", start + 1);
            if(end == -1) {
                end = url.length();
            }
            
            return url.substring(start + name.length() +1, end);
        } else {
            return null;
        }
    }

}
