package se.vgregion.pubsub;

import java.util.List;

import org.joda.time.DateTime;

public interface Feed {

    List<Field> getFields();
    List<Entry> getEntries();
    
    boolean hasUpdates(DateTime since);
}
