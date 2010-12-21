package se.vgregion.pubsub.twitter;

import java.io.IOException;
import java.net.URI;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PubSubEventListener;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.twitter.impl.TwitterPublisher;
import se.vgregion.pubsub.twitter.impl.TwitterStreamConsumer;


public class TwitterTest {

    @Test
    public void test() throws IOException, Exception {
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
        
        TwitterPublisher consumer = new TwitterPublisher(engine, "protocol7", "PUkcqKHN8J2OvU");
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getTopic()).thenReturn(URI.create(TwitterPublisher.FILTER_URL + "#" + "foo"));
        consumer.onSubscribe(subscriber);
        
        Thread.sleep(5000);
        consumer.stop();
    }
    
}
