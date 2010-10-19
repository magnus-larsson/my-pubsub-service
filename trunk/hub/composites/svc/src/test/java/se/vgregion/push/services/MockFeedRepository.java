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
import java.util.List;

import org.joda.time.DateTime;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.types.Feed;

public class MockFeedRepository implements FeedRepository {

    @Override
    public boolean contains(Feed entity) {
        return false;
    }

    @Override
    public Feed find(Long id) {
        return null;
    }

    @Override
    public List<Feed> findAll() {
        return null;
    }

    @Override
    public void flush() {
    }

    @Override
    public Feed merge(Feed object) {
        return object;
    }

    @Override
    public Feed persist(Feed object) {
        return object;
    }

    @Override
    public void refresh(Feed object) {
        
    }

    @Override
    public void remove(Feed object) {
        
    }

    @Override
    public void remove(Long id) {
        
    }

    @Override
    public Feed store(Feed entity) {
        return entity;
    }

    @Override
    public void deleteOutdatedEntries(Feed feed, DateTime date) {
        
    }

    @Override
    public Feed findByUrl(URI url) {
        return null;
    }

    @Override
    public Feed persistOrUpdate(Feed feed) {
        return feed;
    }

}
