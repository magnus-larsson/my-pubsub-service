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
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.services.FeedRetrievalService;

public class MockFeedRetrieverService implements FeedRetrievalService {

    private File tmpDirectory;
    private HttpEntity entity;

    public MockFeedRetrieverService(File tmpDirectory, HttpEntity entity) {
        this.tmpDirectory = tmpDirectory;
        this.entity = entity;
    }

    public File retrieve(URI url) throws IOException {
        File file = File.createTempFile("tmp", "tmp", tmpDirectory);
        OutputStream in = new FileOutputStream(file);
        entity.writeTo(in);
        in.close();
        
        return file;
        
    }
}
