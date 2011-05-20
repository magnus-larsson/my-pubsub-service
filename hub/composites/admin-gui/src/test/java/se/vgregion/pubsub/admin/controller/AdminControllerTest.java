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

package se.vgregion.pubsub.admin.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

import se.vgregion.pubsub.admin.service.AdminService;
import se.vgregion.pubsub.push.PushSubscriber;

public class AdminControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final URI TOPIC = URI.create("http://foo");
    private static final URI CALLBACK = URI.create("http://bar");
    private static final int LEASE = 123;
    private static final String TOKEN = "vt";

    
    private AdminController controller = new AdminController();
    private AdminService adminService = mock(AdminService.class);
    
    @Before
    public void before() throws Exception {
        controller.setAdminService(adminService);
    }
    
    @Test
    public void index() throws Exception {
        List<PushSubscriber> subscribers = Arrays.asList(mock(PushSubscriber.class), mock(PushSubscriber.class));
        when(adminService.getAllPushSubscribers()).thenReturn(subscribers);
        
        ModelAndView mav = controller.index();
        
        Assert.assertEquals("admin/index", mav.getViewName());
        Assert.assertEquals(subscribers, mav.getModel().get("pushSubscribers"));
    }

    @Test
    public void newSubscriber() throws Exception {
        ModelAndView mav = controller.newPushSubscriber();
        
        Assert.assertEquals("admin/edit", mav.getViewName());
    }

    @Test
    public void editSubscriber() throws Exception {
        UUID id = UUID.randomUUID();
        PushSubscriber subscriber = mock(PushSubscriber.class);
        when(adminService.getPushSubscriber(id)).thenReturn(subscriber);
        
        ModelAndView mav = controller.editPushSubscriber(id);
        
        Assert.assertEquals("admin/edit", mav.getViewName());
        Assert.assertEquals(subscriber, mav.getModel().get("subscriber"));
    }

    @Test
    public void createPushSubscriber() throws Exception {
        
        ModelAndView mav = controller.createPushSubscriber(TOPIC, CALLBACK, LEASE, TOKEN);

        Assert.assertEquals("redirect:", mav.getViewName());
        Mockito.verify(adminService).createPushSubscriber(TOPIC, CALLBACK, LEASE, TOKEN);
    }

    @Test
    public void updatePushSubscriber() throws Exception {
        
        ModelAndView mav = controller.updatePushSubscriber(ID, TOPIC, CALLBACK, LEASE, TOKEN, null);
        
        Assert.assertEquals("redirect:..", mav.getViewName());
        Mockito.verify(adminService).updatePushSubscriber(ID, TOPIC, CALLBACK, LEASE, TOKEN);
    }

    @Test
    public void deletePushSubscriber() throws Exception {
        
        ModelAndView mav = controller.updatePushSubscriber(ID, TOPIC, CALLBACK, LEASE, TOKEN, "some value");
        
        Assert.assertEquals("redirect:..", mav.getViewName());
        Mockito.verify(adminService).removePushSubscriber(ID);
    }
}
