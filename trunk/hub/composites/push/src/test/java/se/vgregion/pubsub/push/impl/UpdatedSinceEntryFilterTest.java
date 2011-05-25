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

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.push.UnitTestConstants;

public class UpdatedSinceEntryFilterTest {

    @Test
    public void include() {
        UpdatedSinceEntryFilter filter = new UpdatedSinceEntryFilter(UnitTestConstants.UPDATED2);
        Entry after = new EntryBuilder().updated(UnitTestConstants.UPDATED1).build(); 
        Entry sameTime = new EntryBuilder().updated(UnitTestConstants.UPDATED2).build(); 
        Entry before = new EntryBuilder().updated(UnitTestConstants.UPDATED3).build(); 
        
        Assert.assertTrue(filter.include(after));
        Assert.assertFalse(filter.include(sameTime));
        Assert.assertFalse(filter.include(before));
    }

    @Test
    public void includeWithNull() {
        UpdatedSinceEntryFilter filter = new UpdatedSinceEntryFilter(null);
        Entry after = new EntryBuilder().updated(UnitTestConstants.UPDATED1).build(); 
        
        Assert.assertTrue(filter.include(after));
    }
}
