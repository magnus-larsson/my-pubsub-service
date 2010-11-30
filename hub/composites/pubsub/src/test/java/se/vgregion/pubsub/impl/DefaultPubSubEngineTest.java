package se.vgregion.pubsub.impl;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

    private PubSubEngine engine;

    @Before
    public void before() {
        engine = applicationContext.getBean(PubSubEngine.class);
    }
    
    @Test
    @Transactional
    @Rollback
    public void testPublication() throws PublicationFailedException {
        Topic topic = engine.getOrCreateTopic(UnitTestConstants.TOPIC);
        
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        
        topic.addSubscriber(subscriber);
        
        Feed feed = new FeedBuilder().id("f1").build();
        
        topic.publish(feed);
        
        ArgumentCaptor<Feed> publishedFeed = ArgumentCaptor.forClass(Feed.class);
        Mockito.verify(subscriber).publish(publishedFeed.capture());
        Assert.assertEquals(feed, publishedFeed.getValue());
    }

    @Test
    @Transactional
    @Rollback
    public void testDoublePublication() throws PublicationFailedException {
        Topic topic = engine.getOrCreateTopic(UnitTestConstants.TOPIC);
        
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        
        topic.addSubscriber(subscriber);
        
        Feed feed = new FeedBuilder().id("f1").updated(
                UnitTestConstants.UPDATED2).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED2).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build()).build();
        Feed feed2 = new FeedBuilder()
            .id("f1").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

//        Feed feed = new FeedBuilder().id("f1").updated(UnitTestConstants.UPDATED2).build();
//        Feed feed2 = new FeedBuilder().id("f1").updated(UnitTestConstants.UPDATED1)
//            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
//            .build();
        
        topic.publish(feed);
        topic.publish(feed2);
    }

}
