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
package org.apache.fineract.accounting.rule.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.rule.api.AccountingRuleJsonInputParams;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.office.domain.Office;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "acc_accounting_rule", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name" }, name = "accounting_rule_name_unique") })
public class AccountingRule extends AbstractPersistableCustom<Long> {

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = true)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "debit_account_id", nullable = true)
    private GLAccount accountToDebit;

    @ManyToOne
    @JoinColumn(name = "credit_account_id", nullable = true)
    private GLAccount accountToCredit;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Column(name = "system_defined", nullable = false)
    private Boolean systemDefined;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accountingRule", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AccountingTagRule> accountingTagRules = new ArrayList<>();

    @Column(name = "allow_multiple_credits", nullable = false)
    private boolean allowMultipleCreditEntries;

    @Column(name = "allow_multiple_debits", nullable = false)
    private boolean allowMultipleDebitEntries;

    public static AccountingRule fromJson(final Office office, final GLAccount accountToDebit, final GLAccount accountToCredit,
            final JsonCommand command, final boolean allowMultipleCreditEntries, final boolean allowMultipleDebitEntries) {
        final String name = command.stringValueOfParameterNamed(AccountingRuleJsonInputParams.NAME.getValue());
        final String description = command.stringValueOfParameterNamed(AccountingRuleJsonInputParams.DESCRIPTION.getValue());
        final boolean systemDefined = false;
        return new AccountingRule().setOffice(office).setAccountToDebit(accountToDebit).setAccountToCredit(accountToCredit).setName(name)
                .setDescription(StringUtils.trim(description)).setSystemDefined(systemDefined)
                .setAllowMultipleCreditEntries(allowMultipleCreditEntries).setAllowMultipleDebitEntries(allowMultipleDebitEntries);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.OFFICE_ID.getValue(),
                this.office == null ? 0L : this.office.getId());
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(),
                this.accountToDebit == null ? 0L : this.accountToDebit.getId());
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(),
                this.accountToCredit == null ? 0L : this.accountToCredit.getId());
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.NAME.getValue(), this.name);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.DESCRIPTION.getValue(), this.description);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.SYSTEM_DEFINED.getValue(), this.systemDefined);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue(),
                this.allowMultipleCreditEntries);
        handlePropertyUpdate(command, actualChanges, AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue(),
                this.allowMultipleDebitEntries);
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
            } else if (paramName.equals(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue())) {
                if (this.accountToCredit == null) {
                    this.allowMultipleCreditEntries = newValue;
                }
            } else if (paramName.equals(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue())) {
                if (this.accountToDebit == null) {
                    this.allowMultipleDebitEntries = newValue;
                }
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

    public void updateAccountingRuleForTags(final List<AccountingTagRule> debitAccountingTagRules) {
        for (final AccountingTagRule accountingTagRule : debitAccountingTagRules) {
            accountingTagRule.setAccountingRule(this);
            this.accountingTagRules.add(accountingTagRule);
        }
    }

    public void updateTags(final JournalEntryType type) {
        final Set<AccountingTagRule> existedCreditTags = new HashSet<>();
        final Set<AccountingTagRule> existedDebitTags = new HashSet<>();
        for (final AccountingTagRule accountingTagRule : this.accountingTagRules) {
            if (JournalEntryType.fromInt(accountingTagRule.getAccountType()).isCreditType()) {
                existedCreditTags.add(accountingTagRule);
            } else if (JournalEntryType.fromInt(accountingTagRule.getAccountType()).isDebitType()) {
                existedDebitTags.add(accountingTagRule);
            }
        }

        if (type.isCreditType()) {
            this.accountingTagRules.retainAll(existedCreditTags);
        } else if (type.isDebitType()) {
            this.accountingTagRules.retainAll(existedDebitTags);
        }

    }

    public Set<AccountingTagRule> getAccountingTagRulesByType(final JournalEntryType type) {
        final Set<AccountingTagRule> existedTags = new HashSet<>();
        for (final AccountingTagRule accountingTagRule : this.accountingTagRules) {
            if (accountingTagRule.getAccountType().equals(type.getValue())) {
                existedTags.add(accountingTagRule);
            }
        }
        return existedTags;
    }

    public void removeOldTags(final List<AccountingTagRule> accountsToRemove) {
        this.accountingTagRules.removeAll(accountsToRemove);
    }

}
