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

package se.vgregion.pubsub.twitter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.impl.DefaultEntry.EntryBuilder;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;

public class TwitterStreamConsumer implements Runnable {

    private PubSubEngine pubSubEngine;
    
    private URI url;
    
    private boolean stopped = false;

    private Executor executor;
    
    private String username;
    private String password;
    
    public TwitterStreamConsumer(URI url, PubSubEngine pubSubEngine, String username, String password, Executor executor) {
        this.url = url;
        this.pubSubEngine = pubSubEngine;
        this.username = username;
        this.password = password;
        this.executor = executor;
    }
    
    public void start() {
        executor.execute(this);
    }
    
    public void stop() {
        stopped = true;
    }
    
    @Override
    public void run() {
        while(!stopped) {
            try {
                consume();
            } catch (IOException e) {
                try {
                    // I/O problem, sleep for a while
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
    }

    private void consume() throws IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        client.addRequestInterceptor(new PreemptiveBasicAuth(username, password), 0);
        
        // reconnect directly on normal disconnects
        while(!stopped) {
            HttpUriRequest request;
            if(url.toString().startsWith(TwitterPublisher.FILTER_URL)) {
                String filter = url.getFragment();
                
                HttpPost post = new HttpPost(TwitterPublisher.FILTER_URL);
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Arrays.asList(new BasicNameValuePair("track", filter)));
                post.setEntity(entity);
                
                request = post;
            } else {
                request = new HttpGet(TwitterPublisher.SAMPLE_URL);
            }
            HttpResponse response = client.execute(request);
            
            if(response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("HTTP failure: "+ response.getStatusLine());
            }
            
            HttpEntity responseEntity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
            
            ObjectMapper mapper = new ObjectMapper();
            
            String line = reader.readLine();
            while(line != null && !stopped) {
                JsonNode tweet = mapper.readValue(line, JsonNode.class);

                EntryBuilder entryBuilder = new EntryBuilder()
                    .id(valueAsString(tweet.get("id_str")));
                entryBuilder.field(new JsonNodeField("tweet", tweet));
                
                Feed feed = new FeedBuilder(ContentType.JSON)
                    .id(url.toString())
                    .entry(entryBuilder.build()).build();

                pubSubEngine.publish(url, feed);
                line = reader.readLine();
            }
            
            if(stopped) {
                // try to shut down nicely
                responseEntity.consumeContent();
            }
        }
    }
    
    private static class JsonNodeField implements Field {

        private String name;
        private JsonNode node;
        
        public JsonNodeField(String name, JsonNode node) {
            this.name = name;
            this.node = node;
        }

        @Override
        public String getNamespace() {
            return "";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getContent() {
            return node.getValueAsText();
        }

        @Override
        public List<Field> getFields() {
            List<Field> fields = new ArrayList<Field>();
            Iterator<String> fieldNames = node.getFieldNames();
            while(fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                fields.add(new JsonNodeField(fieldName, node.get(fieldName)));
            }
            
            return fields;
        }

        @Override
        public String toString() {
            return "\"" + name + "\" : " + node.toString();
        }
    }
    
    private String valueAsString(Object value) {
        if(value == null) return null;
        else return value.toString();
    }
}
