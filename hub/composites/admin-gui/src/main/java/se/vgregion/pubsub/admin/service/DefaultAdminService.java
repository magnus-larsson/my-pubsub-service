/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.admin.service;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.DefaultPolledPublisher;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PolledPublisherRepository;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

/**
 * Implementation of the service interface for administering PuSh subscribers
 *
 */
@Service
public class DefaultAdminService implements AdminService {

    @Resource
    private PushSubscriberRepository subscriberRepository;

    @Resource
    private PushSubscriberManager pushSubscriberManager;

    @Resource
    private PolledPublisherRepository polledPublisherRepository;
    
    
    @Override
    @Transactional
    public void createPushSubscriber(URI topic, URI callback, String jmsLoggAddress, int leaseSeconds, String verifyToken, String secret, boolean active) throws IOException, FailedSubscriberVerificationException {
        pushSubscriberManager.subscribe(topic, callback, jmsLoggAddress, leaseSeconds, verifyToken, secret, active, false);
    }

    @Override
    @Transactional
    public void updatePushSubscriber(UUID id, URI topic, URI callback, String jmsLoggAddress, int leaseSeconds, String verifyToken, String secret, boolean active) throws IOException, FailedSubscriberVerificationException {
        removePushSubscriber(id);
        createPushSubscriber(topic, callback, jmsLoggAddress, leaseSeconds, verifyToken, secret, active);
    }
    
    @Override
    @Transactional
    public void removePushSubscriber(UUID id) {
        PushSubscriber subscriber = subscriberRepository.find(id);
        
        if(subscriber != null) {
        	pushSubscriberManager.unsubscribe(subscriber.getTopic(), subscriber.getCallback(), false);
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

    @Override
    @Transactional
	public Collection<PolledPublisher> getAllPolledPublishers() {
		return polledPublisherRepository.findAll();
	}

	@Override
	@Transactional
	public void createPolledPublishers(URI url) throws IOException {
		polledPublisherRepository.persist(new DefaultPolledPublisher(url));
	}

	@Override
	@Transactional
	public PolledPublisher getPolledPublishers(UUID id) {
		return polledPublisherRepository.find(id);
	}

	@Override
	@Transactional
	public void updatePolledPublishers(UUID id, URI url) throws IOException {
		PolledPublisher polledPublisher = getPolledPublishers(id);
		if(polledPublisher != null) {
			polledPublisher.setUrl(url);
		} else {
			throw new RuntimeException("Unknown publisher: " + id);
		}
	}

	@Override
	@Transactional
	public void removePolledPublishers(UUID id) {
		polledPublisherRepository.remove(id);
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

	public PolledPublisherRepository getPolledPublisherRepository() {
		return polledPublisherRepository;
	}

	public void setPolledPublisherRepository(
			PolledPublisherRepository polledPublisherRepository) {
		this.polledPublisherRepository = polledPublisherRepository;
	}
}
