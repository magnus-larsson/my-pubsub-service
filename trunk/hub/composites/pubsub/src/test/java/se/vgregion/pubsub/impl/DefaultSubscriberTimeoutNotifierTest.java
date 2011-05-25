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
