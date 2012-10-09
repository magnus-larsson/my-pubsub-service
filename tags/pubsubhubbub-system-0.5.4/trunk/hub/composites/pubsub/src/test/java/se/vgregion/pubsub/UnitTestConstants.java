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

package se.vgregion.pubsub;

import java.io.File;
import java.net.URI;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class UnitTestConstants {
    public static final URI CALLBACK = URI.create("http://example.com/sub11");
    public static final URI TOPIC = URI.create("http://example.com/feed");
    public static final URI TOPIC2 = URI.create("http://example.com/feed2");
    
    public static String UPDATED1_STR = "2010-03-01T00:00:00.000Z";
    public static String UPDATED2_STR = "2010-02-01T00:00:00.000Z";
    public static DateTime UPDATED1 = new DateTime(2010, 3, 1, 0, 0, 0, 0, DateTimeZone.UTC);
    public static DateTime UPDATED2 = new DateTime(2010, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC);
    public static DateTime UPDATED3 = new DateTime(2010, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

    public static final Element ATOM_TITLE = new Element("title", Namespaces.ATOM);
    public static final Element ATOM_TITLE2 = new Element("title", Namespaces.ATOM);
    public static final Element RSS2_TITLE = new Element("title");
    public static final Element RSS2_TITLE2 = new Element("title");
    static {
        ATOM_TITLE.appendChild("foobar");
        ATOM_TITLE2.appendChild("baz");
        RSS2_TITLE.appendChild("foobar");
        RSS2_TITLE2.appendChild("baz");
    }

    public static final Document ATOM1 = UnitTestConstants.loadXml("atom1.xml");
    public static final Document ATOM2 = UnitTestConstants.loadXml("atom2.xml");
    public static final Document RSS1 = UnitTestConstants.loadXml("rss1.xml");
    static Document loadXml(String fileName) {
        Builder parser = new Builder();
        try {
            return parser.build(new File("src/test/resources/" + fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
