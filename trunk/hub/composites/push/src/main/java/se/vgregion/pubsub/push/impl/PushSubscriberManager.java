package se.vgregion.pubsub.push.impl;

import java.net.URI;

public interface PushSubscriberManager {

    public void loadSubscribers();

    public void subscribe(URI topicUrl, URI callback, int leaseSeconds, String verifyToken);

    public void unsubscribe(URI topic, URI callback);
}
