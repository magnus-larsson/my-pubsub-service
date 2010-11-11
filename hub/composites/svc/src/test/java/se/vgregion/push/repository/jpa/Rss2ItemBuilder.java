package se.vgregion.push.repository.jpa;

import nu.xom.Element;

public class Rss2ItemBuilder extends Rss2Builder<Rss2ItemBuilder> {

    public Rss2ItemBuilder() {
        super("item");
    }
    
    public Element build() {
        return root;
    }

    public Rss2ItemBuilder id(String id) {
        root.appendChild(createElement("guid", null, id));
        return this;
    }


}
