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

package se.vgregion.push.services;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import se.vgregion.push.UnitTestConstants;
import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;

public class DefaultPushServiceRetrialTest {

    private SubscriptionRepository subscriptionRepository;
    private FeedRepository feedRepository;

    @Before
    public void before() {
        Feed feed1 = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM).id("f1").updated(UnitTestConstants.UPDATED1)
                .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
                .build();
        Feed feed2 = new FeedBuilder(UnitTestConstants.TOPIC2, ContentType.ATOM).id("f2").updated(UnitTestConstants.UPDATED3)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED3).build())        
                .build();

        Subscription subscription = new Subscription(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK);

        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        feedRepository = Mockito.mock(FeedRepository.class);
        Mockito.when(feedRepository.findFeedsWithBehindSubscriptions()).thenReturn(Arrays.asList(feed1, feed2));

        Mockito.when(subscriptionRepository.findByTopic(UnitTestConstants.TOPIC)).thenReturn(Arrays.asList(subscription));
    }
    
    @Test
    public void retry() throws Exception {
        PushService service = new DefaultPushService(subscriptionRepository, feedRepository) {
            @Override
            protected void distribute(Feed feed, Subscription subscription) throws FailedDistributionException {
                // success!
            }
        };

        service.retryDistributions();
    }

    @Test
    public void retryWithFailedDistribution() throws Exception {
        PushService service = new DefaultPushService(subscriptionRepository, feedRepository) {
            @Override
            protected void distribute(Feed feed, Subscription subscription) throws FailedDistributionException {
                // failure!
                throw new FailedDistributionException("Failed");
            }
        };

        service.retryDistributions();
        Mockito.verify(subscriptionRepository).store(Mockito.any(Subscription.class));
    }

}
