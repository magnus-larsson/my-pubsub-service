package se.vgregion.push.types;

import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;

public class SomeFeeds {

    public final static String ATOM = "<?xml version=\"1.0\"?>\n"
            + "<feed xmlns=\"http://www.w3.org/2005/Atom\">" + "<title>1177.se</title>"
            + "<link href=\"http://example.org/\" />" + "<updated>2010-09-14T18:30:02Z</updated>"
            + "<id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>" + "    <entry>"
            + "<title>1177.se - Råd om vård på webb och telefon</title>" + "<link href=\"http://1177.se/\" />"
            + "<id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>" + "<updated>2010-09-15T18:30:02Z</updated>"
            + "<content type=\"xhtml\">" + "<div xmlns=\"http://www.w3.org/1999/xhtml\">...</div>" + "</content>"
            + "</entry>" + "<entry>" + "<title>1177.se - Råd om vård på webb och telefon</title>"
            + "<link href=\"http://1177.se/\" />" + "<id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>"
            + "<updated>2010-09-14T18:30:02Z</updated>" + "<content type=\"xhtml\">"
            + "<div xmlns=\"http://www.w3.org/1999/xhtml\">...</div>" + "</content>" + "</entry>" + "</feed>\n";
    
    public static final Document ATOM_DOCUMENT;
    static {
        Builder parser = new Builder();
        try {
            ATOM_DOCUMENT = parser.build(new StringReader(ATOM));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
