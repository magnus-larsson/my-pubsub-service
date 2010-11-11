package se.vgregion.push.repository.jpa;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import se.vgregion.push.types.Feed;

public abstract class AtomBuilder<T extends AtomBuilder<?>> extends FeedBuilder<FeedBuilder<?>> {

    public AtomBuilder(String rootName) {
        super(rootName, Feed.NS_ATOM);
    }
    
    public T id(String id) {
        root.appendChild(createElement("id", Feed.NS_ATOM, id));
        return (T) this;
    }

    public T updated(DateTime updated) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        root.appendChild(createElement("updated", Feed.NS_ATOM, fmt.print(updated)));
        return (T) this;
    }
}
