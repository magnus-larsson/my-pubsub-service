package se.vgregion.push.inttest;

import se.vgregion.pubsub.Feed;


public interface SubscriberListener {

    void published(Feed feed);

    void verified();
}
