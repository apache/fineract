/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.rule.api.AccountingRuleJsonInputParams;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "acc_accounting_rule", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "accounting_rule_name_unique") })
public class AccountingRule extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = true)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "debit_account_id", nullable = false)
    private GLAccount accountToDebit;

    @ManyToOne
    @JoinColumn(name = "credit_account_id", nullable = false)
    private GLAccount accountToCredit;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Column(name = "system_defined", nullable = false)
    private Boolean systemDefined;

    protected AccountingRule() {}

    private AccountingRule(Office office, GLAccount accountToDebit, GLAccount accountToCredit, String name, String description,
            boolean systemDefined) {
        this.accountToDebit = accountToDebit;
        this.accountToCredit = accountToCredit;
        this.name = name;
        this.office = office;
        this.description = StringUtils.defaultIfEmpty(description, null);
        if (this.description != null) {
            this.description = this.description.trim();
        }
        this.systemDefined = systemDefined;
    }

    public static AccountingRule fromJson(final Office office, final GLAccount accountToDebit, final GLAccount accountToCredit,
            JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(AccountingRuleJsonInputParams.NAME.getValue());
        final String description = command.stringValueOfParameterNamed(AccountingRuleJsonInputParams.DESCRIPTION.getValue());
        final boolean systemDefined = false;
        return new AccountingRule(office, accountToDebit, accountToCredit, name, description, systemDefined);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(5);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.OFFICE_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.NAME.getValue(), this.name);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.DESCRIPTION.getValue(), this.description);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.SYSTEM_DEFINED.getValue(), this.systemDefined);
        return actualChanges;
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(AccountingRuleJsonInputParams.DESCRIPTION.getValue())) {
                this.description = newValue;
            } else if (paramName.equals(AccountingRuleJsonInputParams.NAME.getValue())) {
                this.name = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final boolean propertyToBeUpdated) {
        if (command.isChangeInBooleanParameterNamed(paramName, propertyToBeUpdated)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(AccountingRuleJsonInputParams.SYSTEM_DEFINED.getValue())) {
                this.systemDefined = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final Long propertyToBeUpdated) {
        if (command.isChangeInLongParameterNamed(paramName, propertyToBeUpdated)) {
            final Long newValue = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue())) {
                // do nothing as this is a nested property
            } else if (paramName.equals(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue())) {
                // do nothing as this is a nested property
            } else if (paramName.equals(AccountingRuleJsonInputParams.OFFICE_ID.getValue())) {
                // do nothing as this is a nested property
            }
        }
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Office getOffice() {
        return this.office;
    }

    public GLAccount getAccountToDebit() {
        return this.accountToDebit;
    }

    public GLAccount getAccountToCredit() {
        return this.accountToCredit;
    }

    public void setAccountToDebit(GLAccount accountToDebit) {
        this.accountToDebit = accountToDebit;
    }

    public void setAccountToCredit(GLAccount accountToCredit) {
        this.accountToCredit = accountToCredit;
    }

}