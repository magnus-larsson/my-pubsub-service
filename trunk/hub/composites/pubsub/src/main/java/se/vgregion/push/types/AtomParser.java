package se.vgregion.push.types;

import java.net.URI;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;

public class AtomParser extends AbstractParser {

    public Feed parse(URI url, Document document) {
        Element feed = document.getRootElement();
        FeedBuilder builder = new FeedBuilder(url, ContentType.ATOM);
        
        Elements children = feed.getChildElements();
        
        for(int i = 0; i<children.size(); i++) {
            Element child = children.get(i);
            
            if(isAtom(child)) {
                if("id".equals(child.getLocalName())) {
                    builder.id(child.getValue());
                } else if("updated".equals(child.getLocalName())) {
                    builder.updated(parseDateTime(child.getValue()));
                } else if("entry".equals(child.getLocalName())) {
                    builder.entry(parseEntry(child));
                } else {
                    builder.custom(child);
                }
            } else {
                builder.custom(child);
            }
        }
        
        return builder.build();
    }
    
    private Entry parseEntry(Element entry) {
        EntryBuilder entryBuilder = new EntryBuilder();

        Elements children = entry.getChildElements();
        
        for(int i = 0; i<children.size(); i++) {
            Element child = children.get(i);
            
            if("id".equals(child.getLocalName()) && isAtom(child)) {
                entryBuilder.id(child.getValue());
            } else if("updated".equals(child.getLocalName()) && isAtom(child)) {
                entryBuilder.updated(parseDateTime(child.getValue()));
            } else {
                entryBuilder.custom(child);
            }
        }

        return entryBuilder.build();
    }
    
    private boolean isAtom(Element elm) {
        return Feed.NS_ATOM.equals(elm.getNamespaceURI());
    }
}
