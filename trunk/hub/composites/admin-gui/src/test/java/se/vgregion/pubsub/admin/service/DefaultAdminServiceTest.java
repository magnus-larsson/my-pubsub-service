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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PolledPublisherRepository;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class DefaultAdminServiceTest {

    private static final UUID ID = UUID.randomUUID();
    private static final URI TOPIC = URI.create("http://foo");
    private static final URI CALLBACK = URI.create("http://bar");
    private static final int LEASE = 123;
    private static final String TOKEN = "vt";
    private static final String SECRET = "sekrit";

    @Mock private PushSubscriberRepository subscriberRepository;
    @Mock private PushSubscriberManager pushSubscriberManager;
    @Mock private PolledPublisherRepository polledPublisherRepository;
    
    private DefaultAdminService adminService = new DefaultAdminService();
    
    @Before
    public void before() throws Exception {
    	MockitoAnnotations.initMocks(this);
    	
        adminService.setPushSubscriberManager(pushSubscriberManager);
        adminService.setSubscriberRepository(subscriberRepository);
        adminService.setPolledPublisherRepository(polledPublisherRepository);
    }
    
    @Test
    public void createPushSubscriber() throws Exception {
        adminService.createPushSubscriber(TOPIC, CALLBACK, null, LEASE, TOKEN, SECRET, true);

        Mockito.verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, null, LEASE, TOKEN, SECRET, true, false);
    }

    @Test
    public void updatePushSubscriber() throws Exception {
        adminService.updatePushSubscriber(ID, TOPIC, CALLBACK, null, LEASE, TOKEN, SECRET, true);
        
        Mockito.verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, null, LEASE, TOKEN, SECRET, true, false);
    }
    
    @Test
    public void deletePushSubscriber() throws Exception {
        PushSubscriber subscriber = mock(PushSubscriber.class);
        when(subscriber.getTopic()).thenReturn(TOPIC);
        when(subscriber.getCallback()).thenReturn(CALLBACK);
        
        when(subscriberRepository.find(ID)).thenReturn(subscriber);
        adminService.removePushSubscriber(ID);
        
        Mockito.verify(pushSubscriberManager).unsubscribe(TOPIC, CALLBACK, false);
    }

    @Test
    public void createPolledPublisher() throws Exception {
    	adminService.createPolledPublishers(TOPIC);
    	
    	Mockito.verify(polledPublisherRepository).persist(Mockito.any(PolledPublisher.class));
    }
    
    @Test
    public void updatePolledPublisher() throws Exception {
    	PolledPublisher polledPublisher = mock(PolledPublisher.class);
    	when(polledPublisherRepository.find(ID)).thenReturn(polledPublisher);
    	
    	adminService.updatePolledPublishers(ID, TOPIC);
    	
    	Mockito.verify(polledPublisher).setUrl(TOPIC);
    }
    
    @Test
    public void deletePolledPublisher() throws Exception {
    	adminService.removePolledPublishers(ID);
    	
    	Mockito.verify(polledPublisherRepository).remove(ID);
    }
}
