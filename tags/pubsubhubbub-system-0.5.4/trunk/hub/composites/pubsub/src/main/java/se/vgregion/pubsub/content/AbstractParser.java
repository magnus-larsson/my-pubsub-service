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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;

public abstract class AbstractParser {

    public static AbstractParser create(ContentType type) {
        if(type == ContentType.ATOM) {
            return new AtomParser();
        } else if(type == ContentType.RSS) {
            return new Rss2Parser();
        } else {
            throw new IllegalArgumentException("Can not create parser for content type: " + type);
        }
    }

    
    private static final Builder PARSER = new Builder();
    
    public Feed parse(InputStream in, ContentType contentType) throws ParsingException, IOException {
        return parse(PARSER.build(in), contentType);
    }

    public Feed parse(String content, ContentType contentType) throws ParsingException, IOException {
        return parse(PARSER.build(new StringReader(content)), contentType);
    }
    
    public abstract Feed parse(Document document, ContentType contentType);
    
}
