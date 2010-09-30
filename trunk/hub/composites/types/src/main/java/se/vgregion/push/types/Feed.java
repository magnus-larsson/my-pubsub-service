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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import se.vgregion.portal.core.domain.patterns.entity.AbstractEntity;

@Entity
public class Feed extends AbstractEntity<Feed, Long> {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String url;
    
    @Column
    private ContentType contentType;
    
    @Column
    @Lob
    private String xml;
    
    // TODO remove eager loading
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private List<Entry> entries = new ArrayList<Entry>();
    
    /* Make JPA happy */
    protected Feed() {
    }
    
    public Feed(URI url, ContentType contentType, InputStream in) throws IOException {
        this(url, contentType, streamToDocument(in));
    }

    public Feed(URI url, ContentType contentType, Document document) {
        this.url = url.toString();
        this.contentType = contentType;
        
        extractXml(document);
    }
    
    private static Document streamToDocument(InputStream in) throws IOException {
        Builder parser = new Builder();
        try {
            return parser.build(in);
        } catch (ParsingException e) {
            throw new IOException(e);
        }
    }

    private void extractXml(Document document) {
        Document documentWithOutEntries = new Document(document);

        // TODO add for RSS
        Element root = documentWithOutEntries.getRootElement();
        Elements entryElms = root.getChildElements("entry","http://www.w3.org/2005/Atom");
        for(int i = 0; i<entryElms.size(); i++) {
            Element elm = entryElms.get(i);
            root.removeChild(elm);
            
            entries.add(new Entry(elm.toXML()));
        }
        
        xml = documentWithOutEntries.toXML();
    }

    @Override
    public Long getId() {
        return id;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public URI getUrl() {
        return URI.create(url);
    }
    
    public Document getDocument() {

        try {
            Builder parser = new Builder();
            // TODO cache
            Document document = parser.build(new StringReader(xml));
            for(Entry entry : entries) {
                document.getRootElement().appendChild(entry.getElement().copy());
            }
            
            return document;
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

}
