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

package se.vgregion.push.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedDistributor {

    private final static Logger LOG = LoggerFactory.getLogger(FeedDistributor.class);
    
    private final BlockingQueue<String> distributionQueue;
    private final ExecutorService executor;
    private File feedDirectory;
    
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    
    public FeedDistributor(BlockingQueue<String> distributionQueue, File feedDirectory) {
        this.distributionQueue = distributionQueue;
        this.feedDirectory = feedDirectory;
        executor = Executors.newFixedThreadPool(2);
        
        // not allowed to redirect when distributing
        httpclient.setRedirectHandler(new DontRedirectHandler());
    }

    private List<String> getSubscribers(String feed) {
        return Arrays.asList("http://localhost:8000/");
    }
    
    public void start() {
        LOG.info("Starting FeedDistributor");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        String feed = distributionQueue.take();
                        
                        List<String> subscribers = getSubscribers(feed);
                        
                        if(!subscribers.isEmpty()) {
                            String fileName = DigestUtils.md5Hex(feed);
                            File file = new File(feedDirectory, fileName);
                            byte[] buffer = new byte[(int)file.length()];
                            
                            LOG.debug("Distributing " + feed);
                            try {
                                FileInputStream fis = new FileInputStream(file);
                                fis.read(buffer, 0, buffer.length);

                                for(String subscriber : subscribers) {
                                    HttpPost post = new HttpPost(subscriber);
                                    
                                    // TODO might also use FileEntity, but that will not cache the data
                                    post.setEntity(new ByteArrayEntity(buffer));
                                    
                                    HttpResponse response = null;
                                    try {
                                        response = httpclient.execute(post);
                                        if(response.getStatusLine().getStatusCode() == 200) {
                                            LOG.debug("Succeeded distributing to subscriber {}", subscriber);
                                        } else {
                                            LOG.debug("Failed distributing to subscriber \"{}\" with error \"{}\"", subscriber, response.getStatusLine());
                                        }
                                    } catch(IOException e) {
                                        LOG.debug("Failed distributing to subscriber: " + subscriber, e);
                                    } finally {
                                        if(response != null) {
                                            if(response.getEntity() != null) {
                                                InputStream in = response.getEntity().getContent();
                                                if(in != null) in.close();
                                            }
                                        }
                                    }
                                    // TODO handle retries
                                }
                                LOG.info("Feed distributed to {} subscribers: {}", subscribers.size(), feed);
                            } catch (IOException e) {
                                LOG.debug("Failed to distribute feed: " + feed, e);
                            }
                        } else {
                            LOG.debug("No subscribers for published feed, ignoring: {}", feed);
                        }
                    } catch (InterruptedException e) {
                        // shutting down
                        break;
                    }
                }
                
                LOG.info("Stopped FeedDistributor");
            }
        });
    }

    public void stop() {
        LOG.info("Stopping FeedDistributor");
        executor.shutdownNow();
    }

}
