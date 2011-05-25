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

package se.vgregion.pubsub.push.repository.jpa;

import java.net.URI;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.AbstractJpaRepository;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.DefaultPushSubscriber;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

/**
 * Repository for {@link PushSubscriber}s based on JPA
 *
 */
public class JpaPushSubscriberRepository extends AbstractJpaRepository<PushSubscriber, UUID, UUID> implements PushSubscriberRepository {

    public JpaPushSubscriberRepository() {
        super(DefaultPushSubscriber.class);
    }
    
    @Transactional(propagation=Propagation.MANDATORY, readOnly=true)
    public PushSubscriber find(UUID id) {
        try {
            return (PushSubscriber) entityManager.createQuery("select l from DefaultPushSubscriber l " +
            		"where l.id = :id ")
                .setParameter("id", id)
                .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
        }
    }

    @Override
    public PushSubscriber findByTopicAndCallback(URI topic, URI callback) {
        try {
            return (PushSubscriber) entityManager.createQuery("select l from DefaultPushSubscriber l " +
                        "where l.topic = :topic " +
                        "and l.callback = :callback ")
                .setParameter("topic", topic.toString())
                .setParameter("callback", callback.toString())
                .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
        }
    }

}
