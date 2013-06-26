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

package se.vgregion.pubsub.push.web;

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import se.vgregion.pubsub.push.PushSubscriber;
import se.vgregion.pubsub.push.UnitTestConstants;
import se.vgregion.pubsub.push.impl.PushSubscriberManager;
import se.vgregion.pubsub.push.repository.PushSubscriberRepository;

public class PushControllerTest {

	private static final URI CALLBACK = URI.create("http://example.com/callback");
	private static final URI TOPIC = URI.create("http://example.com/topic");

	private static final String SECRET = UnitTestConstants.SECRET;
	
	@Mock private PushSubscriberRepository pushSubscriberRepository;
	@Mock private PushSubscriberManager pushSubscriberManager;
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	
	private PushController controller = new PushController();
	
    @Before
    public void before() throws Exception {
    	MockitoAnnotations.initMocks(this);
    	
    	controller.setPushSubscriberManager(pushSubscriberManager);
    	controller.setSubscriberRepository(pushSubscriberRepository);
    }
    
    @Test
    public void subscribe() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, null, PushSubscriber.DEFAULT_LEASE_SECONDS, null, null, true, true);
    	Mockito.verifyNoMoreInteractions(response);
    }

    @Test
    public void subscribeWithLeaseSeconds() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	when(request.getParameter("hub.lease_seconds")).thenReturn("123");
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, null, 123, null, null, true, true);
    	Mockito.verifyNoMoreInteractions(response);
    }

    @Test
    public void subscribeWithInvalidLeaseSeconds() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	when(request.getParameter("hub.lease_seconds")).thenReturn("foo");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void subscribeWithLeaseSecondsAndToken() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	when(request.getParameter("hub.lease_seconds")).thenReturn("123");
    	when(request.getParameter("hub.verify_token")).thenReturn("vt");
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, null, 123, "vt", null, true, true);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void subscribeWithSecret() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	when(request.getParameter("hub.lease_seconds")).thenReturn("123");
    	when(request.getParameter("hub.secret")).thenReturn(SECRET);
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).subscribe(TOPIC, CALLBACK, null, 123, null, SECRET, true, true);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void subscribeWithoutVerify() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void subscribeWithoutCallback() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void subscribeWithoutTopic() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("subscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }

    @Test
    public void unsubscribe() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("unsubscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).unsubscribe(TOPIC, CALLBACK, true);
    	Mockito.verifyNoMoreInteractions(response);
    }

    @Test
    public void unsubscribeWithoutCallback() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("unsubscribe");
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void unsubscribeWithoutTopic() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("unsubscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.verify")).thenReturn("sync");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void unsubscribeWithoutVerify() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("unsubscribe");
    	when(request.getParameter("hub.callback")).thenReturn(CALLBACK.toString());
    	when(request.getParameter("hub.topic")).thenReturn(TOPIC.toString());
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void publish() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("publish");
    	when(request.getParameterValues("hub.url")).thenReturn(new String[]{TOPIC.toString()});
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).retrive(TOPIC);
    	Mockito.verifyNoMoreInteractions(response);
    	Mockito.verifyNoMoreInteractions(pushSubscriberManager);
    }

    @Test
    public void publishMultipleUrls() throws Exception {
    	URI otherTopic = URI.create("http://example.com/other");
    	
    	when(request.getParameter("hub.mode")).thenReturn("publish");
    	when(request.getParameterValues("hub.url")).thenReturn(new String[]{TOPIC.toString(), otherTopic.toString()});
    	
    	controller.post(request, response);
    	
    	verify(response).setStatus(204);
    	verify(pushSubscriberManager).retrive(TOPIC);
    	verify(pushSubscriberManager).retrive(otherTopic);
    	Mockito.verifyNoMoreInteractions(response);
    	Mockito.verifyNoMoreInteractions(pushSubscriberManager);
    }
    
    @Test
    public void publishWithoutUrl() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("publish");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void publishInvalidUrlSceheme() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("publish");
    	when(request.getParameterValues("hub.url")).thenReturn(new String[]{"foo://dummy"});
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500));
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    
    @Test
    public void withoutMode() throws Exception {
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void invalidMode() throws Exception {
    	when(request.getParameter("hub.mode")).thenReturn("foo");
    	
    	controller.post(request, response);
    	
    	verify(response).sendError(Mockito.eq(500), Mockito.anyString());
    	Mockito.verifyZeroInteractions(pushSubscriberManager);
    	Mockito.verifyNoMoreInteractions(response);
    }
    

}
