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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.hibernate.annotations.Cascade;
import org.joda.time.DateTime;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

@Entity
public class Feed extends AbstractEntity<Feed, Long> {

    public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

    private static final Builder PARSER = new Builder();;

    @Id
    @GeneratedValue
    private long id;

    @Column(unique=true)
    private String url;

    @Column
    private String atomId;
    
    @Column
    private ContentType contentType;
    
    @Column
    @Lob
    private String xml;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    
    // TODO remove eager loading
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    // TODO hack since JPA does not support deleting orphans
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("updated DESC")
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
        
        parseEntries(document);
        this.xml = parseXmlWithoutEntries(document).toXML();
        this.atomId = parseValue(document, "id");
        this.updated = FeedHelper.parseDateTime(parseValue(document, "updated")).toDate();
    }
    
    private static Document streamToDocument(InputStream in) throws IOException {
        Builder parser = new Builder();
        try {
            return parser.build(in);
        } catch (ParsingException e) {
            throw new IOException(e);
        }
    }

    private void parseEntries(Document document) {
        // TODO add for RSS
        Element root = document.getRootElement();
        Elements entryElms = root.getChildElements("entry",NS_ATOM);
        for(int i = 0; i<entryElms.size(); i++) {
            Element elm = entryElms.get(i);
            
            entries.add(new Entry(elm));
        }
    }

    private String parseValue(Document document, String topLevelElement) {
        return document.getRootElement().getFirstChildElement(topLevelElement, Feed.NS_ATOM).getValue();
    }
    
    private Document parseXmlWithoutEntries(Document document) {
        Document documentWithOutEntries = new Document(document);

        // TODO add for RSS
        Element root = documentWithOutEntries.getRootElement();
        Elements entryElms = root.getChildElements("entry",NS_ATOM);
        for(int i = 0; i<entryElms.size(); i++) {
            Element elm = entryElms.get(i);
            root.removeChild(elm);
        }
        
        return documentWithOutEntries;
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

    public String getAtomId() {
        return atomId;
    }
    
    public DateTime getUpdated() {
        return new DateTime(updated);
    }

    public List<Entry> getEntries() {
        return entries;
    }
    
    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    /**
     * Replace entry with same entry ID
     */
    public void addOrReplaceEntry(Entry newEntry) {
        Entry existingEntry = findEntryByAtomId(newEntry.getAtomId());
        
        if(existingEntry != null) {
            entries.remove(existingEntry);
        }
        entries.add(newEntry);
    }

    public boolean hasUpdates(DateTime updatedSince) {
        for(Entry entry : entries) {
            if(updatedSince == null || entry.isNewerThan(updatedSince)) {
                return true;
            }
        }
        
        return false;
        
    }
    
    public Document createDocument(DateTime updatedSince) {
        try {
            // TODO cache
            Document document = PARSER.build(new StringReader(xml));
            for(Entry entry : entries) {

                if(updatedSince == null || entry.isNewerThan(updatedSince)) {
                    document.getRootElement().appendChild(entry.toElement().copy());
                }
            }
            
            return document;
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException(e);
        }
        
    }
    
    public Document createDocument() {
        return createDocument(null);
    }
    
    public void merge(Feed other) {
        this.xml = other.xml;
        this.atomId = other.getAtomId();
        
        List<Entry> otherEntries = other.getEntries();

        for(Entry otherEntry : new ArrayList<Entry>(otherEntries)) {
            addOrReplaceEntry(otherEntry);
        }
    }
    
    private Entry findEntryByAtomId(String atomId) {
        for(Entry entry : entries) {
            if(entry.getAtomId().equals(atomId)) {
                return entry;
            }
        }
        return null;
    }


}
