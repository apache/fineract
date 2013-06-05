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
import org.mifosplatform.accounting.glaccount.data.GLAccountDataForLookup;
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
    private final boolean allowMultipleDebitEntries;
    private final boolean allowMultipleCreditEntries;
    private final GLAccountData debitAccountHead;
    private final GLAccountData creditAccountHead;
    private final List<AccountingTagRuleData> creditTags;
    private final List<AccountingTagRuleData> debitTags;

    // template
    private List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
    private List<GLAccountData> allowedAccounts = new ArrayList<GLAccountData>();
    private final Collection<CodeValueData> allowedCreditTagOptions;
    private final Collection<CodeValueData> allowedDebitTagOptions;
    private final List<GLAccountDataForLookup> creditAccounts;
    private final List<GLAccountDataForLookup> debitAccounts;

    public AccountingRuleData(final AccountingRuleData accountingRuleData, final List<GLAccountData> allowedAccounts,
            final List<OfficeData> allowedOffices, final Collection<CodeValueData> allowedCreditTagOptions,
            final Collection<CodeValueData> allowedDebitTagOptions) {
        this.id = accountingRuleData.id;
        this.officeId = accountingRuleData.officeId;
        this.officeName = accountingRuleData.officeName;
        this.name = accountingRuleData.name;
        this.description = accountingRuleData.description;
        this.systemDefined = accountingRuleData.systemDefined;
        this.allowMultipleDebitEntries = accountingRuleData.allowMultipleDebitEntries;
        this.allowMultipleCreditEntries = accountingRuleData.allowMultipleCreditEntries;
        this.allowedOffices = allowedOffices;
        this.allowedAccounts = allowedAccounts;
        this.debitAccountHead = accountingRuleData.debitAccountHead;
        this.creditAccountHead = accountingRuleData.creditAccountHead;
        this.allowedCreditTagOptions = allowedCreditTagOptions;
        this.allowedDebitTagOptions = allowedDebitTagOptions;
        this.creditTags = accountingRuleData.creditTags;
        this.debitTags = accountingRuleData.debitTags;
        this.creditAccounts = accountingRuleData.creditAccounts;
        this.debitAccounts = accountingRuleData.debitAccounts;
    }

    public AccountingRuleData(final List<GLAccountData> allowedAccounts, final List<OfficeData> allowedOffices,
            final Collection<CodeValueData> allowedCreditTagOptions, final Collection<CodeValueData> allowedDebitTagOptions) {
        this.id = null;
        this.officeId = null;
        this.officeName = null;
        this.name = null;
        this.description = null;
        this.systemDefined = false;
        this.allowMultipleDebitEntries = false;
        this.allowMultipleCreditEntries = false;
        this.allowedOffices = allowedOffices;
        this.allowedAccounts = allowedAccounts;
        this.debitAccountHead = null;
        this.creditAccountHead = null;
        this.allowedCreditTagOptions = allowedCreditTagOptions;
        this.allowedDebitTagOptions = allowedDebitTagOptions;
        this.creditTags = null;
        this.debitTags = null;
        this.creditAccounts = null;
        this.debitAccounts = null;
    }

    public AccountingRuleData(final Long id, final Long officeId, final String officeName, final String name, final String description,
            final boolean systemDefined, final boolean allowMultipleDebitEntries, final boolean allowMultipleCreditEntries,
            final GLAccountData debitAccountData, final GLAccountData creditAccountData) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.name = name;
        this.description = description;
        this.systemDefined = systemDefined;
        this.allowMultipleDebitEntries = allowMultipleDebitEntries;
        this.allowMultipleCreditEntries = allowMultipleCreditEntries;
        this.allowedOffices = null;
        this.allowedAccounts = null;
        this.debitAccountHead = debitAccountData;
        this.creditAccountHead = creditAccountData;
        this.allowedCreditTagOptions = null;
        this.allowedDebitTagOptions = null;
        this.creditTags = null;
        this.debitTags = null;
        this.creditAccounts = new ArrayList<GLAccountDataForLookup>();
        this.debitAccounts = new ArrayList<GLAccountDataForLookup>();
    }

    public AccountingRuleData(final AccountingRuleData accountingRuleData, final List<AccountingTagRuleData> creditTags,
            final List<GLAccountDataForLookup> creditAccounts, final List<AccountingTagRuleData> debitTags,
            final List<GLAccountDataForLookup> debitAccounts) {
        this.id = accountingRuleData.id;
        this.officeId = accountingRuleData.officeId;
        this.officeName = accountingRuleData.officeName;
        this.name = accountingRuleData.name;
        this.description = accountingRuleData.description;
        this.systemDefined = accountingRuleData.systemDefined;
        this.allowMultipleDebitEntries = accountingRuleData.allowMultipleDebitEntries;
        this.allowMultipleCreditEntries = accountingRuleData.allowMultipleCreditEntries;
        this.allowedOffices = accountingRuleData.allowedOffices;
        this.allowedAccounts = accountingRuleData.allowedAccounts;
        this.debitAccountHead = accountingRuleData.debitAccountHead;
        this.creditAccountHead = accountingRuleData.creditAccountHead;
        this.allowedCreditTagOptions = accountingRuleData.allowedCreditTagOptions;
        this.allowedDebitTagOptions = accountingRuleData.allowedDebitTagOptions;
        this.creditTags = creditTags;
        this.creditAccounts = creditAccounts;
        this.debitTags = debitTags;
        this.debitAccounts = debitAccounts;
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

    public List<GLAccountDataForLookup> getCreditAccounts() {
        return this.creditAccounts;
    }

    public List<GLAccountDataForLookup> getDebitAccounts() {
        return this.debitAccounts;
    }
}