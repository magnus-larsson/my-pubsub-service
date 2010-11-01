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

package se.vgregion.push.services;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.types.Subscription;

public class MockSubscriptionRepository implements SubscriptionRepository {

    @Override
    public List<Subscription> findByTopic(URI url) {
        return null;
    }

    @Override
    public Subscription findByTopicAndCallback(URI topic, URI callback) {
        return null;
    }

    @Override
    public boolean contains(Subscription entity) {
        return false;
    }

    @Override
    public Subscription find(Long id) {
        return null;
    }

    @Override
    public List<Subscription> findAll() {
        return null;
    }

    @Override
    public void flush() {
        
    }

    @Override
    public Subscription merge(Subscription object) {
        return object;
    }

    @Override
    public Subscription persist(Subscription object) {
        return object;
    }

    @Override
    public void refresh(Subscription object) {
        
    }

    @Override
    public void remove(Subscription object) {
        
    }

    @Override
    public void remove(Long id) {
        
    }

    @Override
    public Subscription store(Subscription entity) {
        return entity;
    }

    @Override
    public Collection<Subscription> findTimedOutBy(DateTime timeOut) {
        return null;
    }

    @Override
    public Subscription findByPrimaryKey(Long pk) {
        return null;
    }

    @Override
    public void removeByPrimaryKey(Long pk) {
        
    }
}
