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

import org.joda.time.DateTime;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

@Entity
@Table(
        uniqueConstraints=
            @UniqueConstraint(columnNames={"TOPIC", "CALLBACK"})
    )
public class Subscription extends AbstractEntity<Subscription, Long> {

    private static final int MAX_RENEWAL_TRIES = 3;

    // 24 hours
    public static final int DEFAULT_LEASE_SECONDS = 60*60*24;
    
    @Id
    @GeneratedValue
    private long id;
    
    @Column(nullable=false)
    private String callback;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date leaseRenewedAt;

    @Column
    private int leaseSeconds;
    
    @Column
    private String secret;

    @Column(nullable=false)
    private String topic;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column
    private String verifyToken;
    
    @Column
    private int failedVerifications = 0;

    @Column
    private boolean needsVerification = false;

    
    /* Make JPA happy */
    protected Subscription() {
    }

    public Subscription(URI topic, URI callback) {
        this(topic, callback, DEFAULT_LEASE_SECONDS, null, null);
    }

    public Subscription(URI topic, URI callback, int leaseSeconds, String secret, String verifyToken) {
        this.topic = topic.toString();
        this.callback = callback.toString();
        this.leaseSeconds = leaseSeconds;
        this.leaseRenewedAt = new DateTime().toDate();
        this.secret = secret;
        this.verifyToken = verifyToken;
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
    
    public int getLeaseSeconds() {
        return leaseSeconds;
    }

    public DateTime getLeaseRenewedAt() {
        return new DateTime(leaseRenewedAt);
    }

    public void setLeaseRenewedAt(DateTime leaseRenewedAt) {
        this.leaseRenewedAt = leaseRenewedAt.toDate();
    }
    
    public String getSecret() {
        return secret;
    }
    
    public String getVerifyToken() {
        return verifyToken;
    }

    public DateTime getLastUpdated() {
        return new DateTime(lastUpdated);
    }
    
    protected int getFailedVerifications() {
        return failedVerifications;
    }

    public void successfullyVerified() {
        this.failedVerifications = 0;
        this.needsVerification = false;
        setLeaseRenewedAt(new DateTime());
    }

    public void failedVerified() {
        this.failedVerifications++;
    }
    
    public void markForVerification() {
        this.needsVerification = true;
    }

    public boolean isNeedsVerification() {
        return needsVerification || getLeaseRenewedAt().plusSeconds(leaseSeconds).isBefore(new DateTime());
    }
    
    public boolean isFailed() {
        return this.failedVerifications > MAX_RENEWAL_TRIES;
    }

    
    public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated.toDate();
    }
}
