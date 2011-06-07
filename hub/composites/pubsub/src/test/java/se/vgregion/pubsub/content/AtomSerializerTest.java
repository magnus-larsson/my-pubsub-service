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
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;
import se.vgregion.pubsub.impl.DefaultField;


public class AtomSerializerTest {

    
    @Test
    public void print() throws Exception {
        Field content = new DefaultField(Namespaces.ATOM, "", "content", "<div xmlns='http://www.w3.org/1999/xhtml'>foo</div>");
        content.getFields().add(new DefaultField("", "", "type", "xhtml"));
        
        
        FeedBuilder builder = new FeedBuilder(ContentType.ATOM);
        builder.id("f1").updated(UnitTestConstants.UPDATED1)
            .field(Namespaces.ATOM, "id", "f1")
            .field(Namespaces.ATOM, "updated", UnitTestConstants.UPDATED1_STR)
            .field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder()
                    .field(Namespaces.ATOM, "id", "e1")
                    .field(Namespaces.ATOM, "updated", UnitTestConstants.UPDATED1_STR)
                    .field(UnitTestConstants.ATOM_TITLE)
                    .field(content)
                    .build()
                    )
            .entry(new EntryBuilder()
                    .field(Namespaces.ATOM, "id", "e2")
                    .field(Namespaces.ATOM, "updated", UnitTestConstants.UPDATED2_STR)
                    .field(UnitTestConstants.ATOM_TITLE).build());
        
        AtomSerializer serializer = new AtomSerializer();
        Document doc = serializer.print(builder.build());
        
        Assert.assertEquals(Namespaces.ATOM, doc.getRootElement().getNamespaceURI());
        Assert.assertEquals("feed", doc.getRootElement().getLocalName());
        
        Assert.assertEquals("f1", doc.getRootElement().getFirstChildElement("id", Namespaces.ATOM).getValue());
        Assert.assertEquals("foobar", doc.getRootElement().getFirstChildElement("title", Namespaces.ATOM).getValue());
        Assert.assertEquals("2010-03-01T00:00:00.000Z", doc.getRootElement().getFirstChildElement("updated", Namespaces.ATOM).getValue());
        
        
        Assert.assertEquals(2, doc.getRootElement().getChildElements("entry", Namespaces.ATOM).size());
        
        Element entry = doc.getRootElement().getChildElements("entry", Namespaces.ATOM).get(0);
        Assert.assertEquals("e1", entry.getFirstChildElement("id", Namespaces.ATOM).getValue());
        Assert.assertEquals("foobar", entry.getFirstChildElement("title", Namespaces.ATOM).getValue());
        Assert.assertEquals("2010-03-01T00:00:00.000Z", entry.getFirstChildElement("updated", Namespaces.ATOM).getValue());

        Element contentElm = entry.getFirstChildElement("content", Namespaces.ATOM);
        Assert.assertNotNull(contentElm);
        Assert.assertEquals(1, contentElm.getChildCount());
        Assert.assertEquals("div", ((Element)contentElm.getChild(0)).getLocalName());
        Assert.assertEquals("http://www.w3.org/1999/xhtml", ((Element)contentElm.getChild(0)).getNamespaceURI());
        Assert.assertEquals("foo", ((Element)contentElm.getChild(0)).getValue());
        Assert.assertEquals(1, contentElm.getAttributeCount());
        Assert.assertEquals("type", contentElm.getAttribute(0).getLocalName());
        Assert.assertEquals("", contentElm.getAttribute(0).getNamespaceURI());
        Assert.assertEquals("xhtml", contentElm.getAttribute(0).getValue());
    }

    @Test
    public void printWithFilter() throws Exception {
        FeedBuilder builder = new FeedBuilder(ContentType.ATOM);
        builder.id("f1").updated(UnitTestConstants.UPDATED1)
            .field(Namespaces.ATOM, "id", "f1")
            .field(Namespaces.ATOM, "updated", UnitTestConstants.UPDATED1_STR)
            .field(UnitTestConstants.ATOM_TITLE)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1)
                    .field(Namespaces.ATOM, "id", "e1")
                    .field(Namespaces.ATOM, "updated", UnitTestConstants.UPDATED1_STR)
                    .field(UnitTestConstants.ATOM_TITLE).build()
                    )
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED2)
                    .field(Namespaces.ATOM, "id", "e2")
                    .field(Namespaces.ATOM, "updated", UnitTestConstants.UPDATED2_STR)
                    .field(UnitTestConstants.ATOM_TITLE).build());
        
        AtomSerializer serializer = new AtomSerializer();
        Document doc = serializer.print(builder.build(), new EntryFilter() {
            @Override
            public boolean include(Entry entry) {
                return entry.getEntryId().equals("e1");
            }
        });
        
        Assert.assertEquals(1, doc.getRootElement().getChildElements("entry", Namespaces.ATOM).size());
        
        Element entry = doc.getRootElement().getChildElements("entry", Namespaces.ATOM).get(0);
        Assert.assertEquals("e1", entry.getFirstChildElement("id", Namespaces.ATOM).getValue());
    }

}
