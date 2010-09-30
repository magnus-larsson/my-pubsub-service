package se.vgregion.push.types;

import java.net.URI;

import org.junit.Test;


public class FeedTest {

    @Test
    public void test() throws Exception {
        Feed feed = new Feed(URI.create("http://example.com"), ContentType.ATOM, SomeFeeds.ATOM_DOCUMENT);
        
        System.out.println(feed.getDocument().toXML());
        
    }
}
