package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;

public class DefaultSubscriberTimeoutNotifierTest {

    @Test
    public void timeout() throws InterruptedException {
        DefaultSubscriberTimeoutNotifier timeoutNotifier = new DefaultSubscriberTimeoutNotifier();

        final CountDownLatch latch = new CountDownLatch(1);

        timeoutNotifier.addSubscriber(new Subscriber() {
            @Override
            public void timedOut() {
                latch.countDown();
            }
            
            @Override
            public void publish(Feed feed) throws PublicationFailedException {}
            
            @Override
            public URI getTopic() {
                return null;
            }
            
            @Override
            public DateTime getTimeout() {
                return new DateTime().plusSeconds(1);
            }
            
            @Override
            public DateTime getLastUpdated() {
                return null;
            }
        });
        
        // Wait some time until the timeout is reached
        Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    }
}
