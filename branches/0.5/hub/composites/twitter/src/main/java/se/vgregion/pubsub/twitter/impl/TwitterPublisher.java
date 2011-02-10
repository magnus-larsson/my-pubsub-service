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
