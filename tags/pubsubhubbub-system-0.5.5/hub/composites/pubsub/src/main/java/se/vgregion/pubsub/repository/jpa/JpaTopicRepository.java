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

package se.vgregion.pubsub.repository.jpa;

import java.net.URI;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.AbstractJpaRepository;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.impl.DefaultTopic;
import se.vgregion.pubsub.repository.TopicRepository;

public class JpaTopicRepository extends AbstractJpaRepository<Topic, URI, Long> implements TopicRepository {

    public JpaTopicRepository() {
        super(DefaultTopic.class);
    }

    @Transactional(propagation=Propagation.MANDATORY, readOnly=true)
    public Topic find(URI url) {
        try {
            return (Topic) entityManager.createQuery("select l from DefaultTopic l where l.url = :url")
                .setParameter("url", url.toString())
                .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
        }
    }
}
