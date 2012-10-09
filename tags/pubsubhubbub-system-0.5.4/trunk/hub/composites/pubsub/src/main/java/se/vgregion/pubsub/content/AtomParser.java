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
                    builder.field(child);
                } else if("updated".equals(child.getLocalName())) {
                    builder.updated(DateTimeUtils.parseDateTime(child.getValue()));
                    builder.field(child);
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
            }
            
//            } else if("content".equals(child.getLocalName()) && isAtom(child)) {
//                entryBuilder.content(child);
            entryBuilder.field(child);
        }

        return entryBuilder.build();
    }
    
    private boolean isAtom(Element elm) {
        return Namespaces.ATOM.equals(elm.getNamespaceURI());
    }
}
