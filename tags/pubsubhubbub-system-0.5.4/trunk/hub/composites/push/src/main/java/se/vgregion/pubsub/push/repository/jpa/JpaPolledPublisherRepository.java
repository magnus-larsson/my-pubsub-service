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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.AbstractJpaRepository;
import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.DefaultPolledPublisher;
import se.vgregion.pubsub.push.impl.DefaultPushSubscriber;
import se.vgregion.pubsub.push.repository.PolledPublisherRepository;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

/**
 * Repository for {@link PolledPublisher}s based on JPA
 *
 */
public class JpaPolledPublisherRepository extends AbstractJpaRepository<PolledPublisher, UUID, UUID> implements PolledPublisherRepository {

    public JpaPolledPublisherRepository() {
        super(DefaultPolledPublisher.class);
    }
    
    @Transactional(propagation=Propagation.MANDATORY, readOnly=true)
    public PolledPublisher find(UUID id) {
        try {
            return (PolledPublisher) entityManager.createQuery("select l from " + type.getName() + " l " +
            		"where l.id = :id ")
                .setParameter("id", id)
                .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
        }
    }

    @Override
    public PolledPublisher findByUrl(URI url) {
        try {
            return (PolledPublisher) entityManager.createQuery("select l from " + type.getName() + " l " +
                        "where l.url = :url")
                .setParameter("url", url.toString())
                .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
        }
    }
}
