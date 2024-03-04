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
package org.apache.fineract.interoperation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "interop_identifier", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hathor_identifier_account", columnNames = { "account_id", "type" }),
        @UniqueConstraint(name = "uk_hathor_identifier_value", columnNames = { "type", "a_value", "sub_value_or_type" }) })
public class InteropIdentifier extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private SavingsAccount account;

    @Column(name = "type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InteropIdentifierType type;

    @Column(name = "a_value", nullable = false, length = 128)
    private String value;

    @Column(name = "sub_value_or_type", length = 128)
    private String subType;

    @Column(name = "created_by", nullable = false, length = 32)
    private String createdBy;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "modified_by", length = 32)
    private String modifiedBy;

    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    public InteropIdentifier(@NotNull SavingsAccount account, @NotNull InteropIdentifierType type, @NotNull String value, String subType,
            @NotNull String createdBy) {
        this.account = account;
        this.type = type;
        this.value = value;
        this.subType = subType;
        this.createdBy = createdBy;
        this.createdOn = DateUtils.getAuditLocalDateTime();
    }

    public InteropIdentifier(@NotNull SavingsAccount account, @NotNull InteropIdentifierType type, @NotNull String createdBy) {
        this(account, type, null, null, createdBy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof InteropIdentifier)) {
            return false;
        }

        InteropIdentifier that = (InteropIdentifier) o;

        if (!account.equals(that.account)) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }
        return Objects.equals(subType, that.subType);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (subType != null ? subType.hashCode() : 0);
        return result;
    }
}
