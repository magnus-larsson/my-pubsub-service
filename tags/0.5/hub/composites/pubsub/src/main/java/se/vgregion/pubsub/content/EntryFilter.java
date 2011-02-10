package se.vgregion.pubsub.content;

import se.vgregion.pubsub.Entry;

public interface EntryFilter {

    boolean include(Entry entry);
}
