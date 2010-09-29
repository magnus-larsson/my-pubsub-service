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

package se.vgregion.push.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
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

import se.vgregion.push.services.DistributionRequest;
import se.vgregion.push.services.FeedDistributor;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;


public class FeedDistributorTest {

    private static final File TMP = new File("target/test-tmp");
    private static final File FEED = new File(TMP, "feed");
    private static final URI FEED_URI = URI.create("http://example.com");
    private static final URI SUB_URI = URI.create("http://example.com/sub1");

    private static final HttpEntity TEST_ENTITY = Utils.createEntity("hello world");
    
    private LinkedBlockingQueue<DistributionRequest> distributionQueue = new LinkedBlockingQueue<DistributionRequest>();
    private FeedDistributor feedDistributor;
    
    private LocalTestServer server = new LocalTestServer(null, null);
    
    @Before
    public void before() throws Exception {
        Utils.deleteDir(TMP);
        Assert.assertTrue("Failed to create " + TMP.getAbsolutePath(), TMP.mkdirs());
        
        FileOutputStream fos = new FileOutputStream(FEED);
        TEST_ENTITY.writeTo(fos);
        fos.close();
        
        server.start();
        
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        Subscription sub1 = new Subscription(FEED_URI, buildSubscriptionUrl("/sub1"));
        subscriptions.add(sub1);
        
        MockSubscriptionService service = new MockSubscriptionService(subscriptions);
        
        feedDistributor = new FeedDistributor(distributionQueue, service);
        feedDistributor.start();
    }
    
    
    @Test
    public void test() throws Exception {
        distributionQueue.put(new DistributionRequest(new Feed(FEED_URI, TEST_ENTITY.getContent())));
        
        final LinkedBlockingQueue<HttpRequest> issuedRequests = new LinkedBlockingQueue<HttpRequest>();
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                issuedRequests.add(request);
            }
        });
        
        HttpRequest request = issuedRequests.poll(10000, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(request);
        Assert.assertTrue(request instanceof HttpEntityEnclosingRequest);
        
        HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
        Utils.assertEquals(TEST_ENTITY, entity);
    }
    
    private URI buildSubscriptionUrl(String path) throws URISyntaxException {
        return new URI("http://" + server.getServiceHostName() + ":" + server.getServicePort() + path);
    }
    
    @After
    public void after() {
        feedDistributor.stop();
        
        Utils.deleteDir(TMP);
    }
}
