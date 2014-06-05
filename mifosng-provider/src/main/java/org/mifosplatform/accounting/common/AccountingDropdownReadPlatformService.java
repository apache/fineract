/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.common;

import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public interface AccountingDropdownReadPlatformService {

    public List<EnumOptionData> retrieveGLAccountTypeOptions();

    public List<EnumOptionData> retrieveGLAccountUsageOptions();

    public List<EnumOptionData> retrieveJournalEntryTypeOptions();

    public List<EnumOptionData> retrieveAccountingRuleTypeOptions();

    public Map<String, List<GLAccountData>> retrieveAccountMappingOptionsForLoanProducts();

    public Map<String, List<GLAccountData>> retrieveAccountMappingOptionsForSavingsProducts();

    public Map<String, List<GLAccountData>> retrieveAccountMappingOptions();

}