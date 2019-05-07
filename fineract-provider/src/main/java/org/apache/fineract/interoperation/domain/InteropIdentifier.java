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

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "interop_identifier", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hathor_identifier_account", columnNames = {"account_id", "type"}),
        @UniqueConstraint(name = "uk_hathor_identifier_value", columnNames = {"type", "a_value", "sub_value_or_type"})
})
public class InteropIdentifier extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private SavingsAccount account;

    @Column(name = "type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InteropIdentifierType type;

    @Column(name = "a_value", nullable = false, length = 128)
    private String value;

    @Column(name = "sub_value_or_type", length = 128)
    private String subValueOrType;

    @Column(name = "created_by", nullable = false, length = 32)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on", nullable = false)
    private Date createdOn;

    @Column(name = "modified_by", length = 32)
    private String modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_on")
    private Date modifiedOn;


    protected InteropIdentifier() {
    }

    public InteropIdentifier(@NotNull SavingsAccount account, @NotNull InteropIdentifierType type, @NotNull String value,
                                   String subValueOrType, @NotNull String createdBy, @NotNull Date createdOn) {
        this.account = account;
        this.type = type;
        this.value = value;
        this.subValueOrType = subValueOrType;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public InteropIdentifier(@NotNull SavingsAccount account, @NotNull InteropIdentifierType type, @NotNull String createdBy,
                                   @NotNull Date createdOn) {
        this(account, type, null, null, createdBy, createdOn);
    }

    public SavingsAccount getAccount() {
        return account;
    }

    private void setAccount(SavingsAccount account) {
        this.account = account;
    }

    public InteropIdentifierType getType() {
        return type;
    }

    private void setType(InteropIdentifierType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSubValueOrType() {
        return subValueOrType;
    }

    public void setSubValueOrType(String subValueOrType) {
        this.subValueOrType = subValueOrType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    private void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    private void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String geModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InteropIdentifier that = (InteropIdentifier) o;

        if (!account.equals(that.account)) return false;
        if (type != that.type) return false;
        if (!value.equals(that.value)) return false;
        return subValueOrType != null ? subValueOrType.equals(that.subValueOrType) : that.subValueOrType == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (subValueOrType != null ? subValueOrType.hashCode() : 0);
        return result;
    }
}
