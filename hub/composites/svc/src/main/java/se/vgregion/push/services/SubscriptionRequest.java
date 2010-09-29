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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class SubscriptionRequest {

    private URI callback;
    private URI topic;
    private long leaseSeconds;
    private String verifyToken;
    
    public SubscriptionRequest(URI callback, URI topic, long leaseSeconds, String verifyToken) {
        this.callback = callback;
        this.topic = topic;
        this.leaseSeconds = leaseSeconds;
        this.verifyToken = verifyToken;
    }

    public URI getCallback() {
        return callback;
    }

    public URI getTopic() {
        return topic;
    }

    public long getLeaseSeconds() {
        return leaseSeconds;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public URI getVerificationUrl(String challenge) {
        try {
            StringBuffer fullUrl = new StringBuffer();
            fullUrl.append(callback);
            
            if(callback.getQuery() != null) {
                fullUrl.append("&");
            } else {
                fullUrl.append("?");
            }
            
            fullUrl.append("hub.mode=subscribe")
                .append("&hub.topic=").append(URLEncoder.encode(topic.toString(), "UTF-8"))
                .append("&hub.challenge=").append(URLEncoder.encode(challenge, "UTF-8"));
                
            if(leaseSeconds > 0) {
                fullUrl.append("&hub.lease_seconds=").append(leaseSeconds);
            }
            if(verifyToken != null) {
                fullUrl.append("&hub.verify_token=").append(URLEncoder.encode(verifyToken, "UTF-8"));
            }
            
            return URI.create(fullUrl.toString());
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }
    
}
