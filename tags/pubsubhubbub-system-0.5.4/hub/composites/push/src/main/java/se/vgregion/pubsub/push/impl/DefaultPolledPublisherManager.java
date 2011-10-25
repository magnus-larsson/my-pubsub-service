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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.repository.PolledPublisherRepository;

/**
 * {@inheritDoc}
 *
 * Assumes an external scheduler, like Springs task support
 */
public class DefaultPolledPublisherManager implements PolledPublisherManager {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPolledPublisherManager.class);
	
    private PubSubEngine pubSubEngine;
    private PolledPublisherRepository polledPublisherRepository;
    
    public DefaultPolledPublisherManager(PubSubEngine pubSubEngine, PolledPublisherRepository polledPublisherRepository) {
        this.pubSubEngine = pubSubEngine;
        this.polledPublisherRepository = polledPublisherRepository;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	@Transactional
	public void poll() {
		LOG.debug("Polling PolledPublishers");
		
		Collection<PolledPublisher> polledPublishers = polledPublisherRepository.findAll();
		
		for(PolledPublisher polledPublisher : polledPublishers) {
			polledPublisher.pollAndPublish(pubSubEngine);
		}
		LOG.debug("Done polling PolledPublishers");
	}

    
}
