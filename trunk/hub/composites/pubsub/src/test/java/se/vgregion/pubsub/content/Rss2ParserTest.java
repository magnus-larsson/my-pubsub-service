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

package se.vgregion.pubsub.content;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.push.types.UnitTestConstants;


public class Rss2ParserTest {

    @Test
    public void parse() throws Exception {
        Rss2Parser parser = new Rss2Parser();
        Feed feed = parser.parse(UnitTestConstants.RSS1);
        
        Assert.assertEquals("http://www.example.com/", feed.getFeedId());
        Assert.assertEquals(new DateTime(2010, 1, 2, 3, 4, 5, 0, DateTimeZone.UTC), feed.getUpdated());
        Assert.assertEquals(1, feed.getFields().size());
        
        Assert.assertEquals("", feed.getFields().get(0).getNamespace());
        Assert.assertEquals("title", feed.getFields().get(0).getName());
        Assert.assertEquals("foobar", feed.getFields().get(0).getValue());

        Assert.assertEquals(2, feed.getEntries().size());
        
        Entry entry = feed.getEntries().get(0);
        
        Assert.assertEquals("i1", entry.getEntryId());
        Assert.assertEquals(new DateTime(2010, 1, 2, 3, 4, 6, 0, DateTimeZone.UTC), entry.getUpdated());
        
        entry = feed.getEntries().get(1);
        
        Assert.assertEquals("i2", entry.getEntryId());
        Assert.assertEquals(new DateTime(2010, 1, 2, 3, 4, 7, 0, DateTimeZone.UTC), entry.getUpdated());
    }
}
