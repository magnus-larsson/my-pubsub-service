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
import nu.xom.tests.XOMTestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.UnitTestConstants;


public class AtomParserTest {

    @Test
    public void parse() throws Exception {
        AtomParser parser = new AtomParser();
        Feed feed = parser.parse(UnitTestConstants.ATOM1);
        
        Assert.assertEquals("urn:f1", feed.getFeedId());
        Assert.assertEquals(new DateTime(2010, 1, 2, 3, 4, 5, 0, DateTimeZone.UTC), feed.getUpdated());
        Assert.assertEquals(2, feed.getFields().size());
        
        Element expected = new Element("title", Namespaces.NS_ATOM);
        expected.appendChild("foobar");
        XOMTestCase.assertEquals(expected, feed.getFields().get(0).toXml());

        Assert.assertEquals(2, feed.getEntries().size());
        
        Entry entry = feed.getEntries().get(0);
        
        Assert.assertEquals("urn:e1", entry.getEntryId());
        Assert.assertEquals(new DateTime(2010, 1, 2, 3, 4, 6, 0, DateTimeZone.UTC), entry.getUpdated());
        
        entry = feed.getEntries().get(1);
        
        Assert.assertEquals("urn:e2", entry.getEntryId());
        Assert.assertEquals(new DateTime(2010, 1, 2, 3, 4, 7, 0, DateTimeZone.UTC), entry.getUpdated());
    }

    private Element newAtomElement(String name, String value) {
        Element elm = new Element(name, Namespaces.NS_ATOM);
        elm.appendChild(value);
        return elm;
    }
    
    @Test
    public void parseRecursive() throws Exception {
        AtomParser parser = new AtomParser();

        Element feedElm = new Element("feed", Namespaces.NS_ATOM);
        feedElm.appendChild(newAtomElement("id", "f1"));
        
        Element entry = new Element("entry", Namespaces.NS_ATOM);
        entry.appendChild(newAtomElement("id", "e1"));
        
        Element content = new Element("content", Namespaces.NS_ATOM);
        Element div = new Element("div");
        Element span = new Element("span");
        span.appendChild("Hello");
        div.appendChild(span);
        content.appendChild(div);
        entry.appendChild(content);
        feedElm.appendChild(entry);
        
        Document expected = new Document(feedElm);
        
        Feed feed = parser.parse(expected);
        Document actual = AbstractSerializer.printFeed(ContentType.ATOM, feed);
        
        System.out.println(expected.toXML());
        System.out.println(actual.toXML());
        XOMTestCase.assertEquals(expected, actual);
    }
}
