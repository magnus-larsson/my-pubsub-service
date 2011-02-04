package se.vgregion.pubsub.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;

public abstract class AbstractParser {

    public static AbstractParser create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomParser();
        } else if(type == ContentType.RSS) {
            return new Rss2Parser();
        } else {
            throw new IllegalArgumentException("Can not create parser for content type: " + type);
        }
    }

    
    private static final Builder PARSER = new Builder();
    
    public Feed parse(InputStream in, ContentType contentType) throws ParsingException, IOException {
        return parse(PARSER.build(in), contentType);
    }

    public Feed parse(String content, ContentType contentType) throws ParsingException, IOException {
        return parse(PARSER.build(new StringReader(content)), contentType);
    }
    
    public abstract Feed parse(Document document, ContentType contentType);
    
}
