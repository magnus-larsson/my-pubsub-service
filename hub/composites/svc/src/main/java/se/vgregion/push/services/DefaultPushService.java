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

package se.vgregion.push.services;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.AbstractParser;
import se.vgregion.push.types.AbstractSerializer;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;

@Service
public class DefaultPushService implements PushService {
    
    private static final int RETRY_DELAY = 5;

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPushService.class);

    private SubscriptionRepository subscriptionRepository;
    private FeedRepository feedRepository;

    private HttpClient httpclient;
    
    public DefaultPushService(SubscriptionRepository subscriptionRepository, FeedRepository feedRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.feedRepository = feedRepository;

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        
        // configure timeouts
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 10000);

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        httpclient = new DefaultHttpClient(cm, params);
    }

    @Transactional(readOnly=true)
    @Override
    public Collection<Subscription> getAllSubscriptionsForFeed(URI feed) {
        return subscriptionRepository.findByTopic(feed);
    }
    
    @Override
    public void verify(SubscriptionRequest request) throws FailedSubscriberVerificationException, IOException {
        String challenge = UUID.randomUUID().toString();
        
        HttpGet get = new HttpGet(request.getVerificationUrl(challenge));

        HttpResponse response = httpclient.execute(get);
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
    public Subscription subscribe(Subscription subscription) {
        // if subscription already exist, replace it
        unsubscribe(subscription);
        
        return subscriptionRepository.persist(subscription);
    }

    @Override
    public Subscription unsubscribe(Subscription subscription) {
        Subscription existing = subscriptionRepository.findByTopicAndCallback(subscription.getTopic(), subscription.getCallback());
        
        if(existing != null) {
            subscriptionRepository.removeByPrimaryKey(existing.getId());
        }
        return existing;
    }
    
    /* (non-Javadoc)
     * @see se.vgregion.push.services.impl.FeedRetrievalService#retrieve(se.vgregion.push.services.RetrievalRequest)
     */
    @Transactional
    public Feed retrieve(URI url) throws IOException {
        LOG.debug("Downloading feed {}", url);

        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        
        if(response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Failed to download feed: " + response.getStatusLine());
        }
        
        HttpEntity entity = response.getEntity();
        
        Header[] contentTypes = response.getHeaders("Content-Type");
        
        // TODO is this a reasonable default?
        ContentType contentType = ContentType.ATOM;
        if(contentTypes.length > 0) {
            try {
                contentType = ContentType.fromValue(contentTypes[0].getValue());
            } catch(IllegalArgumentException e) {
                // TODO How to handle this, sniff the entity?
                contentType = ContentType.ATOM;
            }
        }

        Feed feed;
        try {
            feed = AbstractParser.create(contentType).parse(url, entity.getContent());
        } catch (Exception e) {
            throw new IOException("Failed to parse feed", e);
        }
//        Feed existing = feedRepository.findByUrl(url);
//        
//        if(existing != null) {
//            existing.merge(feed);
//            feed = existing;
//        }

        HttpUtil.closeQuitely(response);

        feedRepository.persistOrUpdate(feed);
        
        LOG.debug("Feed downloaded: {}", url);

        LOG.warn("Feed successfully retrived, putting for distribution: {}", url);
        
        return feed;
    }

    @Override
    public void distribute(DistributionRequest request) throws IOException {
        Collection<Subscription> subscribers = getAllSubscriptionsForFeed(request.getFeed().getUrl());
        
        Feed feed = request.getFeed();
        
        if(!subscribers.isEmpty()) {
            LOG.debug("Distributing " + request.getFeed().getUrl());
            DateTime oldestUpdated = new DateTime(1970, 1, 1, 0, 0, 0, 0);

            for(Subscription subscription : subscribers) {
                try {
                    distribute(feed, subscription);
                    
                    // purge old feed entries based on the subscriber which is furthers behind
                    if(subscription.getLastUpdated().isBefore(oldestUpdated)) {
                        oldestUpdated = subscription.getLastUpdated();
                    }
                    
                } catch(FailedDistributionException e) {
                    LOG.info(e.getMessage(), e);
                }
            }
            
            
            feedRepository.deleteOutdatedEntries(feed, oldestUpdated);
            LOG.info("Feed distributed to {} subscribers: {}", subscribers.size(), request.getFeed().getUrl());
        }
    }

    protected void distribute(Feed feed, Subscription subscription) throws FailedDistributionException {
        LOG.debug("Checking subscription if verification is needed {}", subscription);
        
        // TODO the spec (6.3) requires a time out to be verified *before* it times out,
        // we do it after. Needs redesign but works for now
        if(subscription.isNeedsVerification()) {
            if(!renewSubcription(subscription)) {
                LOG.debug("Subscription verification failed, will not distributed content to subscription {}", subscription);
                return;
            }
        }
        
        if(feed.hasUpdates(subscription.getLastUpdated())) {
            LOG.info("Distributing to {}", subscription.getCallback());
            HttpPost post = new HttpPost(subscription.getCallback());
            
            post.addHeader(new BasicHeader("Content-Type", feed.getContentType().toString()));
            
            post.setEntity(HttpUtil.createEntity(AbstractSerializer.create(feed.getContentType()).print(feed, 
                    new UpdatedSinceEntryFilter(subscription.getLastUpdated()))));
            
            HttpResponse response = null;
            try {
                response = httpclient.execute(post);
                if(HttpUtil.successStatus(response)) {
                    LOG.debug("Succeeded distributing to subscriber {}", subscription.getCallback());
                    
                    // update last update
                    subscription.setLastUpdated(new DateTime());
                } else {
                    subscription.markForVerification();
                    
                    throw new FailedDistributionException("Failed distributing to subscriber \"" + subscription.getCallback() + "\" with error \"" + response.getStatusLine() + "\"");
                }
            } catch(IOException e) {
                subscription.markForVerification();
                
                throw new FailedDistributionException("Failed distributing to subscriber: " + subscription.getCallback(), e);
            } finally {
                HttpUtil.closeQuitely(response);
    
                subscriptionRepository.store(subscription);
            }
        } else {
            LOG.info("No updates for subscriber {}", subscription);
        }
    }

    public boolean renewSubcription(Subscription subscription) {
        LOG.info("Renewing subscription {}", subscription);
        
        SubscriptionRequest request = new SubscriptionRequest(
                SubscriptionMode.SUBSCRIBE, 
                subscription.getCallback(), 
                subscription.getTopic(), 
                subscription.getLeaseSeconds(), 
                subscription.getVerifyToken());
        
        boolean result = false;
        try {
            verify(request);
            
            // success
            subscription.successfullyVerified();
            
            result = true;
        } catch (FailedSubscriberVerificationException e) {
            // failed verification
            LOG.info("Failed to renew subscription {}", subscription);
            subscription.failedVerified();
        } catch (IOException e) {
            // failed verification
            LOG.info("Failed to renew subscription {}", subscription);
            subscription.failedVerified();
        }

        if(subscription.isFailed()) {
            LOG.info("Subscription reached renewal limit, removed {}", subscription);
            subscriptionRepository.removeByPrimaryKey(subscription.getId());
        } else {
            subscriptionRepository.store(subscription);
        }
        
        return result;
    }

    @Override
    public void retryDistributions() {
        
        Collection<Feed> behindFeeds = feedRepository.findFeedsWithBehindSubscriptions();
        
        for(Feed behindFeed : behindFeeds) {
            Collection<Subscription> subscriptionsForFeed = subscriptionRepository.findByTopic(behindFeed.getUrl());
            
            for(Subscription subscriptionForFeed : subscriptionsForFeed) {
                DateTime subUpdated = subscriptionForFeed.getLastUpdated();
                DateTime feedUpdated = behindFeed.getUpdated();
                
                if(subUpdated == null || subUpdated.plusMinutes(RETRY_DELAY).isBefore(feedUpdated)) {
                    try {
                        distribute(behindFeed, subscriptionForFeed);
                    } catch (FailedDistributionException e) {
                        LOG.info(e.getMessage());

                        subscriptionForFeed.markForVerification();
                        subscriptionRepository.store(subscriptionForFeed);
                    }
                }
            }
        }
    }
}
