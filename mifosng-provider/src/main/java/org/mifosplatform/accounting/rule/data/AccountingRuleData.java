/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.organisation.office.data.OfficeData;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class AccountingRuleData {

    private final Long id;
    private final Long officeId;
    private final String officeName;
    private final String name;
    private final String description;
    private final boolean systemDefined;
    private final GLAccountData debitAccountHead;
    private final GLAccountData creditAccountHead;
    private final List<AccountingTagRuleData> creditTags;
    private final List<AccountingTagRuleData> debitTags;

    // template
    private List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
    private List<GLAccountData> allowedAccounts = new ArrayList<GLAccountData>();
    private final Collection<CodeValueData> allowedAssetsTagOptions;
    private final Collection<CodeValueData> allowedLiabilitiesTagOptions;
    private final Collection<CodeValueData> allowedEquityTagOptions;
    private final Collection<CodeValueData> allowedIncomeTagOptions;
    private final Collection<CodeValueData> allowedExpensesTagOptions;

    public AccountingRuleData(final AccountingRuleData accountingRuleData, final List<GLAccountData> allowedAccounts,
            final List<OfficeData> allowedOffices, final Collection<CodeValueData> allowedAssetsTagOptions,
            final Collection<CodeValueData> allowedLiabilitiesTagOptions, final Collection<CodeValueData> allowedEquityTagOptions,
            final Collection<CodeValueData> allowedIncomeTagOptions, final Collection<CodeValueData> allowedExpensesTagOptions) {
        this.id = accountingRuleData.id;
        this.officeId = accountingRuleData.officeId;
        this.officeName = accountingRuleData.officeName;
        this.name = accountingRuleData.name;
        this.description = accountingRuleData.description;
        this.systemDefined = accountingRuleData.systemDefined;
        this.allowedOffices = allowedOffices;
        this.allowedAccounts = allowedAccounts;
        this.debitAccountHead = accountingRuleData.debitAccountHead;
        this.creditAccountHead = accountingRuleData.creditAccountHead;
        this.allowedAssetsTagOptions = allowedAssetsTagOptions;
        this.allowedLiabilitiesTagOptions = allowedLiabilitiesTagOptions;
        this.allowedEquityTagOptions = allowedEquityTagOptions;
        this.allowedIncomeTagOptions = allowedIncomeTagOptions;
        this.allowedExpensesTagOptions = allowedExpensesTagOptions;
        this.creditTags = accountingRuleData.creditTags;
        this.debitTags = accountingRuleData.debitTags;
    }

    public AccountingRuleData(final List<GLAccountData> allowedAccounts, final List<OfficeData> allowedOffices,
            final Collection<CodeValueData> allowedAssetsTagOptions, final Collection<CodeValueData> allowedLiabilitiesTagOptions,
            final Collection<CodeValueData> allowedEquityTagOptions, final Collection<CodeValueData> allowedIncomeTagOptions,
            final Collection<CodeValueData> allowedExpensesTagOptions) {
        this.id = null;
        this.officeId = null;
        this.officeName = null;
        this.name = null;
        this.description = null;
        this.systemDefined = false;
        this.allowedOffices = allowedOffices;
        this.allowedAccounts = allowedAccounts;
        this.debitAccountHead = null;
        this.creditAccountHead = null;
        this.allowedAssetsTagOptions = allowedAssetsTagOptions;
        this.allowedLiabilitiesTagOptions = allowedLiabilitiesTagOptions;
        this.allowedEquityTagOptions = allowedEquityTagOptions;
        this.allowedIncomeTagOptions = allowedIncomeTagOptions;
        this.allowedExpensesTagOptions = allowedExpensesTagOptions;
        this.creditTags = null;
        this.debitTags = null;
    }

    public AccountingRuleData(final Long id, final Long officeId, final String officeName, final String name, final String description,
            final boolean systemDefined, final GLAccountData debitAccountData, final GLAccountData creditAccountData) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.name = name;
        this.description = description;
        this.systemDefined = systemDefined;
        this.allowedOffices = null;
        this.allowedAccounts = null;
        this.debitAccountHead = debitAccountData;
        this.creditAccountHead = creditAccountData;
        this.allowedAssetsTagOptions = null;
        this.allowedLiabilitiesTagOptions = null;
        this.allowedEquityTagOptions = null;
        this.allowedIncomeTagOptions = null;
        this.allowedExpensesTagOptions = null;
        this.creditTags = null;
        this.debitTags = null;
    }

    public AccountingRuleData(final AccountingRuleData accountingRuleData, final List<AccountingTagRuleData> creditTags,
            final List<AccountingTagRuleData> debitTags) {
        this.id = accountingRuleData.id;
        this.officeId = accountingRuleData.officeId;
        this.officeName = accountingRuleData.officeName;
        this.name = accountingRuleData.name;
        this.description = accountingRuleData.description;
        this.systemDefined = accountingRuleData.systemDefined;
        this.allowedOffices = accountingRuleData.allowedOffices;
        this.allowedAccounts = accountingRuleData.allowedAccounts;
        this.debitAccountHead = accountingRuleData.debitAccountHead;
        this.creditAccountHead = accountingRuleData.creditAccountHead;
        this.allowedAssetsTagOptions = accountingRuleData.allowedAssetsTagOptions;
        this.allowedLiabilitiesTagOptions = accountingRuleData.allowedLiabilitiesTagOptions;
        this.allowedEquityTagOptions = accountingRuleData.allowedEquityTagOptions;
        this.allowedIncomeTagOptions = accountingRuleData.allowedIncomeTagOptions;
        this.allowedExpensesTagOptions = accountingRuleData.allowedExpensesTagOptions;
        this.creditTags = creditTags;
        this.debitTags = debitTags;
    }

    public GLAccountData getDebitAccountHead() {
        return this.debitAccountHead;
    }

    public GLAccountData getCreditAccountHead() {
        return this.creditAccountHead;
    }

    public List<AccountingTagRuleData> getCreditTags() {
        return this.creditTags;
    }

    public List<AccountingTagRuleData> getDebitTags() {
        return this.debitTags;
    }
}