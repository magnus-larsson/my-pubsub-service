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

package se.vgregion.pubsub.push.impl;

import org.joda.time.DateTime;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.content.EntryFilter;

/**
 * Filters {@link Entry}s based update time
 *
 */
public class UpdatedSinceEntryFilter implements EntryFilter {

    private DateTime updatedSince;
    
    public UpdatedSinceEntryFilter(DateTime updatedSince) {
        this.updatedSince = updatedSince;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Entry entry) {
        return updatedSince == null || entry.isNewerThan(updatedSince);
    }

}
