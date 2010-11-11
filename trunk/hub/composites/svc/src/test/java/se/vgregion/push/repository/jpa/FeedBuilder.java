package se.vgregion.push.repository.jpa;

import nu.xom.Element;

import org.joda.time.DateTime;

public abstract class FeedBuilder<T extends FeedBuilder<?>> {

    protected Element root;
    
    public FeedBuilder(String rootName, String ns) {
        this.root = new Element(rootName, ns);
    }
    
    protected Element createElement(String localName, String ns, String text) {
        Element elm = new Element(localName, ns);
        elm.appendChild(text);
        return elm;
    }
    
    public abstract T id(String id);

    public abstract T updated(DateTime updated);
}
