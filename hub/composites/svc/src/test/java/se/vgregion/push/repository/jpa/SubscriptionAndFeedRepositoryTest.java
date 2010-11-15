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

package se.vgregion.push.repository.jpa;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.UnitTestConstants;
import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;


public class SubscriptionAndFeedRepositoryTest {

    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    private SubscriptionRepository subscriptionRepository = ctx.getBean(SubscriptionRepository.class);
    private FeedRepository feedRepository = ctx.getBean(FeedRepository.class);
    
    @Test
    public void findBehindSubscription() {
        Feed feed1 = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED3).build())
            .build();
        feedRepository.persist(feed1);

        Feed feed2 = new FeedBuilder(UnitTestConstants.TOPIC2, ContentType.ATOM)
            .id("f2").updated(UnitTestConstants.UPDATED3)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED3).build())
            .build();
        feedRepository.persist(feed2);
        
        Subscription subscription = new Subscription(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK);
        subscription.setLastUpdated(UnitTestConstants.UPDATED2);
        subscriptionRepository.persist(subscription);
        
        Collection<Feed> feeds = feedRepository.findFeedsWithBehindSubscriptions();
        
        Assert.assertEquals(1, feeds.size());
        Assert.assertEquals(feed1.getFeedId(), feeds.iterator().next().getFeedId());
    }

    @Test
    public void findBehindSubscriptionMultiple() {
        Feed feed1 = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED3).build())
            .build();
        feedRepository.persist(feed1);

        Feed feed2 = new FeedBuilder(UnitTestConstants.TOPIC2, ContentType.ATOM)
            .id("f2").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build())
            .build();
        feedRepository.persist(feed2);
        
        Subscription subscription = new Subscription(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK);
        subscription.setLastUpdated(UnitTestConstants.UPDATED2);
        subscriptionRepository.persist(subscription);

        Subscription subscription2 = new Subscription(UnitTestConstants.TOPIC2, UnitTestConstants.CALLBACK);
        subscription2.setLastUpdated(UnitTestConstants.UPDATED2);
        subscriptionRepository.persist(subscription2);

        Collection<Feed> feeds = feedRepository.findFeedsWithBehindSubscriptions();
        
        Assert.assertEquals(2, feeds.size());
    }

}
