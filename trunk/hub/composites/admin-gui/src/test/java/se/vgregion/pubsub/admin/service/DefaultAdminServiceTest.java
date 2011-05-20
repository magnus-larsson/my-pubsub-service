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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.admin.service.AdminService;
import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class DefaultAdminServiceTest {

    private static final UUID ID = UUID.randomUUID();
    private static final URI TOPIC = URI.create("http://foo");
    private static final URI CALLBACK = URI.create("http://bar");
    private static final int LEASE = 123;
    private static final String TOKEN = "vt";

    private PubSubEngine pubSubEngine = mock(PubSubEngine.class);
    private PushSubscriberRepository subscriberRepository = mock(PushSubscriberRepository.class);
    private PushSubscriberManager pushSubscriberManager = mock(PushSubscriberManager.class);
    
    private DefaultAdminService adminService = new DefaultAdminService();
    
    @Before
    public void before() throws Exception {
        adminService.setPubSubEngine(pubSubEngine);
        adminService.setPushSubscriberManager(pushSubscriberManager);
        adminService.setSubscriberRepository(subscriberRepository);
    }
    
    @Test
    public void createPushSubscriber() throws Exception {
        adminService.createPushSubscriber(TOPIC, CALLBACK, LEASE, TOKEN);

        Mockito.verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, LEASE, TOKEN);
    }

    @Test
    public void updatePushSubscriber() throws Exception {
        adminService.updatePushSubscriber(ID, TOPIC, CALLBACK, LEASE, TOKEN);
        
        Mockito.verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, LEASE, TOKEN);
    }
    
    @Test
    public void deletePushSubscriber() throws Exception {
        PushSubscriber subscriber = mock(PushSubscriber.class);
        
        when(subscriberRepository.find(ID)).thenReturn(subscriber);
        adminService.removePushSubscriber(ID);
        
        Mockito.verify(pubSubEngine).unsubscribe(subscriber);
        Mockito.verify(subscriberRepository).remove(subscriber);
    }
}
