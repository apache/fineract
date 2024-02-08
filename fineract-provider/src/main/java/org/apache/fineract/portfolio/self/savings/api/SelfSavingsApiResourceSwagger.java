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
package org.apache.fineract.portfolio.self.savings.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Created by Chirag Gupta on 12/25/17.
 */
final class SelfSavingsApiResourceSwagger {

    private SelfSavingsApiResourceSwagger() {}

    @Schema(description = "GetSelfSavingsAccountsResponse")
    public static final class GetSelfSavingsAccountsResponse {

        private GetSelfSavingsAccountsResponse() {}

        static final class GetSelfSavingsStatus {

            private GetSelfSavingsStatus() {}

            @Schema(example = "100")
            public Integer id;
            @Schema(example = "savingsAccountStatusType.submitted.and.pending.approval")
            public String code;
            @Schema(example = "Submitted and pending approval")
            public String description;
            @Schema(example = "true")
            public Boolean submittedAndPendingApproval;
            @Schema(example = "false")
            public Boolean approved;
            @Schema(example = "false")
            public Boolean rejected;
            @Schema(example = "false")
            public Boolean withdrawnByApplicant;
            @Schema(example = "false")
            public Boolean active;
            @Schema(example = "false")
            public Boolean closed;

        }

        static final class GetSelfSavingsTimeline {

            private GetSelfSavingsTimeline() {}

            @Schema(example = "[2013, 3, 1]")
            public LocalDate submittedOnDate;
        }

        static final class GetSelfSavingsCurrency {

            private GetSelfSavingsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetSelfSavingsInterestCompoundingPeriodType {

            private GetSelfSavingsInterestCompoundingPeriodType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @Schema(example = "Daily")
            public String description;
        }

        static final class GetSelfSavingsInterestPostingPeriodType {

            private GetSelfSavingsInterestPostingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetSelfSavingsInterestCalculationType {

            private GetSelfSavingsInterestCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @Schema(example = "Daily Balance")
            public String description;
        }

        static final class GetSelfSavingsInterestCalculationDaysInYearType {

            private GetSelfSavingsInterestCalculationDaysInYearType() {}

            @Schema(example = "365")
            public Integer id;
            @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @Schema(example = "365 Days")
            public String description;
        }

        static final class GetSelfSavingsSummary {

            private GetSelfSavingsSummary() {}

            public GetSelfSavingsCurrency currency;
            @Schema(example = "0")
            public Integer accountBalance;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "000000001")
        public Long accountNo;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "small business")
        public String clientName;
        @Schema(example = "1")
        public Integer savingsProductId;
        @Schema(example = "Passbook Savings")
        public String savingsProductName;
        @Schema(example = "0")
        public Integer fieldOfficerId;
        public GetSelfSavingsStatus status;
        public GetSelfSavingsTimeline timeline;
        public GetSelfSavingsCurrency currency;
        @Schema(example = "5")
        public Double nominalAnnualInterestRate;
        public GetSelfSavingsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSelfSavingsInterestPostingPeriodType interestPostingPeriodType;
        public GetSelfSavingsInterestCalculationType interestCalculationType;
        public GetSelfSavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetSelfSavingsSummary summary;
    }

    @Schema(description = "GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse")
    public static final class GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse {

        private GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse() {}

        static final class GetSelfSavingsTransactionType {

            private GetSelfSavingsTransactionType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "savingsAccountTransactionType.withdrawal")
            public String code;
            @Schema(example = "Withdrawal")
            public String description;
            @Schema(example = "false")
            public Boolean deposit;
            @Schema(example = "true")
            public Boolean withdrawal;
            @Schema(example = "false")
            public Boolean interestPosting;
            @Schema(example = "false")
            public Boolean feeDeduction;
        }

        static final class GetSelfSavingsTransactionCurrency {

            private GetSelfSavingsTransactionCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "0")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetSelfSavingsPaymentDetailData {

            private GetSelfSavingsPaymentDetailData() {}

            static final class GetSelfSavingsPaymentType {

                private GetSelfSavingsPaymentType() {}

                @Schema(example = "11")
                public Integer id;
                @Schema(example = "cash")
                public String name;
            }

            @Schema(example = "62")
            public Integer id;
            public GetSelfSavingsPaymentType paymentType;
            @Schema(example = "")
            public Integer accountNumber;
            @Schema(example = "")
            public Integer checkNumber;
            @Schema(example = "")
            public Integer routingCode;
            @Schema(example = "")
            public Integer receiptNumber;
            @Schema(example = "")
            public Integer bankNumber;
        }

        @Schema(example = "1")
        public Integer id;
        public GetSelfSavingsTransactionType transactionType;
        @Schema(example = "1")
        public Integer accountId;
        @Schema(example = "000000001")
        public Long accountNo;
        @Schema(example = "[2013, 8, 7]")
        public LocalDate date;
        public GetSelfSavingsTransactionCurrency currency;
        public GetSelfSavingsPaymentDetailData paymentDetailData;
        @Schema(example = "5000")
        public Integer amount;
        @Schema(example = "0")
        public Integer runningBalance;
        @Schema(example = "true")
        public Boolean reversed;
    }

    @Schema(description = "GetSelfSavingsAccountsAccountIdChargesResponse")
    public static final class GetSelfSavingsAccountsAccountIdChargesResponse {

        private GetSelfSavingsAccountsAccountIdChargesResponse() {}

        static final class GetSelfSavingsChargeTimeType {

            private GetSelfSavingsChargeTimeType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @Schema(example = "Specified due date")
            public String description;
        }

        static final class GetSelfSavingsChargeCalculationType {

            private GetSelfSavingsChargeCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "3")
        public Integer chargeId;
        @Schema(example = "57")
        public Integer accountId;
        @Schema(example = "Savings account maintenance fee")
        public String name;
        public GetSelfSavingsChargeTimeType chargeTimeType;
        public GetSelfSavingsChargeCalculationType chargeCalculationType;
        @Schema(example = "0")
        public Double percentage;
        @Schema(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSelfSavingsAccountsResponse.GetSelfSavingsCurrency currency;
        @Schema(example = "100")
        public Integer amount;
        @Schema(example = "0")
        public Integer amountPaid;
        @Schema(example = "0")
        public Integer amountWaived;
        @Schema(example = "0")
        public Integer amountWrittenOff;
        @Schema(example = "100")
        public Integer amountOutstanding;
        @Schema(example = "100")
        public Integer amountOrPercentage;
        @Schema(example = "false")
        public Boolean penalty;
    }

    @Schema(description = "GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse")
    public static final class GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse {

        private GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "1")
        public Integer chargeId;
        @Schema(example = "Passbook fee")
        public String name;
        public GetSelfSavingsAccountsAccountIdChargesResponse.GetSelfSavingsChargeTimeType chargeTimeType;
        public GetSelfSavingsAccountsAccountIdChargesResponse.GetSelfSavingsChargeCalculationType chargeCalculationType;
        @Schema(example = "0")
        public Double percentage;
        @Schema(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSelfSavingsAccountsResponse.GetSelfSavingsCurrency currency;
        @Schema(example = "100")
        public Integer amount;
        @Schema(example = "0")
        public Integer amountPaid;
        @Schema(example = "0")
        public Integer amountWaived;
        @Schema(example = "0")
        public Integer amountWrittenOff;
        @Schema(example = "100")
        public Integer amountOutstanding;
        @Schema(example = "100")
        public Integer amountOrPercentage;
        @Schema(example = "false")
        public Boolean penalty;
    }
}
