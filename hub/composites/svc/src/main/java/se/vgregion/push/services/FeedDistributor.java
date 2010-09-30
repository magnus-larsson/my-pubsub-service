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
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.vgregion.push.types.Subscription;

public class FeedDistributor {

    private final static Logger LOG = LoggerFactory.getLogger(FeedDistributor.class);
    
    private final BlockingQueue<DistributionRequest> distributionQueue;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);;
    
    private PushService subscriptionService;
    
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    
    public FeedDistributor(BlockingQueue<DistributionRequest> distributionQueue, PushService subscriptionService) {
        this.distributionQueue = distributionQueue;
        this.subscriptionService = subscriptionService;
        
        // not allowed to redirect when distributing
        httpclient.setRedirectHandler(new DontRedirectHandler());
    }

    public void start() {
        LOG.info("Starting FeedDistributor");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        DistributionRequest request = distributionQueue.take();
                        
                        List<Subscription> subscribers = subscriptionService.getAllSubscriptionsForFeed(request.getFeed().getUrl());
                        
                        if(!subscribers.isEmpty()) {
                            LOG.debug("Distributing " + request.getFeed().getUrl());
                            try {
                                for(Subscription subscription : subscribers) {
                                    LOG.debug("Distributing to " + subscription.getCallback());
                                    HttpPost post = new HttpPost(subscription.getCallback());
                                    
                                    // TODO get from feed
                                    post.addHeader(new BasicHeader("Content-Type", "application/atom+xm"));
                                    
                                    post.setEntity(new StringEntity(request.getFeed().getDocument().toXML(), "UTF-8"));
                                    
                                    HttpResponse response = null;
                                    try {
                                        response = httpclient.execute(post);
                                        if(HttpUtil.successStatus(response)) {
                                            LOG.debug("Succeeded distributing to subscriber {}", subscription.getCallback());
                                        } else {
                                            // TODO handle retrying
                                            LOG.debug("Failed distributing to subscriber \"{}\" with error \"{}\"", subscription.getCallback(), response.getStatusLine());
                                        }
                                    } catch(IOException e) {
                                        // TODO handle retrying
                                        LOG.debug("Failed distributing to subscriber: " + subscription.getCallback(), e);
                                    } finally {
                                        if(response != null) {
                                            if(response.getEntity() != null) {
                                                InputStream in = response.getEntity().getContent();
                                                if(in != null) in.close();
                                            }
                                        }
                                    }
                                }
                                LOG.info("Feed distributed to {} subscribers: {}", subscribers.size(), request.getFeed().getUrl());
                            } catch (IOException e) {
                                LOG.debug("Failed to distribute feed: " + request.getFeed().getUrl(), e);
                            }
                        } else {
                            LOG.debug("No subscribers for published feed, ignoring: {}", request.getFeed().getUrl());
                        }
                    } catch (InterruptedException e) {
                        // shutting down
                        break;
                    }
                }
                
                LOG.info("Stopped FeedDistributor");
            }
        });
    }

    public void stop() {
        LOG.info("Stopping FeedDistributor");
        executor.shutdownNow();
    }

}
