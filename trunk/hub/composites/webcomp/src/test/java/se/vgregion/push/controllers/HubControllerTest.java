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

package se.vgregion.push.controllers;

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import se.vgregion.push.services.RetrievalRequest;
import se.vgregion.push.services.SubscriptionMode;
import se.vgregion.push.services.SubscriptionRequest;
import se.vgregion.push.types.Subscription;

public class HubControllerTest {

    private static final URI SUBSCRIPTION_TOPIC = URI.create("http://example.com/topic");

    private static final URI SUBSCRIPTION_CALLBACK = URI.create("http://example.com/callback");

    private LinkedBlockingQueue<RetrievalRequest> retrieverQueue = new LinkedBlockingQueue<RetrievalRequest>();
    
    private HubController controller = new HubController();
    
    @Before
    public void before() {
        controller.setRetrieverQueue(retrieverQueue);
    }
    
    @Test
    public void publication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "publish");
        request.setParameter("hub.url", "http://example.com");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);

        Assert.assertEquals(204, response.getStatus());
        
        RetrievalRequest retrievalRequest = retrieverQueue.poll(10000, TimeUnit.MILLISECONDS);
        
        Assert.assertEquals("http://example.com", retrievalRequest.getUrl().toString());
    }

    @Test
    public void publicationWithoutUrl() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "publish");
        // missing hub.url
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);

        Assert.assertEquals(500, response.getStatus());
        
        Assert.assertTrue(retrieverQueue.isEmpty());
    }

    @Test
    public void publicationWithInvalidUrl() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "publish");
        request.setParameter("hub.url", "dummy");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);

        Assert.assertEquals(500, response.getStatus());
        
        Assert.assertTrue(retrieverQueue.isEmpty());
    }
    
    @Test
    public void subscribe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        request.setParameter("hub.verify", "sync");
        request.setParameter("hub.lease_seconds", "123");
        request.setParameter("hub.secret", "sekrit!");
        request.setParameter("hub.verify_token", "token");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        final LinkedBlockingQueue<Subscription> subscriptions = new LinkedBlockingQueue<Subscription>();
        final LinkedBlockingQueue<SubscriptionRequest> subscriptionRequests = new LinkedBlockingQueue<SubscriptionRequest>();

        controller.setSubscriptionService(new MockPushService() {
            @Override
            public void verify(SubscriptionRequest request) {
                subscriptionRequests.add(request);
            }

            @Override
            public Subscription subscribe(Subscription subscription) {
                subscriptions.add(subscription);
                return subscription;
            }
            
            
        });
        
        controller.post(request, response);

        Assert.assertEquals(204, response.getStatus());
        
        Subscription subscription = subscriptions.poll();
        Assert.assertNotNull(subscription);
        Assert.assertEquals(SUBSCRIPTION_CALLBACK, subscription.getCallback());
        Assert.assertEquals(SUBSCRIPTION_TOPIC, subscription.getTopic());
        Assert.assertEquals("sekrit!", subscription.getSecret());
        
        SubscriptionRequest subscriptionRequest = subscriptionRequests.poll();

        Assert.assertEquals(SubscriptionMode.SUBSCRIBE, subscriptionRequest.getMode());
        Assert.assertEquals(SUBSCRIPTION_CALLBACK, subscriptionRequest.getCallback());
        Assert.assertEquals(SUBSCRIPTION_TOPIC, subscriptionRequest.getTopic());
        Assert.assertEquals(123, subscriptionRequest.getLeaseSeconds());
        Assert.assertEquals("token", subscriptionRequest.getVerifyToken());
    }
    
    @Test
    public void subscriptionWithMissingCallback() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        // hub.callback missing
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        request.setParameter("hub.verify", "sync");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void subscriptionWithMissingTopic() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        // hub.topic missing
        request.setParameter("hub.verify", "sync");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void subscriptionWithMissingVerify() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        // hub.verify missing
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void subscriptionWithInvalidCallback() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", "dummy");
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        request.setParameter("hub.verify", "sync");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void subscriptionWithInvalidTopic() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        request.setParameter("hub.topic", "dummy");
        request.setParameter("hub.verify", "sync");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void subscriptionWithInvalidVerify() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        request.setParameter("hub.verify", "dummy");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void subscriptionWithInvalidLeaseSeconds() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "subscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        request.setParameter("hub.verify", "sync");
        request.setParameter("hub.lease_seconds", "dummy");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);
        
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void unsubscribe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "unsubscribe");
        request.setParameter("hub.callback", SUBSCRIPTION_CALLBACK.toString());
        request.setParameter("hub.topic", SUBSCRIPTION_TOPIC.toString());
        request.setParameter("hub.verify", "sync");
        request.setParameter("hub.verify_token", "token");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        final LinkedBlockingQueue<Subscription> subscriptions = new LinkedBlockingQueue<Subscription>();
        final LinkedBlockingQueue<SubscriptionRequest> subscriptionRequests = new LinkedBlockingQueue<SubscriptionRequest>();

        controller.setSubscriptionService(new MockPushService() {
            @Override
            public void verify(SubscriptionRequest request) {
                subscriptionRequests.add(request);
            }

            @Override
            public Subscription subscribe(Subscription subscription) {
                subscriptions.add(subscription);
                return subscription;
            }
        });
        
        controller.post(request, response);

        Assert.assertEquals(204, response.getStatus());
        
        Subscription subscription = subscriptions.poll();
        Assert.assertNotNull(subscription);
        Assert.assertEquals(SUBSCRIPTION_CALLBACK, subscription.getCallback());
        Assert.assertEquals(SUBSCRIPTION_TOPIC, subscription.getTopic());
        
        SubscriptionRequest subscriptionRequest = subscriptionRequests.poll();

        Assert.assertEquals(SubscriptionMode.UNSUBSCRIBE, subscriptionRequest.getMode());
        Assert.assertEquals(SUBSCRIPTION_CALLBACK, subscriptionRequest.getCallback());
        Assert.assertEquals(SUBSCRIPTION_TOPIC, subscriptionRequest.getTopic());
        Assert.assertEquals("token", subscriptionRequest.getVerifyToken());
    }

    
    @Test
    public void invalidMode() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.setParameter("hub.mode", "dummy");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        controller.post(request, response);

        Assert.assertEquals(500, response.getStatus());
        
        Assert.assertTrue(retrieverQueue.isEmpty());
    }
}
