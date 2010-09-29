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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;

import se.vgregion.push.services.PushService;
import se.vgregion.push.services.SubscriptionRequest;
import se.vgregion.push.types.ContentType;
import se.vgregion.push.types.Feed;
import se.vgregion.push.types.Subscription;

public class MockFeedRetrieverService implements PushService {

    private File tmpDirectory;
    private HttpEntity entity;

    public MockFeedRetrieverService(File tmpDirectory, HttpEntity entity) {
        this.tmpDirectory = tmpDirectory;
        this.entity = entity;
    }

    public Feed retrieve(URI url) throws IOException {
        return new Feed(url, ContentType.ATOM, entity.getContent());
        
    }

    @Override
    public List<Subscription> getAllSubscriptionsForFeed(URI feed) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscription subscribe(Subscription subscription) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscription unsubscribe(Subscription subscription) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verify(SubscriptionRequest request) throws IOException {
        // TODO Auto-generated method stub
        
    }
}
