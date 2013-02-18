/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.common;

import java.util.HashMap;
import java.util.Map;

public class AccountingConstants {

    /*** Accounting placeholders for cash based accounting for loan products ***/
    public static enum CASH_ACCOUNTS_FOR_LOAN {
        FUND_SOURCE(1), LOAN_PORTFOLIO(2), INTEREST_ON_LOANS(3), INCOME_FROM_FEES(4), INCOME_FROM_PENALTIES(5), LOSSES_WRITTEN_OFF(6);

        private final Integer value;

        private CASH_ACCOUNTS_FOR_LOAN(final Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public Integer getValue() {
            return this.value;
        }

        private static final Map<Integer, CASH_ACCOUNTS_FOR_LOAN> intToEnumMap = new HashMap<Integer, CASH_ACCOUNTS_FOR_LOAN>();
        static {
            for (final CASH_ACCOUNTS_FOR_LOAN type : CASH_ACCOUNTS_FOR_LOAN.values()) {
                intToEnumMap.put(type.value, type);
            }
        }

        public static CASH_ACCOUNTS_FOR_LOAN fromInt(final int i) {
            final CASH_ACCOUNTS_FOR_LOAN type = intToEnumMap.get(Integer.valueOf(i));
            return type;
        }
    }

    /*** Accounting placeholders for accrual based accounting for loan products ***/
    public static enum ACCRUAL_ACCOUNTS_FOR_LOAN {
        FUND_SOURCE(1), LOAN_PORTFOLIO(2), INTEREST_ON_LOANS(3), INCOME_FROM_FEES(4), INCOME_FROM_PENALTIES(5), LOSSES_WRITTEN_OFF(6), INTEREST_RECEIVABLE(
                7), FEES_RECEIVABLE(8), PENALTIES_RECEIVABLE(9);

        private final Integer value;

        private ACCRUAL_ACCOUNTS_FOR_LOAN(final Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public Integer getValue() {
            return this.value;
        }

        private static final Map<Integer, ACCRUAL_ACCOUNTS_FOR_LOAN> intToEnumMap = new HashMap<Integer, ACCRUAL_ACCOUNTS_FOR_LOAN>();
        static {
            for (final ACCRUAL_ACCOUNTS_FOR_LOAN type : ACCRUAL_ACCOUNTS_FOR_LOAN.values()) {
                intToEnumMap.put(type.value, type);
            }
        }

        public static ACCRUAL_ACCOUNTS_FOR_LOAN fromInt(final int i) {
            final ACCRUAL_ACCOUNTS_FOR_LOAN type = intToEnumMap.get(Integer.valueOf(i));
            return type;
        }

    }

    /***
     * Enum of all accounting related input parameter names used while
     * creating/updating a loan product
     ***/
    public static enum LOAN_PRODUCT_ACCOUNTING_PARAMS {
        FUND_SOURCE("fundSourceAccountId"), LOAN_PORTFOLIO("loanPortfolioAccountId"), INTEREST_ON_LOANS("interestOnLoanAccountId"), INCOME_FROM_FEES(
                "incomeFromFeeAccountId"), INCOME_FROM_PENALTIES("incomeFromPenaltyAccountId"), LOSSES_WRITTEN_OFF("writeOffAccountId"), INTEREST_RECEIVABLE(
                "receivableInterestAccountId"), FEES_RECEIVABLE("receivableFeeAccountId"), PENALTIES_RECEIVABLE(
                "receivablePenaltyAccountId");

        private final String value;

        private LOAN_PRODUCT_ACCOUNTING_PARAMS(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

}
