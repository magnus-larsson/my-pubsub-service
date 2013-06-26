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

package se.vgregion.pubsub.push.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import nu.xom.Document;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.hibernate.annotations.Index;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.docpublishing.v1.DocumentStatusType;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.PushJms;
import se.vgregion.pubsub.content.AbstractParser;
import se.vgregion.pubsub.content.AbstractSerializer;
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;

/**
 * Implementation of {@link PushSubscriber} with support for JPA
 */
@Entity
@Table(name = "PUSH_SUBSCRIBERS",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"topic", "callback"})
        }
)
public class DefaultPushSubscriber extends AbstractEntity<UUID> implements PushSubscriber {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPushSubscriber.class);

    private final static Map<UUID, Boolean> loggedErrorOnLastPush = new HashMap<UUID, Boolean>() {
        @Override
        public Boolean get(Object key) {
            Boolean result = super.get(key);
            if (result == null) {
                put((UUID) key, result = false);
            }
            return result;
        }
    };

    @Id
    private UUID id;

    @Column
    private Long timeout;

    @Column
    private Long lastUpdated;

    @Column(nullable = false)
    @Index(name = "push_subscriber_topic")
    private String topic;

    @Column(nullable = false)
    private String callback;

    @Column(name = "jms_logg_address")
    private String jmsLoggAddress;

    @Column(nullable = false)
    private int leaseSeconds;

    @Column
    private String verifyToken;

    @Column
    private String secret;

    @Column(nullable = false)
    private boolean active;

    @Transient
    private int failedVerifications;

    // For JPA
    protected DefaultPushSubscriber() {

    }

    public DefaultPushSubscriber(URI topic, URI callback,
                                 DateTime timeout, DateTime lastUpdated,
                                 int leaseSeconds, String verifyToken, String secret, boolean active) {
        id = UUID.randomUUID();

        Assert.notNull(topic);
        Assert.notNull(callback);

        if (timeout != null) {
            this.timeout = timeout.getMillis();
        }
        if (lastUpdated != null) {
            this.lastUpdated = lastUpdated.getMillis();
        }
        this.topic = topic.toString();
        this.callback = callback.toString();
        this.leaseSeconds = leaseSeconds;
        this.verifyToken = verifyToken;
        this.secret = secret;
        this.active = active;
    }

    public DefaultPushSubscriber(URI topic, URI callback,
                                 int leaseSeconds, String verifyToken, String secret, boolean active) {
        this(topic, callback,
                (leaseSeconds > 0) ? new DateTime().plusSeconds(leaseSeconds) : null, null,
                leaseSeconds, verifyToken, secret, active);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime getTimeout() {
        if (timeout != null && timeout > 0) {
            return new DateTime(timeout, DateTimeZone.UTC);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void publish(Feed feed, PushJms pushJms) throws PublicationFailedException {
        if (active) {
            LOG.info("Getting subscribed feed for {}", callback);
            LOG.debug("Using PushSubcriber {}", this);

            if (feed.hasUpdates(getLastUpdated())) {

                LOG.info("Feed has updates, distributing to {}", callback);
                HttpPost post = new HttpPost(callback);

                post.addHeader(new BasicHeader("Content-Type", feed.getContentType().toString()));

                Document doc = AbstractSerializer.create(feed.getContentType()).print(feed,
                        new MustHaveRequestIdFilter(getLastUpdated()));

                String xml = doc.toXML();

                LOG.debug("XML being pushed to subscriber:");
                LOG.debug(xml);

                post.setEntity(HttpUtil.createEntity(xml));

                if (StringUtils.isNotBlank(secret)) {
                    // calculate HMAC header
                    post.addHeader(new BasicHeader("X-Hub-Signature", "sha1=" + CryptoUtil.calculateHmacSha1(secret, xml)));
                }
                PushJms localJms = pushJms;
                HttpResponse response = null;
                try {
                    DefaultHttpClient httpClient = HttpUtil.getClient();

                    // don't redirect publications
                    httpClient.setRedirectHandler(new DontRedirectHandler());

                    response = httpClient.execute(post);
                    LOG.debug("XML has been pushed to subscriber");

                    Feed newFeed = AbstractParser.create(ContentType.ATOM).parse(xml, ContentType.ATOM);

                    if (localJms != null) {
                        if (getJmsLoggAddress() != null && !"".equals(getJmsLoggAddress())) {
                            localJms = pushJms.copy(getJmsLoggAddress());
                        }
                        localJms.send(newFeed, "event-text", DocumentStatusType.OK);
                    }

                    if (HttpUtil.successStatus(response)) {
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
                    loggedErrorOnLastPush.put(getId(), false);
                } catch (Exception e) {
                    // TODO revisit
                    //subscription.markForVerification();

                    String msg = "Failed distributing to subscriber \"" + callback + "\" with error: ";
                    LOG.error(msg);
                    if (localJms != null && !loggedErrorOnLastPush.get(getId())) {
                        try {
                            Feed newFeed = AbstractParser.create(ContentType.ATOM).parse(xml, ContentType.ATOM);
                            localJms.send(newFeed, e.getMessage(), DocumentStatusType.ERROR);
                        } catch (Exception e1) {
                            LOG.error("Failed to log errors to jms", e1);
                        }
                    }
                    loggedErrorOnLastPush.put(getId(), true);

                    throw new PublicationFailedException(msg, e);
                } finally {
                    HttpUtil.closeQuitely(response);
                }
            } else {
                LOG.info("No updates for subscriber {}", callback);
            }
        } else {
            LOG.debug("Subscriber for {} disabled", callback);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public void verify(SubscriptionMode mode) throws IOException, FailedSubscriberVerificationException {
        String challenge = UUID.randomUUID().toString();

        HttpGet get = new HttpGet(getVerificationUrl(mode, challenge));

        HttpResponse response = HttpUtil.getClient().execute(get);
        try {

            if (HttpUtil.successStatus(response)) {
                String returnedChallenge = HttpUtil.readContent(response.getEntity());

                if (challenge.equals(returnedChallenge)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime getLastUpdated() {
        if (lastUpdated == null) {
            return null;
        } else {
            return new DateTime(lastUpdated);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getTopic() {
        return URI.create(topic);
    }

    /**
     * {@inheritDoc}
     */
    public URI getCallback() {
        return URI.create(callback);
    }

    public int getLeaseSeconds() {
        return leaseSeconds;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Builds the URL used for verifying a subscriber
     *
     * @param mode
     * @param challenge
     * @return
     */
    public URI getVerificationUrl(SubscriptionMode mode, String challenge) {
        try {
            StringBuffer url = new StringBuffer();
            url.append(callback);

            if (getCallback().getQuery() != null) {
                url.append("&");
            } else {
                url.append("?");
            }

            if (mode.equals(SubscriptionMode.SUBSCRIBE)) {
                url.append("hub.mode=subscribe");
            } else {
                url.append("hub.mode=unsubscribe");
            }
            url.append("&hub.topic=").append(URLEncoder.encode(topic.toString(), "UTF-8"))
                    .append("&hub.challenge=").append(URLEncoder.encode(challenge, "UTF-8"));

            if (leaseSeconds > 0) {
                url.append("&hub.lease_seconds=").append(leaseSeconds);
            }
            if (verifyToken != null) {
                url.append("&hub.verify_token=").append(URLEncoder.encode(verifyToken, "UTF-8"));
            }

            return URI.create(url.toString());
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    public String getJmsLoggAddress() {
        return jmsLoggAddress;
    }

    public void setJmsLoggAddress(String jmsLoggAddress) {
        this.jmsLoggAddress = jmsLoggAddress;
    }
}
