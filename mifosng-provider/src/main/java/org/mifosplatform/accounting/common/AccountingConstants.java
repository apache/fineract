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
        FUND_SOURCE(1), LOAN_PORTFOLIO(2), INTEREST_ON_LOANS(3), INCOME_FROM_FEES(4), INCOME_FROM_PENALTIES(5), LOSSES_WRITTEN_OFF(6), TRANSFERS_SUSPENSE(
                10), OVERPAYMENT(11);

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
                7), FEES_RECEIVABLE(8), PENALTIES_RECEIVABLE(9), TRANSFERS_SUSPENSE(10), OVERPAYMENT(11);

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
                "incomeFromFeeAccountId"), INCOME_FROM_PENALTIES("incomeFromPenaltyAccountId"), LOSSES_WRITTEN_OFF("writeOffAccountId"), OVERPAYMENT(
                "overpaymentLiabilityAccountId"), INTEREST_RECEIVABLE("receivableInterestAccountId"), FEES_RECEIVABLE(
                "receivableFeeAccountId"), PENALTIES_RECEIVABLE("receivablePenaltyAccountId"), TRANSFERS_SUSPENSE(
                "transfersInSuspenseAccountId"), PAYMENT_CHANNEL_FUND_SOURCE_MAPPING("paymentChannelToFundSourceMappings"), PAYMENT_TYPE(
                "paymentTypeId"), FEE_INCOME_ACCOUNT_MAPPING("feeToIncomeAccountMappings"), PENALTY_INCOME_ACCOUNT_MAPPING(
                "penaltyToIncomeAccountMappings"), CHARGE_ID("chargeId"), INCOME_ACCOUNT_ID("incomeAccountId");

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

    public static enum LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS {
        FUND_SOURCE("fundSourceAccount"), LOAN_PORTFOLIO("loanPortfolioAccount"), INTEREST_ON_LOANS("interestOnLoanAccount"), INCOME_FROM_FEES(
                "incomeFromFeeAccount"), INCOME_FROM_PENALTIES("incomeFromPenaltyAccount"), LOSSES_WRITTEN_OFF("writeOffAccount"), OVERPAYMENT(
                "overpaymentLiabilityAccount"), INTEREST_RECEIVABLE("receivableInterestAccount"), FEES_RECEIVABLE(
                "receivableFeeAccount"), PENALTIES_RECEIVABLE("receivablePenaltyAccount"), TRANSFERS_SUSPENSE(
                "transfersInSuspenseAccount"), INCOME_ACCOUNT_ID("incomeAccount");

        private final String value;

        private LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS(final String value) {
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

    
    /*** Accounting placeholders for cash based accounting for savings products ***/
    public static enum CASH_ACCOUNTS_FOR_SAVINGS {
        SAVINGS_REFERENCE(1), SAVINGS_CONTROL(2), INTEREST_ON_SAVINGS(3), INCOME_FROM_FEES(4), INCOME_FROM_PENALTIES(5), TRANSFERS_SUSPENSE(
                10);

        private final Integer value;

        private CASH_ACCOUNTS_FOR_SAVINGS(final Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public Integer getValue() {
            return this.value;
        }

        private static final Map<Integer, CASH_ACCOUNTS_FOR_SAVINGS> intToEnumMap = new HashMap<Integer, CASH_ACCOUNTS_FOR_SAVINGS>();
        static {
            for (final CASH_ACCOUNTS_FOR_SAVINGS type : CASH_ACCOUNTS_FOR_SAVINGS.values()) {
                intToEnumMap.put(type.value, type);
            }
        }

        public static CASH_ACCOUNTS_FOR_SAVINGS fromInt(final int i) {
            final CASH_ACCOUNTS_FOR_SAVINGS type = intToEnumMap.get(Integer.valueOf(i));
            return type;
        }
    }

    /***
     * Enum of all accounting related input parameter names used while
     * creating/updating a savings product
     ***/
    public static enum SAVINGS_PRODUCT_ACCOUNTING_PARAMS {
        SAVINGS_REFERENCE("savingsReferenceAccountId"), SAVINGS_CONTROL("savingsControlAccountId"), INCOME_FROM_FEES(
                "incomeFromFeeAccountId"), INCOME_FROM_PENALTIES("incomeFromPenaltyAccountId"), INTEREST_ON_SAVINGS(
                "interestOnSavingsAccountId"), PAYMENT_CHANNEL_FUND_SOURCE_MAPPING("paymentChannelToFundSourceMappings"), PAYMENT_TYPE(
                "paymentTypeId"), FUND_SOURCE("fundSourceAccountId"), TRANSFERS_SUSPENSE("transfersInSuspenseAccountId"), FEE_INCOME_ACCOUNT_MAPPING(
                "feeToIncomeAccountMappings"), PENALTY_INCOME_ACCOUNT_MAPPING("penaltyToIncomeAccountMappings"), CHARGE_ID("chargeId"), INCOME_ACCOUNT_ID(
                "incomeAccountId");

        private final String value;

        private SAVINGS_PRODUCT_ACCOUNTING_PARAMS(final String value) {
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
    
    public static enum SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS {
        SAVINGS_REFERENCE("savingsReferenceAccount"), SAVINGS_CONTROL("savingsControlAccount"), INCOME_FROM_FEES(
                "incomeFromFeeAccount"), INCOME_FROM_PENALTIES("incomeFromPenaltyAccount"), INTEREST_ON_SAVINGS(
                "interestOnSavingsAccount"), PAYMENT_TYPE("paymentType"), FUND_SOURCE("fundSourceAccount"), 
                TRANSFERS_SUSPENSE("transfersInSuspenseAccount"), PENALTY_INCOME_ACCOUNT_MAPPING("penaltyToIncomeAccountMappings"), CHARGE_ID("charge"), INCOME_ACCOUNT_ID(
                "incomeAccount");

        private final String value;

        private SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS(final String value) {
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


    public static final String ASSESTS_TAG_OPTION_CODE_NAME = "AssetAccountTags";
    public static final String LIABILITIES_TAG_OPTION_CODE_NAME = "LiabilityAccountTags";
    public static final String EQUITY_TAG_OPTION_CODE_NAME = "EquityAccountTags";
    public static final String INCOME_TAG_OPTION_CODE_NAME = "IncomeAccountTags";
    public static final String EXPENSES_TAG_OPTION_CODE_NAME = "ExpenseAccountTags";

}
