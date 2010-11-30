package se.vgregion.pubsub.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import nu.xom.Element;

import org.hibernate.annotations.Cascade;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;

@Entity
@Table(name="FEEDS")
public class DefaultFeed extends AbstractEntity<String> implements Feed {

    public static class FeedBuilder {
        
        private DefaultFeed feed;
        
        public FeedBuilder() {
            feed = new DefaultFeed();
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

        public FeedBuilder field(String namespace, String name, String value) {
            feed.fields.add(new DefaultField(namespace, name, value));
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
    @SuppressWarnings("unused") // only used by JPA
    protected Long pk;
    
    @Column(nullable=false, unique=true)
    protected String feedId = UUID.randomUUID().toString();

    @Basic
    private Long updated;
    
    @OneToMany(cascade=CascadeType.ALL, targetEntity=DefaultField.class)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<Field> fields = new ArrayList<Field>();
    
    @OneToMany(cascade=CascadeType.ALL, targetEntity=DefaultEntry.class)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("updated DESC")
    private List<Entry> entries = new ArrayList<Entry>();
    
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
    public void merge(Feed feed) {
        this.feedId = feed.getFeedId();
        if(feed.getUpdated() != null) {
            this.updated = feed.getUpdated().getMillis();
        } else {
            this.updated = null;
        }
        
        fields.clear();
        
        for(Field otherField : feed.getFields()) {
            fields.add(otherField);
        }
        
        List<Entry> newEntries = new ArrayList<Entry>();
        
        
        // check for updated entries
        for(Entry otherEntry : feed.getEntries()) {
            Entry existingEntry = getEntry(otherEntry.getEntryId());
            
            if(existingEntry != null) {
                existingEntry.merge(otherEntry);
                newEntries.add(existingEntry);
            } else {
                newEntries.add(otherEntry);
            }
        }
        
        // check if any of the existing entries do not exist in the new feed
        for(Entry entry : entries) {
            Entry otherEntry = getEntry(feed.getEntries(), entry.getEntryId());
            if(otherEntry == null) {
                newEntries.add(entry);
            }
        }

        this.entries = newEntries;
    }

}
