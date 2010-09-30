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
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.services.SubscriptionMode;
import se.vgregion.push.services.SubscriptionRequest;
import se.vgregion.push.services.PushService;
import se.vgregion.push.types.Subscription;


public class DefaultPushServiceTest {

    private static final URI CALLBACK = URI.create("http://example.com/callback");
    private static final URI TOPIC = URI.create("http://example.com/topic");
    
    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    
    private SubscriptionRepository repository = ctx.getBean(SubscriptionRepository.class);
    private PushService service = ctx.getBean(PushService.class);

    private LocalTestServer server = new LocalTestServer(null, null);
    
    private SubscriptionRequest subscriptionRequest;
    
    @Before
    public void before() throws Exception {
        server.start();
        
        URI callback = buildTestUrl("/callback");
        URI topic = buildTestUrl("/topic");
        long leaseSeconds = 123;
        String verifyToken = "token";
        
        subscriptionRequest = new SubscriptionRequest(SubscriptionMode.SUBSCRIBE, callback, topic, leaseSeconds, verifyToken);

    }
    
    private URI buildTestUrl(String path) throws URISyntaxException {
        return new URI("http://" + server.getServiceHostName() + ":" + server.getServicePort() + path);
    }
    
    @Test
    public void verify() throws Exception {
        final LinkedBlockingQueue<HttpRequest> requests = new LinkedBlockingQueue<HttpRequest>();
        
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                requests.add(request);
                
                response.setEntity(new StringEntity(getQueryParamValue(request.getRequestLine().getUri(), "hub.challenge")));
            }});

        service.verify(subscriptionRequest);
        
        Assert.assertEquals(1, requests.size());
        
        HttpRequest actualRequest = requests.poll();
        String requestUri = actualRequest.getRequestLine().getUri();
        Assert.assertEquals("subscribe", getQueryParamValue(requestUri, "hub.mode"));
        Assert.assertEquals(subscriptionRequest.getTopic().toString(), URLDecoder.decode(getQueryParamValue(requestUri, "hub.topic"), "UTF-8"));
        Assert.assertNotNull(getQueryParamValue(requestUri, "hub.challenge"));
        Assert.assertEquals("123", getQueryParamValue(requestUri, "hub.lease_seconds"));
        Assert.assertEquals(subscriptionRequest.getVerifyToken(), getQueryParamValue(requestUri, "hub.verify_token"));
    }

    @Test(expected=IOException.class)
    public void verifyWithMissingChallenge() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                // do not return challenge
            }});

        service.verify(subscriptionRequest);
    }

    @Test(expected=IOException.class)
    public void verifyWithInvalidChallenge() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setEntity(Utils.createEntity("dummy"));
            }});

        service.verify(subscriptionRequest);
    }

    @Test(expected=IOException.class)
    public void verifyWithError() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setStatusCode(500);
            }});

        service.verify(subscriptionRequest);
    }
    
    @Test
    public void subscribe() {
        service.subscribe(new Subscription(TOPIC, CALLBACK));
        
        Assert.assertEquals(1, repository.findAll().size());
    }

    @Test
    public void subscribeWithExisting() {
        repository.persist(new Subscription(TOPIC, CALLBACK));
        
        service.subscribe(new Subscription(TOPIC, CALLBACK, 123, "sekrit"));
        
        Assert.assertEquals(1, repository.findAll().size());
        Assert.assertEquals(123, repository.findByTopicAndCallback(TOPIC, CALLBACK).getLeaseSeconds());
    }

    @Test
    public void unsubscribe() {
        repository.persist(new Subscription(TOPIC, CALLBACK));
        
        Assert.assertEquals(1, repository.findAll().size());

        service.unsubscribe(new Subscription(TOPIC, CALLBACK));
        
        Assert.assertEquals(0, repository.findAll().size());
    }

    @Test
    public void unsubscribeNonExisting() {
        Assert.assertEquals(0, repository.findAll().size());

        service.unsubscribe(new Subscription(TOPIC, CALLBACK));
        
        Assert.assertEquals(0, repository.findAll().size());
    }
    
    
    private String getQueryParamValue(String url, String name) {
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
    
    @After
    public void after() {
        try {
            server.stop();
        } catch (Exception e) {
            // ignore
        }
    }
}