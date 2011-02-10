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
