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
import java.net.URI;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.SubscriberManager;
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;

/**
 * Service for handling PuSH subscriptions
 *
 */
public interface PushSubscriberManager extends SubscriberManager {

    /**
     * Create a new subscriber
     * @param topicUrl
     * @param callback
     * @param leaseSeconds
     * @param verifyToken
     * @throws FailedSubscriberVerificationException 
     * @throws IOException 
     */
    public void subscribe(URI topicUrl, URI callback, int leaseSeconds, String verifyToken, String secret, boolean active, boolean verify) throws IOException, FailedSubscriberVerificationException;

    /**
     * Unsubscribe a subscriber if one exists with the provided topic and callback
     * @param topic
     * @param callback
     */
    public void unsubscribe(URI topic, URI callback, boolean verify);

    /**
     * Queue a feed for retrieval
     * @param topicUrl The URL of the feed to retrieve
     * @throws InterruptedException
     */
	void retrive(URI topicUrl) throws InterruptedException;

	/**
	 * PuSH internal method for invoking {@link PubSubEngine#publish(URI, Feed)}
	 * @param topicUrl
	 * @param feed
	 */
	void publish(URI topicUrl, Feed feed);

	/**
	 * Ask for feeds queued to retrieve
	 * @return The URL of the next feed to retreive
	 * @throws InterruptedException
	 */
	URI pollForRetrieval() throws InterruptedException;

	
}
