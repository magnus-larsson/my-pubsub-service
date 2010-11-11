package se.vgregion.push.repository.jpa;

import java.net.URI;

import org.joda.time.DateTime;

import nu.xom.Document;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;

public class FeedBuilder extends AtomBuilder<FeedBuilder> {

    private URI url;
    private ContentType type;
    
    public FeedBuilder(URI url, ContentType type) {
        super("feed");
        this.url= url;
        this.type= type;
    }
    
    public FeedBuilder entry(String id, DateTime updated) {
        EntryBuilder entryBuilder = new EntryBuilder();
        root.appendChild(entryBuilder.id(id).updated(updated).build());
        return this;
    }
    
    public Feed build() {
        return new Feed(url, type, new Document(root));
    }
}
