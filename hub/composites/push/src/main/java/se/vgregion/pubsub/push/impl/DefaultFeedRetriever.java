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

package se.vgregion.pubsub.push.impl;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.content.AbstractParser;
import se.vgregion.pubsub.push.FeedRetriever;

/**
 * Implementation of {@link FeedRetriever}
 *
 */
public class DefaultFeedRetriever implements FeedRetriever {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultFeedRetriever.class);

    private final PushSubscriberManager pushSubscriberManager;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public DefaultFeedRetriever(PushSubscriberManager pushSubscriberManager) {
        this.pushSubscriberManager = pushSubscriberManager;
    }

    /**
     * Start the retriever so that it will listen for incoming publications to download and publish
     */
    public void start() {
        LOG.info("Starting FeedRetriever");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                        	// wait for publications to retrieve
                            LOG.debug("FeedRetriever polling");
                            URI url = pushSubscriberManager.pollForRetrieval();
                            
                            // null if poll timed out, in which case we start polling again
                            if(url != null) {
                                try {
                                    LOG.info("FeedRetriever got URL {} for download, retrieving now", url);
                                    retrieve(url);
                                } catch (IOException e) {
                                    LOG.warn("Failed to download feed from " + url.toString(), e);
                                } catch (RuntimeException e) {
                                	LOG.warn("Failed to publish feed from " + url.toString(), e);
                                }
                            } else {
                                LOG.info("FeedRetriever timed out waiting, polling again");
                            }
                        } catch (InterruptedException e) {
                            try {
                                LOG.warn("FeedRetriever is getting interrupted and will stop");
                            } catch (NullPointerException npe) {
                                // logging might throw a NPE during app server shutdown, if so, ignore
                            }
                            // shutting down
                            break;
                        }
                    }
                    
                    try {
                        LOG.info("FeedRetriever stopping");   
                    } catch(NullPointerException e) {
                        // ignore, might happen during shutdown
                    }
                } catch(RuntimeException e) {
                    LOG.warn("FeedRetriever threw an exception, shutting down", e);
                    throw e;
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void retrieve(URI topicUrl) throws InterruptedException, IOException {
        LOG.info("Retrieving feed: {}", topicUrl);

        Feed feed = download(topicUrl);

        // remove fragment of the URL as it's only used during the retrieval phase
        topicUrl = URI.create(topicUrl.toString().replace("#" + topicUrl.getFragment(), ""));
        
        LOG.info("Feed successfully retrived, putting for distribution: {}", topicUrl);

        pushSubscriberManager.publish(topicUrl, feed);
        
        LOG.info("Feed published on topic: {}", topicUrl);
    }

    private Feed download(URI url) throws IOException {
        LOG.debug("Downloading feed {}", url);

        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;
        try {
            response = HttpUtil.getClient().execute(httpget);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Failed to download feed: " + response.getStatusLine());
            }

            HttpEntity entity = response.getEntity();

            Header[] contentTypes = response.getHeaders("Content-Type");

            String content = IOUtils.toString(entity.getContent());
            
            LOG.debug("Feed retrieved:");
            LOG.debug(content);
            
            // TODO is this a reasonable default?
            ContentType contentType = ContentType.ATOM;
            if (contentTypes.length > 0) {
                contentType = ContentType.fromValue(contentTypes[0].getValue());
                
                if(contentType != ContentType.ATOM && contentType != ContentType.RSS) {
                    // unknown content type, try sniffing
                    if(content.contains("<rss")) {
                        contentType = ContentType.RSS;
                    } else if(content.contains("<feed")) {
                        contentType = ContentType.ATOM;
                    } else {
                        throw new RuntimeException("Unknown content type: " + contentType);
                    }
                }
            }

            try {
                Feed feed = AbstractParser.create(contentType).parse(content, contentType);
                
                LOG.debug("Feed downloaded from {}; {}", url, feed);
                return feed;
            } catch (Exception e) {
                throw new IOException("Failed to parse feed", e);
            }
        } finally {
            HttpUtil.closeQuitely(response);
        }
    }

    /**
     * Stops the retriver
     */
    public void stop() {
        LOG.info("Stopping FeedRetriever");
        executor.shutdownNow();
    }
}
