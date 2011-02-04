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

public class ContentType {
    public static final ContentType ATOM = new ContentType("application/atom+xml");
    public static final ContentType RSS = new ContentType("application/rss+xml");
    public static final ContentType JSON = new ContentType("application/json");
    
    private String value;
    
    public ContentType(String value) {
        this.value = value;
    }

    public static ContentType fromValue(String value) {
        if("application/atom+xml".equals(value)) return ATOM;
        else if("application/rss+xml".equals(value)) return RSS;
        else return new ContentType(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    
}
