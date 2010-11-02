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

package se.vgregion.push.repository.jpa;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.NoResultException;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.DefaultJpaRepository;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;
    
@Repository
public class JpaSubscriptionRepository extends DefaultJpaRepository<Subscription> implements SubscriptionRepository {
    
    public JpaSubscriptionRepository() {
       setType(Subscription.class);
    }

    @Transactional(propagation=Propagation.REQUIRED)
    public void remove(Subscription entity) {
//        entityManager.remove(entityManager.merge(entity));
        super.remove(entity);
        
        // need to flush or DefaultPushService.subscribe() will cause
        // constraint violation
        entityManager.flush();
    }
    
    @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
    @SuppressWarnings("unchecked")
    public Collection<Subscription> findByTopic(URI topic) {
        try {
            return entityManager.createQuery("select l from Subscription l where l.topic = :topic")
                .setParameter("topic", topic.toString()).getResultList();
        } catch(NoResultException e) {
            return Collections.EMPTY_LIST;
        }
    }   

    @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
    public Subscription findByTopicAndCallback(URI topic, URI callback) {
        try {
            return (Subscription) entityManager.createQuery("select l from Subscription l where l.topic = :topic " +
            		"and l.callback = :callback")
        		.setParameter("topic", topic.toString())
        		.setParameter("callback", callback.toString())
                        .getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
    public Collection<Subscription> findForVerification(DateTime timeOut) {
        try {
            return entityManager.createQuery("select l from Subscription l where l.leaseTimeout < :leaseTimeout or l.needsVerification = true")
                .setParameter("leaseTimeout", timeOut.toDate()).getResultList();
        } catch(NoResultException e) {
            return Collections.EMPTY_LIST;
        }
    }   
}