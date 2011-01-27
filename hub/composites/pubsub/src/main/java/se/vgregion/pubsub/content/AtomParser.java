package se.vgregion.pubsub.content;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class AtomParser extends AbstractParser {

    public Feed parse(Document document, ContentType contentType) {
        Element feed = document.getRootElement();
        FeedBuilder builder = new FeedBuilder(contentType);
        
        Elements children = feed.getChildElements();
        
        for(int i = 0; i<children.size(); i++) {
            Element child = children.get(i);
            
            if(isAtom(child)) {
                if("id".equals(child.getLocalName())) {
                    builder.id(child.getValue());
                } else if("updated".equals(child.getLocalName())) {
                    builder.updated(DateTimeUtils.parseDateTime(child.getValue()));
                } else if("entry".equals(child.getLocalName())) {
                    builder.entry(parseEntry(child));
                } else {
                    builder.field(child);
                }
            } else {
                builder.field(child);
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
                entryBuilder.updated(DateTimeUtils.parseDateTime(child.getValue()));
            } else if("content".equals(child.getLocalName()) && isAtom(child)) {
                entryBuilder.content(child);
            } else {
                entryBuilder.field(child);
            }
        }

        return entryBuilder.build();
    }
    
    private boolean isAtom(Element elm) {
        return Namespaces.NS_ATOM.equals(elm.getNamespaceURI());
    }
}
