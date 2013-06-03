/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.*;
import se.vgregion.pubsub.repository.TopicRepository;

public class DefaultPubSubEngine implements PubSubEngine {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPubSubEngine.class);

    private TopicRepository topicRepository;
    private SubscriberTimeoutNotifier subscriberTimeoutNotifier = new DefaultSubscriberTimeoutNotifier();
    private PublicationRetryer publicationRetryer;

    private PushJms pushJms;

    private Map<URI, DefaultTopic> topics = new ConcurrentHashMap<URI, DefaultTopic>();

    private List<PubSubEventListener> eventListeners = new ArrayList<PubSubEventListener>();
    private List<SubscriberManager> subscriberManagers = new ArrayList<SubscriberManager>();

    public DefaultPubSubEngine(TopicRepository topicRepository, PublicationRetryer publicationRetryer) {
        this.topicRepository = topicRepository;
        this.publicationRetryer = publicationRetryer;

        Collection<Topic> storedTopics = topicRepository.findAll();

        for (Topic storedTopic : storedTopics) {
            DefaultTopic defaultTopic = (DefaultTopic) storedTopic;

            topics.put(defaultTopic.getUrl(), defaultTopic);
        }
    }

    @Override
    @Transactional
    public DefaultTopic getTopic(URI url) {
        return topics.get(url);
    }

    @Override
    @Transactional
    public synchronized DefaultTopic createTopic(URI url) {
        DefaultTopic topic = new DefaultTopic(url, publicationRetryer);
        topics.put(url, topic);
        topicRepository.persist(topic);
        topicRepository.flush();
        return topic;
    }

    @Override
    @Transactional
    public DefaultTopic getOrCreateTopic(URI url) {
        DefaultTopic topic = getTopic(url);
        if (topic == null) {
            topic = (DefaultTopic) topicRepository.find(url);
            if (topic == null) {
                topic = createTopic(url);
            }
        }
        return topic;
    }

    @Override
    @Transactional
    public void publish(URI url, Feed feed) {
        Topic topic = getOrCreateTopic(url);

        LOG.debug("Publishing directly on topic");
        topic.publish(feed, pushJms);
        LOG.debug("Done publishing directly on topic");
        // now, also notify all SubscriberManagers
        if (!subscriberManagers.isEmpty()) {
            LOG.debug("Publishing to subscriber managers");
            for (SubscriberManager subscriberManager : subscriberManagers) {
                subscriberManager.publishToSubscribers(topic, feed);
            }
            LOG.debug("Done publishing to subscriber managers");
        } else {
            LOG.debug("No subscriber managers registered for publication");
        }
    }

    @Override
    @Transactional
    public void subscribe(Subscriber subscriber) {
        DefaultTopic topic = getOrCreateTopic(subscriber.getTopic());
        topic.addSubscriber(subscriber);

        subscriberTimeoutNotifier.addSubscriber(subscriber);

        for (PubSubEventListener listener : eventListeners) {
            listener.onSubscribe(subscriber);
        }
    }

    @Override
    @Transactional
    public void subscribe(SubscriberManager subscriberManager) {
        subscriberManagers.add(subscriberManager);
    }

    @Override
    @Transactional
    public void unsubscribe(Subscriber subscriber) {
        DefaultTopic topic = getTopic(subscriber.getTopic());

        if (topic != null) {
            topic.removeSubscriber(subscriber);
        }

        subscriberTimeoutNotifier.removeSubscriber(subscriber);

        for (PubSubEventListener listener : eventListeners) {
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

    public PushJms getPushJms() {
        return pushJms;
    }

    public void setPushJms(PushJms pushJms) {
        this.pushJms = pushJms;
    }
}
