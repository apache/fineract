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
package org.apache.fineract.cob.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.data.domain.Persistable;

@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class AccountLock implements Persistable<Long>, Serializable {

    protected static final long serialVersionUID = 2272591907035824317L;

    @Id
    @Getter
    @Column(name = "loan_id", nullable = false)
    protected Long loanId;

    @Version
    @Getter
    @Column(name = "version")
    protected Long version;

    @Enumerated(EnumType.STRING)
    @Getter
    @Column(name = "lock_owner", nullable = false)
    protected LockOwner lockOwner;

    @Column(name = "lock_placed_on", nullable = false)
    @Getter
    protected OffsetDateTime lockPlacedOn;

    @Column(name = "error")
    @Getter
    protected String error;

    @Column(name = "stacktrace")
    @Getter
    protected String stacktrace;

    @Column(name = "lock_placed_on_cob_business_date")
    @Getter
    protected LocalDate lockPlacedOnCobBusinessDate;

    @Transient
    @Setter(value = AccessLevel.NONE)
    @Getter
    protected boolean isNew = true;

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public Long getId() {
        return getLoanId();
    }

    public AccountLock(Long loanId, LockOwner lockOwner, LocalDate lockPlacedOnCobBusinessDate) {
        this.loanId = loanId;
        this.lockOwner = lockOwner;
        this.lockPlacedOn = DateUtils.getAuditOffsetDateTime();
        this.lockPlacedOnCobBusinessDate = lockPlacedOnCobBusinessDate;
    }

    public void setError(String errorMessage, String stacktrace) {
        this.error = errorMessage;
        this.stacktrace = stacktrace;
    }

    public void setNewLockOwner(LockOwner newLockOwner) {
        this.lockOwner = newLockOwner;
        this.lockPlacedOn = DateUtils.getAuditOffsetDateTime();
    }
}
