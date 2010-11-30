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

package se.vgregion.pubsub.impl;

import nu.xom.tests.XOMTestCase;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;


public class DefaultFeedTest {

    @Test
    public void merge() throws Exception {
        Feed feed = new FeedBuilder()
            .id("f1").updated(UnitTestConstants.UPDATED2).field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

        Feed feed2 = new FeedBuilder()
            .id("f1").updated(UnitTestConstants.UPDATED1).field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build())
            .build();

        
        feed.merge(feed2);
        
        Assert.assertEquals("f1", feed.getFeedId());
        Assert.assertEquals(UnitTestConstants.UPDATED1, feed.getUpdated());
        Assert.assertEquals(1, feed.getFields().size());
        XOMTestCase.assertEquals(UnitTestConstants.ATOM_TITLE, feed.getFields().get(0).toXml());
        
        Assert.assertEquals(3, feed.getEntries().size());
        
        Entry entry = feed.getEntries().get(0);
        Assert.assertEquals("e3", entry.getEntryId());
        Assert.assertEquals(UnitTestConstants.UPDATED1, entry.getUpdated());

        entry = feed.getEntries().get(1);
        Assert.assertEquals("e2", entry.getEntryId());
        Assert.assertEquals(UnitTestConstants.UPDATED1, entry.getUpdated());
        
        entry = feed.getEntries().get(2);
        Assert.assertEquals("e1", entry.getEntryId());
        Assert.assertEquals(UnitTestConstants.UPDATED1, entry.getUpdated());
        

    }
}
