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

import org.joda.time.DateTime;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;

public abstract class AbstractSerializer {

    public static AbstractSerializer create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomSerializer();
        } else {
            return new Rss2Serializer();
        }
    }

    public static Document printFeed(ContentType type, Feed feed) {
        return create(type).print(feed);
    }

    public static Document printFeed(ContentType type, Feed feed, EntryFilter entryFilter) {
        return create(type).print(feed, entryFilter);
    }


    public Document print(Feed feed) {
        return print(feed, null);
    }

    public abstract Document print(Feed feed, EntryFilter entryFilter);

    protected Element print(String name, String ns, String value) {
        Element elm = new Element(name, ns);
        elm.appendChild(value);
        return elm;
    }

    protected Element print(String name, String ns, DateTime value) {
        if(value != null) {
            Element elm = new Element(name, ns);
            elm.appendChild(DateTimeUtils.print(value));
            return elm;
        } else {
            return null;
        }
    }

    protected static boolean include(Entry entry, EntryFilter entryFilter) {
        return entryFilter == null || entryFilter.include(entry);
    }
}
