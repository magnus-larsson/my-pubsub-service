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

package se.vgregion.pubsub;

import java.net.URI;

/**
 * The main driver of the application, coordinating publications and subscriptions
 *
 */
public interface PubSubEngine {

	/**
	 * Create a topic based on the provided URL
	 * @param url
	 * @return
	 */
    Topic createTopic(URI url);
    
    /**
     * Get a topic based on the provided URL if it already exists, or create it otherwise
     * @param url
     * @return
     */
    Topic getOrCreateTopic(URI url);
    
    /**
     * Get a topic for the provided URL, returns null if no topic exists for the URL
     * @param url
     * @return
     */
    Topic getTopic(URI url);
    
    /**
     * Subscribe a subscriber to the topic provided by {@link Subscriber#getTopic()}
     * @param subscriber
     */
    void subscribe(Subscriber subscriber);

    /**
     * Unsubscribe a subscriber from the topic provided by {@link Subscriber#getTopic()}
     * @param subscriber
     */
    void unsubscribe(Subscriber subscriber);
    
    /**
     * Publish a {@link Feed} on the {@link Topic} represented by the provided URL
     * @param url
     * @param feed
     */
    void publish(URI url, Feed feed);
    
    /**
     * Add a listener that will get events for subscription events
     * @param eventListener
     */
    void addPubSubEventListener(PubSubEventListener eventListener);

    /**
     * Remove a listener
     * @param eventListener
     */
    void removePubSubEventListener(PubSubEventListener eventListener);
}
