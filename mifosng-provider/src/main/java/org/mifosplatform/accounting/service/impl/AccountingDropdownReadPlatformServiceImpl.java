package org.mifosplatform.accounting.service.impl;

import java.util.List;

import org.mifosplatform.accounting.domain.GLAccountType;
import org.mifosplatform.accounting.domain.GLAccountUsage;
import org.mifosplatform.accounting.domain.JournalEntryType;
import org.mifosplatform.accounting.service.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.service.AccountingEnumerations;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.springframework.stereotype.Service;

@Service
public class AccountingDropdownReadPlatformServiceImpl implements AccountingDropdownReadPlatformService {

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

}