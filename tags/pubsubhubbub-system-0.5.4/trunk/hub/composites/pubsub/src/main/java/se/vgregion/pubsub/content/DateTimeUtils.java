/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.content;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeUtils {

	/**
	 * Formats a date time to ISO 8601 format
	 * @param value
	 * @return
	 */
    public static String print(DateTime value) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        return fmt.print(value);
    }
    
    /**
     * Parses common date time formats in feeds
     * @param value
     * @return
     */
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
