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

import nu.xom.Document;
import nu.xom.Element;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;
import se.vgregion.push.types.UnitTestConstants;



public class Rss2SerializerTest {

    
    @Test
    public void print() throws Exception {
        FeedBuilder builder = new FeedBuilder();
        builder.id("f1").updated(UnitTestConstants.UPDATED1).custom(UnitTestConstants.RSS2_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).custom(UnitTestConstants.RSS2_TITLE).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).custom(UnitTestConstants.RSS2_TITLE).build());
        
        Rss2Serializer serializer = new Rss2Serializer();
        Document doc = serializer.print(builder.build());
        
        Assert.assertEquals("", doc.getRootElement().getNamespaceURI());
        Assert.assertEquals("rss", doc.getRootElement().getLocalName());
        
        Element channel = doc.getRootElement().getFirstChildElement("channel");
        
        Assert.assertNotNull(channel);
        
        System.out.println(doc.toXML());
        
        Assert.assertEquals("f1", channel.getFirstChildElement("link").getValue());
        Assert.assertEquals("foobar", channel.getFirstChildElement("title").getValue());
        Assert.assertEquals("2010-02-28T23:00:00.000Z", channel.getFirstChildElement("pubDate").getValue());
        
        Assert.assertEquals(2, channel.getChildElements("item").size());
        
        Element entry = channel.getChildElements("item").get(0);
        Assert.assertEquals("e1", entry.getFirstChildElement("guid").getValue());
        Assert.assertEquals("foobar", entry.getFirstChildElement("title").getValue());
        Assert.assertEquals("2010-02-28T23:00:00.000Z", entry.getFirstChildElement("pubDate").getValue());

    }

    @Test
    public void printWithFilter() throws Exception {
        FeedBuilder builder = new FeedBuilder();
        builder.id("f1").updated(UnitTestConstants.UPDATED1).custom(UnitTestConstants.RSS2_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).custom(UnitTestConstants.RSS2_TITLE).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).custom(UnitTestConstants.RSS2_TITLE).build());
        
        Rss2Serializer serializer = new Rss2Serializer();
        Document doc = serializer.print(builder.build(), new EntryFilter() {
            @Override
            public boolean include(Entry entry) {
                return entry.getEntryId().equals("e1");
            }
        });
        
        Element channel = doc.getRootElement().getFirstChildElement("channel");

        Assert.assertEquals(1, channel.getChildElements("item").size());
        
        Element entry = channel.getChildElements("item").get(0);
        Assert.assertEquals("e1", entry.getFirstChildElement("guid").getValue());
    }

}
