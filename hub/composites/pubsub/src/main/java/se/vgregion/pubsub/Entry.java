package se.vgregion.pubsub;

import java.util.List;

import org.joda.time.DateTime;

import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Entry extends Entity<String> {

    String getEntryId();
    DateTime getUpdated();
    
    List<Field> getFields();
    boolean isNewerThan(DateTime since);
}
