package se.vgregion.pubsub.push.repository.jpa;

import java.net.URI;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.AbstractJpaRepository;
import se.vgregion.pubsub.push.Pair;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.DefaultPushSubscriber;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class JpaPushSubscriberRepository extends AbstractJpaRepository<PushSubscriber, Pair<URI, URI>, Long> implements PushSubscriberRepository {

    public JpaPushSubscriberRepository() {
        super(DefaultPushSubscriber.class);
    }
    
    @Transactional(propagation=Propagation.MANDATORY, readOnly=true)
    public PushSubscriber find(Pair<URI, URI> urls) {
        try {
            return (PushSubscriber) entityManager.createQuery("select l from DefaultPushSubscriber l " +
            		"where l.topic = :topic " +
            		"and l.callback = :callback")
                .setParameter("topic", urls.getFirst().toString())
                .setParameter("callback", urls.getSecond().toString())
                .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
        }
    }

}
