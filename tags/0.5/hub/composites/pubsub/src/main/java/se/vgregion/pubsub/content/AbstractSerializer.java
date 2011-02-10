package se.vgregion.pubsub.content;

import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;

public abstract class AbstractSerializer {

    public static AbstractSerializer create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomSerializer();
        } else {
            return new Rss2Serializer();
        }
    }
    
    public static Document printFeed(ContentType type, Feed feed) {
        return create(type).print(feed);
    }

    public static Document printFeed(ContentType type, Feed feed, EntryFilter entryFilter) {
        return create(type).print(feed, entryFilter);
    }

    
    public Document print(Feed feed) {
        return print(feed, null);
    }
    
    public abstract Document print(Feed feed, EntryFilter entryFilter);
    
    protected Element print(String name, String ns, String value) {
        Element elm = new Element(name, ns);
        elm.appendChild(value);
        return elm;
    }

    protected Element print(String name, String ns, DateTime value) {
        if(value != null) {
            Element elm = new Element(name, ns);
            elm.appendChild(DateTimeUtils.print(value));
            return elm;
        } else {
            return null;
        }
    }
}
