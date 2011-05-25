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

package se.vgregion.pubsub.twitter.impl;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PubSubEventListener;
import se.vgregion.pubsub.Subscriber;

public class TwitterPublisher implements PubSubEventListener {

    public static final String FILTER_URL = "http://stream.twitter.com/1/statuses/filter.json";
    public static final String SAMPLE_URL = "http://stream.twitter.com/1/statuses/sample.json";

    private PubSubEngine pubSubEngine;
    
    private ExecutorService executor = Executors.newFixedThreadPool(40);
    
    private String username;
    private String password;

    
    private Map<URI, TwitterStreamConsumer> consumers = new ConcurrentHashMap<URI, TwitterStreamConsumer>();
    
    public TwitterPublisher(PubSubEngine pubSubEngine, String username, String password) {
        this.pubSubEngine = pubSubEngine;
        this.username = username;
        this.password = password;
        
        pubSubEngine.addPubSubEventListener(this);
    }

    @Override
    public void onSubscribe(Subscriber subscriber) {
        URI topic = subscriber.getTopic();

        // check to see if this is one of the URLs we support
        if(topic.toString().equals(SAMPLE_URL) || 
                topic.toString().startsWith(FILTER_URL)) {
            if(!consumers.containsKey(topic)) {
                // no consumer running, start one
                
                TwitterStreamConsumer consumer = new TwitterStreamConsumer(topic, pubSubEngine, username, password, executor);
                consumer.start();
                consumers.put(topic, consumer);
            }
        }
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) {
        // TODO keep track of the number of subscribers and handle full unsubs
    }
    
    public void stop() {
        for(TwitterStreamConsumer consumer : consumers.values()) {
            consumer.stop();
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        executor.shutdownNow();

    }

}
