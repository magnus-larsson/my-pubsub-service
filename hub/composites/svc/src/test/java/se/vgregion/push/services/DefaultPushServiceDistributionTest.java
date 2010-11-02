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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;

public class DefaultPushServiceDistributionTest {

    private static final URI FEED_URI = URI.create("http://example.com");
    
    private DefaultPushService service;
    private LocalTestServer server = new LocalTestServer(null, null);
    
    @Before
    public void before() throws Exception {
        server.start();
    }
    
    private URI buildTestUrl(String path) throws URISyntaxException {
        return new URI("http://" + server.getServiceHostName() + ":" + server.getServicePort() + path);
    }
    
    @Test
    public void test() throws Exception {
        SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);

        Subscription sub = new Subscription(FEED_URI, buildTestUrl("/sub"));
        sub.setLastUpdated(new DateTime(2010, 1, 1, 0, 0, 0, 0));
        when(subscriptionRepository.findByTopic(any(URI.class))).thenReturn(Arrays.asList(sub));
        
        service = new DefaultPushService(subscriptionRepository, mock(FeedRepository.class));
        
        final LinkedBlockingQueue<HttpRequest> issuedRequests = new LinkedBlockingQueue<HttpRequest>();
        final LinkedBlockingQueue<byte[]> issuedRequestBodies = new LinkedBlockingQueue<byte[]>();
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                issuedRequests.add(request);
                
                HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                entity.writeTo(buffer);
                issuedRequestBodies.add(buffer.toByteArray());
            }
        });
        
        service.distribute(new DistributionRequest(new Feed(FEED_URI, ContentType.ATOM, SomeFeeds.ATOM1)));
        
        HttpRequest request = issuedRequests.poll(10000, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(request);
        Assert.assertEquals(ContentType.ATOM.toString(), request.getHeaders("Content-Type")[0].getValue());
        Assert.assertTrue(request instanceof HttpEntityEnclosingRequest);
        
        HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
        
        Assert.assertNotNull(entity);
        
        Document actualAtom = new Builder().build(new ByteArrayInputStream(issuedRequestBodies.poll()));

        Assert.assertEquals(1, actualAtom.getRootElement().getChildElements("entry", Feed.NS_ATOM).size());
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
