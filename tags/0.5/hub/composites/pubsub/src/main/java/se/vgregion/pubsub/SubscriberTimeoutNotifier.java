package se.vgregion.pubsub;


public interface SubscriberTimeoutNotifier {

    void addSubscriber(Subscriber subscriber);

    void removeSubscriber(Subscriber subscriber);
    
}
