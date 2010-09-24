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

package se.vgregion.push.services;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedRetriever {

    private final static Logger LOG = LoggerFactory.getLogger(FeedRetriever.class);
    
    private BlockingQueue<String> retrieveQueue;
    private BlockingQueue<String> distributionQueue;
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    
    private FeedRetrieverService feedRetrieverService;
    
    public void start() {
        LOG.info("Starting FeedRetriever");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        String feed = retrieveQueue.take();

                        
                        try {
                            LOG.info("Retrieving feed: {}", feed);

                            feedRetrieverService.retrieve(feed);
                            
                            LOG.warn("Feed successfully retrived, putting for distribution: {}", feed);
                            distributionQueue.put(feed);
                        } catch (IOException e) {
                            LOG.error("Failed to download feed: " + feed, e);
                        }
                    } catch (InterruptedException e) {
                        // shutting down
                        break;
                    }
                }
                
                LOG.info("Stopped FeedRetriever");
            }
        });
    }

    public void stop() {
        LOG.info("Stopping FeedRetriever");
        executor.shutdownNow();
    }

    public BlockingQueue<String> getRetrieveQueue() {
        return retrieveQueue;
    }

    public BlockingQueue<String> getDistributionQueue() {
        return distributionQueue;
    }
    
    @Resource(name="retrieveQueue")
    public void setRetrieveQueue(BlockingQueue<String> retrieveQueue) {
        this.retrieveQueue = retrieveQueue;
    }

    @Resource(name="distributionQueue")
    public void setDistributionQueue(BlockingQueue<String> distributionQueue) {
        this.distributionQueue = distributionQueue;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
}
