package se.vgregion.pubsub.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Field;

@Entity
@Table(name="ENTRIES")
public class DefaultEntry extends AbstractEntity<Long> implements Entry {

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
            entry.fields.add(new DefaultField(elm.getNamespaceURI(), elm.getLocalName(), elm.getValue()));
            return this;
        }

        public EntryBuilder field(String namespace, String name, String value) {
            entry.fields.add(new DefaultField(namespace, name, value));
            return this;
        }

        
        public Entry build() {
            return entry;
        }
    }
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Basic(optional=false)
    private String entryId;
    
    @Basic
    private long updated;
    
    @Transient
    private List<Field> fields = new ArrayList<Field>();
    

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
