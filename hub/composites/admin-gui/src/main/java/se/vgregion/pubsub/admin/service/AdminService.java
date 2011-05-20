package se.vgregion.pubsub.admin.service;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import se.vgregion.pubsub.push.PushSubscriber;

public interface AdminService {

    Collection<PushSubscriber> getAllPushSubscribers();
    
    void createPushSubscriber(URI topic, URI callback, int leaseSeconds, String verifyToken);

    PushSubscriber getPushSubscriber(UUID id);

    void updatePushSubscriber(UUID id, URI topic, URI callback, int leaseSeconds, String verifyToken);

    void removePushSubscriber(UUID id);
    
}
