package se.vgregion.push.services.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.services.SubscriptionRequest;


public class SubscriptionRequestTest {

    private static final URI CALLBACK = URI.create("http://example.com/callback");
    private static final URI TOPIC = URI.create("http://example.com/topic");

    
    @Test
    public void verificationUrl() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(CALLBACK, TOPIC, 123, "token");
        
        Assert.assertEquals(URI.create("http://example.com/callback?hub.mode=subscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch&hub.lease_seconds=123&hub.verify_token=token"), request.getVerificationUrl("ch"));
    }

    @Test
    public void verificationUrlNoLeaseSecondsAndToken() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(CALLBACK, TOPIC, 0, null);
        
        Assert.assertEquals(URI.create("http://example.com/callback?hub.mode=subscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch"), request.getVerificationUrl("ch"));
    }

    @Test
    public void verificationUrlCallbackWithQuery() throws UnsupportedEncodingException {
        SubscriptionRequest request = new SubscriptionRequest(URI.create(CALLBACK.toString() + "?abc=def"), TOPIC, 0, null);
        
        Assert.assertEquals(URI.create("http://example.com/callback?abc=def&hub.mode=subscribe&hub.topic=" + URLEncoder.encode(TOPIC.toString(), "UTF-8") + "&hub.challenge=ch"), request.getVerificationUrl("ch"));
    }

}
