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

import org.joda.time.DateTime;

import se.vgregion.dao.domain.patterns.entity.Entity;

/**
 * A complete feed, typically Atom or RSS
 *
 */
public interface Feed extends Entity<String> {

    ContentType getContentType();
    
    String getFeedId();
    DateTime getUpdated();
    
    List<Field> getFields();
    List<Entry> getEntries();
    
    boolean hasUpdates(DateTime since);
}
