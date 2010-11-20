package se.vgregion.pubsub.push;

import se.vgregion.dao.domain.patterns.entity.Entity;
import se.vgregion.pubsub.Subscriber;

public interface PushSubscriber extends Subscriber, Entity<PushSubscriber, Long>  {


}
