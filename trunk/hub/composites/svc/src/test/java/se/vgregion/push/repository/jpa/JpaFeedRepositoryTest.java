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

import java.net.URI;

import nu.xom.tests.XOMTestCase;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.services.SomeFeeds;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;


public class JpaFeedRepositoryTest {

    private static final URI URL = URI.create("http://example.com/sub11");
    
    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    private FeedRepository repository = ctx.getBean(FeedRepository.class);
    
    private Feed feed1;
    
    @Before
    public void setup() {
        Feed feed = new Feed(URL, ContentType.ATOM, SomeFeeds.ATOM1);
        feed1 = repository.persist(feed);
    }
    
    @Test
    public void findByPk() {
        Feed feed = repository.find(feed1.getId());
        
        Assert.assertEquals(SomeFeeds.ATOM1.toXML(), feed.createDocument().toXML());
        XOMTestCase.assertEquals(SomeFeeds.ATOM1, feed.createDocument());
    }

    @Test
    public void findByUrl() {
        Feed feed = repository.findByUrl(URL);
        
        Assert.assertEquals(SomeFeeds.ATOM1.toXML(), feed.createDocument().toXML());
        XOMTestCase.assertEquals(SomeFeeds.ATOM1, feed.createDocument());
    }

    
    @Test
    public void testDeleteOldEntries() {
        Feed feed = repository.find(feed1.getId());
        
        repository.deleteEntriesOlderThan(feed, new DateTime(2110, 1, 1, 0, 0, 0, 0));
        
        repository.store(feed);
        
        Feed storedFeed = repository.find(feed1.getId());
        
        Assert.assertEquals(1, storedFeed.getEntries().size());
    }

    @Test
    public void persistOrUpdateWithNonExistingFeed() {
        URI otherUri = URI.create("http://dummy.com");
        Feed feed2 = new Feed(otherUri, ContentType.ATOM, SomeFeeds.ATOM2);
        
        repository.persistOrUpdate(feed2);
        
        Feed storedFeed = repository.findByUrl(otherUri);
        System.out.println(storedFeed);
        Assert.assertEquals(2, storedFeed.getEntries().size());
    }

    
    @Test
    public void persistOrUpdateWithExistingFeed() {
        Feed feed2 = new Feed(URL, ContentType.ATOM, SomeFeeds.ATOM2);
        
        repository.persistOrUpdate(feed2);
        
        Feed storedFeed = repository.find(feed1.getId());
        Assert.assertEquals(3, storedFeed.getEntries().size());
    }
    
        
    
}
