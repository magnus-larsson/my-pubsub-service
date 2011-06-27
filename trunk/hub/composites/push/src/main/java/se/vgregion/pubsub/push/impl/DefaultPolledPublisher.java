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
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import nu.xom.ParsingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.content.AbstractParser;
import se.vgregion.pubsub.impl.DefaultFeed.FeedBuilder;
import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.PushSubscriber;

/**
 * Implementation of {@link PushSubscriber} with support for JPA
 *
 */
@Entity
@Table(name="PUSH_POLLED_PUBLISHERS")
public class DefaultPolledPublisher extends AbstractEntity<UUID> implements PolledPublisher {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPolledPublisher.class);
    
    @Id
    private UUID id;

    @Column(nullable=false, unique= true)
    private String url;
    
    // For JPA
    protected DefaultPolledPublisher() {
        
    }
    
    public DefaultPolledPublisher(URI url) {
        id = UUID.randomUUID();
        
        Assert.notNull(url);
        this.url = url.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return id;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUrl() {
        return URI.create(url);
    }
    
	@Override
	public void setUrl(URI url) {
		this.url = url.toString();
	}

	@Override
	public void pollAndPublish(PubSubEngine pubSubEngine) {
		LOG.debug("Polling {}", url);
		
		DefaultHttpClient httpClient = HttpUtil.getClient();
		
		HttpGet get = new HttpGet(getUrl());
		
		HttpResponse response = null;
		try {
			response = httpClient.execute(get);
			
			if(HttpUtil.successStatus(response)) {
				String feedXml = HttpUtil.readContent(response.getEntity());
				List<String> contentTypes = HttpUtil.getContentTypes(response);
				
				ContentType contentType = ContentType.sniff(contentTypes, feedXml);
				
				Feed feed = AbstractParser.create(contentType).parse(feedXml, contentType);
				
				LOG.debug("Publishing feed polled from {}", url);
				pubSubEngine.publish(getUrl(), feed);
			}
			
		} catch (Exception e) {
			LOG.warn("Error polling \"" + url + "\"", e);
		} finally {
			HttpUtil.closeQuitely(response);
		}
	}
}
