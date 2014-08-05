/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.data;

import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;

public class FinancialActivityAccountData {

    private final Long id;
    private final FinancialActivityData financialActivityData;
    private final GLAccountData glAccountData;
    private Map<String, List<GLAccountData>> glAccountOptions;
    private List<FinancialActivityData> financialActivityOptions;

    public FinancialActivityAccountData() {
        this.id = null;
        this.glAccountData = null;
        this.financialActivityData = null;
        this.glAccountOptions = null;
        this.financialActivityOptions = null;
    }

    public FinancialActivityAccountData(final Long id, final FinancialActivityData financialActivityData, final GLAccountData glAccountData) {
        this.id = id;
        this.glAccountData = glAccountData;
        this.financialActivityData = financialActivityData;
    }

    public FinancialActivityAccountData(Map<String, List<GLAccountData>> glAccountOptions,
            List<FinancialActivityData> financialActivityOptions) {
        this.id = null;
        this.glAccountData = null;
        this.financialActivityData = null;
        this.glAccountOptions = glAccountOptions;
        this.financialActivityOptions = financialActivityOptions;

    }

    public List<FinancialActivityData> getFinancialActivityOptions() {
        return financialActivityOptions;
    }

    public void setFinancialActivityOptions(List<FinancialActivityData> financialActivityOptions) {
        this.financialActivityOptions = financialActivityOptions;
    }

    public Map<String, List<GLAccountData>> getAccountingMappingOptions() {
        return this.glAccountOptions;
    }

    public void setAccountingMappingOptions(Map<String, List<GLAccountData>> accountingMappingOptions) {
        this.glAccountOptions = accountingMappingOptions;
    }

    public GLAccountData getGlAccountData() {
        return glAccountData;
    }

    public FinancialActivityData getFinancialActivityData() {
        return financialActivityData;
    }

    public Long getId() {
        return id;
    }

}
