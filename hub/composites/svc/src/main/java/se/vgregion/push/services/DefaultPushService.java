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
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;

@Service
public class DefaultPushService implements PushService {
    
    private final static Logger LOG = LoggerFactory.getLogger(DefaultPushService.class);

    private SubscriptionRepository subscriptionRepository;
    private FeedRepository feedRepository;

    private HttpClient httpclient = new DefaultHttpClient();
    
    public DefaultPushService(SubscriptionRepository subscriptionRepository, FeedRepository feedRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.feedRepository = feedRepository;
        
        // configure timeouts
        HttpParams params = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 10000);
    }

    @Transactional(readOnly=true)
    @Override
    public List<Subscription> getAllSubscriptionsForFeed(URI feed) {
        return subscriptionRepository.findByTopic(feed);
    }

    @Override
    public void verify(SubscriptionRequest request) throws IOException {
        String challenge = UUID.randomUUID().toString();
        
        HttpGet get = new HttpGet(request.getVerificationUrl(challenge));

        HttpResponse response = httpclient.execute(get);
        try {
            
            if(HttpUtil.successStatus(response)) {
                String returnedChallenge = HttpUtil.readContent(response.getEntity());
                
                if(challenge.equals(returnedChallenge)) {
                    // all okay
                } else {
                    throw new IOException("Challenge did not match");
                }
                
            } else {
                throw new IOException("Failed to verify subscription, status: " + response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase());
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
            
            subscriptionRepository.remove(existing);
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

        Feed feed = new Feed(url, contentType, entity.getContent());

        HttpUtil.closeQuitely(response);

        feed = feedRepository.persistOrUpdate(feed);
        
        LOG.debug("Feed downloaded: {}", url);

        LOG.warn("Feed successfully retrived, putting for distribution: {}", url);
        
        return feed;
    }

    @Override
    public void distribute(DistributionRequest request) throws IOException {
        List<Subscription> subscribers = getAllSubscriptionsForFeed(request.getFeed().getUrl());
        
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

    private void distribute(Feed feed, Subscription subscription) throws FailedDistributionException {
        LOG.debug("Distributing to " + subscription.getCallback());
        HttpPost post = new HttpPost(subscription.getCallback());
        
        post.addHeader(new BasicHeader("Content-Type", feed.getContentType().toString()));
        
        post.setEntity(HttpUtil.createEntity(feed.createDocument(subscription.getLastUpdated())));
        
        HttpResponse response = null;
        try {
            response = httpclient.execute(post);
            if(HttpUtil.successStatus(response)) {
                LOG.debug("Succeeded distributing to subscriber {}", subscription.getCallback());
                
                // update last update
                subscription.setLastUpdated(new DateTime());
                subscriptionRepository.store(subscription);
            } else {
                // TODO handle retrying
                throw new FailedDistributionException("Failed distributing to subscriber \"" + subscription.getCallback() + "\" with error \"" + response.getStatusLine() + "\"");
            }
        } catch(IOException e) {
            // TODO handle retrying
            throw new FailedDistributionException("Failed distributing to subscriber: " + subscription.getCallback(), e);
        } finally {
            HttpUtil.closeQuitely(response);
        }
    }

}
