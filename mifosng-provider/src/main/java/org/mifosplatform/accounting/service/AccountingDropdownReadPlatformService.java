package org.mifosplatform.accounting.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public interface AccountingDropdownReadPlatformService {

    public List<EnumOptionData> retrieveGLAccountTypeOptions();

    public List<EnumOptionData> retrieveGLAccountUsageOptions();

    public List<EnumOptionData> retrieveJournalEntryTypeOptions();
}