package se.vgregion.pubsub;

import java.net.URI;

public interface PubSubEngine {

    Topic createTopic(URI url);
    
    Topic getOrCreateTopic(URI url);
    
    Topic getTopic(URI url);

    void publish(URI url, Feed feed);
}
