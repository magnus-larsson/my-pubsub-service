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

import java.net.URI;

import nu.xom.Document;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;


public class FeedTest {

    @Test
    public void getDocument() throws Exception {
        
        Feed feed = new Feed(URI.create("http://example.com"), ContentType.ATOM, SomeFeeds.ATOM1);
        
        Document actualDoc = feed.createDocument();
        Assert.assertEquals(2, actualDoc.getRootElement().getChildElements("entry", Feed.NS_ATOM).size());
    }

    @Test
    public void updatedSince() throws Exception {
        Feed feed = new Feed(URI.create("http://example.com"), ContentType.ATOM, SomeFeeds.ATOM1);
        
        DateTime dt = new DateTime(2010, 9, 14, 18, 30, 2, 0);
        
        Document actualDoc = feed.createDocument(dt);
        
        Assert.assertEquals(1, actualDoc.getRootElement().getChildElements("entry", Feed.NS_ATOM).size());
    }
    
    @Test
    public void merge() {
        Feed feed = new Feed(URI.create("http://example.com"), ContentType.ATOM, SomeFeeds.ATOM1);
        Feed feed2 = new Feed(URI.create("http://example.com"), ContentType.ATOM, SomeFeeds.ATOM2);
        
        feed.merge(feed2);
        
        // verify that the feed level elements has been updated
        Assert.assertEquals("urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af8", feed.getAtomId());
        
        // verify that the entries as been updated
        Assert.assertEquals(3, feed.getEntries().size());
        
    }

}
