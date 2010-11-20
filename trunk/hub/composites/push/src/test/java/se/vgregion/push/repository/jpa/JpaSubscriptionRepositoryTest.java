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

import java.net.URI;
import java.util.Collection;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.UnitTestConstants;
import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;

@ContextConfiguration("classpath:services-test.xml")
public class JpaSubscriptionRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private SubscriptionRepository repository;
    
    private Subscription sub1;
    
    @Before
    public void setup() {
        repository = applicationContext.getBean(SubscriptionRepository.class);
        sub1 = repository.persist(new Subscription(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK));
    }
    
    @Test
    @Transactional
    @Rollback
    public void findByPk() {
        Subscription loaded = repository.find(sub1.getId());
        
        Assert.assertEquals(UnitTestConstants.CALLBACK, loaded.getCallback());
    }

    @Test
    @Transactional
    @Rollback
    public void findByTopic() {
        Collection<Subscription> loaded = repository.findByTopic(UnitTestConstants.TOPIC);
        
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(UnitTestConstants.CALLBACK, loaded.iterator().next().getCallback());
    }
    
    @Test
    @Transactional
    @Rollback
    public void findByTopicNoneExisting() {
        Assert.assertEquals(0, repository.findByTopic(URI.create("http://dummy")).size());
    }

    @Test
    @Transactional
    @Rollback
    public void findByTopicAndCallback() {
        Subscription subscription = repository.findByTopicAndCallback(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK);
        
        Assert.assertNotNull(subscription);
        Assert.assertEquals(UnitTestConstants.CALLBACK, subscription.getCallback());
    }

    @Test
    @Transactional
    @Rollback
    public void findByTopicAndCallbackNoneExisting() {
        Assert.assertNull(repository.findByTopicAndCallback(UnitTestConstants.TOPIC, URI.create("http://dummy")));
    }
    
    @Test(expected=PersistenceException.class)
    @Transactional
    @Rollback
    public void persistDuplicates() {
        repository.persist(new Subscription(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK));
        repository.persist(new Subscription(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK));
    }
}
