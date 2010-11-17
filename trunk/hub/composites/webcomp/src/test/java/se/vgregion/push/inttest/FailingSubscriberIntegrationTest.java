package se.vgregion.push.inttest;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;

public class FailingSubscriberIntegrationTest extends IntegrationTestTemplate {

    @Test
    public void failedFirstPublication() throws Exception {
        Feed feed = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM).id("f1").updated(
                UnitTestConstants.UPDATED1).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build()).build();

        Feed feed2 = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM).id("f1").updated(
                UnitTestConstants.UPDATED1).entry(
                new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build()).build();

        Assert.assertTrue(verifications.poll(5000, TimeUnit.MILLISECONDS));
        Assert.assertTrue(verifications.isEmpty());
        
        publisher.publish(hubUrl, feed);

        // this is the failed distribution
        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(2, publishedFeed.getEntries().size());

        publisher.publish(hubUrl, feed2);

        // since it failed last time, it should now be verified
        Assert.assertTrue(verifications.poll(5000, TimeUnit.MILLISECONDS));
        
        // this time, all entries should come again
        publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(publishedFeed);
        Assert.assertEquals("f1", publishedFeed.getFeedId());
        Assert.assertEquals(3, publishedFeed.getEntries().size());

        publisher.publish(hubUrl, feed2);

        // now, nothing should be published
        publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        Assert.assertNull(publishedFeed);

    }

    @Override
    protected SubscriberResult createSubscriberResult() {
        return new SubscriberResult() {
            private boolean first = true;
            
            @Override
            public boolean fail() {
                if(first) {
                    first = false;
                    return true;
                } else {
                    return false;
                }
            }
        };
    }


}