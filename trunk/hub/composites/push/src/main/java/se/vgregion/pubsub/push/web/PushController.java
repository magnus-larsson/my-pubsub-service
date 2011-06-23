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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

/**
 * Spring MVC controller for handling the HTTP parts of PuSH. Receives publications, subscriptions and unsubscriptions.
 *
 */
@Controller
public class PushController {

    private final static Logger LOG = LoggerFactory.getLogger(PushController.class);

    private final static List<String> ALLOWED_SCHEMES = Arrays.asList("http", "https");
    
    @Resource
    private PushSubscriberRepository subscriberRepository;
    
    @Resource
    private PushSubscriberManager pushSubscriberManager;
    
    @RequestMapping(value="/", method=RequestMethod.GET)
    public void get(HttpServletResponse response) throws IOException {
        response.sendError(405, "This is the endpoint for the PubSubHubbub hub. " +
        		"It only support POST requests according to the PubSubHubbub protocol");
    }
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
                	if(mode == SubscriptionMode.SUBSCRIBE) {
                		LOG.info("Received subscription request");

                		String leaseSecondsString = request.getParameter("hub.lease_seconds");
                		try {
                			int leaseSeconds = leaseSecondsString != null ? Integer.parseInt(leaseSecondsString) : PushSubscriber.DEFAULT_LEASE_SECONDS;
                			
                			String secret = request.getParameter("hub.secret");
                			String verifyToken = request.getParameter("hub.verify_token");
                			
                			try {
                				pushSubscriberManager.subscribe(topicUrl, callback, leaseSeconds, verifyToken, true);
                			
                				response.setStatus(204);
                				LOG.info("Subscription request for topic {} with callback {} successful", topicUrl, callback);
                			} catch(Exception e) {
                				LOG.warn("Exception thrown during subscription from callback " + callback, e);
                				response.sendError(500);
                			}
                		} catch(NumberFormatException e) {
                        	LOG.warn("Invalid hub.lease_seconds from callback {}", callback);

                			response.sendError(500, "Invalid hub.lease_seconds");
                		}
                	} else {
                		LOG.info("Received unsubscription request");
                		try {
                			pushSubscriberManager.unsubscribe(topicUrl, callback, true);
            				response.setStatus(204);
            				LOG.info("Unsubscription request for topic {} with callback {} successful", topicUrl, callback);
            			} catch(Exception e) {
            				LOG.warn("Exception thrown during unsubscription from callback " + callback, e);
            				response.sendError(500);
            			}
                	}
                } else {
                	LOG.warn("Invalid hub.verify from callback {}", callback);
                    response.sendError(500, "Invalid hub.verify");
                } 
            } else {
            	LOG.warn("Invalid hub.topic from callback {}", callback);
                response.sendError(500, "Invalid hub.topic");
            }
        } else {
        	LOG.warn("Invalid hub.callback from {}", request.getRemoteAddr());
            response.sendError(500, "Invalid hub.callback");
        }
    }

    private void publish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.info("Received publish request");
        String[] urls = request.getParameterValues("hub.url");
        if(urls != null) {
        	try {
	            for(String url : urls) {
	                publish(url);
	            }
	            
	            LOG.info("Published feeds successfully queued, returning 204 to publisher");
	            response.setStatus(204);
        	} catch(Exception e) {
        		LOG.warn("Exception thrown during publication", e);
        		response.sendError(500);
			}
        } else {
            response.sendError(500, "Missing hub.url parameter");
        }

    }
    
    private void publish(String url) throws IOException, InterruptedException {
        try {
            URI uri = new URI(url);
            
            if(allowScheme(uri)) {
            	pushSubscriberManager.retrive(uri);
            } else {
            	throw new RuntimeException("Only HTTP URLs allowed: " + url);
            }
        } catch (URISyntaxException e1) {
        	throw new RuntimeException("Invalid hub.url value: " + url);
        }
    }

    public PushSubscriberRepository getSubscriberRepository() {
		return subscriberRepository;
	}
	
    public void setSubscriberRepository(
			PushSubscriberRepository subscriberRepository) {
		this.subscriberRepository = subscriberRepository;
	}
	
	public PushSubscriberManager getPushSubscriberManager() {
		return pushSubscriberManager;
	}
	
	public void setPushSubscriberManager(PushSubscriberManager pushSubscriberManager) {
		this.pushSubscriberManager = pushSubscriberManager;
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
