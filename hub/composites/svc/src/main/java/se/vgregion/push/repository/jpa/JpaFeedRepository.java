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

import javax.persistence.NoResultException;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.dao.domain.patterns.repository.db.jpa.DefaultJpaRepository;
import se.vgregion.push.repository.FeedRepository;
import se.vgregion.push.types.Entry;
import se.vgregion.push.types.Feed;
    
@Repository
public class JpaFeedRepository extends DefaultJpaRepository<Feed> implements FeedRepository {
    
    public JpaFeedRepository() {
       setType(Feed.class);
    }

    @Transactional
    public void deleteOutdatedEntries(Feed feed, DateTime date) {
        List<Entry> entries = feed.getEntries();
        for(int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if(!entry.isNewerThan(date)) {
                entries.remove(entry);
            }
        }

        store(feed);
    }

    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Feed persistOrUpdate(Feed feed) {
        Feed existing = findByUrl(feed.getUrl());
        if(existing != null) {
            // feed already exists, merge
            existing.merge(feed);
            
            return store(existing);
        } else {
            return persist(feed);
        }
    }
    
    @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
    public Feed findByUrl(URI url) {
        try {
            return (Feed) entityManager.createQuery("select l from Feed l where l.url = :url")
                .setParameter("url", url.toString()).getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }   
}