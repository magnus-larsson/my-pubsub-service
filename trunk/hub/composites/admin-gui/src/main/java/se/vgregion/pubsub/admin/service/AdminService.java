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

package se.vgregion.pubsub.admin.service;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.PushSubscriber;

/**
 * Service interface for administering PuSH subscribers
 *
 */
public interface AdminService {

    Collection<PushSubscriber> getAllPushSubscribers();
    
    void createPushSubscriber(URI topic, URI callback, int leaseSeconds, String verifyToken, String secret) throws IOException, FailedSubscriberVerificationException;

    PushSubscriber getPushSubscriber(UUID id);

    void updatePushSubscriber(UUID id, URI topic, URI callback, int leaseSeconds, String verifyToken, String secret) throws IOException, FailedSubscriberVerificationException;

    void removePushSubscriber(UUID id);

    Collection<PolledPublisher> getAllPolledPublishers();
    
    void createPolledPublishers(URI url) throws IOException;
    
    PolledPublisher getPolledPublishers(UUID id);
    
    void updatePolledPublishers(UUID id, URI url) throws IOException;
    
    void removePolledPublishers(UUID id);
    
}
