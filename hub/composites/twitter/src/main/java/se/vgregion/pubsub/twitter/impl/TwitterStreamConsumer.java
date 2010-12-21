package se.vgregion.pubsub.twitter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import se.vgregion.pubsub.Feed;
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
                @SuppressWarnings("unchecked")
                Map<String, ?> tweet = mapper.readValue(line, Map.class);

                Feed feed = new FeedBuilder().id(url.toString())
                    .entry(new EntryBuilder().id(valueAsString(tweet.get("id_str"))).content(valueAsString(tweet.get("text"))).build()).build();

                pubSubEngine.publish(url, feed);
                line = reader.readLine();
            }
            
            if(stopped) {
                // try to shut down nicely
                responseEntity.consumeContent();
            }
        }
    }
    
    private String valueAsString(Object value) {
        if(value == null) return null;
        else return value.toString();
    }
}
