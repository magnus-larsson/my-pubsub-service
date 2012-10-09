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

package se.vgregion.pubsub.push.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.content.AbstractSerializer;
import se.vgregion.pubsub.push.UnitTestConstants;

public class DefaultFeedRetrieverTest {

    private LocalTestServer server = new LocalTestServer(null, null);
    private HttpEntity testEntity = HttpUtil.createEntity(AbstractSerializer.printFeed(ContentType.ATOM, UnitTestConstants.atom1()));
    @Mock private PushSubscriberManager pushSubscriberManager;
    private DefaultFeedRetriever retriever;
    
    @Before
    public void before() throws Exception {
    	MockitoAnnotations.initMocks(this);
    	
        server.start();

        retriever = new DefaultFeedRetriever(pushSubscriberManager);
    }
    
    private URI buildTestUrl(String path) throws URISyntaxException {
        return new URI("http://" + server.getServiceHostName() + ":" + server.getServicePort() + path);
    }
    
    @Test
    public void simpleRetrieval() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setEntity(testEntity);
            }});
        
        URI url = buildTestUrl("/test");
        
        retriever.retrieve(url);
        
        ArgumentCaptor<Feed> publishedFeed = ArgumentCaptor.forClass(Feed.class);
        Mockito.verify(pushSubscriberManager).publish(Mockito.eq(url), publishedFeed.capture());
        
        Assert.assertEquals("f1", publishedFeed.getValue().getFeedId());
    }

    /**
     * Fragments should only be used during retrievel and stripped before publication
     */
    @Test
    public void retrievalWithFragment() throws Exception {
        final BlockingQueue<String> paths = new LinkedBlockingQueue<String>();
        
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                paths.add(request.getRequestLine().getUri());
                response.setEntity(testEntity);
            }});
        
        String retrivalPath = "/test#foo";
        URI publicationUrl = buildTestUrl("/test");
        URI url = buildTestUrl(retrivalPath);
        
        retriever.retrieve(url);
        
        String path = paths.poll(2000, TimeUnit.MILLISECONDS);

        // retrived URI must contain fragment
        Assert.assertEquals(retrivalPath, path);
        
        ArgumentCaptor<Feed> publishedFeed = ArgumentCaptor.forClass(Feed.class);
        
        // published URI must no contain fragment
        Mockito.verify(pushSubscriberManager).publish(Mockito.eq(publicationUrl), publishedFeed.capture());
        
        Assert.assertEquals("f1", publishedFeed.getValue().getFeedId());
    }


    
    @Test(expected=IOException.class)
    public void nonExistingFeed() throws Exception {
        server.register("/*", new HttpRequestHandler() {

            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setStatusCode(404);
            }});
        
        
        retriever.retrieve(buildTestUrl("/test"));
    }

    
    @After
    public void after() {
        try {
            server.stop();
        } catch (Exception e) {
            // ignore
        }
    }
}
