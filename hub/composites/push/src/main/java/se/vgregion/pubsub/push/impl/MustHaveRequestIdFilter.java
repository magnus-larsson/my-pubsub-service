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

import static se.vgregion.pubsub.impl.DefaultEntry.getValueFromEntry;

/**
 * Filters {@link se.vgregion.pubsub.Entry}s based update time ant if the requestId is empty or not.
 *
 */
public class MustHaveRequestIdFilter extends UpdatedSinceEntryFilter {

    public MustHaveRequestIdFilter(DateTime updatedSince) {
        super(updatedSince);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Entry entry) {
        return super.include(entry) && !isEmpty(getValueFromEntry(entry, "requestId"));
    }

    private boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        if ("".equals(s.trim())) {
            return true;
        }
        return false;
    }

}
