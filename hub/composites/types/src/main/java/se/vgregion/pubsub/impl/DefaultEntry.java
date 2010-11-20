package se.vgregion.pubsub.impl;

import java.util.Collections;
import java.util.List;

import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.FieldType;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.content.DateTimeUtils;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class DefaultEntry extends AbstractEntity<Entry, Long> implements Entry {

    public static class EntryBuilder {
        
        private DefaultEntry entry;
        
        public EntryBuilder() {
            entry = new DefaultEntry();
        }
        
        public EntryBuilder id(String id) {
            entry.entryId = id;
            return this;
        }

        public EntryBuilder updated(DateTime updated) {
            entry.updated = updated.getMillis();
            return this;
        }
        
        public EntryBuilder custom(Element elm) {
            entry.fields.add(new DefaultField(elm.getNamespaceURI(), elm.getLocalName(), FieldType.ELEMENT, elm.getValue()));
            return this;
        }

        public Entry build() {
            return entry;
        }
    }
    
    private Long id;
    private String entryId;
    private long updated;
    private List<Field> fields = Collections.emptyList();
    

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getEntryId() {
        return entryId;
    }

    @Override
    public DateTime getUpdated() {
        return new DateTime(updated, DateTimeZone.UTC);
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    @Override
    public boolean isNewerThan(DateTime since) {
        return getUpdated().isAfter(since);
    }

}
