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
