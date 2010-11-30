package se.vgregion.pubsub.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import nu.xom.Element;

import org.hibernate.annotations.Cascade;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Field;

@Entity
@Table(name="ENTRIES")
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

        
        public Entry build() {
            return entry;
        }
    }
    
    @Id
    @GeneratedValue
    @SuppressWarnings("unused") // only used by JPA
    private Long pk;
    
    @Column(nullable=false, unique=true)
    private String entryId = UUID.randomUUID().toString();
    
    @Basic
    private Long updated;
    
    @OneToMany(cascade=CascadeType.ALL, targetEntity=DefaultField.class)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
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
        if(updated > 0) {
            return new DateTime(updated, DateTimeZone.UTC);
        } else {
            return null;
        }
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }
    
    private Field getField(String namespace, String name) {
        // TODO handle multiple fields with same ns and name
        for(Field field : fields) {
            Element elm = field.toXml();
            if(elm.getNamespaceURI().equals(namespace) && elm.getLocalName().equals(name)) {
                return field;
            }
        }
        
        return null;
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

    @Override
    public void merge(Entry entry) {
        this.entryId = entry.getEntryId();
        if(entry.getUpdated() != null) {
            this.updated = entry.getUpdated().getMillis();
        } else {
            this.updated = null;
        }
        
        fields.clear();
        
        for(Field otherField : entry.getFields()) {
            fields.add(otherField);
        }
        
    }

}
