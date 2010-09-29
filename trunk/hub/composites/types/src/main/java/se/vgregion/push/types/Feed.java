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

package se.vgregion.push.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.persistence.Entity;

import se.vgregion.portal.core.domain.patterns.entity.AbstractEntity;

@Entity
public class Feed extends AbstractEntity<Feed, Long> {

    private URI url;
    private byte[] data;
    
    /* Make JPA happy */
    protected Feed() {
    }

    public Feed(URI url, byte[] data) {
        this.url = url;
        this.data = data;
    }

    public Feed(URI url, InputStream in) throws IOException {
        this.url = url;
        
        this.data = drain(in);
    }
    
    private byte[] drain(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        
        int read = in.read(b, 0, 1024);
        while(read > -1) {
            out.write(b, 0, read);
            read = in.read(b, 0, 1024);
        }
     
        return out.toByteArray();
    }

    
    public Long getId() {
        return null;
    }

    public URI getUrl() {
        return url;
    }

    public void writeTo(OutputStream out) throws IOException { 
        out.write(data);
    }
    
    public InputStream getContent() {
        return new ByteArrayInputStream(data);
    }
}
