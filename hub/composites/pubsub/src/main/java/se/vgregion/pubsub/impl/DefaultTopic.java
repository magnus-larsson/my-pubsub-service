package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;

public class DefaultTopic extends AbstractEntity<Topic, Long> implements Topic {

    private Long id;
    private String url;
    private Feed feed;
    private List<Subscriber> subscribers = new ArrayList<Subscriber>();
    
    private FeedMerger feedMerger;
    private SubscriberTimeoutNotifier subscriberTimoutNotifier;
    
    // For JPA
    protected DefaultTopic() {
    }
    
    public DefaultTopic(URI url, SubscriberTimeoutNotifier subscriberTimoutNotifier) {
        this.url = url.toString();
        this.subscriberTimoutNotifier = subscriberTimoutNotifier;
    }
    
    @Override
    public Long getId() {
        return id;
    }

    
    @Override
    public URI getUrl() {
        return URI.create(url);
    }

    @Override
    public void publish(List<Entry> entries) {
        this.feed = feedMerger.merge(this.feed, feed);
        
        // if all publications success, purge until now
        DateTime lastUpdatedSubscriber = new DateTime();
        for(Subscriber subscriber : subscribers) {
            try {
                subscriber.publish(this.getFeed());
            } catch (PublicationFailedException e) {
                lastUpdatedSubscriber = subscriber.getLastUpdated();
            }
        }
        
        // TODO purge old entries based on lastUpdatedSubscriber
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        Assert.notNull(subscriber);
        
        removeSubscriber(subscriber);
        
        subscriberTimoutNotifier.addSubscriber(subscriber);
        subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public Feed getFeed() {
        return feed;
    }


}
