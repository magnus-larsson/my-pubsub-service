package se.vgregion.pubsub.push;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import se.vgregion.dao.domain.patterns.entity.Entity;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public interface PushSubscriber extends Subscriber, Entity<UUID>  {

    // 24 hours
    public static final int DEFAULT_LEASE_SECONDS = 60*60*24;

    URI getCallback();
    
    void verify(SubscriptionMode mode) throws IOException, FailedSubscriberVerificationException;

    void setSubscriberRepository(PushSubscriberRepository subscriberRepository);
}