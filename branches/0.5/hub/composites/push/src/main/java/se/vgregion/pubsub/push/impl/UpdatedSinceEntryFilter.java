package se.vgregion.pubsub.push.impl;

import org.joda.time.DateTime;

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.content.EntryFilter;


public class UpdatedSinceEntryFilter implements EntryFilter {

    private DateTime updatedSince;
    
    public UpdatedSinceEntryFilter(DateTime updatedSince) {
        this.updatedSince = updatedSince;
    }

    @Override
    public boolean include(Entry entry) {
        return updatedSince == null || entry.isNewerThan(updatedSince);
    }

}
