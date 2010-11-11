package se.vgregion.push.repository.jpa;

import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import se.vgregion.push.types.Feed;

public abstract class AtomBuilder<T extends AtomBuilder<?>> {

    protected Element root;
    
    public AtomBuilder(String rootName) {
        this.root = new Element(rootName, Feed.NS_ATOM);
    }
    
    protected Element createElement(String localName, String text) {
        Element elm = new Element(localName, Feed.NS_ATOM);
        elm.appendChild(text);
        return elm;
    }
    
    public T id(String id) {
        root.appendChild(createElement("id", id));
        return (T) this;
    }

    public T updated(DateTime updated) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        root.appendChild(createElement("updated", fmt.print(updated)));
        return (T) this;
    }
}
