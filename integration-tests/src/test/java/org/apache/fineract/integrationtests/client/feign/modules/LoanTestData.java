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
package org.apache.fineract.integrationtests.client.feign.modules;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.fineract.integrationtests.common.accounting.Account;

public final class LoanTestData {

    public static final String DATETIME_PATTERN = "dd MMMM yyyy";
    public static final String LOCALE = "en";

    private LoanTestData() {}

    @ToString
    @AllArgsConstructor
    public static class Transaction {

        public Double amount;
        public String type;
        public String date;
        public Boolean reversed;
    }

    @ToString
    @AllArgsConstructor
    public static class TransactionExt {

        public Double amount;
        public String type;
        public String date;
        public Double outstandingPrincipal;
        public Double principalPortion;
        public Double interestPortion;
        public Double feePortion;
        public Double penaltyPortion;
        public Double unrecognizedPortion;
        public Double overpaymentPortion;
        public Boolean reversed;
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Journal {

        public Double amount;
        public Account account;
        public String type;

        public static Journal debit(Long glAccountId, double amount) {
            Journal journal = new Journal();
            journal.amount = amount;
            journal.account = new Account(glAccountId.intValue(), null);
            journal.type = "DEBIT";
            return journal;
        }

        public static Journal credit(Long glAccountId, double amount) {
            Journal journal = new Journal();
            journal.amount = amount;
            journal.account = new Account(glAccountId.intValue(), null);
            journal.type = "CREDIT";
            return journal;
        }
    }

    @ToString
    @AllArgsConstructor
    public static class Installment {

        public Double principalAmount;
        public Double interestAmount;
        public Double feeAmount;
        public Double penaltyAmount;
        public Double totalOutstandingAmount;
        public Boolean completed;
        public String dueDate;
        public OutstandingAmounts outstandingAmounts;
        public Double loanBalance;
    }

    @AllArgsConstructor
    @ToString
    public static class OutstandingAmounts {

        public Double principalOutstanding;
        public Double interestOutstanding;
        public Double feeOutstanding;
        public Double penaltyOutstanding;
        public Double totalOutstanding;
    }

    public static final class AmortizationType {

        public static final Integer EQUAL_INSTALLMENTS = 1;

        private AmortizationType() {}
    }

    public static final class InterestType {

        public static final Integer DECLINING_BALANCE = 0;
        public static final Integer FLAT = 1;

        private InterestType() {}
    }

    public static final class InterestRecalculationCompoundingMethod {

        public static final Integer NONE = 0;

        private InterestRecalculationCompoundingMethod() {}
    }

    public static final class RepaymentFrequencyType {

        public static final Integer MONTHS = 2;
        public static final Long MONTHS_L = 2L;
        public static final String MONTHS_STRING = "MONTHS";
        public static final Integer DAYS = 0;
        public static final Long DAYS_L = 0L;
        public static final String DAYS_STRING = "DAYS";

        private RepaymentFrequencyType() {}
    }

    public static final class RecalculationRestFrequencyType {

        public static final Integer SAME_AS_REPAYMENT_PERIOD = 1;
        public static final Integer DAILY = 2;

        private RecalculationRestFrequencyType() {}
    }

    public static final class InterestCalculationPeriodType {

        public static final Integer DAILY = 0;
        public static final Integer SAME_AS_REPAYMENT_PERIOD = 1;

        private InterestCalculationPeriodType() {}
    }

    public static final class InterestRateFrequencyType {

        public static final Integer MONTHS = 2;
        public static final Integer YEARS = 3;
        public static final Integer WHOLE_TERM = 4;

        private InterestRateFrequencyType() {}
    }

    public static final class TransactionProcessingStrategyCode {

        public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

        private TransactionProcessingStrategyCode() {}
    }

    public static final class RescheduleStrategyMethod {

        public static final Integer RESCHEDULE_NEXT_REPAYMENTS = 1;
        public static final Integer REDUCE_EMI_AMOUNT = 3;
        public static final Integer ADJUST_LAST_UNPAID_PERIOD = 4;

        private RescheduleStrategyMethod() {}
    }

    public static final class DaysInYearType {

        public static final Integer INVALID = 0;
        public static final Integer ACTUAL = 1;
        public static final Integer DAYS_360 = 360;
        public static final Integer DAYS_364 = 364;
        public static final Integer DAYS_365 = 365;

        private DaysInYearType() {}
    }

    public static final class DaysInMonthType {

        public static final Integer INVALID = 0;
        public static final Integer ACTUAL = 1;
        public static final Integer DAYS_30 = 30;

        private DaysInMonthType() {}
    }

    public static final class FuturePaymentAllocationRule {

        public static final String LAST_INSTALLMENT = "LAST_INSTALLMENT";
        public static final String NEXT_INSTALLMENT = "NEXT_INSTALLMENT";
        public static final String NEXT_LAST_INSTALLMENT = "NEXT_LAST_INSTALLMENT";

        private FuturePaymentAllocationRule() {}
    }

    public static final class SupportedInterestRefundTypesItem {

        public static final String MERCHANT_ISSUED_REFUND = "MERCHANT_ISSUED_REFUND";
        public static final String PAYOUT_REFUND = "PAYOUT_REFUND";

        private SupportedInterestRefundTypesItem() {}
    }

    public static final class DaysInYearCustomStrategy {

        public static final String FEB_29_PERIOD_ONLY = "FEB_29_PERIOD_ONLY";
        public static final String FULL_LEAP_YEAR = "FULL_LEAP_YEAR";

        private DaysInYearCustomStrategy() {}
    }
}
