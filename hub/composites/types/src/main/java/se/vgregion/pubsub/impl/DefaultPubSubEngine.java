package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.Collection;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.repository.TopicRepository;

public class DefaultPubSubEngine implements PubSubEngine {

    private TopicRepository topicRepository;
    private SubscriberTimeoutNotifier subscriberTimeoutNotifier = new SubscriberTimeoutNotifier();
    
    public DefaultPubSubEngine(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public Collection<Topic> getTopics() {
        return topicRepository.findAll();
    }

    public Topic getTopic(URI url) {
        return topicRepository.findByUrl(url);
    }

    @Override
    public Topic createTopic(URI url) {
        Topic topic = new DefaultTopic(url, subscriberTimeoutNotifier);
        topicRepository.persist(topic);
        return topic;
    }

    @Override
    public Topic getOrCreateTopic(URI url) {
        Topic topic = getTopic(url);
        if(topic == null) {
            topic = createTopic(url);
        }
        return topic;
    }
}
