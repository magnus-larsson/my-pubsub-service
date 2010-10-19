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

package se.vgregion.push.types;

import java.io.File;

import nu.xom.Builder;
import nu.xom.Document;

public class SomeFeeds {

    public static final Document ATOM1 = loadXml("atom1.xml");
    public static final Document ATOM2 = loadXml("atom2.xml");

    private static Document loadXml(String fileName) {
        Builder parser = new Builder();
        try {
            return parser.build(new File("src/test/resources/" + fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

