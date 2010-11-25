package se.vgregion.pubsub.repository.jpa;
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


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;
import se.vgregion.pubsub.repository.FeedRepository;

@ContextConfiguration({"classpath:spring/pubsub-jpa.xml", "classpath:spring/test-jpa.xml"})
public class JpaFeedRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private FeedRepository feedRepository;
    
    private Feed expected;
    
    @Before
    @Transactional
    @Rollback(false)
    public void setup() {
        feedRepository = applicationContext.getBean(FeedRepository.class);
        expected = new FeedBuilder().id("f1").field("http://namespace.org", "n", "v")
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();
        feedRepository.persist(expected);
    }
    
    @Test
    @Transactional
    @Rollback
    public void find() {
        Feed actual = feedRepository.find("f1");
        
        Assert.assertEquals(expected.getFeedId(), actual.getFeedId());
        Assert.assertEquals(2, actual.getEntries().size());
        
        Assert.assertEquals(expected.getEntries().get(0).getEntryId(), actual.getEntries().get(0).getEntryId());
    }

}
