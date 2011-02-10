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
