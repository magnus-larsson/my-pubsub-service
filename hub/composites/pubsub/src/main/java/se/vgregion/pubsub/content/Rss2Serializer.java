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

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.impl.XmlUtil;

public class Rss2Serializer extends AbstractSerializer {

    public Document print(Feed feed, EntryFilter entryFilter) {
        Element feedElm = new Element("rss");
        feedElm.addAttribute(new Attribute("version", "2.0"));
        
        Element channel = new Element("channel");
        feedElm.appendChild(channel);
        
        channel.appendChild(print("link", feed.getFeedId()));
        channel.appendChild(print("pubDate", feed.getUpdated()));
        
        List<Field> customs = feed.getFields();
        
        for(Field custom : customs) {
            channel.appendChild(XmlUtil.fieldToXml(custom));
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
        
//        entryElm.appendChild(print("guid", entry.getEntryId()));
//        entryElm.appendChild(print("pubDate", entry.getUpdated()));
        
        
        List<Field> customs = entry.getFields();
        
        for(Field custom : customs) {
            entryElm.appendChild(XmlUtil.fieldToXml(custom));
        }
        
//        Element content = entry.getContent();
//        if(content != null) {
//            // in Atom NS, needs be be changed
//            Element rssContent = new Element("description");
//            for(int i = 0; i<content.getChildCount(); i++) {
//                rssContent.appendChild(content.getChild(i).copy());
//            }
//        }
        
        return entryElm;

    }
    
    private Element print(String name, String value) {
        return print(name, null, value);
    }

    private Element print(String name, DateTime value) {
        return print(name, null, value);
    }
}
