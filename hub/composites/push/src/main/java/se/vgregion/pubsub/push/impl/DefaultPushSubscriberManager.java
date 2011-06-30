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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.impl.PublicationRetryer;
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class DefaultPushSubscriberManager implements PushSubscriberManager {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPushSubscriberManager.class);

	
    private PubSubEngine pubSubEngine;
    private PushSubscriberRepository subscriptionRepository;
    private BlockingQueue<URI> retrieverQueue = new LinkedBlockingQueue<URI>();
    private PublicationRetryer publicationRetryer;
    
    public DefaultPushSubscriberManager(PubSubEngine pubSubEngine, PushSubscriberRepository subscriptionRepository, PublicationRetryer publicationRetryer) {
        this.pubSubEngine = pubSubEngine;
        this.subscriptionRepository = subscriptionRepository;
        this.publicationRetryer = publicationRetryer;
    }

    /**
     * Initiates and starts the manager
     */
    public void start() {
    	pubSubEngine.subscribe(this);
    }
    
    @Override
    @Transactional
    public void subscribe(URI topicUrl, URI callback, int leaseSeconds, String verifyToken, String secret, boolean verify) throws IOException, FailedSubscriberVerificationException {
        unsubscribe(topicUrl, callback, verify);
        
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(topicUrl, callback, leaseSeconds, verifyToken, secret);
        
        if(verify) {
        	subscriber.verify(SubscriptionMode.SUBSCRIBE);
        }
        
        subscriptionRepository.persist(subscriber);
    }

    @Override
    @Transactional
    public void unsubscribe(URI topicUrl, URI callback, boolean verify) {
        PushSubscriber subscriber = subscriptionRepository.findByTopicAndCallback(topicUrl, callback);
        if(subscriber != null) {
            try {
            	if(verify) {
            		subscriber.verify(SubscriptionMode.UNSUBSCRIBE);
            	}

                subscriptionRepository.remove(subscriber);
            } catch (IOException e) {
                // ignore
            } catch (FailedSubscriberVerificationException e) {
                // ignore
            }
        }
    }
    
    @Override
    @Transactional
    public void retrive(URI topicUrl) throws InterruptedException {
    	if(retrieverQueue.offer(topicUrl, 10000, TimeUnit.MILLISECONDS)) {
    		LOG.info("Published feed queued for retrieval: {}", topicUrl);
    	} else {
    		throw new RuntimeException("Failed to queue feed for retrieval: " + topicUrl);
    	}
    }

    @Override
    public URI pollForRetrieval() throws InterruptedException {
    	URI url = retrieverQueue.poll(5, TimeUnit.MINUTES);
    	
    	if(url == null) {
    		LOG.info("DefaultPushSubscriberManager timed out waiting. Size of queue: {}", retrieverQueue.size());
    	}
    	return url;
    }
    
    @Override
    @Transactional
    public void publish(URI topicUrl, Feed feed) {
    	pubSubEngine.publish(topicUrl, feed);
    }

	@Override
	@Transactional
	public void publishToSubscribers(Topic topic, Feed feed) {
		List<PushSubscriber> subscribers = subscriptionRepository.findByTopic(topic.getUrl());
		
        if(!subscribers.isEmpty()) {
            // if all publications success, purge until now
            DateTime lastUpdatedSubscriber = new DateTime();
            for(PushSubscriber subscriber : subscribers) {
                try {
                	
                    subscriber.publish(feed);
                } catch (Exception e) {
                    LOG.warn("Subscriber failed: {}", e.getMessage());
                    
                    if(publicationRetryer != null) {
                        publicationRetryer.addRetry(topic, subscriber, feed);
                    }
                    lastUpdatedSubscriber = subscriber.getLastUpdated();
                } finally {
                	// merge the updated subscriber
                	subscriptionRepository.merge(subscriber);
                }
            }
        } else {
            LOG.info("No PuSH subscribers for topic {}, publication dropped", topic);
        }
	}
}
