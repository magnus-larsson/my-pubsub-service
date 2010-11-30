package se.vgregion.pubsub.repository.jpa;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.AbstractJpaRepository;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.impl.DefaultFeed;
import se.vgregion.pubsub.repository.FeedRepository;

public class JpaFeedRepository extends AbstractJpaRepository<Feed, String, Long> implements FeedRepository {

    public JpaFeedRepository() {
        super(DefaultFeed.class);
    }

    @Transactional(propagation=Propagation.MANDATORY, readOnly=true)
    public Feed find(String id) {
        try {
            Feed feed = (Feed) entityManager.createQuery("select l from DefaultFeed l where l.feedId = :id")
                .setParameter("id", id)
                .getSingleResult();
            return feed;
        } catch(NoResultException e) {
            return null;
        } finally {
        }
    }
}
