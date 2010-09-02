package se.vgregion.push.processors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedRetriever {

    private final static Logger LOG = LoggerFactory.getLogger(FeedRetriever.class);
    
    private final BlockingQueue<String> retrieveQueue;
    private final BlockingQueue<String> distributionQueue;
    private final ExecutorService executor;
    private File feedDirectory;
    private File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));
    
    private HttpClient httpclient = new DefaultHttpClient();
    
    public FeedRetriever(BlockingQueue<String> retrieveQueue, BlockingQueue<String> distributionQueue, File feedDirectory) {
        this.retrieveQueue = retrieveQueue;
        this.distributionQueue = distributionQueue;
        this.feedDirectory = feedDirectory;
        
        executor = Executors.newFixedThreadPool(2);
    }

    public void start() {
        LOG.info("Starting FeedRetriever");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        String feed = retrieveQueue.take();
                        
                        String fileName = DigestUtils.md5Hex(feed);
                        
                        LOG.debug("Downloading feed {}", feed);

                        HttpGet httpget = new HttpGet(feed);
                        try {
                            HttpResponse response = httpclient.execute(httpget);
                            HttpEntity entity = response.getEntity();
                            
                            File destFile = new File(feedDirectory, fileName);
                            File tmpFile = new File(tmpDirectory, fileName);
                            FileOutputStream tmpFOS = new FileOutputStream(tmpFile);
                            entity.writeTo(tmpFOS);
                            tmpFOS.close();
                            
                            LOG.debug("Feed downloaded: {}", feed);
                            
                            if(tmpFile.renameTo(destFile)) {
                                LOG.warn("Feed successfully retrived, putting for distribution: {}", feed);
                                distributionQueue.put(feed);
                            } else {
                                LOG.warn("Failed to move tmp file \"{}\" to destination \"{}\"", tmpFile.getAbsolutePath(), destFile.getAbsolutePath());
                            }
                            
                        } catch (ClientProtocolException e) {
                            LOG.error("Failed to download feed: " + feed, e);
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

}
