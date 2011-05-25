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

import se.vgregion.dao.domain.patterns.entity.Entity;

/**
 * A URL on which feeds a published and subscribers subscribe
 *
 */
public interface Topic extends Entity<URI> {

	/**
	 * The topic URL
	 * @return
	 */
    URI getUrl();
    
    /**
     * Publish a {@link Feed} to all subscribed subscribers
     * @param feed
     */
    void publish(Feed feed);
    
    /**
     * Subscribe a {@link Subscriber} to this topic
     * @param subscriber
     */
    void addSubscriber(Subscriber subscriber);

    /**
     * Unsubscribe a {@link Subscriber} from this topic
     * @param subscriber
     */
    void removeSubscriber(Subscriber subscriber);

}
