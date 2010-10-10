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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;


public class FeedDistributorTest {

    private static final URI FEED_URI = URI.create("http://example.com");
    private static final URI SUB_URI = URI.create("http://example.com/sub1");

    private LinkedBlockingQueue<DistributionRequest> distributionQueue = new LinkedBlockingQueue<DistributionRequest>();
    private FeedDistributor feedDistributor;
    
    @Test
    public void distribute() throws Exception {
        final List<Subscription> subscriptions = new ArrayList<Subscription>();
        Subscription sub1 = new Subscription(FEED_URI, SUB_URI);
        subscriptions.add(sub1);

        
        distributionQueue.put(new DistributionRequest(new Feed(FEED_URI, ContentType.ATOM, SomeFeeds.ATOM_DOCUMENT)));
        
        final LinkedBlockingQueue<DistributionRequest> issuedRequests = new LinkedBlockingQueue<DistributionRequest>();
        
        PushService service = new DefaultPushService(null, null) {
            @Override
            public List<Subscription> getAllSubscriptionsForFeed(URI feed) {
                return subscriptions;
            }

            @Override
            public void distribute(DistributionRequest request) throws IOException {
                issuedRequests.add(request);
            }
        };
        
        feedDistributor = new FeedDistributor(distributionQueue, service);
        feedDistributor.start();

        DistributionRequest request = issuedRequests.poll(10000, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(request);
        Assert.assertEquals(FEED_URI, request.getFeed().getUrl());
    }
    
    @After
    public void after() {
        feedDistributor.stop();
    }
}
