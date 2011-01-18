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
        } else {
            return new Rss2Parser();
        }
    }

    
    private static final Builder PARSER = new Builder();
    
    public Feed parse(InputStream in) throws ParsingException, IOException {
        return parse(PARSER.build(in));
    }

    public Feed parse(String content) throws ParsingException, IOException {
        return parse(PARSER.build(new StringReader(content)));
    }
    
    public abstract Feed parse(Document document);
    
}
