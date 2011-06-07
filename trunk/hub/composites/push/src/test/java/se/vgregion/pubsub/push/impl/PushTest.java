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


import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;

@ContextConfiguration({"classpath:spring/pubsub-common.xml", "classpath:spring/pubsub-jpa.xml", "classpath:spring/pubsub-push.xml", "classpath:spring/pubsub-push-jpa.xml", "classpath:spring/test-jpa.xml"})
public class PushTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Before
    @Transactional
    @Rollback(false)
    public void setup() {
    }
    
    @Test
    @Transactional // TODO remove
    public void test() throws InterruptedException {
        final URI testUri = URI.create("http://feeds.feedburner.com/protocol7/main");
        
        PubSubEngine pubSubEngine = applicationContext.getBean(PubSubEngine.class);
        
        
        final LinkedBlockingQueue<Feed> publishedFeeds = new LinkedBlockingQueue<Feed>();
        
        pubSubEngine.getOrCreateTopic(testUri).addSubscriber(new Subscriber() {
            
            @Override
            public void timedOut() {
            }
            
            @Override
            public void publish(Feed feed) throws PublicationFailedException {
                publishedFeeds.add(feed);
            }
            
            @Override
            public URI getTopic() {
                return testUri;
            }
            
            @Override
            public DateTime getTimeout() {
                return null;
            }
            
            @Override
            public DateTime getLastUpdated() {
                return null;
            }
        });
        
//        pubSubEngine.getOrCreateTopic(testUri).addSubscriber(new DefaultPushSubscriber(
//                applicationContext.getBean(PushSubscriberRepository.class),
//                testUri, URI.create("http://localhost:9000"), 100, "verify"));
        
        PushSubscriberManager pushSubscriberManager = applicationContext.getBean(PushSubscriberManager.class);
        pushSubscriberManager.retrive(testUri);

        Feed feed = publishedFeeds.poll(10000, TimeUnit.MILLISECONDS);
        
//        Thread.sleep(200000);
    }

}
