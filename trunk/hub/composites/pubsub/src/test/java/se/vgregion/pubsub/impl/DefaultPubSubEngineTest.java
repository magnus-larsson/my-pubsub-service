package se.vgregion.pubsub.impl;
import javax.persistence.EntityManagerFactory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;


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
        
        Feed feed = new FeedBuilder().id("f1").build();
        
        topic.publish(feed);
        ArgumentCaptor<Feed> publishedFeed = ArgumentCaptor.forClass(Feed.class);
        Mockito.verify(subscriber).publish(publishedFeed.capture());
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
        
        Feed feed = new FeedBuilder().id("f2").updated(
                UnitTestConstants.UPDATED2).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED2).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build()).build();
        Feed feed2 = new FeedBuilder()
            .id("f2").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

        topic.publish(feed);
        topic.publish(feed2);

    }
    
}
