/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.content;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class Rss2Parser extends AbstractParser {

    public Feed parse(Document document, ContentType contentType) {
        Element rss = document.getRootElement();
        FeedBuilder builder = new FeedBuilder(contentType);
        
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
                builder.field(child);
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
//            } else if("description".equals(child.getLocalName())) {
//                entryBuilder.content(child);
            } 
                
            entryBuilder.field(child);
        }

        return entryBuilder.build();
    }
}
