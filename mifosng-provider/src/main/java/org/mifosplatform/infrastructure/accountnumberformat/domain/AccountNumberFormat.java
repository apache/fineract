/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.mifosplatform.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = AccountNumberFormatConstants.ACCOUNT_NUMBER_FORMAT_TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { AccountNumberFormatConstants.ACCOUNT_TYPE_ENUM_COLUMN_NAME }, name = AccountNumberFormatConstants.ACCOUNT_TYPE_UNIQUE_CONSTRAINT_NAME) })
public class AccountNumberFormat extends AbstractPersistable<Long> {

    @Column(name = AccountNumberFormatConstants.ACCOUNT_TYPE_ENUM_COLUMN_NAME, nullable = false)
    private Integer accountTypeEnum;

    @Column(name = AccountNumberFormatConstants.PREFIX_TYPE_ENUM_COLUMN_NAME, nullable = false)
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