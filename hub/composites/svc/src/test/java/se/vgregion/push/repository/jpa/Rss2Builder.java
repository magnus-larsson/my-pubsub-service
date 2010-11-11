package se.vgregion.push.repository.jpa;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public abstract class Rss2Builder<T extends Rss2Builder<?>> extends FeedBuilder<FeedBuilder<?>> {

    public Rss2Builder(String rootName) {
        super(rootName, null);
    }
 
    
    public T updated(DateTime updated) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        root.appendChild(createElement("pubDate", null, fmt.print(updated)));
        return (T) this;
    }

}
