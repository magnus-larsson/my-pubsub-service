package se.vgregion.pubsub;

import java.net.URI;
import java.util.List;

import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Topic extends Entity<Topic, Long> {

    URI getUrl();
    
    Feed getFeed();
    
    void publish(List<Entry> entries);
    
    void addSubscriber(Subscriber subscriber);

    void removeSubscriber(Subscriber subscriber);
}
