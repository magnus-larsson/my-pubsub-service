package se.vgregion.pubsub.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import nu.xom.Element;

import org.hibernate.annotations.Cascade;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.FieldType;

@Entity
@Table(name="FEEDS")
public class DefaultFeed extends AbstractEntity<Long> implements Feed {

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
            feed.fields.add(new DefaultField(elm.getNamespaceURI(), elm.getLocalName(), FieldType.ELEMENT, elm.getValue()));
            return this;
        }

        public FeedBuilder field(String namespace, String name, String value) {
            feed.fields.add(new DefaultField(namespace, name, FieldType.ELEMENT, value));
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
    private Long id;
    
    @Basic
    private String feedId;

    @Basic
    private long updated;
    
    @OneToMany(cascade=CascadeType.ALL, targetEntity=DefaultField.class)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<Field> fields = new ArrayList<Field>();
    
    @OneToMany(cascade=CascadeType.ALL, targetEntity=DefaultEntry.class)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("updated DESC")
    private List<Entry> entries = new ArrayList<Entry>();
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
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
        return new DateTime(updated, DateTimeZone.UTC);
    }

}
