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
