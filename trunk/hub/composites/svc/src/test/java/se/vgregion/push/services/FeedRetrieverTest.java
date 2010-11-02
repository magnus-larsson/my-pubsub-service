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

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;

public class FeedRetrieverTest {

    private static final URI TEST_URI = URI.create("http://example.com");
    private static final HttpEntity TEST_ENTITY = HttpUtil.createEntity(SomeFeeds.ATOM1.toXML());
    
    private LinkedBlockingQueue<RetrievalRequest> retrievalQueue = new LinkedBlockingQueue<RetrievalRequest>();
    private LinkedBlockingQueue<DistributionRequest> distributionQueue = new LinkedBlockingQueue<DistributionRequest>();
    
    private FeedRetriever feedRetriever;
    
    @Before
    public void before() throws Exception {
        PushService service = mock(PushService.class);
        when(service.retrieve(any(URI.class))).thenReturn(new Feed(TEST_URI, ContentType.ATOM, TEST_ENTITY.getContent()));

        feedRetriever = new FeedRetriever(retrievalQueue, distributionQueue, service);
        feedRetriever.start();
    }
    
    @Test
    public void test() throws Exception {
        retrievalQueue.put(new RetrievalRequest(TEST_URI));
        
        DistributionRequest distributionRequest = distributionQueue.poll(1000, TimeUnit.MILLISECONDS);
        
        Assert.assertNotNull(distributionRequest);
        Assert.assertEquals(TEST_URI, distributionRequest.getFeed().getUrl());
        Assert.assertNotNull(distributionRequest.getFeed().createDocument());
    }
    
    @After
    public void after() {
        feedRetriever.stop();
    }
}
