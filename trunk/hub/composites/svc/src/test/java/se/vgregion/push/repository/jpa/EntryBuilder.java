package se.vgregion.push.repository.jpa;

import nu.xom.Element;

public class EntryBuilder extends AtomBuilder<EntryBuilder> {

    public EntryBuilder() {
        super("entry");
    }
    
    public Element build() {
        return root;
    }
}
