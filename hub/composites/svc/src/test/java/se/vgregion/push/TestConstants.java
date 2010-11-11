package se.vgregion.push;

import java.net.URI;

import org.joda.time.DateTime;

public class TestConstants {
    public static final URI CALLBACK = URI.create("http://example.com/sub11");
    public static final URI TOPIC = URI.create("http://example.com/feed");
    public static final URI TOPIC2 = URI.create("http://example.com/feed2");
    
    public static DateTime UPDATED1 = new DateTime(2010, 3, 1, 0, 0, 0, 0);
    public static DateTime UPDATED2 = new DateTime(2010, 2, 1, 0, 0, 0, 0);
    public static DateTime UPDATED3 = new DateTime(2010, 1, 1, 0, 0, 0, 0);

}
