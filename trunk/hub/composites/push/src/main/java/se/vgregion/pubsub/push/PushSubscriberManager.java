package se.vgregion.pubsub.push;

import java.util.Collection;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.Topic;

public class PushSubscriberManager {

    private PubSubEngine pubSubEngine;
    private PushSubscriberRepository subscriptionRepository;
    
    public void start() {
        Collection<PushSubscriber> subscribers = subscriptionRepository.findAll();
        
        for(PushSubscriber subscriber : subscribers) {
            Topic topic = pubSubEngine.getOrCreateTopic(subscriber.getTopic());
            topic.addSubscriber(subscriber);
        }
    }
}
