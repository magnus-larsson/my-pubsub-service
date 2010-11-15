package se.vgregion.push.inttest;

import se.vgregion.push.types.Feed;

public interface SubscriberListener {

    void published(Feed feed);

    void verified();
}
