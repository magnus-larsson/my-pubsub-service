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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.repository.SubscriptionRepository;
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
        
        if(successStatus(response.getStatusLine().getStatusCode())) {
            String returnedChallenge = readContent(response.getEntity());
            
            if(challenge.equals(returnedChallenge)) {
                // all okay
            } else {
                throw new IOException("Challenge did not match");
            }
            
        } else {
            throw new IOException("Failed to verify subscription, status: " + response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase());
        }
    }
    
    private String readContent(HttpEntity entity) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entity.writeTo(out);
        
        return out.toString("UTF-8");
    }
    
    private boolean successStatus(int status) {
        return status >= 200 && status <300;
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
            subscriptionRepository.removeEntity(existing);
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

        Feed feed = new Feed(url, entity.getContent());
        
        feed = feedRepository.persist(feed);
        
        LOG.debug("Feed downloaded: {}", url);

        LOG.warn("Feed successfully retrived, putting for distribution: {}", url);
        
        return feed;
    }

}
