package se.vgregion.pubsub.admin.service;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.impl.DefaultPushSubscriber;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

@Service
public class DefaultAdminService implements AdminService {

    @Resource
    private PushSubscriberRepository subscriberRepository;
    
    @Resource
    private PushSubscriberManager pushSubscriberManager;
    
    @Override
    @Transactional
    public void createPushSubscriber(URI topic, URI callback, int leaseSeconds, String verifyToken) {
        pushSubscriberManager.subscribe(topic, callback, leaseSeconds, verifyToken);
    }

    @Override
    public Collection<PushSubscriber> getAllPushSubscribers() {
        return subscriberRepository.findAll();
    }

}
