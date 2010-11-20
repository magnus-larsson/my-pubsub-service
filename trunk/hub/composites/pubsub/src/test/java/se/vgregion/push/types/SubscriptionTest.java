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

package se.vgregion.push.types;

import java.net.URI;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


public class SubscriptionTest {
    private static final URI CALLBACK = URI.create("http://example.com/sub11");
    private static final URI TOPIC = URI.create("http://example.com/feed");

    @Test
    public void failedVerifications() throws Exception {
        Subscription subscription = new Subscription(TOPIC, CALLBACK);
        Assert.assertEquals(0, subscription.getFailedVerifications());
        
        subscription.failedVerified();
        Assert.assertEquals(1, subscription.getFailedVerifications());

        subscription.failedVerified();
        Assert.assertEquals(2, subscription.getFailedVerifications());

        subscription.successfullyVerified();
        Assert.assertEquals(0, subscription.getFailedVerifications());
    }

    @Test
    public void needsVerification() throws Exception {
        Subscription subscription = new Subscription(TOPIC, CALLBACK);
        Assert.assertFalse(subscription.isNeedsVerification());
        
        subscription.markForVerification();
        Assert.assertTrue(subscription.isNeedsVerification());

        subscription.successfullyVerified();
        Assert.assertFalse(subscription.isNeedsVerification());
    }

    @Test
    public void needsVerificationByTimeout() throws Exception {
        // hack using negative timeout
        Subscription subscription = new Subscription(TOPIC, CALLBACK, -100, "sekrit", "token");
        Assert.assertTrue(subscription.isNeedsVerification());
    }

    
    @Test
    public void defaultLeaseSeconds() throws Exception {
        Subscription subscription = new Subscription(TOPIC, CALLBACK);
        
        Assert.assertEquals(Subscription.DEFAULT_LEASE_SECONDS, subscription.getLeaseSeconds());
    }

    @Test
    public void setLeaseTimeout() throws Exception {
        DateTime testTime = new DateTime();
        
        Subscription subscription = new Subscription(TOPIC, CALLBACK);
        subscription.setLeaseRenewedAt(testTime);
        Assert.assertEquals(testTime, subscription.getLeaseRenewedAt());
    }

    
    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();   
    }
}
