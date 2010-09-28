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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FeedRetriever {

    private final static Logger LOG = LoggerFactory.getLogger(FeedRetriever.class);
    
    @Resource(name="retrieveQueue")
    private BlockingQueue<RetrievalRequest> retrieveQueue;
    
    @Resource(name="distributionQueue")
    private BlockingQueue<DistributionRequest> distributionQueue;
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    
    private FeedRetrievalService feedRetrieverService;
    
    public FeedRetriever(BlockingQueue<RetrievalRequest> retrieveQueue,
            BlockingQueue<DistributionRequest> distributionQueue, FeedRetrievalService feedRetrieverService) {
        this.retrieveQueue = retrieveQueue;
        this.distributionQueue = distributionQueue;
        this.feedRetrieverService = feedRetrieverService;
    }

    public void start() {
        LOG.info("Starting FeedRetriever");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        RetrievalRequest request = retrieveQueue.take();
                        
                        try {
                            LOG.info("Retrieving feed: {}", request.getUrl());

                            File file = feedRetrieverService.retrieve(request.getUrl());
                            
                            LOG.warn("Feed successfully retrived, putting for distribution: {}", request.getUrl());
                            
                            DistributionRequest distributionRequest = new DistributionRequest(request.getUrl(), file);
                            
                            distributionQueue.put(distributionRequest);
                        } catch (IOException e) {
                            LOG.error("Failed to download feed: " + request.getUrl(), e);
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
}
