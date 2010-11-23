package se.vgregion.pubsub.repository.jpa;

import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.SubscriberTimeoutNotifier;

public class NoopSubscriberTimeoutNotifier implements SubscriberTimeoutNotifier {

    @Override
    public void addSubscriber(Subscriber subscriber) {
        
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        
    }

}
