package se.vgregion.pubsub.twitter;

import java.net.URI;

import org.mockito.Mockito;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PubSubEventListener;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.twitter.impl.TwitterPublisher;


public class TwitterRunner {

    public static void main(String[] args) throws InterruptedException {
        String username = args[0];
        String password = args[1];
        
        PubSubEngine engine = new PubSubEngine() {
            @Override
            public void publish(URI url, Feed feed) {
                System.out.println(feed.getEntries().get(0).getContent().toXML());
            }
            
            @Override
            public Topic getTopic(URI url) {return null;}
            
            @Override
            public Topic getOrCreateTopic(URI url) {return null;}
            
            @Override
            public Topic createTopic(URI url) {return null;}

            @Override
            public void subscribe(Subscriber subscriber) {
            }

            @Override
            public void unsubscribe(Subscriber subscriber) {
            }

            @Override
            public void addPubSubEventListener(PubSubEventListener eventListener) {
            }

            @Override
            public void removePubSubEventListener(PubSubEventListener eventListener) {
            }
        };
        
        TwitterPublisher consumer = new TwitterPublisher(engine, username, username);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getTopic()).thenReturn(URI.create(TwitterPublisher.FILTER_URL + "#" + "foo"));
        consumer.onSubscribe(subscriber);
        
        Thread.sleep(5000);
        consumer.stop();
    }
    
}
