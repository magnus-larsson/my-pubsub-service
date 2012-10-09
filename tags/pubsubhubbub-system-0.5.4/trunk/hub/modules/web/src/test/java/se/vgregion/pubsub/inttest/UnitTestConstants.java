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

package se.vgregion.pubsub.inttest;


import java.net.URI;

import org.joda.time.DateTime;

public class UnitTestConstants {
    public static final URI CALLBACK = URI.create("http://example.com/sub11");
    public static final URI TOPIC = URI.create("http://example.com/feed");
    public static final URI TOPIC2 = URI.create("http://example.com/feed2");
    
    public static DateTime UPDATED1 = new DateTime(2010, 3, 1, 0, 0, 0, 0);
    public static DateTime UPDATED2 = new DateTime(2010, 2, 1, 0, 0, 0, 0);
    public static DateTime UPDATED3 = new DateTime(2010, 1, 1, 0, 0, 0, 0);
    
    public static DateTime FUTURE = new DateTime(2050, 1, 1, 0, 0, 0, 0);

}
