package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nu.xom.Element;

import org.joda.time.DateTime;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.FieldType;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.content.DateTimeUtils;


public class DefaultFeed implements Feed {

    public static class FeedBuilder {
        
        private DefaultFeed feed;
        
        public FeedBuilder() {
            feed = new DefaultFeed();
        }
        
        public FeedBuilder id(String id) {
            feed.fields.add(new DefaultField(Namespaces.NS_ATOM, "id", FieldType.ELEMENT, id));
            return this;
        }

        public FeedBuilder updated(DateTime updated) {
            feed.fields.add(new DefaultField(Namespaces.NS_ATOM, "updated", FieldType.ELEMENT, DateTimeUtils.print(updated)));
            return this;
        }
        
        public FeedBuilder custom(Element elm) {
            feed.fields.add(new DefaultField(elm.getNamespaceURI(), elm.getLocalName(), FieldType.ELEMENT, elm.getValue()));
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

    private List<Field> fields = new ArrayList<Field>();
    private List<Entry> entries = new ArrayList<Entry>();
    
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
        for(Entry entry : entries) {
            if(entry.isNewerThan(since)) return true;
        }
        
        return false;
    }

}
