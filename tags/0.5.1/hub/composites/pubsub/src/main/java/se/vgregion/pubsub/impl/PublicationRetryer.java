package se.vgregion.pubsub.impl;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;

public interface PublicationRetryer {

    void addRetry(Topic topic, Subscriber subscriber, Feed feed);
}
