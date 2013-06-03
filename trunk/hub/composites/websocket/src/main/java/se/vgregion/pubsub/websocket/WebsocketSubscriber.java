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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.WebSocket;
import org.joda.time.DateTime;

import se.vgregion.docpublishing.v1.DocumentStatusType;
import se.vgregion.pubsub.*;

public class WebsocketSubscriber implements Subscriber, WebSocket  {

    private PubSubEngine pubSubEngine;
    
    private URI topic;
    private DateTime lastUpdate;
    private Outbound outbound;
    
    public WebsocketSubscriber(PubSubEngine pubSubEngine, URI topic) {
        this.pubSubEngine = pubSubEngine;
        this.topic = topic;
    }

    @Override
    public DateTime getLastUpdated() {
        return lastUpdate;
    }

    @Override
    public DateTime getTimeout() {
        return null;
    }

    @Override
    public URI getTopic() {
        return topic;
    }

    private ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public void publish(Feed feed, PushJms pushJms) throws PublicationFailedException {
        if(outbound != null) {
            try {
                StringWriter writer = new StringWriter();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", feed.getId());
                map.put("updated", feed.getUpdated());
                
                List<Map<String, Object>> entries = new ArrayList<Map<String,Object>>();
                for(Entry entry : feed.getEntries()) {
                    Map<String, Object> entryMap = new HashMap<String, Object>();
                    
                    for(Field field : entry.getFields()) {
                        entryMap.put(field.getName(), field.getContent());
                    }
                    
                    entries.add(entryMap);
                }
                map.put("entries", entries);
                
                mapper.writeValue(writer, map);

                outbound.sendMessage(writer.toString());
                if (pushJms != null) {
                    pushJms.send(feed, WebsocketSubscriber.class.getName(), DocumentStatusType.OK);
                }
                lastUpdate = new DateTime();
            } catch (IOException e) {
                // ignore
                e.printStackTrace();
            }
        }
    }

    @Override
    public void timedOut() {
        
    }

    @Override
    public void onConnect(Outbound outbound) {
        pubSubEngine.subscribe(this);
        this.outbound = outbound;
    }

    @Override
    public void onMessage(byte opcode, String data) {
        // ignore
    }

    @Override
    public void onFragment(boolean more, byte opcode, byte[] data, int offset, int length) {
        // ignore
    }

    @Override
    public void onMessage(byte opcode, byte[] data, int offset, int length) {
        // ignore
    }

    @Override
    public void onDisconnect() {
        pubSubEngine.unsubscribe(this);
    }
}
