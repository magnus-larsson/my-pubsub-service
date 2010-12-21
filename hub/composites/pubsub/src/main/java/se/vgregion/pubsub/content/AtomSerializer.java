package se.vgregion.pubsub.content;

import java.util.List;

import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.Namespaces;

public class AtomSerializer extends AbstractSerializer {

    public Document print(Feed feed, EntryFilter entryFilter) {
        Element feedElm = new Element("feed", Namespaces.NS_ATOM);
        
        feedElm.appendChild(print("id", feed.getFeedId()));
        Element updated = print("updated", feed.getUpdated());
        if(updated != null) {
            feedElm.appendChild(updated);
        }
        
        List<Field> customs = feed.getFields();
        
        for(Field custom : customs) {
            feedElm.appendChild(custom.toXml());
        }
        
        for(Entry entry : feed.getEntries()) {
            if(entryFilter == null || entryFilter.include(entry)) {
                feedElm.appendChild(print(entry));
            }
        }
        
        
        return new Document(feedElm);
    }
    
    private Element print(Entry entry) {
        Element entryElm = new Element("entry", Namespaces.NS_ATOM);
        
        entryElm.appendChild(print("id", entry.getEntryId()));
        Element updated = print("updated", entry.getUpdated());
        if(updated != null) {
            entryElm.appendChild(updated);
        }
        
        List<Field> customs = entry.getFields();
        
        for(Field custom : customs) {
            entryElm.appendChild(custom.toXml());
        }
        
        Element content = entry.getContent();
        if(content != null) {
            entryElm.appendChild(content);
        }
        
        return entryElm;

    }
    
    private Element print(String name, String value) {
        return print(name, Namespaces.NS_ATOM, value);
    }

    private Element print(String name, DateTime value) {
        return print(name, Namespaces.NS_ATOM, value);
    }
}
