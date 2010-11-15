package se.vgregion.push;

import java.net.URI;

import org.joda.time.DateTime;

import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Entry.EntryBuilder;
import se.vgregion.push.types.Feed.FeedBuilder;

public class UnitTestConstants {
    public static final URI CALLBACK = URI.create("http://example.com/sub11");
    public static final URI TOPIC = URI.create("http://example.com/feed");
    public static final URI TOPIC2 = URI.create("http://example.com/feed2");
    
    public static DateTime UPDATED1 = new DateTime(2010, 3, 1, 0, 0, 0, 0);
    public static DateTime UPDATED2 = new DateTime(2010, 2, 1, 0, 0, 0, 0);
    public static DateTime UPDATED3 = new DateTime(2010, 1, 1, 0, 0, 0, 0);

    public static Feed atom1() {
        return new FeedBuilder(UnitTestConstants.TOPIC, ContentType.ATOM)
            .id("f1").updated(UnitTestConstants.UPDATED1)
            .entry(new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).build())
            .entry(new EntryBuilder().id("e2").updated(UnitTestConstants.UPDATED1).build())
            .build();
    }

    public static Feed atom2() {
        return new FeedBuilder(UnitTestConstants.TOPIC2, ContentType.ATOM)
        .id("f2").updated(UnitTestConstants.UPDATED1)
        .entry(new EntryBuilder().id("e3").updated(UnitTestConstants.UPDATED1).build())
        .entry(new EntryBuilder().id("e4").updated(UnitTestConstants.UPDATED1).build())
        .build();
    }
}
