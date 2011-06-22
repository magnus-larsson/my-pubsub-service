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

package se.vgregion.pubsub.repository.jpa;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.UnitTestConstants;
import se.vgregion.pubsub.impl.DefaultTopic;
import se.vgregion.pubsub.repository.TopicRepository;

@ContextConfiguration({"classpath:spring/pubsub-jpa.xml", "classpath:spring/test-jpa.xml"})
public class JpaTopicRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private TopicRepository topicRepository;
    
    private DefaultTopic topic1;
    
    @Before
    public void setup() {
        topicRepository = applicationContext.getBean(TopicRepository.class);
        topic1 = new DefaultTopic(UnitTestConstants.TOPIC, null);
        topicRepository.persist(topic1);
    }
    
    @Test
    @Transactional
    @Rollback
    public void find() {
        Topic topic = topicRepository.find(UnitTestConstants.TOPIC);
        
        Assert.assertEquals(UnitTestConstants.TOPIC, topic.getUrl());
    }
}
