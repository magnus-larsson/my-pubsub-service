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
import se.vgregion.pubsub.Namespaces;

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

        public EntryBuilder content(Element elm) {
            entry.content = XmlUtil.xmlToString(elm);
            return this;
        }

        public EntryBuilder content(String content) {
            Element elm = new Element("content", Namespaces.NS_ATOM);
            elm.appendChild(content);
            entry.content = XmlUtil.xmlToString(elm);
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
    
    private Long pk;
    
    private String entryId = UUID.randomUUID().toString();
    
    private Long updated;

    private String content;
    
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
    public Element getContent() {
        if(content == null) return null;
        
        return XmlUtil.stringToXml(content);
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

    @Override
    public void merge(Entry entry) {
        this.entryId = entry.getEntryId();
        if(entry.getContent() != null) {
            this.content = XmlUtil.xmlToString(entry.getContent());
        } else {
            this.content = null;
        }
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
