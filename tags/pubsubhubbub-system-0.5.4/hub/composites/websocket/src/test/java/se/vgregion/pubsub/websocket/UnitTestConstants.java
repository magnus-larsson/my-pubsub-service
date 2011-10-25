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

package se.vgregion.pubsub.websocket;

import java.net.URI;

import org.joda.time.DateTime;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class UnitTestConstants {
    public static final URI CALLBACK = URI.create("http://example.com/sub11");
    public static final URI TOPIC = URI.create("http://example.com/feed");
    public static final URI TOPIC2 = URI.create("http://example.com/feed2");
    
    public static DateTime UPDATED1 = new DateTime(2010, 3, 1, 0, 0, 0, 0);
    public static DateTime UPDATED2 = new DateTime(2010, 2, 1, 0, 0, 0, 0);
    public static DateTime UPDATED3 = new DateTime(2010, 1, 1, 0, 0, 0, 0);
    
    public static DateTime FUTURE = new DateTime(2050, 1, 1, 0, 0, 0, 0);

    public static Feed atom1() {
        return new FeedBuilder(ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build())
            .build();
    }

    public static Feed atom2() {
        return new FeedBuilder(ContentType.ATOM)
        .id("f2").updated(UnitTestConstants.UPDATED1)
        .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
        .entry(new EntryBuilder().id("e4").updated(UnitTestConstants.UPDATED1).build())
        .build();
    }
}
