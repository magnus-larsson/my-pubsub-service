package se.vgregion.pubsub.push.impl;

import se.vgregion.pubsub.push.PushSubscriber;

public interface PushSubscriberManager {

    public void loadSubscribers();

    public void subscribe(PushSubscriber subscriber);

    public void unsubscribe(PushSubscriber subscriber);
    
    
    
    
}
