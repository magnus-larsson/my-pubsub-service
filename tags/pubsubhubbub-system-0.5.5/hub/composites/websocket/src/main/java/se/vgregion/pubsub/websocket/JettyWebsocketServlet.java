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

package se.vgregion.pubsub.websocket;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import se.vgregion.pubsub.PubSubEngine;

public class JettyWebsocketServlet extends WebSocketServlet  {

    @Override
    protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        PubSubEngine pubSubEngine = ctx.getBean(PubSubEngine.class);
        
        String topic = request.getParameter("topic");
        if(topic != null) {
            return new WebsocketSubscriber(pubSubEngine, URI.create(topic));
        } else {
            // invalid request
            return null;
        }
    }
}
