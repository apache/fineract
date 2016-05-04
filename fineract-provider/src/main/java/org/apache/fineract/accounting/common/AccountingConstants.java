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
package org.apache.fineract.accounting.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;

public class AccountingConstants {

    /*** Accounting placeholders for cash based accounting for loan products ***/
    public static enum CASH_ACCOUNTS_FOR_LOAN {
        FUND_SOURCE(1), LOAN_PORTFOLIO(2), INTEREST_ON_LOANS(3), INCOME_FROM_FEES(4), INCOME_FROM_PENALTIES(5), LOSSES_WRITTEN_OFF(6), TRANSFERS_SUSPENSE(
                10), OVERPAYMENT(11), INCOME_FROM_RECOVERY(12);

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

        private static final Map<Integer, CASH_ACCOUNTS_FOR_LOAN> intToEnumMap = new HashMap<>();
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
                7), FEES_RECEIVABLE(8), PENALTIES_RECEIVABLE(9), TRANSFERS_SUSPENSE(10), OVERPAYMENT(11), INCOME_FROM_RECOVERY(12);

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

        private static final Map<Integer, ACCRUAL_ACCOUNTS_FOR_LOAN> intToEnumMap = new HashMap<>();
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
                "penaltyToIncomeAccountMappings"), CHARGE_ID("chargeId"), INCOME_ACCOUNT_ID("incomeAccountId"), INCOME_FROM_RECOVERY(
                "incomeFromRecoveryAccountId");

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
                "overpaymentLiabilityAccount"), INTEREST_RECEIVABLE("receivableInterestAccount"), FEES_RECEIVABLE("receivableFeeAccount"), PENALTIES_RECEIVABLE(
                "receivablePenaltyAccount"), TRANSFERS_SUSPENSE("transfersInSuspenseAccount"), INCOME_ACCOUNT_ID("incomeAccount"), INCOME_FROM_RECOVERY(
                "incomeFromRecoveryAccount"), LIABILITY_TRANSFER_SUSPENSE("liabilityTransferInSuspenseAccount");

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
                10), OVERDRAFT_PORTFOLIO_CONTROL(11), INCOME_FROM_INTEREST(12), LOSSES_WRITTEN_OFF(13), ESCHEAT_LIABILITY(14);

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

        private static final Map<Integer, CASH_ACCOUNTS_FOR_SAVINGS> intToEnumMap = new HashMap<>();
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
                "incomeAccountId"), OVERDRAFT_PORTFOLIO_CONTROL("overdraftPortfolioControlId"), INCOME_FROM_INTEREST("incomeFromInterestId"), LOSSES_WRITTEN_OFF(
                "writeOffAccountId"), ESCHEAT_LIABILITY("escheatLiabilityId");

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
        SAVINGS_REFERENCE("savingsReferenceAccount"), SAVINGS_CONTROL("savingsControlAccount"), INCOME_FROM_FEES("incomeFromFeeAccount"), INCOME_FROM_PENALTIES(
                "incomeFromPenaltyAccount"), INTEREST_ON_SAVINGS("interestOnSavingsAccount"), PAYMENT_TYPE("paymentType"), FUND_SOURCE(
                "fundSourceAccount"), TRANSFERS_SUSPENSE("transfersInSuspenseAccount"), PENALTY_INCOME_ACCOUNT_MAPPING(
                "penaltyToIncomeAccountMappings"), CHARGE_ID("charge"), INCOME_ACCOUNT_ID("incomeAccount"), OVERDRAFT_PORTFOLIO_CONTROL(
                "overdraftPortfolioControl"), INCOME_FROM_INTEREST("incomeFromInterest"), LOSSES_WRITTEN_OFF("writeOffAccount"),
                ESCHEAT_LIABILITY("escheatLiabilityAccount");

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

    public static enum FINANCIAL_ACTIVITY {
        ASSET_TRANSFER(100, "assetTransfer", GLAccountType.ASSET), LIABILITY_TRANSFER(200, "liabilityTransfer", GLAccountType.LIABILITY), CASH_AT_MAINVAULT(
                101, "cashAtMainVault", GLAccountType.ASSET), CASH_AT_TELLER(102, "cashAtTeller", GLAccountType.ASSET), OPENING_BALANCES_TRANSFER_CONTRA(
                300, "openingBalancesTransferContra", GLAccountType.EQUITY), ASSET_FUND_SOURCE(103, "fundSource", GLAccountType.ASSET), PAYABLE_DIVIDENDS(
                201, "payableDividends", GLAccountType.LIABILITY);

        private final Integer value;
        private final String code;
        private final GLAccountType mappedGLAccountType;
        private static List<FinancialActivityData> financialActivities;

        private FINANCIAL_ACTIVITY(final Integer value, final String code, final GLAccountType mappedGLAccountType) {
            this.value = value;
            this.code = code;
            this.mappedGLAccountType = mappedGLAccountType;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public Integer getValue() {
            return this.value;
        }

        public String getCode() {
            return this.code;
        }

        public GLAccountType getMappedGLAccountType() {
            return mappedGLAccountType;
        }

        public String getValueAsString() {
            return this.value.toString();
        }

        private static final Map<Integer, FINANCIAL_ACTIVITY> intToEnumMap = new HashMap<>();
        static {
            for (final FINANCIAL_ACTIVITY type : FINANCIAL_ACTIVITY.values()) {
                intToEnumMap.put(type.value, type);
            }
        }

        public static FINANCIAL_ACTIVITY fromInt(final int financialActivityId) {
            final FINANCIAL_ACTIVITY type = intToEnumMap.get(Integer.valueOf(financialActivityId));
            return type;
        }

        public static FinancialActivityData toFinancialActivityData(final int financialActivityId) {
            final FINANCIAL_ACTIVITY type = fromInt(financialActivityId);
            return convertToFinancialActivityData(type);
        }

        public static List<FinancialActivityData> getAllFinancialActivities() {
            if (financialActivities == null) {
                financialActivities = new ArrayList<>();
                for (final FINANCIAL_ACTIVITY type : FINANCIAL_ACTIVITY.values()) {
                    FinancialActivityData financialActivityData = convertToFinancialActivityData(type);
                    financialActivities.add(financialActivityData);
                }
            }
            return financialActivities;
        }

        private static FinancialActivityData convertToFinancialActivityData(final FINANCIAL_ACTIVITY type) {
            FinancialActivityData financialActivityData = new FinancialActivityData(type.value, type.code, type.getMappedGLAccountType());
            return financialActivityData;
        }
    }

    /*** Accounting placeholders for cash based accounting for Share products ***/
    public static enum CASH_ACCOUNTS_FOR_SHARES {
        SHARES_REFERENCE(1), SHARES_SUSPENSE(2), INCOME_FROM_FEES(3), SHARES_EQUITY(4);

        private final Integer value;

        private CASH_ACCOUNTS_FOR_SHARES(final Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public Integer getValue() {
            return this.value;
        }

        private static final Map<Integer, CASH_ACCOUNTS_FOR_SHARES> intToEnumMap = new HashMap<>();
        static {
            for (final CASH_ACCOUNTS_FOR_SHARES type : CASH_ACCOUNTS_FOR_SHARES.values()) {
                intToEnumMap.put(type.value, type);
            }
        }

        public static CASH_ACCOUNTS_FOR_SHARES fromInt(final int i) {
            final CASH_ACCOUNTS_FOR_SHARES type = intToEnumMap.get(Integer.valueOf(i));
            return type;
        }
    }

    /***
     * Enum of all accounting related input parameter names used while
     * creating/updating a savings product
     ***/
    public static enum SHARES_PRODUCT_ACCOUNTING_PARAMS {
        SHARES_REFERENCE("shareReferenceId"), SHARES_SUSPENSE("shareSuspenseId"), INCOME_FROM_FEES("incomeFromFeeAccountId"), SHARES_EQUITY(
                "shareEquityId");

        private final String value;

        private SHARES_PRODUCT_ACCOUNTING_PARAMS(final String value) {
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
