package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.repository.FeedRepository;

@Entity
public class DefaultTopic extends AbstractEntity<Long> implements Topic {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultTopic.class);

    
    @Id
    @GeneratedValue
    private Long id;
    
    @Basic(optional=false)
    private String url;
    
    // TODO JPA map
    @Transient
    private Feed feed;
    
    @Transient
    private List<Subscriber> subscribers = new ArrayList<Subscriber>();
    
    @Transient
    private FeedMerger feedMerger = new FeedMerger() {
        @Override
        public Feed merge(Feed oldFeed, Feed newFeed) {
            return newFeed;
        }
    };;;
    
    @Transient
    private SubscriberTimeoutNotifier subscriberTimeoutNotifier;

    @Transient
    private FeedRepository feedRepository;

    
    // For JPA
    protected DefaultTopic() {
    }
    
    public DefaultTopic(URI url, FeedRepository feedRepository, SubscriberTimeoutNotifier subscriberTimoutNotifier) {
        Assert.notNull(url);
        Assert.notNull(feedRepository);
        Assert.notNull(subscriberTimoutNotifier);
        
        this.url = url.toString();
        this.feedRepository = feedRepository;
        this.subscriberTimeoutNotifier = subscriberTimoutNotifier;
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
    public void publish(Feed publishedFeed) {
        LOG.info("Publishing on topic {}", url);
        
        this.feed = feedMerger.merge(this.feed, publishedFeed);
        // if all publications success, purge until now
        DateTime lastUpdatedSubscriber = new DateTime();
        for(Subscriber subscriber : subscribers) {
            try {
                LOG.info("Publishing to {}", subscriber);
                subscriber.publish(this.getFeed());
            } catch (PublicationFailedException e) {
                LOG.warn("Subscriber failed: {}", e.getMessage());
                lastUpdatedSubscriber = subscriber.getLastUpdated();
            }
        }
        
        // TODO purge old entries based on lastUpdatedSubscriber
        
        feedRepository.store(this.feed);
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        Assert.notNull(subscriber);
        
        removeSubscriber(subscriber);
        
        subscriberTimeoutNotifier.addSubscriber(subscriber);
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

    public SubscriberTimeoutNotifier getSubscriberTimeoutNotifier() {
        return subscriberTimeoutNotifier;
    }

    public void setSubscriberTimeoutNotifier(SubscriberTimeoutNotifier subscriberTimoutNotifier) {
        this.subscriberTimeoutNotifier = subscriberTimoutNotifier;
    }

    public FeedRepository getFeedRepository() {
        return feedRepository;
    }

    public void setFeedRepository(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

}
