package se.vgregion.pubsub.inttest;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.content.DateTimeUtils;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;
import se.vgregion.pubsub.push.SubscriptionMode;

public class IntegrationTest extends IntegrationTestTemplate {

    @Test
    public void simpleAtomPublication() throws Exception {
        Feed feed = new FeedBuilder(ContentType.ATOM).field(Namespaces.ATOM, "id", "f1").entry(
                new EntryBuilder().field(Namespaces.ATOM, "id", "e1").build()).entry(
                new EntryBuilder().field(Namespaces.ATOM, "id", "e2").build()).build();

        publisher.publish(hubUrl, feed);

        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(2, publishedFeed.getEntries().size());
    }

    @Test
    public void simpleRssPublication() throws Exception {
        Feed feed = new FeedBuilder(ContentType.ATOM).field(Namespaces.ATOM, "id", "f1")
            .entry(
                new EntryBuilder().field(Namespaces.ATOM, "id", "e1").build()).entry(
                new EntryBuilder().field(Namespaces.ATOM, "id", "e2").build()).build();

        publisher.publish(hubUrl, feed);

        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(2, publishedFeed.getEntries().size());
    }

    
    @Test
    public void doublePublication() throws Exception {
        Entry e1Old = new EntryBuilder()
            .field(Namespaces.ATOM, "id", "e1")
            .field(Namespaces.ATOM, "updated", DateTimeUtils.print(UnitTestConstants.UPDATED2))
            .build();
        Entry e2Old = new EntryBuilder()            
            .field(Namespaces.ATOM, "id", "e2")
            .field(Namespaces.ATOM, "updated", DateTimeUtils.print(UnitTestConstants.UPDATED2))
            .build();

        Entry e1Future = new EntryBuilder()
            .field(Namespaces.ATOM, "id", "e1")
            .field(Namespaces.ATOM, "updated", DateTimeUtils.print(UnitTestConstants.FUTURE))
            .build();
        Entry e3Future = new EntryBuilder()
            .field(Namespaces.ATOM, "id", "e3")
            .field(Namespaces.ATOM, "updated", DateTimeUtils.print(UnitTestConstants.FUTURE))
            .build();

        
        Feed feed = new FeedBuilder(ContentType.ATOM).updated(
                UnitTestConstants.UPDATED2)
                .field(Namespaces.ATOM, "id", "f1")
                .entry(e1Old)
                .entry(e2Old).build();
        Feed feed2 = new FeedBuilder(ContentType.ATOM)
            .updated(UnitTestConstants.FUTURE)
            .field(Namespaces.ATOM, "id", "f1")
            .entry(e3Future)
            .entry(e1Future)
            .entry(e1Old)
            .build();

        publisher.publish(hubUrl, feed);

        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(2, publishedFeed.getEntries().size());

        publisher.publish(hubUrl, feed2);

        publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(2, publishedFeed.getEntries().size());
        Assert.assertEquals("e3", publishedFeed.getEntries().get(0).getEntryId());
        Assert.assertEquals("e1", publishedFeed.getEntries().get(1).getEntryId());
    }

    @Test
    public void doublePublicationWithoutUpdate() throws Exception {
        Feed feed = new FeedBuilder(ContentType.ATOM).field(Namespaces.ATOM, "id", "f1")
            .entry(
                new EntryBuilder().field(Namespaces.ATOM, "id", "e1")
                .field(Namespaces.ATOM, "updated", DateTimeUtils.print(UnitTestConstants.UPDATED2))
                .build()).entry(
                new EntryBuilder().field(Namespaces.ATOM, "id", "e2")
                .field(Namespaces.ATOM, "updated", DateTimeUtils.print(UnitTestConstants.UPDATED2))
                .build()).build();

        publisher.publish(hubUrl, feed);

        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(2, publishedFeed.getEntries().size());

        publisher.publish(hubUrl, feed);

        publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        // nothing should be pushed to the subscriber
        Assert.assertNull(publishedFeed);
    }

    @Test
    public void unsubscribe() throws Exception {
        Feed feed = new FeedBuilder(ContentType.ATOM).id("f1").updated(
                UnitTestConstants.UPDATED1).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build()).build();
        subscriber.subscribe(SubscriptionMode.UNSUBSCRIBE, hubUrl, publisher.getUrl());
        
        publisher.publish(hubUrl, feed);

        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNull(publishedFeed);
    }

}
