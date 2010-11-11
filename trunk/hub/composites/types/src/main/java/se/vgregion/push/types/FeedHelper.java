package se.vgregion.push.types;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class FeedHelper {

    public static DateTime parseDateTime(String value) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
        return fmt.parseDateTime(value);
    }
}
