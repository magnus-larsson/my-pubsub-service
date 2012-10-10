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

package se.vgregion.pubsub.push.repository;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import se.vgregion.dao.domain.patterns.repository.Repository;
import se.vgregion.pubsub.push.PushSubscriber;
    
/**
 * Repository for {@link PushSubscriber}s
 *
 */
public interface PushSubscriberRepository extends Repository<PushSubscriber, UUID> {

	List<PushSubscriber> findByTopic(URI topic);

	PushSubscriber findByTopicAndCallback(URI topic, URI callback);
}