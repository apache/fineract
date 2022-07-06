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

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractAuditable;

/**
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
public abstract class AbstractAuditableCustom extends AbstractPersistableCustom implements Auditable<Long, Long, Instant> {

    private static final long serialVersionUID = 141481953116476081L;

    @Column(name = "createdby_id")
    private Long createdBy;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "lastmodifiedby_id")
    private Long lastModifiedBy;

    @Column(name = "lastmodified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Override
    public Optional<Long> getCreatedBy() {
        return Optional.ofNullable(this.createdBy);
    }

    @Override
    public void setCreatedBy(final Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Optional<Instant> getCreatedDate() {
        return null == this.createdDate ? Optional.empty() : Optional.of(this.createdDate.toInstant());
    }

    @Override
    public void setCreatedDate(final Instant createdDate) {
        this.createdDate = null == createdDate ? null : Date.from(createdDate);
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
    public Optional<Instant> getLastModifiedDate() {
        return null == this.lastModifiedDate ? Optional.empty() : Optional.of(this.lastModifiedDate.toInstant());
    }

    @Override
    public void setLastModifiedDate(final Instant lastModifiedDate) {
        this.lastModifiedDate = null == lastModifiedDate ? null : Date.from(lastModifiedDate);
    }
}
