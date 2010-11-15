package se.vgregion.push.services;

import org.joda.time.DateTime;

import se.vgregion.push.types.Entry;
import se.vgregion.push.types.EntryFilter;

public class UpdatedSinceEntryFilter implements EntryFilter {

    private DateTime updatedSince;
    
    public UpdatedSinceEntryFilter(DateTime updatedSince) {
        this.updatedSince = updatedSince;
    }

    @Override
    public boolean include(Entry entry) {
        return entry.isNewerThan(updatedSince);
    }

}
