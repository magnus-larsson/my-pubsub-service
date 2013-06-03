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
import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

@Ignore
@ContextConfiguration({"classpath:spring/pubsub-common.xml", "classpath:spring/pubsub-jpa.xml", "classpath:spring/test-jpa.xml"})
public class DefaultPubSubEngineTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Test
    @Transactional
    @Rollback(false)
    public void testPublication() throws PublicationFailedException {
        PubSubEngine engine = applicationContext.getBean(PubSubEngine.class);
        Topic topic = engine.getOrCreateTopic(UnitTestConstants.TOPIC);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getTopic()).thenReturn(UnitTestConstants.TOPIC);
        
        engine.subscribe(subscriber);
        
        Feed feed = new FeedBuilder(ContentType.ATOM).id("f1").build();
        
        topic.publish(feed, null);
        ArgumentCaptor<Feed> publishedFeed = ArgumentCaptor.forClass(Feed.class);
        Mockito.verify(subscriber).publish(publishedFeed.capture(), null);
        Assert.assertEquals(feed, publishedFeed.getValue());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testDoublePublication() throws PublicationFailedException {
        PubSubEngine engine = applicationContext.getBean(PubSubEngine.class);

        Topic topic = engine.getOrCreateTopic(UnitTestConstants.TOPIC2);
        
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getTopic()).thenReturn(UnitTestConstants.TOPIC);
        
        engine.subscribe(subscriber);
        
        Feed feed = new FeedBuilder(ContentType.ATOM).id("f2").updated(
                UnitTestConstants.UPDATED2).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED2).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build()).build();
        Feed feed2 = new FeedBuilder(ContentType.ATOM)
            .id("f2").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

        topic.publish(feed, null);
        topic.publish(feed2, null);

    }
    
}
