package se.vgregion.pubsub;

public interface PubSubEventListener {

    void onSubscribe(Subscriber subscriber);

    void onUnsubscribe(Subscriber subscriber);
    
}
