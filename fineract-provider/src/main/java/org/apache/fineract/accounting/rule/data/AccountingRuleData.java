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
package org.apache.fineract.accounting.rule.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.office.data.OfficeData;

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
    private final List<AccountingTagRuleData> creditTags;
    private final List<AccountingTagRuleData> debitTags;

    // template
    @SuppressWarnings("unused")
    private List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
    @SuppressWarnings("unused")
    private List<GLAccountData> allowedAccounts = new ArrayList<GLAccountData>();
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> allowedCreditTagOptions;
    @SuppressWarnings("unused")
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
        this.allowedCreditTagOptions = allowedCreditTagOptions;
        this.allowedDebitTagOptions = allowedDebitTagOptions;
        this.creditTags = null;
        this.debitTags = null;
        this.creditAccounts = null;
        this.debitAccounts = null;
    }

    public AccountingRuleData(final Long id, final Long officeId, final String officeName, final String name, final String description,
            final boolean systemDefined, final boolean allowMultipleDebitEntries, final boolean allowMultipleCreditEntries,
            final List<AccountingTagRuleData> creditTags, final List<AccountingTagRuleData> debitTags,
            final List<GLAccountDataForLookup> creditAccounts, final List<GLAccountDataForLookup> debitAccounts) {
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
        this.allowedCreditTagOptions = null;
        this.allowedDebitTagOptions = null;
        this.creditTags = creditTags;
        this.debitTags = debitTags;
        this.creditAccounts = creditAccounts;
        this.debitAccounts = debitAccounts;
    }

    public List<AccountingTagRuleData> getCreditTags() {
        return this.creditTags;
    }

    public List<AccountingTagRuleData> getDebitTags() {
        return this.debitTags;
    }

}