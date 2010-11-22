package se.vgregion.pubsub.push;

import java.io.IOException;

import se.vgregion.dao.domain.patterns.entity.Entity;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.push.services.FailedSubscriberVerificationException;

public interface PushSubscriber extends Subscriber, Entity<Long>  {

    // 24 hours
    public static final int DEFAULT_LEASE_SECONDS = 60*60*24;

    
    void verify(SubscriptionMode mode) throws IOException, FailedSubscriberVerificationException;

}
