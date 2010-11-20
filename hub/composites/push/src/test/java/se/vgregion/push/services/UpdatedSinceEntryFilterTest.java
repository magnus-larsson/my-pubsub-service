package se.vgregion.push.services;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.push.UnitTestConstants;
import se.vgregion.push.types.Entry;
import se.vgregion.push.types.Entry.EntryBuilder;

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
