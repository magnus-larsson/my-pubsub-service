package se.vgregion.pubsub.push.repository.jpa;

import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.DefaultJpaRepository;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.DefaultPushSubscriber;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class JpaPushSubscriberRepository extends DefaultJpaRepository<PushSubscriber> implements PushSubscriberRepository {

    public JpaPushSubscriberRepository() {
        super(DefaultPushSubscriber.class);
    }

}
