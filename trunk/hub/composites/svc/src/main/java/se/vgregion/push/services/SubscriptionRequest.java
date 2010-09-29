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
    private SubscriptionMode mode;
    
    public SubscriptionRequest(SubscriptionMode mode, URI callback, URI topic, long leaseSeconds, String verifyToken) {
        this.mode = mode;
        this.callback = callback;
        this.topic = topic;
        this.leaseSeconds = leaseSeconds;
        this.verifyToken = verifyToken;
    }

    public SubscriptionMode getMode() {
        return mode;
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
            StringBuffer url = new StringBuffer();
            url.append(callback);
            
            if(callback.getQuery() != null) {
                url.append("&");
            } else {
                url.append("?");
            }
            
            if(mode.equals(SubscriptionMode.SUBSCRIBE)) {
                url.append("hub.mode=subscribe");
            } else {
                url.append("hub.mode=unsubscribe");
            }
            url.append("&hub.topic=").append(URLEncoder.encode(topic.toString(), "UTF-8"))
                .append("&hub.challenge=").append(URLEncoder.encode(challenge, "UTF-8"));
                
            if(leaseSeconds > 0) {
                url.append("&hub.lease_seconds=").append(leaseSeconds);
            }
            if(verifyToken != null) {
                url.append("&hub.verify_token=").append(URLEncoder.encode(verifyToken, "UTF-8"));
            }
            
            return URI.create(url.toString());
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }
    
}
