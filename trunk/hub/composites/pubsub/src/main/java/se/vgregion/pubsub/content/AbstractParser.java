package se.vgregion.pubsub.content;

import java.io.IOException;
import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;

public abstract class AbstractParser {

    public static AbstractParser create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomParser();
        } else {
            return new Rss2Parser();
        }
    }

    
    private static final Builder PARSER = new Builder();
    
    public Feed parse(InputStream in) throws ParsingException, IOException {
        return parse(PARSER.build(in));
    }
    
    public abstract Feed parse(Document document);
    
}
