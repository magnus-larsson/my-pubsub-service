package se.vgregion.pubsub.inttest;

import se.vgregion.pubsub.Feed;


public interface SubscriberListener {

    void published(Feed feed);

    void verified();
}
