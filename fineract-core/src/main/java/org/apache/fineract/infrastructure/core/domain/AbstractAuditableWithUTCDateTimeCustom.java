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

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_BY_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE_DB_FIELD;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractAuditable;

/**
 * A custom copy of {@link AbstractAuditable} to override the column names used on database. It also uses OffsetDateTime
 * for created and modified. The datetimes will be converted from tenant TZ to UTC before storing (automatically
 * happens) and converted from System TZ to tenant TZ after fetching from DB
 *
 * Abstract base class for auditable entities. Stores the audit values in persistent fields.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractAuditableWithUTCDateTimeCustom<T extends Serializable> extends AbstractPersistableCustom<T>
        implements Auditable<Long, T, OffsetDateTime> {

    private static final long serialVersionUID = 141481953116476081L;

    @Column(name = CREATED_BY_DB_FIELD, nullable = false)
    @Setter(onMethod = @__(@Override))
    private Long createdBy;

    @Column(name = CREATED_DATE_DB_FIELD, nullable = false)
    @Setter(onMethod = @__(@Override))
    private OffsetDateTime createdDate;

    @Column(name = LAST_MODIFIED_BY_DB_FIELD, nullable = false)
    @Setter(onMethod = @__(@Override))
    private Long lastModifiedBy;

    @Column(name = LAST_MODIFIED_DATE_DB_FIELD, nullable = false)
    @Setter(onMethod = @__(@Override))
    private OffsetDateTime lastModifiedDate;

    @Override
    @NotNull
    public Optional<Long> getCreatedBy() {
        return Optional.ofNullable(this.createdBy);
    }

    @Override
    @NotNull
    public Optional<OffsetDateTime> getCreatedDate() {
        return Optional.ofNullable(createdDate);
    }

    @NotNull
    public OffsetDateTime getCreatedDateTime() {
        return getCreatedDate().orElseGet(DateUtils::getAuditOffsetDateTime);
    }

    @Override
    @NotNull
    public Optional<Long> getLastModifiedBy() {
        return Optional.ofNullable(this.lastModifiedBy);
    }

    @Override
    @NotNull
    public Optional<OffsetDateTime> getLastModifiedDate() {
        return Optional.ofNullable(lastModifiedDate);
    }

    @NotNull
    public OffsetDateTime getLastModifiedDateTime() {
        return getLastModifiedDate().orElseGet(DateUtils::getAuditOffsetDateTime);
    }
}
