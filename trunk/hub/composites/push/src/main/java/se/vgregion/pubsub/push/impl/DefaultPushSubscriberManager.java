package se.vgregion.pubsub.push.impl;

import java.net.URI;
import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class DefaultPushSubscriberManager implements PushSubscriberManager {

    private PubSubEngine pubSubEngine;
    private PushSubscriberRepository subscriptionRepository;
    
    public DefaultPushSubscriberManager(PubSubEngine pubSubEngine, PushSubscriberRepository subscriptionRepository) {
        this.pubSubEngine = pubSubEngine;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public void loadSubscribers() {
        Collection<PushSubscriber> subscribers = subscriptionRepository.findAll();
        
        for(PushSubscriber subscriber : subscribers) {
            Topic topic = pubSubEngine.getOrCreateTopic(subscriber.getTopic());
            topic.addSubscriber(subscriber);
        }
    }

    @Override
    @Transactional
    public void subscribe(PushSubscriber subscriber) {
        Topic topic = pubSubEngine.getOrCreateTopic(subscriber.getTopic());
        
        topic.addSubscriber(subscriber);
        
        subscriptionRepository.store(subscriber);
    }

    @Override
    @Transactional
    public void unsubscribe(URI url, PushSubscriber subscriber) {
        // TODO Auto-generated method stub
        
    }
    
    
}
