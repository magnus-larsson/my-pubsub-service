package se.vgregion.pubsub.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Field;

public class DefaultEntry extends AbstractEntity<String> implements Entry {

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

        public EntryBuilder field(Element elm) {
            entry.fields.add(new DefaultField(elm));
            return this;
        }

        public EntryBuilder field(String namespace, String name, String value) {
            entry.fields.add(new DefaultField(namespace, name, value));
            return this;
        }

        public EntryBuilder field(String name, String value) {
            entry.fields.add(new DefaultField("", name, value));
            return this;
        }

        public EntryBuilder field(Field field) {
            entry.fields.add(field);
            return this;
        }
        
        public Entry build() {
            return entry;
        }
    }
    
    private String entryId = UUID.randomUUID().toString();
    
    private Long updated;

    private List<Field> fields = new ArrayList<Field>();
    
    @Override
    public String getId() {
        return entryId;
    }

    @Override
    public String getEntryId() {
        return entryId;
    }

    @Override
    public DateTime getUpdated() {
        if(updated != null && updated > 0) {
            return new DateTime(updated, DateTimeZone.UTC);
        } else {
            return null;
        }
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }
    
    @Override
    public boolean isNewerThan(DateTime since) {
        DateTime thisUpdated = getUpdated();
        if(thisUpdated == null) {
            return true;
        } else {
            return thisUpdated.isAfter(since);
        }
    }
}
