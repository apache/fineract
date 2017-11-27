/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.data.AccessTokenData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

@Entity
@Table(name = "twofactor_access_token",
        uniqueConstraints = {@UniqueConstraint(columnNames = { "token", "appuser_id" }, name = "token_appuser_UNIQUE")})
public class TFAccessToken extends AbstractPersistableCustom<Long> {

    @Column(name = "token", nullable = false, length = 32)
    private String token;

    @ManyToOne
    @JoinColumn(name = "appuser_id", nullable = false)
    private AppUser user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_from", nullable = false)
    private Date validFrom;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_to", nullable = false)
    private Date validTo;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public TFAccessToken() {
    }

    public static TFAccessToken create(String token, AppUser user, int tokenLiveTimeInSec) {
        DateTime validFrom = DateUtils.getLocalDateTimeOfTenant().toDateTime();
        DateTime validTo = validFrom.plusSeconds(tokenLiveTimeInSec);

        return new TFAccessToken(token, user, validFrom.toDate(), validTo.toDate(), true);
    }

    public TFAccessToken(String token, AppUser user, Date validFrom, Date validTo, boolean enabled) {
        this.token = token;
        this.user = user;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.enabled = enabled;
    }

    public boolean isValid() {
        return this.enabled && isDateInTheFuture(getValidToDate())
                && isDateInThePast(getValidFromDate());
    }

    public AccessTokenData toTokenData() {
        return new AccessTokenData(this.token, getValidFromDate().toDateTime(),
                getValidToDate().toDateTime());
    }

    public String getToken() {
        return token;
    }

    public AppUser getUser() {
        return user;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getValidFromDate() {
        return new LocalDateTime(validFrom);
    }

    public LocalDateTime getValidToDate() {
        return new LocalDateTime(validTo);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean isDateInTheFuture(LocalDateTime dateTime) {
        return dateTime.isAfter(DateUtils.getLocalDateTimeOfTenant());
    }

    private boolean isDateInThePast(LocalDateTime dateTime) {
        return dateTime.isBefore(DateUtils.getLocalDateTimeOfTenant());
    }
}
