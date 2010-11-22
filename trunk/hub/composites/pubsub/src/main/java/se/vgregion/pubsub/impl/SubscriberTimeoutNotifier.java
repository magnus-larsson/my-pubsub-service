package se.vgregion.pubsub.impl;

import se.vgregion.pubsub.Subscriber;

public interface SubscriberTimeoutNotifier {

    void addSubscriber(Subscriber subscriber);

    void removeSubscriber(Subscriber subscriber);
    
}
