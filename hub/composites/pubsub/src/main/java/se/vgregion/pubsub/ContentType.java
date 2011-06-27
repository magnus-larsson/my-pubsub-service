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

import java.util.List;

/**
 * Representation of a content type
 *
 */
public class ContentType {
    public static final ContentType ATOM = new ContentType("application/atom+xml");
    public static final ContentType RSS = new ContentType("application/rss+xml");
    public static final ContentType JSON = new ContentType("application/json");
    
    private String value;
    
    /**
     * Create a content type using it's string representation, e.g. application/json
     * @param value
     */
    public ContentType(String value) {
        this.value = value;
    }

    /**
     * Parse a content type and return constants for common content types
     * @param value
     * @return
     */
    public static ContentType fromValue(String value) {
        if("application/atom+xml".equals(value)) return ATOM;
        else if("application/rss+xml".equals(value)) return RSS;
        else return new ContentType(value);
    }
    
    public static ContentType sniff(List<String> contentTypeHeaders, String content) {
        ContentType contentType = null;
        if (!contentTypeHeaders.isEmpty()) {
            contentType = ContentType.fromValue(contentTypeHeaders.get(0));            
        }
        if(contentType != ContentType.ATOM && contentType != ContentType.RSS) {
        	// unknown content type, try sniffing
        	if(content.contains("<rss")) {
        		contentType = ContentType.RSS;
        	} else if(content.contains("<feed")) {
        		contentType = ContentType.ATOM;
        	} else {
        		throw new RuntimeException("Unknown content type: " + contentType);
        	}
        }
        
        return contentType;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    
}
