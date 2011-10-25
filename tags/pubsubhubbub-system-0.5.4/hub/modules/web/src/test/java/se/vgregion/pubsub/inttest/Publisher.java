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
import org.junit.Assert;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.content.AbstractSerializer;



public class Publisher {

    private String localServerName;
    private LocalTestServer server;
    private Feed feed;
    
    public Publisher(String host) throws Exception {
        this.localServerName = host;
        server = new LocalTestServer(null, null);
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setHeader("Content-type", ContentType.ATOM.toString());
                response.setEntity(new StringEntity(AbstractSerializer.printFeed(ContentType.ATOM, feed).toXML()));
            }
        });
        server.start();
    }
    
    public URI getUrl() {
        return buildTestUrl(localServerName, server, "/");
    }
    
    public void publish(URI hub, Feed feed) throws URISyntaxException, IOException {
    	publish(hub, feed, getUrl().toString());
    }
    
    public void publish(URI hub, Feed feed, String...urls) throws URISyntaxException, IOException {
    	this.feed = feed;
    	
    	HttpPost post = new HttpPost(hub);
    	
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
    	parameters.add(new BasicNameValuePair("hub.mode", "publish"));
    	for(String url : urls) {
    		parameters.add(new BasicNameValuePair("hub.url", url));
    	}
    	
    	post.setEntity(new UrlEncodedFormEntity(parameters));
    	
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse response = client.execute(post);
    	
    	Assert.assertEquals(204, response.getStatusLine().getStatusCode());
    }
    
    private static URI buildTestUrl(String host, LocalTestServer server, String path) {
        try {
            return new URI("http://" + host + ":" + server.getServicePort() + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void stop() throws Exception {
        server.stop();
    }
}
