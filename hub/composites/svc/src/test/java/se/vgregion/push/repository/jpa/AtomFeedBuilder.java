package se.vgregion.push.repository.jpa;

import java.net.URI;

import nu.xom.Document;

import org.joda.time.DateTime;

import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;

public class AtomFeedBuilder extends AtomBuilder<AtomFeedBuilder> {

    private URI url;
    private ContentType type = ContentType.ATOM;
    
    public AtomFeedBuilder(URI url) {
        super("feed");
        this.url= url;
    }
    
    public AtomFeedBuilder entry(String id, DateTime updated) {
        AtomEntryBuilder entryBuilder = new AtomEntryBuilder();
        root.appendChild(entryBuilder.id(id).updated(updated).build());
        return this;
    }
    
    public Feed build() {
        return new Feed(url, type, new Document(root));
    }
}
