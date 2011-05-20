package se.vgregion.pubsub.admin.service;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

@Service
public class DefaultAdminService implements AdminService {

    @Resource
    private PubSubEngine pubSubEngine;
    
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
    @Transactional
    public void updatePushSubscriber(UUID id, URI topic, URI callback, int leaseSeconds, String verifyToken) {
        removePushSubscriber(id);
        createPushSubscriber(topic, callback, leaseSeconds, verifyToken);
    }
    
    @Override
    @Transactional
    public void removePushSubscriber(UUID id) {
        PushSubscriber subscriber = subscriberRepository.find(id);
        
        if(subscriber != null) {
            pubSubEngine.unsubscribe(subscriber);
            subscriberRepository.remove(subscriber);
        }
    }
    
    @Override
    @Transactional
    public Collection<PushSubscriber> getAllPushSubscribers() {
        return subscriberRepository.findAll();
    }

    @Override
    @Transactional
    public PushSubscriber getPushSubscriber(UUID id) {
        return subscriberRepository.find(id);
    }

    public PubSubEngine getPubSubEngine() {
        return pubSubEngine;
    }

    public void setPubSubEngine(PubSubEngine pubSubEngine) {
        this.pubSubEngine = pubSubEngine;
    }

    public PushSubscriberRepository getSubscriberRepository() {
        return subscriberRepository;
    }

    public void setSubscriberRepository(PushSubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public PushSubscriberManager getPushSubscriberManager() {
        return pushSubscriberManager;
    }

    public void setPushSubscriberManager(PushSubscriberManager pushSubscriberManager) {
        this.pushSubscriberManager = pushSubscriberManager;
    }
}
