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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import nu.xom.Document;
import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

@Entity
public class Entry extends AbstractEntity<Long> {

    public static class EntryBuilder {
        
        private Entry entry = new Entry();
        
        public EntryBuilder id(String id) {
            entry.entryId = id;
            return this;
        }

        public EntryBuilder updated(DateTime updated) {
            entry.updated = updated.getMillis();
            return this;
        }
        
        public EntryBuilder custom(Element elm) {
            // TODO ugly hack, fix
            entry.xml += new Document((Element) elm.copy()).toXML().replaceFirst("<.+>", "");
            return this;
        }
        
        public Entry build() {
            return entry;
        }
    }
    
    @Id
    @GeneratedValue
    private long id;
    
    @Column(nullable=false)
    private long updated;

    @Column(nullable=false)
    private String entryId;
    
    @Column(nullable=true)
    @Lob
    private String xml;

    /* Make JPA happy */
    protected Entry() {
        
    }
    
    @Override
    public Long getId() {
        return id;
    }

    public String getEntryId() {
        return entryId;
    }

    public DateTime getUpdated() {
        return new DateTime(updated, DateTimeZone.UTC);
    }
    
    public boolean isNewerThan(DateTime other) {
        return this.getUpdated().isAfter(other);
    }
    
    public List<Element> getCustom() {
        return XmlUtil.stringToXml(xml);
    }
}
