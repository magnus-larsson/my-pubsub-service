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

import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;

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
    public void publish(Feed feed) throws PublicationFailedException {
        if(outbound != null) {
            try {
                // TODO replace with real JSON encoding
                StringWriter writer = new StringWriter();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", feed.getId());
                map.put("updated", feed.getUpdated());
                
                List<Map<String, Object>> entries = new ArrayList<Map<String,Object>>();
                for(Entry entry : feed.getEntries()) {
                    Map<String, Object> entryMap = new HashMap<String, Object>();
                    entryMap.put("id", entry.getId());
                    entryMap.put("content", entry.getContent().toXML());
                    entries.add(entryMap);
                }
                map.put("entries", entries);
                
                mapper.writeValue(writer, map);
                outbound.sendMessage(writer.toString());
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
