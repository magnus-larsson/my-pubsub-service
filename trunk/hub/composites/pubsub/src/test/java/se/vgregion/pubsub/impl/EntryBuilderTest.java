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

package se.vgregion.pubsub.impl;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;


public class EntryBuilderTest {

    @Test
    public void entryId() throws Exception {
        Entry entry = new EntryBuilder().id("e1").updated(UnitTestConstants.UPDATED1).field("ns", "n", "v").build();
        
        Assert.assertEquals("e1", entry.getEntryId());
        Assert.assertEquals(UnitTestConstants.UPDATED1, entry.getUpdated());

        Assert.assertEquals(1, entry.getFields().size());
        Assert.assertEquals("ns", entry.getFields().get(0).getNamespace());
        Assert.assertEquals("n", entry.getFields().get(0).getName());
        Assert.assertEquals("v", entry.getFields().get(0).getValue());
    }
}
