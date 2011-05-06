package se.vgregion.pubsub.push.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import nu.xom.Document;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.content.AbstractSerializer;
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

@Entity
@Table(name="PUSH_SUBSCRIBERS",
    uniqueConstraints={
        @UniqueConstraint(columnNames={"topic", "callback"})
    }
)
public class DefaultPushSubscriber extends AbstractEntity<UUID> implements PushSubscriber {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPushSubscriber.class);
    
    @Transient
    private PushSubscriberRepository subscriberRepository;
    
    @Id
    private UUID id;

    @Column
    private Long timeout;
    
    @Column
    private Long lastUpdated;
    
    @Column(nullable=false)
    private String topic;
    
    @Column(nullable=false)
    private String callback;
    
    @Column(nullable=false)
    private int leaseSeconds;
    
    @Column
    private String verifyToken;
    
    @Transient
    private int failedVerifications;
    
    // For JPA
    protected DefaultPushSubscriber() {
        
    }
    
    public DefaultPushSubscriber(PushSubscriberRepository subscriberRepository, URI topic, URI callback, 
            DateTime timeout, DateTime lastUpdated,
            int leaseSeconds, String verifyToken) {
        id = UUID.randomUUID();
        
        Assert.notNull(subscriberRepository);
        Assert.notNull(topic);
        Assert.notNull(callback);
        
        this.subscriberRepository = subscriberRepository;
        if(timeout != null) this.timeout = timeout.getMillis();
        if(lastUpdated != null) this.lastUpdated = lastUpdated.getMillis();
        this.topic = topic.toString();
        this.callback = callback.toString();
        this.leaseSeconds = leaseSeconds;
        this.verifyToken = verifyToken;
    }

    public DefaultPushSubscriber(PushSubscriberRepository subscriberRepository, URI topic, URI callback, 
            int leaseSeconds, String verifyToken) {
        this(subscriberRepository, topic, callback, 
                (leaseSeconds > 0) ? new DateTime().plusSeconds(leaseSeconds) : null, null,
                leaseSeconds, verifyToken);
    }

    
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public DateTime getTimeout() {
        if(timeout != null && timeout > 0) {
            return new DateTime(timeout, DateTimeZone.UTC);
        } else {
            return null;
        }
    }

    @Override
    public synchronized void publish(Feed feed) throws PublicationFailedException {
        LOG.info("Getting subscribed feed for {}", callback);
        
        if(feed.hasUpdates(getLastUpdated())) {
            LOG.info("Feed has updates, distributing to {}", callback);
            HttpPost post = new HttpPost(callback);
            
            post.addHeader(new BasicHeader("Content-Type", feed.getContentType().toString()));
            
            Document doc = AbstractSerializer.create(feed.getContentType()).print(feed, 
                    new UpdatedSinceEntryFilter(getLastUpdated()));

            post.setEntity(HttpUtil.createEntity(doc));
            
            HttpResponse response = null;
            try {
                DefaultHttpClient httpClient = HttpUtil.getClient();
                
                // don't redirect publications
                httpClient.setRedirectHandler(new DontRedirectHandler());
                response = httpClient.execute(post);
                if(HttpUtil.successStatus(response)) {
                    LOG.info("Succeeded distributing to subscriber {}", callback);
                    
                    // update last update
                    lastUpdated = new DateTime().getMillis();
                } else {
                    // TODO revisit
                    // subscription.markForVerification();
                    String msg = "Failed distributing to subscriber \"" + callback + "\" with error \"" + response.getStatusLine() + "\"";
                    
                    LOG.warn(msg);
                    throw new PublicationFailedException(msg);
                }
            } catch(IOException e) {
                // TODO revisit
                //subscription.markForVerification();

                String msg = "Failed distributing to subscriber \"" + callback + "\" with error \"" + response.getStatusLine() + "\"";
                LOG.warn(msg);
                throw new PublicationFailedException(msg);
            } finally {
                HttpUtil.closeQuitely(response);
    
                LOG.info("Merging subscriber in repository {}", subscriberRepository);
                
                subscriberRepository.merge(this);
            }
        } else {
            LOG.info("No updates for subscriber {}", callback);
        }

    }

    @Override
    public void timedOut() {
        // TODO verify
        
        try {
            verify(SubscriptionMode.SUBSCRIBE);
        } catch (Exception e) {
            failedVerifications++;
            
            // TODO up for removal?
        }
    }
    
    public void verify(SubscriptionMode mode) throws IOException, FailedSubscriberVerificationException {
        String challenge = UUID.randomUUID().toString();
        
        HttpGet get = new HttpGet(getVerificationUrl(mode, challenge));

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
        if(lastUpdated == null) {
            return null;
        } else {
            return new DateTime(lastUpdated);
        }
    }

    @Override
    public URI getTopic() {
        return URI.create(topic);
    }

    public URI getCallback() {
        return URI.create(callback);
    }

    public int getLeaseSeconds() {
        return leaseSeconds;
    }

    public String getVerifyToken() {
        return verifyToken;
    }
    
    public URI getVerificationUrl(SubscriptionMode mode, String challenge) {
        try {
            StringBuffer url = new StringBuffer();
            url.append(callback);
            
            if(getCallback().getQuery() != null) {
                url.append("&");
            } else {
                url.append("?");
            }
            
            if(mode.equals(SubscriptionMode.SUBSCRIBE)) {
                url.append("hub.mode=subscribe");
            } else {
                url.append("hub.mode=unsubscribe");
            }
            url.append("&hub.topic=").append(URLEncoder.encode(topic.toString(), "UTF-8"))
                .append("&hub.challenge=").append(URLEncoder.encode(challenge, "UTF-8"));
                
            if(leaseSeconds > 0) {
                url.append("&hub.lease_seconds=").append(leaseSeconds);
            }
            if(verifyToken != null) {
                url.append("&hub.verify_token=").append(URLEncoder.encode(verifyToken, "UTF-8"));
            }
            
            return URI.create(url.toString());
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    public void setSubscriberRepository(PushSubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }
    
    
}
