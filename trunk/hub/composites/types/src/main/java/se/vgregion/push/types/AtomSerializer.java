package se.vgregion.push.types;

import java.util.List;

import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;

public class AtomSerializer extends AbstractSerializer {

    public Document print(Feed feed, EntryFilter entryFilter) {
        Element feedElm = new Element("feed", Feed.NS_ATOM);
        
        feedElm.appendChild(print("id", feed.getFeedId()));
        feedElm.appendChild(print("updated", feed.getUpdated()));
        
        List<Element> customs = feed.getCustom();
        
        for(Element custom : customs) {
            feedElm.appendChild(custom);
        }
        
        for(Entry entry : feed.getEntries()) {
            if(entryFilter == null || entryFilter.include(entry)) {
                feedElm.appendChild(print(entry));
            }
        }
        
        
        return new Document(feedElm);
    }
    
    private Element print(Entry entry) {
        Element entryElm = new Element("entry", Feed.NS_ATOM);
        
        entryElm.appendChild(print("id", entry.getEntryId()));
        entryElm.appendChild(print("updated", entry.getUpdated()));
        
        List<Element> customs = entry.getCustom();
        
        for(Element custom : customs) {
            entryElm.appendChild(custom);
        }
        
        return entryElm;

    }
    
    private Element print(String name, String value) {
        return print(name, Feed.NS_ATOM, value);
    }

    private Element print(String name, DateTime value) {
        return print(name, Feed.NS_ATOM, value);
    }
}
