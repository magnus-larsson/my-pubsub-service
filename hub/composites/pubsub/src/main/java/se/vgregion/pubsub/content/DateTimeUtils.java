package se.vgregion.pubsub.content;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeUtils {

    public static String print(DateTime value) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        return fmt.print(value);
    }
    
    public static DateTime parseDateTime(String value) {
        // Tue, 18 Jan 2011 07:42:14 +0000
        
        DateTimeFormatter parser;
        if(Character.isDigit(value.charAt(0))) {
            // assume ISO
            parser = ISODateTimeFormat.dateTimeParser();
        } else {
            // assume RSS datetime
            parser = new DateTimeFormatterBuilder()
                .appendDayOfWeekShortText()
                .appendLiteral(", ")
                .appendDayOfMonth(1)
                .appendLiteral(" ")
                .appendMonthOfYearShortText()
                .appendLiteral(" ")
                .appendYear(4, 4)
                .appendLiteral(" ")
                .appendHourOfDay(2)
                .appendLiteral(":")
                .appendMinuteOfHour(2)
                .appendLiteral(":")
                .appendSecondOfMinute(2)
                .appendLiteral(" +0000").toFormatter();
        }
        
        parser = parser.withZone(DateTimeZone.UTC).withLocale(Locale.US);
        return parser.parseDateTime(value);
    }

}
