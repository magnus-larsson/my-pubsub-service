package se.vgregion.pubsub.push;

import java.io.IOException;
import java.net.URI;

import se.vgregion.dao.domain.patterns.entity.Entity;
import se.vgregion.pubsub.Subscriber;

public interface PushSubscriber extends Subscriber, Entity<Pair<URI, URI>>  {

    // 24 hours
    public static final int DEFAULT_LEASE_SECONDS = 60*60*24;

    URI getCallback();
    
    void verify(SubscriptionMode mode) throws IOException, FailedSubscriberVerificationException;

}
