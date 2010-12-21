package se.vgregion.pubsub;

import java.net.URI;

public interface PubSubEngine {

    Topic createTopic(URI url);
    
    Topic getOrCreateTopic(URI url);
    
    Topic getTopic(URI url);
    
    void subscribe(Subscriber subscriber);

    void unsubscribe(Subscriber subscriber);
    
    void publish(URI url, Feed feed);
    
    void addPubSubEventListener(PubSubEventListener eventListener);

    void removePubSubEventListener(PubSubEventListener eventListener);
}
