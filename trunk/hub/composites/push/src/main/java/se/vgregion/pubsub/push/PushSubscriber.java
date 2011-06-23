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

package se.vgregion.pubsub.push;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import se.vgregion.dao.domain.patterns.entity.Entity;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

/**
 * A PuSH subscriber
 *
 */
public interface PushSubscriber extends Subscriber, Entity<UUID>  {

    // 24 hours
    public static final int DEFAULT_LEASE_SECONDS = 60*60*24;

    /**
     * The callback URL on which the subscribers will be notified on publications
     * @return
     */
    URI getCallback();
    
    /**
     * Verify subscriptions and unsubscriptions
     * @param mode
     * @throws IOException
     * @throws FailedSubscriberVerificationException
     */
    void verify(SubscriptionMode mode) throws IOException, FailedSubscriberVerificationException;
}
