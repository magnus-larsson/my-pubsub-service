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

import java.io.StringReader;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import se.vgregion.portal.core.domain.patterns.entity.AbstractEntity;

@Entity
public class Entry extends AbstractEntity<Entry, Long> {

    @Id
    @GeneratedValue
    private long id;
    
    @Column(nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    
    @Column(nullable=false)
    @Lob
    private String xml;

    /* Make JPA happy */
    protected Entry() {
        
    }
    
    public Entry(Element elm) {
        this.xml = elm.toXML();
        
        // TODO add exception handling
        String updateString = elm.getChildElements("updated", Feed.NS_ATOM).get(0).getValue();
        
        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
        updated = fmt.parseDateTime(updateString).toDate();
    }

    @Override
    public Long getId() {
        return id;
    }

    public Date getDate() {
        return (Date) updated.clone();
    }
    
    public boolean isNewerThan(Date other) {
        return this.updated.after(other);
    }

    public Element getElement() {
        try {
            Builder parser = new Builder();
            Document doc = parser.build(new StringReader(xml));
            return doc.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
