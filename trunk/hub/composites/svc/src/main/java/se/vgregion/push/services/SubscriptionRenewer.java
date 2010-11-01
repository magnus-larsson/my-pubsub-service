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
import java.util.Collection;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;

@Service
public class SubscriptionRenewer {

    private Logger log = LoggerFactory.getLogger(SubscriptionRenewer.class);
    
    private SubscriptionRepository subscriptionRepository;
    
    private PushService pushService;
    
    public SubscriptionRenewer(PushService pushService, SubscriptionRepository subscriptionRepository) {
        this.pushService = pushService;
        this.subscriptionRepository = subscriptionRepository;
    }

    public void renew() {
        log.info("Starting renewal of timed out subscriptions");
        
        Collection<Subscription> timedOutSubscriptions = subscriptionRepository.findTimedOutBy(new DateTime());
        
        for(Subscription timedOutSubscription : timedOutSubscriptions) {
            log.info("Renewing subscription {}", timedOutSubscription);
            
            SubscriptionRequest request = new SubscriptionRequest(SubscriptionMode.SUBSCRIBE, 
                    timedOutSubscription.getCallback(), 
                    timedOutSubscription.getTopic(), 
                    Subscription.DEFAULT_LEASE_SECONDS, 
                    timedOutSubscription.getVerifyToken());
            
            try {
                pushService.verify(request);
                
                // success, update timeout
                timedOutSubscription.setLeaseTimeout(new DateTime().plusSeconds(Subscription.DEFAULT_LEASE_SECONDS));
                timedOutSubscription.resetFailedVerifications();
            } catch (FailedSubscriberVerificationException e) {
                // failed verification
                log.info("Failed to renew subscription {}", timedOutSubscription);
                timedOutSubscription.increaseFailedVerifications();
            } catch (IOException e) {
                // failed verification
                log.info("Failed to renew subscription {}", timedOutSubscription);
                timedOutSubscription.increaseFailedVerifications();                
            }

            if(timedOutSubscription.isFailed()) {
                log.info("Subscription reached renewal limit, removed {}", timedOutSubscription);
                subscriptionRepository.removeByPrimaryKey(timedOutSubscription.getId());
            } else {
                subscriptionRepository.store(timedOutSubscription);
            }
        }
        
    }
    
    
}
