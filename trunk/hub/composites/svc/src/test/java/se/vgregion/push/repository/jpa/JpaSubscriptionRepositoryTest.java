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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;


public class JpaSubscriptionRepositoryTest {

    private ApplicationContext ctx = new ClassPathXmlApplicationContext("services-test.xml");
    private SubscriptionRepository dao = ctx.getBean(SubscriptionRepository.class);
    
    private Subscription sub1;
    
    @Before
    public void setup() {
        sub1 = dao.persist(new Subscription(URI.create("http://example.com/feed"), "http://example.com/sub11"));
    }
    
    @Test
    public void findByPk() {
        Subscription loaded = dao.findByPk(sub1.getId());
        
        Assert.assertEquals(sub1.getCallback(), loaded.getCallback());
    }

    @Test
    public void findByTopic() {
        List<Subscription> loaded = dao.findByTopic(URI.create("http://example.com/feed"));
        
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(sub1.getCallback(), loaded.get(0).getCallback());
    }

}
