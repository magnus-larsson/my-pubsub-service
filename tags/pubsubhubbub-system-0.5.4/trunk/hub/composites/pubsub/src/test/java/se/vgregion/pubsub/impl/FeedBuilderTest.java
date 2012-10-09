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

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;



public class FeedBuilderTest {

    @Test
    public void merge() {
        Feed feed = new FeedBuilder(ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1).field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

        Assert.assertEquals("f1", feed.getFeedId());
        Assert.assertEquals(UnitTestConstants.UPDATED1, feed.getUpdated());
        Assert.assertEquals(1, feed.getFields().size());
        XOMTestCase.assertEquals(UnitTestConstants.ATOM_TITLE, XmlUtil.fieldToXml(feed.getFields().get(0)));
        
        Assert.assertEquals(2, feed.getEntries().size());
        Assert.assertEquals("e1", feed.getEntries().get(0).getEntryId());
        Assert.assertEquals("e2", feed.getEntries().get(1).getEntryId());
    }
}
