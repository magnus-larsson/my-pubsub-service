package se.vgregion.push.inttest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.UnitTestConstants;
import se.vgregion.push.types.FeedSerializer;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;

public class IntegrationTest extends IntegrationTestTemplate {

    @Test
    public void test() throws URISyntaxException, IOException, InterruptedException {
        Feed feed = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM).id("f1").updated(
                UnitTestConstants.UPDATED1).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build()).build();

        System.out.println(FeedSerializer.printFeed(feed).toXML());
        
        publisher.publish(hubUrl, feed);

        Feed publishedFeed = publishedFeeds.poll(5000, TimeUnit.MILLISECONDS);

        System.out.println(FeedSerializer.printFeed(publishedFeed).toXML());
        
        Assert.assertNotNull(publishedFeed);

    }
}
