package se.vgregion.push.repository.jpa;

import java.net.URI;

import nu.xom.Document;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;

public class Rss2FeedBuilder extends Rss2Builder<Rss2FeedBuilder> {

    private URI url;
    private ContentType type = ContentType.RSS;
    
    public Rss2FeedBuilder(URI url) {
        super("rss");
        this.url= url;
    }
    
    public Rss2FeedBuilder entry(String id, DateTime updated) {
        AtomEntryBuilder entryBuilder = new AtomEntryBuilder();
        root.appendChild(entryBuilder.id(id).updated(updated).build());
        return this;
    }
    
    public Feed build() {
        return new Feed(url, type, new Document(root));
    }
    
    public Rss2FeedBuilder id(String id) {
        // no ID element on feed element
        return this;
    }


}
