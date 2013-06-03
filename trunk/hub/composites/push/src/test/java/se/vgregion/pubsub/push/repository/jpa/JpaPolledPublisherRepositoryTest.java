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

package se.vgregion.pubsub.push.repository.jpa;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.UnitTestConstants;
import se.vgregion.pubsub.push.impl.DefaultPolledPublisher;
import se.vgregion.pubsub.push.repository.PolledPublisherRepository;

@ContextConfiguration({"classpath:spring/pubsub-push-jpa.xml", "classpath:spring/test-jpa.xml"})
public class JpaPolledPublisherRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private PolledPublisherRepository polledPublisherRepository;
    
    private PolledPublisher expected;
    
    @Before
    @Transactional
    @Rollback(false)
    public void setup() {
    	polledPublisherRepository = applicationContext.getBean(PolledPublisherRepository.class);
        expected = new DefaultPolledPublisher(UnitTestConstants.TOPIC);
        polledPublisherRepository.persist(expected);
    }
    
    @Test
    @Transactional
    @Rollback
    public void find() {
    	PolledPublisher actual = polledPublisherRepository.find(expected.getId());
        
        Assert.assertEquals(expected.getUrl(), actual.getUrl());
    }

    @Test
    @Transactional
    @Rollback
    public void findByTopic() {
    	PolledPublisher actual = polledPublisherRepository.findByUrl(UnitTestConstants.TOPIC);
        
        Assert.assertEquals(expected.getUrl(), actual.getUrl());
    }
}
