package se.vgregion.push.types;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;

public class Rss2Serializer extends AbstractSerializer {

    public Document print(Feed feed, EntryFilter entryFilter) {
        Element feedElm = new Element("rss");
        feedElm.addAttribute(new Attribute("version", "2.0"));
        
        Element channel = new Element("channel");
        feedElm.appendChild(channel);
        
        channel.appendChild(print("link", feed.getFeedId()));
        channel.appendChild(print("pubDate", feed.getUpdated()));
        
        List<Element> customs = feed.getCustom();
        
        for(Element custom : customs) {
            channel.appendChild(custom);
        }
        
        for(Entry entry : feed.getEntries()) {
            if(entryFilter == null || entryFilter.include(entry)) {
                channel.appendChild(print(entry));
            }
        }
        
        return new Document(feedElm);
    }
    
    private Element print(Entry entry) {
        Element entryElm = new Element("item");
        
        entryElm.appendChild(print("guid", entry.getEntryId()));
        entryElm.appendChild(print("pubDate", entry.getUpdated()));
        
        List<Element> customs = entry.getCustom();
        
        for(Element custom : customs) {
            entryElm.appendChild(custom);
        }
        
        return entryElm;

    }
    
    private Element print(String name, String value) {
        return print(name, null, value);
    }

    private Element print(String name, DateTime value) {
        return print(name, null, value);
    }
}
