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

import org.joda.time.DateTime;

/**
 * A {@link Subscriber} for a {@link Topic} in a {@link PubSubEngine}
 *
 */
public interface Subscriber {

	/**
	 * Get the time when this subscriber was last updated. Returns null if never.
	 * @return
	 */
    DateTime getLastUpdated();
    
    /**
     * The time when this subscriber will be timed out and might required renewal.
     * null if never.
     * @return
     */
    DateTime getTimeout();
    
    /**
     * The {@link Topic} for which this subscriber is subscribed
     * @return
     */
    URI getTopic();
    
    /**
     * Publish a {@link Feed} to this subscriber
     * @param feed
     * @throws PublicationFailedException
     */
    void publish(Feed feed, PushJms pushJms) throws PublicationFailedException;

    /**
     * Called when a subcriber gets timed out
     */
    void timedOut();
}
