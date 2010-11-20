package se.vgregion.pubsub.push;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import nu.xom.Document;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.content.AbstractSerializer;
import se.vgregion.push.services.FailedSubscriberVerificationException;
import se.vgregion.push.services.HttpUtil;

public class DefaultPushSubscriber extends AbstractEntity<PushSubscriber, Long> implements PushSubscriber {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPushSubscriber.class);
    
    private PushSubscriberRepository subscriberRepository;
    
    private Long id;
    private long timeout;
    private long lastUpdated;
    private String topic;
    private String callback;
    private long leaseSeconds;
    private String verifyToken;
    private int failedVerifications;
    
    // For JPA
    protected DefaultPushSubscriber() {
        
    }
    
    public DefaultPushSubscriber(PushSubscriberRepository subscriberRepository, URI topic, URI callback, 
            DateTime timeout, DateTime lastUpdated,
            long leaseSeconds, String verifyToken) {
        this.subscriberRepository = subscriberRepository;
        this.timeout = timeout.getMillis();
        this.lastUpdated = lastUpdated.getMillis();
        this.topic = topic.toString();
        this.callback = callback.toString();
        this.leaseSeconds = leaseSeconds;
        this.verifyToken = verifyToken;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public DateTime getTimeout() {
        return new DateTime(timeout, DateTimeZone.UTC);
    }

    @Override
    public void publish(Feed feed) throws PublicationFailedException {
        //Feed feed = topic.getFeed();
        if(feed.hasUpdates(getLastUpdated())) {
            LOG.info("Distributing to {}", callback);
            HttpPost post = new HttpPost(callback);
            
            // TODO revisit, how do we know what content type a subscriber wants?
            ContentType contentType = ContentType.ATOM;
            
            post.addHeader(new BasicHeader("Content-Type", contentType.toString()));
            
            Document doc = AbstractSerializer.create(contentType).print(feed, 
                    new UpdatedSinceEntryFilter(getLastUpdated()));
            post.setEntity(HttpUtil.createEntity(doc));
            
            HttpResponse response = null;
            try {
                response = HttpUtil.getClient().execute(post);
                if(HttpUtil.successStatus(response)) {
                    LOG.debug("Succeeded distributing to subscriber {}", callback);
                    
                    // update last update
                    lastUpdated = new DateTime().getMillis();
                } else {
                    // TODO revisit
                    // subscription.markForVerification();
                    
                    throw new PublicationFailedException("Failed distributing to subscriber \"" + callback + "\" with error \"" + response.getStatusLine() + "\"");
                }
            } catch(IOException e) {
                // TODO revisit
                //subscription.markForVerification();
                
                throw new PublicationFailedException("Failed distributing to subscriber: " + callback, e);
            } finally {
                HttpUtil.closeQuitely(response);
    
                subscriberRepository.store(this);
            }
        } else {
            LOG.info("No updates for subscriber {}", this);
        }

    }

    @Override
    public void timedOut() {
        // TODO verify
        
        SubscriptionRequest request = new SubscriptionRequest(SubscriptionMode.SUBSCRIBE, getCallback(), getTopic(), leaseSeconds, verifyToken);
        
        try {
            verify(request);
        } catch (Exception e) {
            failedVerifications++;
            
            // TODO up for removal?
        }
    }
    
    public void verify(SubscriptionRequest request) throws IOException, FailedSubscriberVerificationException {
        String challenge = UUID.randomUUID().toString();
        
        HttpGet get = new HttpGet(request.getVerificationUrl(challenge));

        HttpResponse response = HttpUtil.getClient().execute(get);
        try {
            
            if(HttpUtil.successStatus(response)) {
                String returnedChallenge = HttpUtil.readContent(response.getEntity());
                
                if(challenge.equals(returnedChallenge)) {
                    // all okay
                } else {
                    throw new FailedSubscriberVerificationException("Challenge did not match");
                }
                
            } else {
                throw new FailedSubscriberVerificationException("Failed to verify subscription, status: " + response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase());
            }
        } finally {
            HttpUtil.closeQuitely(response);
        }

    }

    @Override
    public DateTime getLastUpdated() {
        return new DateTime(lastUpdated, DateTimeZone.UTC);
    }

    @Override
    public URI getTopic() {
        return URI.create(topic);
    }

    public URI getCallback() {
        return URI.create(callback);
    }

    public long getLeaseSeconds() {
        return leaseSeconds;
    }

    public String getVerifyToken() {
        return verifyToken;
    }
    
    

}
