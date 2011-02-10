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

package se.vgregion.pubsub.inttest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

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

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.content.AbstractParser;
import se.vgregion.pubsub.push.SubscriptionMode;


public class Subscriber {

    private String localServerName;
    private List<SubscriberListener> listeners = new ArrayList<SubscriberListener>();
    private LocalTestServer server;
    
    public Subscriber(String localServerName) throws Exception {
        this(localServerName, null);
    }
    
    public Subscriber(String localServerName, final SubscriberResult result) throws Exception {
        this.localServerName = localServerName;
        server = new LocalTestServer(null, null);
        
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                String challenge = getQueryParamValue(request.getRequestLine().getUri(), "hub.challenge");
                
                if(challenge != null) {
                    doVerify();
                    // subscription verification, confirm
                    response.setEntity(new StringEntity(challenge));
                } else if(request instanceof HttpEntityEnclosingRequest) {
                    if(result != null && result.fail()) {
                        response.setStatusCode(500);
                    }
                    HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
                    doPublish(entity);
                } else {
                    System.err.println("Unknown request");
                }
            }
        });
        server.start();
    }
    
    private void doPublish(HttpEntity entity) {
        ContentType contentType = ContentType.fromValue(entity.getContentType().getValue());
        Feed feed;
        try {
            feed = AbstractParser.create(contentType).parse(entity.getContent(), contentType);
            for(SubscriberListener listener : listeners) {
                listener.published(feed);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    private void doVerify() {
        for(SubscriberListener listener : listeners) {
            listener.verified();
        }
    }

    
    public void subscribe(SubscriptionMode mode, URI hub, URI topic) throws URISyntaxException, IOException {
        HttpPost post = new HttpPost(hub);
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("hub.callback", buildTestUrl(localServerName, server, "/").toString()));
        if(mode == SubscriptionMode.SUBSCRIBE) {
            parameters.add(new BasicNameValuePair("hub.mode", "subscribe"));
        } else {
            parameters.add(new BasicNameValuePair("hub.mode", "unsubscribe"));
        }
        parameters.add(new BasicNameValuePair("hub.topic", topic.toString()));
        parameters.add(new BasicNameValuePair("hub.verify", "sync"));
        
        post.setEntity(new UrlEncodedFormEntity(parameters));
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(post);
        
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());
    }
    
    public void addListener(SubscriberListener listener) {
        this.listeners.add(listener);
    }
    
    private static URI buildTestUrl(String localServerName, LocalTestServer server, String path) throws URISyntaxException {
        return new URI("http://" + localServerName + ":" + server.getServicePort() + path);
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

    public void stop() throws Exception {
        server.stop();
    }

}
