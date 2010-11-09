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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;


public class DefaultPushServiceRenewalTest {

    private static final URI CALLBACK = URI.create("http://example.com/callback");
    private static final URI TOPIC = URI.create("http://example.com/topic");
    
    @Before
    public void before() throws Exception {

    }
    
    @Test
    public void renew() throws Exception {
        SubscriptionRepository subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        
        PushService service = new DefaultPushService(subscriptionRepository, null) {
            @Override
            public void verify(SubscriptionRequest request) throws FailedSubscriberVerificationException, IOException {
                // success!
            }
        };
        
        Subscription subscription = new Subscription(TOPIC, CALLBACK);
        
        service.renewSubcription(subscription);
        
        Mockito.verify(subscriptionRepository).store(subscription);
        Assert.assertFalse(subscription.isFailed());
    }

    @Test
    public void renewWithFailedVerification() throws Exception {
        SubscriptionRepository subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        
        PushService service = new DefaultPushService(subscriptionRepository, null) {
            @Override
            public void verify(SubscriptionRequest request) throws FailedSubscriberVerificationException, IOException {
                // failure!
                throw new FailedSubscriberVerificationException();
            }
        };
        
        Subscription subscription = new Subscription(TOPIC, CALLBACK);
        
        for(int i = 0; i<Subscription.MAX_RENEWAL_TRIES + 1; i++) {
            service.renewSubcription(subscription);
        }   
        
        Mockito.verify(subscriptionRepository).removeByPrimaryKey(0L);
        Assert.assertTrue(subscription.isFailed());
    }
}
