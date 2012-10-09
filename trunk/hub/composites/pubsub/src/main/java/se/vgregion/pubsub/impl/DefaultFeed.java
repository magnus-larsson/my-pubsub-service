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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;

public class DefaultFeed extends AbstractEntity<String> implements Feed {

    public static class FeedBuilder {
        
        private DefaultFeed feed;
        
        public FeedBuilder(ContentType contentType) {
            feed = new DefaultFeed(contentType);
        }
        
        public FeedBuilder id(String id) {
            feed.feedId = id;
            return this;
        }

        public FeedBuilder updated(DateTime updated) {
            feed.updated = updated.getMillis();
            return this;
        }
        
        public FeedBuilder field(Element elm) {
            feed.fields.add(new DefaultField(elm));
            return this;
        }

        public FeedBuilder field(String namespace, String prefix, String name, String value) {
            feed.fields.add(new DefaultField(namespace, prefix, name, value));
            return this;
        }

        public FeedBuilder field(String namespace, String name, String value) {
        	return field(namespace, "", name, value);
        }
        
        public FeedBuilder field(String name, String value) {
        	return field("", "", name, value);
        }
        
        public FeedBuilder entry(Entry entry) {
            feed.entries.add(entry);
            return this;
        }
        
        public Feed build() {
            return feed;
        }
    }

    protected String feedId = UUID.randomUUID().toString();

    private Long updated;
    
    private ContentType contentType;
    
    private List<Field> fields = new ArrayList<Field>();
    
    private List<Entry> entries = new ArrayList<Entry>();
    
    public DefaultFeed(ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getId() {
        return feedId;
    }
    
    @Override
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }
    
    @Override
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
    
    private Entry getEntry(String entryId) {
        return getEntry(this.entries, entryId);
    }

    private Entry getEntry(List<Entry> entries, String entryId) {
        for(Entry entry : entries) {
            if(entry.getEntryId() != null && entry.getEntryId().equals(entryId)) {
                return entry;
            }
        }
        return null;
    }

    
    @Override
    public boolean hasUpdates(DateTime since) {
        if(since == null) return true;
        
        for(Entry entry : entries) {
            if(entry.isNewerThan(since)) return true;
        }
        
        return false;
    }

    @Override
    public String getFeedId() {
        return feedId;
    }

    @Override
    public DateTime getUpdated() {
        if(updated != null) {
            return new DateTime(updated, DateTimeZone.UTC);
        } else {
            return null;
        }
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

}
