package se.vgregion.pubsub.admin.service;

import java.net.URI;
import java.util.Collection;

import se.vgregion.pubsub.push.PushSubscriber;

public interface AdminService {

    Collection<PushSubscriber> getAllPushSubscribers();
    
    PushSubscriber createPushSubscriber(URI topic, URI callback, int leaseSeconds, String verifyToken);
    
}
