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

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;



public class DateTimeUtilsTest {

    
    @Test
    public void parseRss2Date() {
        String value = "Tue, 18 Jan 2011 07:42:14 +0000";
        
        DateTime time = DateTimeUtils.parseDateTime(value);
        Assert.assertEquals(2011, time.getYear());
        Assert.assertEquals(1, time.getMonthOfYear());
        Assert.assertEquals(18, time.getDayOfMonth());
        Assert.assertEquals(7, time.getHourOfDay());
        Assert.assertEquals(42, time.getMinuteOfHour());
        Assert.assertEquals(14, time.getSecondOfMinute());
    }

    @Test
    public void parseIsoDate() {
        String value = "2011-01-18T07:42:14+0000";
        
        DateTime time = DateTimeUtils.parseDateTime(value);
        Assert.assertEquals(2011, time.getYear());
        Assert.assertEquals(1, time.getMonthOfYear());
        Assert.assertEquals(18, time.getDayOfMonth());
        Assert.assertEquals(7, time.getHourOfDay());
        Assert.assertEquals(42, time.getMinuteOfHour());
        Assert.assertEquals(14, time.getSecondOfMinute());
    }


    @Test
    public void parseUtcDate() {
        String value = "2013-06-26T09:13:00Z";

        DateTime time = DateTimeUtils.parseDateTime(value);
        Assert.assertEquals(2013, time.getYear());
        Assert.assertEquals(6, time.getMonthOfYear());
        Assert.assertEquals(26, time.getDayOfMonth());
        Assert.assertEquals(9, time.getHourOfDay());
        Assert.assertEquals(13, time.getMinuteOfHour());
        Assert.assertEquals(0, time.getSecondOfMinute());
    }

}
