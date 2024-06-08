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
package org.apache.fineract.infrastructure.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractAuditable;

/**
 * Make sure to modify the same class in the modules (fineract-investor, etc)
 *
 * A custom copy of {@link AbstractAuditable} to override the column names used on database. It also uses Instant
 * instead of LocalDateTime for created and modified.
 *
 * Abstract base class for auditable entities. Stores the audit values in persistent fields.
 *
 * @param <U>
 *            the auditing type. Typically some kind of user.
 * @param <PK>
 *            the type of the auditing type's identifier
 */
@MappedSuperclass
public abstract class AbstractAuditableCustom extends AbstractPersistableCustom<Long> implements Auditable<Long, Long, LocalDateTime> {

    private static final long serialVersionUID = 141481953116476081L;

    @Column(name = "createdby_id")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "lastmodifiedby_id")
    private Long lastModifiedBy;

    @Column(name = "lastmodified_date")
    private LocalDateTime lastModifiedDate;

    @Override
    public Optional<Long> getCreatedBy() {
        return Optional.ofNullable(this.createdBy);
    }

    @Override
    public void setCreatedBy(final Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Optional<LocalDateTime> getCreatedDate() {
        return Optional.ofNullable(this.createdDate);
    }

    @Override
    public void setCreatedDate(final LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Optional<Long> getLastModifiedBy() {
        return Optional.ofNullable(this.lastModifiedBy);
    }

    @Override
    public void setLastModifiedBy(final Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public Optional<LocalDateTime> getLastModifiedDate() {
        return Optional.ofNullable(this.lastModifiedDate);
    }

    @Override
    public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
