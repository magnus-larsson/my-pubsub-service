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

package se.vgregion.push.types;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;


public class FeedTest {

    @Test
    public void merge() {
        Feed feed = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1).custom(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

        Feed feed2 = new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED2).custom(UnitTestConstants.ATOM_TITLE2)
            .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).build())
            .build();

        
        feed.merge(feed2);
        
        // verify that the feed level elements has been updated
        Assert.assertEquals(UnitTestConstants.ATOM_TITLE2.getValue(), feed.getCustom().get(0).getValue());
        
        // verify that the entries as been updated
        Assert.assertEquals(3, feed.getEntries().size());
        
    }

}
