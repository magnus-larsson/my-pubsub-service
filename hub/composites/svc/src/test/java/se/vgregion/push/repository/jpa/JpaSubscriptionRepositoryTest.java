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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;


public class JpaSubscriptionRepositoryTest {

    private static final URI CALLBACK = URI.create("http://example.com/sub11");
    private static final URI TOPIC = URI.create("http://example.com/feed");
    
    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    private SubscriptionRepository repository = ctx.getBean(SubscriptionRepository.class);
    
    private Subscription sub1;
    
    @Before
    public void setup() {
        sub1 = repository.persist(new Subscription(TOPIC, CALLBACK));
    }
    
    @Test
    public void findByPk() {
        Subscription loaded = repository.find(sub1.getId());
        
        Assert.assertEquals(CALLBACK, loaded.getCallback());
    }

    @Test
    public void findByTopic() {
        Collection<Subscription> loaded = repository.findByTopic(TOPIC);
        
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(CALLBACK, loaded.iterator().next().getCallback());
    }
    
    @Test
    public void findByTopicNoneExisting() {
        Assert.assertEquals(0, repository.findByTopic(URI.create("http://dummy")).size());
    }

    @Test
    public void findByTopicAndCallback() {
        Subscription subscription = repository.findByTopicAndCallback(TOPIC, CALLBACK);
        
        Assert.assertNotNull(subscription);
        Assert.assertEquals(CALLBACK, subscription.getCallback());
    }

    @Test
    public void findByTopicAndCallbackNoneExisting() {
        Assert.assertNull(repository.findByTopicAndCallback(TOPIC, URI.create("http://dummy")));
    }
    
    @Test(expected=PersistenceException.class)
    public void persistDuplicates() {
        repository.persist(new Subscription(TOPIC, CALLBACK));
        repository.persist(new Subscription(TOPIC, CALLBACK));
    }
}
