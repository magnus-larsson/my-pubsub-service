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

package se.vgregion.pubsub.push.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.impl.DefaultPushSubscriber;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

@Controller
public class PushController {

    private final static Logger LOG = LoggerFactory.getLogger(PushController.class);

    private final static List<String> ALLOWED_SCHEMES = Arrays.asList("http", "https");
    
    @Resource(name="retrieveQueue")
    private BlockingQueue<URI> retrieverQueue;
    
    @Resource
    private PubSubEngine pubSubEngine;

    @Resource
    private PushSubscriberRepository subscriberRepository;
    
    @Resource
    private PushSubscriberManager pushSubscriberManager;
    
    @RequestMapping(value="/", method=RequestMethod.POST)
    public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String mode = request.getParameter("hub.mode");
        
        if("publish".equals(mode)) {
            publish(request, response);
        } else if("subscribe".equals(mode)) {
            subscribe(request, response, SubscriptionMode.SUBSCRIBE);
        } else if("unsubscribe".equals(mode)) {
            subscribe(request, response, SubscriptionMode.UNSUBSCRIBE);
        } else {
            response.sendError(500, "Unknown hub.mode parameter");
        }
    }
    
    private void subscribe(HttpServletRequest request, HttpServletResponse response, SubscriptionMode mode) throws IOException {
        URI callback = notNullAndValidUrl(request.getParameter("hub.callback"));
        if(callback != null) {
            URI topicUrl = notNullAndValidUrl(request.getParameter("hub.topic"));
            
            if(topicUrl != null) {
                String verify = request.getParameter("hub.verify");
                if("sync".equals(verify) || "async".equals(verify)) {
                    String leaseSecondsString = request.getParameter("hub.lease_seconds");
                    
                    try {
                        int leaseSeconds = leaseSecondsString != null ? Integer.parseInt(leaseSecondsString) : PushSubscriber.DEFAULT_LEASE_SECONDS;
                        
                        String secret = request.getParameter("hub.secret");
                        String verifyToken = request.getParameter("hub.verify_token");
                        
                        PushSubscriber subscriber = new DefaultPushSubscriber(subscriberRepository, topicUrl, callback, leaseSeconds, verifyToken);
                        
                        
                        try {
                            subscriber.verify(SubscriptionMode.SUBSCRIBE);
                            
                            pushSubscriberManager.subscribe(subscriber);
                            
                            response.setStatus(204);
                        } catch(Exception e) {
                            e.printStackTrace();
                            response.sendError(500);
                        }
                    } catch(NumberFormatException e) {
                        response.sendError(500, "Invalid hub.lease_seconds");
                    }
                } else {
                    response.sendError(500, "Invalid hub.verify");
                } 
            } else {
                response.sendError(500, "Invalid hub.topic");
            }
        } else {
            response.sendError(500, "Invalid hub.callback");
        }
    }

    private void publish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.info("Received publish request");
        String[] urls = request.getParameterValues("hub.url");
        if(urls != null) {
            for(String url : urls) {
                publish(url, response);
            }
        } else {
            response.sendError(500, "Missing hub.url parameter");
        }

    }
    
    private void publish(String url, HttpServletResponse response) throws IOException {
        try {
            URI uri = new URI(url);
            
            if(allowScheme(uri)) {
                Topic topic = pubSubEngine.getOrCreateTopic(uri);
                
                boolean wasAdded = false;
                try {
                    wasAdded = retrieverQueue.offer(uri, 2000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    wasAdded = false;
                }
                
                if(wasAdded) {
                    response.sendError(204);
                } else {
                    response.sendError(500, "Internal error, failed to publish update");
                }
            } else {
                response.sendError(500, "Only HTTP URLs allowed: " + url);
            }
        } catch (URISyntaxException e1) {
            response.sendError(500, "Invalid hub.url value: " + url);
        }
    }

    public BlockingQueue<URI> getRetrieverQueue() {
        return retrieverQueue;
    }

    public void setRetrieverQueue(BlockingQueue<URI> retrieverQueue) {
        this.retrieverQueue = retrieverQueue;
    }

    private URI notNullAndValidUrl(String url) {
        if(url != null) {
            try {
                return new URI(url);
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    private boolean allowScheme(URI uri) {
        return ALLOWED_SCHEMES.contains(uri.getScheme());
    }
}
