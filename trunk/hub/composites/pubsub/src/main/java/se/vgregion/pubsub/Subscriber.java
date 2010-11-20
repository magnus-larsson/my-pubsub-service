package se.vgregion.pubsub;

import java.net.URI;

import org.joda.time.DateTime;

public interface Subscriber {

    DateTime getLastUpdated();
    
    DateTime getTimeout();
    
    URI getTopic();
    
    void publish(Feed feed) throws PublicationFailedException;

    void timedOut();
}
