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
import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class DefaultPushSubscriberManager implements PushSubscriberManager {

    private PubSubEngine pubSubEngine;
    private PushSubscriberRepository subscriptionRepository;
    
    public DefaultPushSubscriberManager(PubSubEngine pubSubEngine, PushSubscriberRepository subscriptionRepository) {
        this.pubSubEngine = pubSubEngine;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public void loadSubscribers() {
        Collection<PushSubscriber> subscribers = subscriptionRepository.findAll();
        
        for(PushSubscriber subscriber : subscribers) {
            pubSubEngine.subscribe(subscriber);
            subscriber.setSubscriberRepository(subscriptionRepository);
            
        }
    }

    @Override
    @Transactional
    public void subscribe(URI topicUrl, URI callback, int leaseSeconds, String verifyToken) {
        unsubscribe(topicUrl, callback);
        
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(subscriptionRepository, topicUrl, callback, leaseSeconds, verifyToken);
        
        pubSubEngine.subscribe(subscriber);
        
        subscriptionRepository.persist(subscriber);
    }

    @Override
    @Transactional
    public void unsubscribe(URI topicUrl, URI callback) {
        PushSubscriber subscriber = subscriptionRepository.findByTopicAndCallback(topicUrl, callback);
        if(subscriber != null) {
            try {
                subscriber.verify(SubscriptionMode.UNSUBSCRIBE);

                pubSubEngine.unsubscribe(subscriber);
                subscriptionRepository.remove(subscriber);
            } catch (IOException e) {
                // ignore
            } catch (FailedSubscriberVerificationException e) {
                // ignore
            }
        }
    }
}
