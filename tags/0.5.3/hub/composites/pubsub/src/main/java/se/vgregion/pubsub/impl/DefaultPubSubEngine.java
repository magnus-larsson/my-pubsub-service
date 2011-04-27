package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PubSubEventListener;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.SubscriberTimeoutNotifier;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.repository.TopicRepository;

public class DefaultPubSubEngine implements PubSubEngine {

    private TopicRepository topicRepository;
    private SubscriberTimeoutNotifier subscriberTimeoutNotifier = new DefaultSubscriberTimeoutNotifier();
    private PublicationRetryer publicationRetryer;

    private Map<URI, Topic> topics = new ConcurrentHashMap<URI, Topic>();
    
    private List<PubSubEventListener> eventListeners = new ArrayList<PubSubEventListener>();
    
    public DefaultPubSubEngine(TopicRepository topicRepository, PublicationRetryer publicationRetryer) {
        this.topicRepository = topicRepository;
        this.publicationRetryer = publicationRetryer;
        
        Collection<Topic> storedTopics = topicRepository.findAll();
        
        for(Topic storedTopic : storedTopics) {
            DefaultTopic defaultTopic = (DefaultTopic) storedTopic;
            
            defaultTopic.setSubscriberTimeoutNotifier(subscriberTimeoutNotifier);
            topics.put(defaultTopic.getUrl(), defaultTopic);
        }
    }

    @Transactional
    public Topic getTopic(URI url) {
        return topics.get(url);
    }

    @Override
    @Transactional
    public synchronized Topic createTopic(URI url) {
        Topic topic = new DefaultTopic(url, subscriberTimeoutNotifier, publicationRetryer);
        topics.put(url, topic);
        
        topicRepository.persist(topic);
        return topic;
    }

    @Override
    @Transactional
    public Topic getOrCreateTopic(URI url) {
        Topic topic = getTopic(url);
        if(topic == null) {
            topic = createTopic(url);
        }
        return topic;
    }

    @Override
    @Transactional
    public void publish(URI url, Feed feed) {
        Topic topic = getOrCreateTopic(url);
        topic.publish(feed);
    }

    @Override
    @Transactional
    public void subscribe(Subscriber subscriber) {
        Topic topic = getOrCreateTopic(subscriber.getTopic());
        topic.addSubscriber(subscriber);
        
        for(PubSubEventListener listener : eventListeners) {
            listener.onSubscribe(subscriber);
        }
    }

    @Override
    @Transactional
    public void unsubscribe(Subscriber subscriber) {
        Topic topic = getTopic(subscriber.getTopic());
        
        if(topic != null) {
            topic.removeSubscriber(subscriber);
        }

        for(PubSubEventListener listener : eventListeners) {
            listener.onUnsubscribe(subscriber);
        }
    }

    @Override
    public void addPubSubEventListener(PubSubEventListener eventListener) {
        eventListeners.add(eventListener);        
    }

    @Override
    public void removePubSubEventListener(PubSubEventListener eventListener) {
        eventListeners.remove(eventListener);
    }
}
