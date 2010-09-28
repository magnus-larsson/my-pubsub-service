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

package se.vgregion.push.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.services.FeedRetrievalService;

@Service
public class DefaultFeedRetrieverService implements FeedRetrievalService {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultFeedRetrieverService.class);

    private File feedDirectory;

    private File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));

    private HttpClient httpclient = new DefaultHttpClient();

    public DefaultFeedRetrieverService(File feedDirectory) {
        this.feedDirectory = feedDirectory;
    }
    
    public DefaultFeedRetrieverService(File feedDirectory, File tmpDirectory) {
        this.feedDirectory = feedDirectory;
        this.tmpDirectory = tmpDirectory;
    }

    /* (non-Javadoc)
     * @see se.vgregion.push.services.impl.FeedRetrievalService#retrieve(se.vgregion.push.services.RetrievalRequest)
     */
    @Transactional
    public File retrieve(URI url) throws IOException {
        if(!feedDirectory.exists()) {
            LOG.info("Creating feed directory at \"{}\"", feedDirectory.getAbsolutePath());
            feedDirectory.mkdirs();
        }
        
        String fileName = DigestUtils.md5Hex(url.toString());

        LOG.debug("Downloading feed {}", url);

        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        
        if(response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Failed to download feed: " + response.getStatusLine());
        }
        
        HttpEntity entity = response.getEntity();

        File destFile = new File(feedDirectory, fileName);
        File tmpFile = new File(tmpDirectory, fileName);
        FileOutputStream tmpFOS = new FileOutputStream(tmpFile);
        entity.writeTo(tmpFOS);
        tmpFOS.close();
        LOG.debug("Feed downloaded: {}", url);

        if (tmpFile.renameTo(destFile)) {
            LOG.warn("Feed successfully retrived, putting for distribution: {}", url);
            
            return destFile;
        } else {
            throw new IOException("Failed to move tmp file \"" + tmpFile.getAbsolutePath() + "\" to destination \""
                    + destFile.getAbsolutePath() + "\"");
        }
    }

    public File getFeedDirectory() {
        return feedDirectory;
    }

    @Resource(name = "feedDirectory")
    public void setFeedDirectory(File feedDirectory) {
        this.feedDirectory = feedDirectory;
    }

    public File getTmpDirectory() {
        return tmpDirectory;
    }

    public void setTmpDirectory(File tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
    }
}
