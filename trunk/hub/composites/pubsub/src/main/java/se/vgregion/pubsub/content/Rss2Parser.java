package se.vgregion.pubsub.content;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class Rss2Parser extends AbstractParser {

    public Feed parse(Document document) {
        Element rss = document.getRootElement();
        FeedBuilder builder = new FeedBuilder();
        
        Element channel = rss.getFirstChildElement("channel");
        
        if(channel == null) {
            throw new IllegalArgumentException("Invalid RSS, missing channel element");
        }
        
        Elements children = channel.getChildElements();
        
        for(int i = 0; i<children.size(); i++) {
            Element child = children.get(i);
            if("link".equals(child.getLocalName())) {
                builder.id(child.getValue());
            } else if("pubDate".equals(child.getLocalName())) {
                builder.updated(DateTimeUtils.parseDateTime(child.getValue()));
            } else if("item".equals(child.getLocalName())) {
                builder.entry(parseItem(child));
            } else {
                builder.custom(child);
            }
        }
        
        return builder.build();
    }
    
    private Entry parseItem(Element entry) {
        EntryBuilder entryBuilder = new EntryBuilder();

        Elements children = entry.getChildElements();
        
        for(int i = 0; i<children.size(); i++) {
            Element child = children.get(i);
            
            if("guid".equals(child.getLocalName())) {
                entryBuilder.id(child.getValue());
            } else if("pubDate".equals(child.getLocalName())) {
                entryBuilder.updated(DateTimeUtils.parseDateTime(child.getValue()));
            } else {
                entryBuilder.custom(child);
            }
        }

        return entryBuilder.build();
    }
}
