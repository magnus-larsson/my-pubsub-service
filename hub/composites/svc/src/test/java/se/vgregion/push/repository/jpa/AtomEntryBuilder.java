package se.vgregion.push.repository.jpa;

import nu.xom.Element;

public class AtomEntryBuilder extends AtomBuilder<AtomEntryBuilder> {

    public AtomEntryBuilder() {
        super("entry");
    }
    
    public Element build() {
        return root;
    }
}
