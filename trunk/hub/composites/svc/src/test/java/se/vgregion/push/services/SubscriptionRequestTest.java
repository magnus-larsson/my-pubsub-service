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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.services.SubscriptionMode;
import se.vgregion.push.services.SubscriptionRequest;


public class SubscriptionRequestTest {

    private static final URI CALLBACK = URI.create("http://example.com/callback");
    private static final URI TOPIC = URI.create("http://example.com/topic");

    
    @Test
    public void verificationUrl() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(SubscriptionMode.SUBSCRIBE, CALLBACK, TOPIC, 123, "token");
        
        Assert.assertEquals(URI.create("http://example.com/callback?hub.mode=subscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch&hub.lease_seconds=123&hub.verify_token=token"), request.getVerificationUrl("ch"));
    }

    @Test
    public void verificationUrlUnsubscribe() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(SubscriptionMode.UNSUBSCRIBE, CALLBACK, TOPIC, 123, "token");
        
        Assert.assertEquals(URI.create("http://example.com/callback?hub.mode=unsubscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch&hub.lease_seconds=123&hub.verify_token=token"), request.getVerificationUrl("ch"));
    }

    
    @Test
    public void verificationUrlNoLeaseSecondsAndToken() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(SubscriptionMode.SUBSCRIBE, CALLBACK, TOPIC, 0, null);
        
        Assert.assertEquals(URI.create("http://example.com/callback?hub.mode=subscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch"), request.getVerificationUrl("ch"));
    }

    @Test
    public void verificationUrlCallbackWithQuery() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(SubscriptionMode.SUBSCRIBE, URI.create(CALLBACK.toString() + "?abc=def"), TOPIC, 0, null);
        
        Assert.assertEquals(URI.create("http://example.com/callback?abc=def&hub.mode=subscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch"), request.getVerificationUrl("ch"));
    }

}
