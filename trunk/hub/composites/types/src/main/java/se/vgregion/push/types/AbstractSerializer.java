package se.vgregion.push.types;

import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public abstract class AbstractSerializer {

    public static AbstractSerializer create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomSerializer();
        } else {
            return new Rss2Serializer();
        }
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
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        Element elm = new Element(name, ns);
        elm.appendChild(fmt.print(value));
        return elm;
    }
}
