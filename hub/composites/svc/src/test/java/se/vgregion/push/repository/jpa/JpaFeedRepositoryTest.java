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

import nu.xom.tests.XOMTestCase;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.UnitTestConstants;
import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;


public class JpaFeedRepositoryTest {

    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    private FeedRepository repository = ctx.getBean(FeedRepository.class);
    
    private Feed feed1;
    
    @Before
    public void setup() {
        feed1 = UnitTestConstants.atom1();
        repository.persist(feed1);
    }
    
    @Test
    public void findByPk() {
        Feed feed = repository.find(feed1.getId());
        
        XOMTestCase.assertEquals(feed1.getFeedId(), feed.getFeedId());
    }

    @Test
    public void findByUrl() {
        Feed feed = repository.findByUrl(UnitTestConstants.TOPIC);
        
        XOMTestCase.assertEquals(feed1.getFeedId(), feed.getFeedId());
    }

    
    @Test
    public void testDeleteOldEntries() {
        Feed feed = repository.find(feed1.getId());
        
        repository.deleteOutdatedEntries(feed, new DateTime(2110, 1, 1, 0, 0, 0, 0));
        
        repository.store(feed);
        
        Feed storedFeed = repository.find(feed1.getId());
        
        Assert.assertEquals(1, storedFeed.getEntries().size());
    }

    @Test
    public void persistOrUpdateWithNonExistingFeed() {
        repository.persistOrUpdate(UnitTestConstants.atom2());
        
        Feed storedFeed = repository.findByUrl(UnitTestConstants.TOPIC2);
        Assert.assertEquals(2, storedFeed.getEntries().size());
    }

    
    @Test
    public void persistOrUpdateWithExistingFeed() {
        Feed feed2 = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build())  // duplicated entry
            .build();
            
        repository.persistOrUpdate(feed2);
        
        Feed storedFeed = repository.find(feed1.getId());
        Assert.assertEquals(3, storedFeed.getEntries().size());
    }
    
        
    
}
