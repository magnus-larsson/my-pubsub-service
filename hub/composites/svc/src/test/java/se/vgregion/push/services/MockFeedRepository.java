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

import java.util.List;
import java.util.Map;

import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.types.Feed;

public class MockFeedRepository implements FeedRepository {

    @Override
    public void clear() {
        
    }

    @Override
    public boolean contains(Feed entity) {
        return false;
    }

    @Override
    public void deleteByPk(Long pk) {
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
    public List<Feed> findByNamedQuery(String queryName, Map<String, ? extends Object> args) {
        return null;
    }

    @Override
    public List<Feed> findByNamedQuery(String queryName, Object[] args) {
        return null;
    }

    @Override
    public Feed findByPk(Long pk) {
        return null;
    }

    @Override
    public Feed findInstanceByNamedQuery(String queryName, Object[] args) {
        return null;
    }

    @Override
    public Feed findInstanceByNamedQuery(String queryName, Map<String, ? extends Object> args) {
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

}