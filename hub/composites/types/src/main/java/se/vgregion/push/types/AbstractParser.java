package se.vgregion.push.types;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public abstract class AbstractParser {

    public static AbstractParser create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomParser();
        } else {
            return new Rss2Parser();
        }
    }

    
    private static final Builder PARSER = new Builder();
    
    public Feed parse(URI url, InputStream in) throws ParsingException, IOException {
        return parse(url, PARSER.build(in));
    }
    
    public abstract Feed parse(URI url, Document document);
    
    protected DateTime parseDateTime(String value) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.UTC);
        return fmt.parseDateTime(value);
    }
}
