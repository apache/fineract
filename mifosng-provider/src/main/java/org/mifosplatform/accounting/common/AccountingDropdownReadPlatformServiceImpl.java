/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.domain.GLAccountUsage;
import org.mifosplatform.accounting.glaccount.service.GLAccountReadPlatformService;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountingDropdownReadPlatformServiceImpl implements AccountingDropdownReadPlatformService {

    private final GLAccountReadPlatformService accountReadPlatformService;

    @Autowired
    public AccountingDropdownReadPlatformServiceImpl(final GLAccountReadPlatformService accountReadPlatformService) {
        this.accountReadPlatformService = accountReadPlatformService;
    }

    @Override
    public List<EnumOptionData> retrieveGLAccountTypeOptions() {
        return AccountingEnumerations.gLAccountType(GLAccountType.values());
    }

    @Override
    public List<EnumOptionData> retrieveGLAccountUsageOptions() {
        return AccountingEnumerations.gLAccountUsage(GLAccountUsage.values());
    }

    @Override
    public List<EnumOptionData> retrieveJournalEntryTypeOptions() {
        return AccountingEnumerations.journalEntryTypes(JournalEntryType.values());
    }

    @Override
    public List<EnumOptionData> retrieveAccountingRuleTypeOptions() {
        return AccountingEnumerations.accountingRuleTypes(AccountingRuleType.values());
    }

    @Override
    public Map<String, List<GLAccountData>> retrieveAccountMappingOptionsForLoanProducts() {
        return retrieveAccountMappingOptions();
    }

    @Override
    public Map<String, List<GLAccountData>> retrieveAccountMappingOptionsForSavingsProducts() {
        return retrieveAccountMappingOptions();
    }

    @Override
    public Map<String, List<GLAccountData>> retrieveAccountMappingOptions() {
        final Map<String, List<GLAccountData>> accountOptions = new HashMap<>();
        List<GLAccountData> assetAccountOptions = this.accountReadPlatformService.retrieveAllEnabledDetailGLAccounts(GLAccountType.ASSET);
        if (assetAccountOptions.isEmpty()) {
            assetAccountOptions = null;
        }
        accountOptions.put("assetAccountOptions", assetAccountOptions);

        List<GLAccountData> incomeAccountOptions = this.accountReadPlatformService.retrieveAllEnabledDetailGLAccounts(GLAccountType.INCOME);
        if (incomeAccountOptions.isEmpty()) {
            incomeAccountOptions = null;
        }
        accountOptions.put("incomeAccountOptions", incomeAccountOptions);

        List<GLAccountData> expenseAccountOptions = this.accountReadPlatformService
                .retrieveAllEnabledDetailGLAccounts(GLAccountType.EXPENSE);
        if (expenseAccountOptions.isEmpty()) {
            expenseAccountOptions = null;
        }
        accountOptions.put("expenseAccountOptions", expenseAccountOptions);

        List<GLAccountData> liabilityAccountOptions = this.accountReadPlatformService
                .retrieveAllEnabledDetailGLAccounts(GLAccountType.LIABILITY);
        if (liabilityAccountOptions.isEmpty()) {
            liabilityAccountOptions = null;
        }
        accountOptions.put("liabilityAccountOptions", liabilityAccountOptions);
        
		List<GLAccountData> equityAccountOptions = this.accountReadPlatformService
				.retrieveAllEnabledDetailGLAccounts(GLAccountType.EQUITY);
		if (equityAccountOptions.isEmpty()) {
			equityAccountOptions = null;
		}
		accountOptions.put("equityAccountOptions",equityAccountOptions);
        
        return accountOptions;
    }

}