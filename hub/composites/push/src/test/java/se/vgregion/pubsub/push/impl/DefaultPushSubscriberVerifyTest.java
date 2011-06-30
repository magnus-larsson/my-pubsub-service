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
import java.net.URLDecoder;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.UnitTestConstants;

public class DefaultPushSubscriberVerifyTest {

    private DefaultPushSubscriber subscriber;
    private LocalTestServer server = new LocalTestServer(null, null);
    
    @Before
    public void before() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime().getMillis());
        server.start();
        
        subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, buildTestUrl("/"), UnitTestConstants.FUTURE, UnitTestConstants.UPDATED1, 123, "verify", UnitTestConstants.SECRET);
    }
    
    private URI buildTestUrl(String path) throws URISyntaxException {
        return new URI("http://" + server.getServiceHostName() + ":" + server.getServicePort() + path);
    }
    
    @Test
    @Transactional
    @Rollback
    public void verify() throws Exception {
        final LinkedBlockingQueue<HttpRequest> requests = new LinkedBlockingQueue<HttpRequest>();
        
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                requests.add(request);
                
                response.setEntity(new StringEntity(getQueryParamValue(request.getRequestLine().getUri(), "hub.challenge")));
            }});

        subscriber.verify(SubscriptionMode.SUBSCRIBE);
        
        Assert.assertEquals(1, requests.size());
        
        HttpRequest actualRequest = requests.poll();
        String requestUri = actualRequest.getRequestLine().getUri();
        Assert.assertEquals("subscribe", getQueryParamValue(requestUri, "hub.mode"));
        Assert.assertEquals(subscriber.getTopic().toString(), URLDecoder.decode(getQueryParamValue(requestUri, "hub.topic"), "UTF-8"));
        Assert.assertNotNull(getQueryParamValue(requestUri, "hub.challenge"));
        Assert.assertEquals("123", getQueryParamValue(requestUri, "hub.lease_seconds"));
        Assert.assertEquals(subscriber.getVerifyToken(), getQueryParamValue(requestUri, "hub.verify_token"));
    }

    @Test(expected=FailedSubscriberVerificationException.class)
    @Transactional
    @Rollback
    public void verifyWithMissingChallenge() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                // do not return challenge
            }});

        subscriber.verify(SubscriptionMode.SUBSCRIBE);
    }

    @Test(expected=FailedSubscriberVerificationException.class)
    @Transactional
    @Rollback
    public void verifyWithInvalidChallenge() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setEntity(HttpUtil.createEntity("dummy"));
            }});

        subscriber.verify(SubscriptionMode.SUBSCRIBE);
    }

    @Test(expected=FailedSubscriberVerificationException.class)
    @Transactional
    @Rollback
    public void verifyWithError() throws Exception {
        server.register("/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
                    IOException {
                response.setStatusCode(500);
            }});

        subscriber.verify(SubscriptionMode.SUBSCRIBE);
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
        DateTimeUtils.setCurrentMillisSystem();

        try {
            server.stop();
        } catch (Exception e) {
            // ignore
        }
    }
}
