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

package se.vgregion.push.repository.jpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
    
public class FileSystemFeedRepository implements FeedRepository {
    
    private final static Logger LOG = LoggerFactory.getLogger(FileSystemFeedRepository.class);
    
    private File feedDirectory;
    private File tmpDirectory;

    
    public FileSystemFeedRepository(File feedDirectory) {
        this(feedDirectory, new File(System.getProperty("java.io.tmpdir")));
    }

    public FileSystemFeedRepository(File feedDirectory, File tmpDirectory) {
        this.feedDirectory = feedDirectory;
        this.tmpDirectory = tmpDirectory;
        
        if(!feedDirectory.exists()) {
            LOG.info("Creating feed directory at \"{}\"", feedDirectory.getAbsolutePath());
            feedDirectory.mkdirs();
        }
        if(!tmpDirectory.exists()) {
            LOG.info("Creating tmp directory at \"{}\"", tmpDirectory.getAbsolutePath());
            tmpDirectory.mkdirs();
        }
    }

    public Feed findByUrl(URI url) throws IOException {
        String fileName = DigestUtils.md5Hex(url.toString());
        File destFile = new File(feedDirectory, fileName);
        
        if(destFile.exists()) {
            // TODO cache inputstream!
            return new Feed(url, ContentType.ATOM, new FileInputStream(destFile));
        } else {
            return null;
        }
    }   

    @Override
    public Feed persist(Feed feed) throws IOException {
        String fileName = DigestUtils.md5Hex(feed.getUrl().toString());
        
        File destFile = new File(feedDirectory, fileName);
        File tmpFile = new File(tmpDirectory, fileName);
        FileOutputStream tmpFOS = new FileOutputStream(tmpFile);
        feed.writeTo(tmpFOS);
        tmpFOS.close();

        if (!tmpFile.renameTo(destFile)) {
            throw new IOException("Failed to move tmp file \"" + tmpFile.getAbsolutePath() + "\" to destination \""
                    + destFile.getAbsolutePath() + "\"");
        }

        return feed;
    }   
}