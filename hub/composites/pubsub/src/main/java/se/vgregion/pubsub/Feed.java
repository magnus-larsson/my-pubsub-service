package se.vgregion.pubsub;

import java.util.List;

import org.joda.time.DateTime;

import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Feed extends Entity<String> {

    String getFeedId();
    DateTime getUpdated();
    
    List<Field> getFields();
    List<Entry> getEntries();
    
    boolean hasUpdates(DateTime since);
}
