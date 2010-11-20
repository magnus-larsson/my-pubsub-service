package se.vgregion.pubsub;

import java.net.URI;
import java.util.Collection;

public interface PubSubEngine {

    Topic createTopic(URI url);
    
    Topic getOrCreateTopic(URI url);
    
    Collection<Topic> getTopics();

    Topic getTopic(URI url);
}
