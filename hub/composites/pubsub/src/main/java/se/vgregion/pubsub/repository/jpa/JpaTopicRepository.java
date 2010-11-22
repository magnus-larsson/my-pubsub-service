package se.vgregion.pubsub.repository.jpa;

import java.net.URI;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.DefaultJpaRepository;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.impl.DefaultTopic;
import se.vgregion.pubsub.repository.TopicRepository;

public class JpaTopicRepository extends DefaultJpaRepository<Topic> implements TopicRepository {

    public JpaTopicRepository() {
        super(DefaultTopic.class);
    }

    @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
    public Topic findByUrl(URI url) {
        try {
            return (Topic) entityManager.createQuery("select l from DefaultTopic l where l.url = :url")
                .setParameter("url", url.toString()).getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }


}
