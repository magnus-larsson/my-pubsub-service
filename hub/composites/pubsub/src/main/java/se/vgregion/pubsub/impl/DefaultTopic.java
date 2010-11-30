package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.FeedMerger;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.SubscriberTimeoutNotifier;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.repository.FeedRepository;

@Entity
@Table(name="TOPICS")
public class DefaultTopic extends AbstractEntity<URI> implements Topic {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultTopic.class);

    
    @Id
    @GeneratedValue
    @SuppressWarnings("unused") // only used by JPA
    private Long pk;
    
    @Column(nullable=false, unique=true)
    private String url;
    
    @OneToOne(targetEntity=DefaultFeed.class, cascade=CascadeType.ALL)
    private Feed feed;
    
    @Transient
    private List<Subscriber> subscribers = new ArrayList<Subscriber>();
    
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
    public URI getId() {
        return getUrl();
    }

    
    @Override
    public URI getUrl() {
        return URI.create(url);
    }

    @Override
    public synchronized void publish(Feed publishedFeed) {
        LOG.info("Publishing on topic {}", url);
        if(this.feed != null) {
            this.feed.merge(publishedFeed);
        } else {
            this.feed = publishedFeed;
        }
        
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

        //feedRepository.store(this.feed);
        if (feed.getId() == null || feedRepository.find(feed.getId()) == null) {
            System.out.println("persist");
            feedRepository.persist(feed);
        } else {
            System.out.println("merge");
            feedRepository.merge(feed);
        }

    }

    @Override
    public synchronized void addSubscriber(Subscriber subscriber) {
        Assert.notNull(subscriber);
        
        removeSubscriber(subscriber);
        
        subscriberTimeoutNotifier.addSubscriber(subscriber);
        subscribers.add(subscriber);
    }

    @Override
    public synchronized void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public Feed getFeed() {
        return feed;
    }

    protected void setSubscriberTimeoutNotifier(SubscriberTimeoutNotifier subscriberTimoutNotifier) {
        this.subscriberTimeoutNotifier = subscriberTimoutNotifier;
    }

    protected void setFeedRepository(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

}
