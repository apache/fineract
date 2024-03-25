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
package org.apache.fineract.test.data.accounttype;

public enum DefaultAccountType implements AccountType {

    // Asset
    LOANS_RECEIVABLE("Loans Receivable"), INTEREST_FEE_RECEIVABLE("Interest/Fee Receivable"), OTHER_RECEIVABLES(
            "Other Receivables"), UNC_RECEIVABLE("UNC Receivable"), FUND_RECEIVABLES(
                    "Fund Receivables"), TRANSFER_IN_SUSPENSE_ACCOUNT("Transfer in suspense account"), ASSET_TRANSFER("Asset transfer"),
    // Income
    DEFERRED_INTEREST_REVENUE("Deferred Interest Revenue"), RETAINED_EARNINGS_PRIOR_YEAR("Retained Earnings Prior Year"), INTEREST_INCOME(
            "Interest Income"), FEE_INCOME("Fee Income"), FEE_CHARGE_OFF(
                    "Fee Charge Off"), RECOVERIES("Recoveries"), INTEREST_INCOME_CHARGE_OFF("Interest Income Charge Off"),
    // Liability
    AA_SUSPENSE_BALANCE("AA Suspense Balance"), SUSPENSE_CLEARING_ACCOUNT("Suspense/Clearing account"), OVERPAYMENT_ACCOUNT(
            "Overpayment account"),
    // Expense
    CREDIT_LOSS_BAD_DEBT("Credit Loss/Bad Debt"), CREDIT_LOSS_BAD_DEBT_FRAUD("Credit Loss/Bad Debt-Fraud"), GOODWILL_EXPENSE_ACCOUNT(
            "Goodwill Expense Account"), WRITTEN_OFF("Written off");

    private final String customName;

    DefaultAccountType() {
        this(null);
    }

    DefaultAccountType(String customName) {
        this.customName = customName;
    }

    @Override
    public String getName() {
        if (customName != null) {
            return customName;
        }
        return name();
    }
}
