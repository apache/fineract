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
package org.apache.fineract.infrastructure.accountnumberformat.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = AccountNumberFormatConstants.ACCOUNT_NUMBER_FORMAT_TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { AccountNumberFormatConstants.ACCOUNT_TYPE_ENUM_COLUMN_NAME }, name = AccountNumberFormatConstants.ACCOUNT_TYPE_UNIQUE_CONSTRAINT_NAME) })
public class AccountNumberFormat extends AbstractPersistableCustom<Long> {

    @Column(name = AccountNumberFormatConstants.ACCOUNT_TYPE_ENUM_COLUMN_NAME, nullable = false)
    private Integer accountTypeEnum;

    @Column(name = AccountNumberFormatConstants.PREFIX_TYPE_ENUM_COLUMN_NAME, nullable = true)
    private Integer prefixEnum;

    protected AccountNumberFormat() {
        //
    }

    public AccountNumberFormat(EntityAccountType entityAccountType, AccountNumberPrefixType prefixType) {
        this.accountTypeEnum = entityAccountType.getValue();
        if (prefixType != null) {
            this.prefixEnum = prefixType.getValue();
        }
    }

    public Integer getAccountTypeEnum() {
        return this.accountTypeEnum;
    }

    public EntityAccountType getAccountType() {
        return EntityAccountType.fromInt(this.accountTypeEnum);
    }

    private void setAccountTypeEnum(Integer accountTypeEnum) {
        this.accountTypeEnum = accountTypeEnum;
    }

    public void setAccountType(EntityAccountType entityAccountType) {
        setAccountTypeEnum(entityAccountType.getValue());
    }

    public Integer getPrefixEnum() {
        return this.prefixEnum;
    }

    private void setPrefixEnum(Integer prefixEnum) {
        this.prefixEnum = prefixEnum;
    }

    public void setPrefix(AccountNumberPrefixType accountNumberPrefixType) {
        setPrefixEnum(accountNumberPrefixType.getValue());
    }
}