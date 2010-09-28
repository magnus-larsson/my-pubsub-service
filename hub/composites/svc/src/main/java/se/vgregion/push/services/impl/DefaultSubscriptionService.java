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

package se.vgregion.push.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.vgregion.push.repository.SubscriptionRepository;
import se.vgregion.push.services.SubscriptionRequest;
import se.vgregion.push.services.SubscriptionService;
import se.vgregion.push.types.Subscription;

@Service
public class DefaultSubscriptionService implements SubscriptionService {

    private SubscriptionRepository subscriptionRepository;

    private HttpClient httpclient = new DefaultHttpClient();
    
    public DefaultSubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly=true)
    @Override
    public List<Subscription> getAllSubscriptionsForFeed(URI feed) {
        return subscriptionRepository.findByTopic(feed);
    }

    @Override
    public void verify(SubscriptionRequest request) throws IOException {
        String challenge = UUID.randomUUID().toString();
        
        
        StringBuffer fullUrl = new StringBuffer();
        fullUrl.append(request.getCallback())
            .append("?")
            .append("hub.mode=subscribe")
            .append("&hub.topic=").append(URLEncoder.encode(request.getTopic().toString(), "UTF-8"))
            .append("&hub.challenge=").append(challenge)
            .append("&hub.lease_seconds=").append(request.getLeaseSeconds());
        
        if(request.getVerifyToken() != null) {
            fullUrl.append("&hub.verify_token=").append(URLEncoder.encode(request.getVerifyToken(), "UTF-8"));
        }
        
        
        HttpGet get = new HttpGet(fullUrl.toString());
        
        HttpResponse response = httpclient.execute(get);
        
        if(successStatus(response.getStatusLine().getStatusCode())) {
            String returnedChallenge = readContent(response.getEntity());
            
            if(challenge.equals(returnedChallenge)) {
                // all okay
            } else {
                throw new IOException("Challenge did not match");
            }
            
        } else {
            throw new IOException("Failed to verify subscription, status: " + response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase());
        }
    }
    
    private String readContent(HttpEntity entity) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entity.writeTo(out);
        
        return out.toString("ASCII");
    }
    
    private boolean successStatus(int status) {
        return status >= 200 && status <300;
    }
}
