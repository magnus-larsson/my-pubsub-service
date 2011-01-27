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
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;


public class AtomSerializerTest {

    
    @Test
    public void print() throws Exception {
        FeedBuilder builder = new FeedBuilder(ContentType.ATOM);
        builder.id("f1").updated(UnitTestConstants.UPDATED1).field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).field(UnitTestConstants.ATOM_TITLE).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).field(UnitTestConstants.ATOM_TITLE).build());
        
        AtomSerializer serializer = new AtomSerializer();
        Document doc = serializer.print(builder.build());
        
        Assert.assertEquals(Namespaces.NS_ATOM, doc.getRootElement().getNamespaceURI());
        Assert.assertEquals("feed", doc.getRootElement().getLocalName());
        
        Assert.assertEquals("f1", doc.getRootElement().getFirstChildElement("id", Namespaces.NS_ATOM).getValue());
        Assert.assertEquals("foobar", doc.getRootElement().getFirstChildElement("title", Namespaces.NS_ATOM).getValue());
        Assert.assertEquals("2010-03-01T00:00:00.000Z", doc.getRootElement().getFirstChildElement("updated", Namespaces.NS_ATOM).getValue());
        
        Assert.assertEquals(2, doc.getRootElement().getChildElements("entry", Namespaces.NS_ATOM).size());
        
        Element entry = doc.getRootElement().getChildElements("entry", Namespaces.NS_ATOM).get(0);
        Assert.assertEquals("e1", entry.getFirstChildElement("id", Namespaces.NS_ATOM).getValue());
        Assert.assertEquals("foobar", entry.getFirstChildElement("title", Namespaces.NS_ATOM).getValue());
        Assert.assertEquals("2010-03-01T00:00:00.000Z", entry.getFirstChildElement("updated", Namespaces.NS_ATOM).getValue());

    }

    @Test
    public void printWithFilter() throws Exception {
        FeedBuilder builder = new FeedBuilder(ContentType.ATOM);
        builder.id("f1").updated(UnitTestConstants.UPDATED1).field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).field(UnitTestConstants.ATOM_TITLE).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2).field(UnitTestConstants.ATOM_TITLE).build());
        
        AtomSerializer serializer = new AtomSerializer();
        Document doc = serializer.print(builder.build(), new EntryFilter() {
            @Override
            public boolean include(Entry entry) {
                return entry.getEntryId().equals("e1");
            }
        });
        
        Assert.assertEquals(1, doc.getRootElement().getChildElements("entry", Namespaces.NS_ATOM).size());
        
        Element entry = doc.getRootElement().getChildElements("entry", Namespaces.NS_ATOM).get(0);
        Assert.assertEquals("e1", entry.getFirstChildElement("id", Namespaces.NS_ATOM).getValue());
    }

}
