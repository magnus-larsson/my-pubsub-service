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

package se.vgregion.pubsub.inttest;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class FailingSubscriberIntegrationTest extends IntegrationTestTemplate {

    @Test
    @Ignore
    public void failedFirstPublication() throws Exception {
        Feed feed = new FeedBuilder(ContentType.ATOM).id("f1").updated(
                UnitTestConstants.UPDATED1).entry(
                new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build()).entry(
                new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build()).build();

        Feed feed2 = new FeedBuilder(ContentType.ATOM).id("f1").updated(
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
