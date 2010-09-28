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

package se.vgregion.push.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

import se.vgregion.push.services.SubscriptionRequest;
import se.vgregion.push.services.SubscriptionService;
import se.vgregion.push.types.Subscription;

@Service
public class MockSubscriptionService implements SubscriptionService {

    @Override
    public List<Subscription> getAllSubscriptionsForFeed(URI feed) {
        return null;
    }

    @Override
    public void verify(SubscriptionRequest request) {
        
    }

    @Override
    public Subscription addSubscription(Subscription subscription) {
        return subscription;
    }
}
