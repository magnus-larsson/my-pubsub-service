package se.vgregion.pubsub.content;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeUtils {

    public static String print(DateTime value) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        return fmt.print(value);
    }
    
    public static DateTime parseDateTime(String value) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.UTC);
        return fmt.parseDateTime(value);
    }

}
