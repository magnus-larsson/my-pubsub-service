package se.vgregion.pubsub.repository.jpa;

import se.vgregion.dao.domain.patterns.repository.db.jpa.DefaultJpaRepository;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.impl.DefaultFeed;
import se.vgregion.pubsub.repository.FeedRepository;

public class JpaFeedRepository extends DefaultJpaRepository<Feed> implements FeedRepository {

    public JpaFeedRepository() {
        super(DefaultFeed.class);
    }

}
