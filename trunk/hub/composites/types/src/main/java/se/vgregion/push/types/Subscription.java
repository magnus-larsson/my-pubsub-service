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

package se.vgregion.push.types;

import java.net.URI;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

@Entity
@Table(
        uniqueConstraints=
            @UniqueConstraint(columnNames={"TOPIC", "CALLBACK"})
    )
public class Subscription extends AbstractEntity<Subscription, Long> {

    @Id
    @GeneratedValue
    private long id;
    
    @Column(nullable=false)
    private String callback;
    
    @Column
    private long leaseSeconds;
    
    @Column
    private String secret;

    @Column(nullable=false)
    private String topic;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    /* Make JPA happy */
    protected Subscription() {
    }

    public Subscription(URI topic, URI callback) {
        this.topic = topic.toString();
        this.callback = callback.toString();
    }

    public Subscription(URI topic, URI callback, long leaseSeconds, String secret) {
        this.topic = topic.toString();
        this.callback = callback.toString();
        this.leaseSeconds = leaseSeconds;
        this.secret = secret;
    }

    
    public Long getId() {
        return id;
    }
    
    public URI getCallback() {
        return URI.create(callback);
    }

    public URI getTopic() {
        return URI.create(topic);
    }

    public long getLeaseSeconds() {
        return leaseSeconds;
    }

    public String getSecret() {
        return secret;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
