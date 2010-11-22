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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.hibernate.annotations.Cascade;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

@Entity
public class Feed extends AbstractEntity<Long> {

    private static final Builder PARSER = new Builder();
    
    public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

    public static class FeedBuilder {
        
        private Feed feed;
        
        public FeedBuilder(URI url, ContentType type) {
            feed = new Feed(url, type);
        }
        
        public FeedBuilder id(String id) {
            feed.feedId = id;
            return this;
        }

        public FeedBuilder updated(DateTime updated) {
            feed.updated = updated.getMillis();
            return this;
        }
        
        public FeedBuilder custom(Element elm) {
            // TODO ugly hack, fix
            feed.xml += new Document((Element) elm.copy()).toXML().replaceFirst("<.+>", "");
            return this;
        }

        public FeedBuilder entry(Entry entry) {
            feed.entries.add(entry);
            return this;
        }
        
        public Feed build() {
            return feed;
        }
    }

    @Id
    @GeneratedValue
    private long id;

    @Column(unique=true)
    private String url;

    @Column
    private String feedId;
    
    @Column
    private ContentType contentType;
    
    @Column(nullable=true)
    @Lob
    private String xml = "";

    @Column
    private long updated;
    
    // TODO remove eager loading
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    // TODO hack since JPA does not support deleting orphans
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("updated DESC")
    private List<Entry> entries = new ArrayList<Entry>();

    
    /* Make JPA happy */
    protected Feed() {
    }
    
    public Feed(URI url, ContentType contentType) {
        this.url = url.toString();
        this.contentType = contentType;
    }
    
    @Override
    public Long getId() {
        return id;
    }
    
    public ContentType getContentType() {
        return contentType;
    }

    public URI getUrl() {
        return URI.create(url);
    }

    public String getFeedId() {
        return feedId;
    }
    
    public DateTime getUpdated() {
        return new DateTime(updated, DateTimeZone.UTC);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public List<Element> getCustom() {
        return XmlUtil.stringToXml(xml);
    }
    
    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    /**
     * Replace entry with same entry ID
     */
    public void addOrReplaceEntry(Entry newEntry) {
        Entry existingEntry = findEntryByEntryId(newEntry.getEntryId());
        
        if(existingEntry != null) {
            entries.remove(existingEntry);
        }
        entries.add(newEntry);
    }

    public boolean hasUpdates(DateTime updatedSince) {
        for(Entry entry : entries) {
            if(updatedSince == null || entry.isNewerThan(updatedSince)) {
                return true;
            }
        }
        
        return false;
        
    }
    
    public void merge(Feed other) {
        this.xml = other.xml;
        this.feedId = other.getFeedId();
        
        List<Entry> otherEntries = other.getEntries();

        for(Entry otherEntry : new ArrayList<Entry>(otherEntries)) {
            addOrReplaceEntry(otherEntry);
        }
    }
    
    private Entry findEntryByEntryId(String entryId) {
        for(Entry entry : entries) {
            if(entry.getEntryId().equals(entryId)) {
                return entry;
            }
        }
        return null;
    }


}
