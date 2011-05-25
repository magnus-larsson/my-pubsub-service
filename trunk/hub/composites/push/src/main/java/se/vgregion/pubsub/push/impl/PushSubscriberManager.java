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

import java.net.URI;

import se.vgregion.pubsub.PubSubEngine;

/**
 * Service for handling PuSH subscriptions
 *
 */
public interface PushSubscriberManager {

	/**
	 * Load all persisted subscribers and add these to a {@link PubSubEngine}
	 */
    public void loadSubscribers();

    /**
     * Create a new subscriber
     * @param topicUrl
     * @param callback
     * @param leaseSeconds
     * @param verifyToken
     */
    public void subscribe(URI topicUrl, URI callback, int leaseSeconds, String verifyToken);

    /**
     * Unsubscribe a subscriber if one exists with the provided topic and callback
     * @param topic
     * @param callback
     */
    public void unsubscribe(URI topic, URI callback);
}
