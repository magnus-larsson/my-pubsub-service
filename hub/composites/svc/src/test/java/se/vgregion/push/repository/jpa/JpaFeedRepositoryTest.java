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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.SomeFeeds;


public class JpaFeedRepositoryTest {

    private static final URI URL = URI.create("http://example.com/sub11");
    
    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    private FeedRepository repository = ctx.getBean(FeedRepository.class);
    
    private Feed feed1;
    
    @Before
    public void setup() {
        Feed feed = new Feed(URL, ContentType.ATOM, SomeFeeds.ATOM_DOCUMENT);
        feed1 = repository.persist(feed);
        System.out.println(feed1.getId());
    }
    
    @Test
    public void test() {
        Feed feed = repository.find(feed1.getId());
        
        // TODO fix, very dependent on XML serializtion details
        Assert.assertEquals(SomeFeeds.ATOM, feed.getDocument().toXML());
    }
    
}
