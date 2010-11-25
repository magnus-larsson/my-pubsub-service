package se.vgregion.pubsub;

import java.net.URI;

import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Topic extends Entity<URI> {

    URI getUrl();
    
    Feed getFeed();
    
    void publish(Feed feed);
    
    void addSubscriber(Subscriber subscriber);

    void removeSubscriber(Subscriber subscriber);
}
