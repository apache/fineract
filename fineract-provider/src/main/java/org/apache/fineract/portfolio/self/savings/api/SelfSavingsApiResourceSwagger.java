/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.self.savings.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

/**
 * Created by Chirag Gupta on 12/25/17.
 */
final class SelfSavingsApiResourceSwagger {
    private SelfSavingsApiResourceSwagger() {
    }

    @ApiModel(value = "GetSelfSavingsAccountsResponse")
    public final static class GetSelfSavingsAccountsResponse {
        private GetSelfSavingsAccountsResponse() {
        }

        final class GetSelfSavingsStatus {
            private GetSelfSavingsStatus() {
            }

            @ApiModelProperty(example = "100")
            public Integer id;
            @ApiModelProperty(example = "savingsAccountStatusType.submitted.and.pending.approval")
            public String code;
            @ApiModelProperty(example = "Submitted and pending approval")
            public String value;
            @ApiModelProperty(example = "true")
            public Boolean submittedAndPendingApproval;
            @ApiModelProperty(example = "false")
            public Boolean approved;
            @ApiModelProperty(example = "false")
            public Boolean rejected;
            @ApiModelProperty(example = "false")
            public Boolean withdrawnByApplicant;
            @ApiModelProperty(example = "false")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean closed;

        }

        final class GetSelfSavingsTimeline {
            private GetSelfSavingsTimeline() {
            }

            @ApiModelProperty(example = "[2013, 3, 1]")
            public LocalDate submittedOnDate;
        }

        final class GetSelfSavingsCurrency {
            private GetSelfSavingsCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetSelfSavingsInterestCompoundingPeriodType {
            private GetSelfSavingsInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @ApiModelProperty(example = "Daily")
            public String value;
        }

        final class GetSelfSavingsInterestPostingPeriodType {
            private GetSelfSavingsInterestPostingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetSelfSavingsInterestCalculationType {
            private GetSelfSavingsInterestCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @ApiModelProperty(example = "Daily Balance")
            public String value;
        }

        final class GetSelfSavingsInterestCalculationDaysInYearType {
            private GetSelfSavingsInterestCalculationDaysInYearType() {
            }

            @ApiModelProperty(example = "365")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @ApiModelProperty(example = "365 Days")
            public String value;
        }

        final class GetSelfSavingsSummary {
            private GetSelfSavingsSummary() {
            }

            public GetSelfSavingsCurrency currency;
            @ApiModelProperty(example = "0")
            public Integer accountBalance;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "000000001")
        public Long accountNo;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "small business")
        public String clientName;
        @ApiModelProperty(example = "1")
        public Integer savingsProductId;
        @ApiModelProperty(example = "Passbook Savings")
        public String savingsProductName;
        @ApiModelProperty(example = "0")
        public Integer fieldOfficerId;
        public GetSelfSavingsStatus status;
        public GetSelfSavingsTimeline timeline;
        public GetSelfSavingsCurrency currency;
        @ApiModelProperty(example = "5")
        public Double nominalAnnualInterestRate;
        public GetSelfSavingsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSelfSavingsInterestPostingPeriodType interestPostingPeriodType;
        public GetSelfSavingsInterestCalculationType interestCalculationType;
        public GetSelfSavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetSelfSavingsSummary summary;
    }

    @ApiModel(value = "GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse")
    public final static class GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse {
        private GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse() {
        }

        final class GetSelfSavingsTransactionType {
            private GetSelfSavingsTransactionType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "savingsAccountTransactionType.withdrawal")
            public String code;
            @ApiModelProperty(example = "Withdrawal")
            public String value;
            @ApiModelProperty(example = "false")
            public Boolean deposit;
            @ApiModelProperty(example = "true")
            public Boolean withdrawal;
            @ApiModelProperty(example = "false")
            public Boolean interestPosting;
            @ApiModelProperty(example = "false")
            public Boolean feeDeduction;
        }

        final class GetSelfSavingsTransactionCurrency {
            private GetSelfSavingsTransactionCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "0")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetSelfSavingsPaymentDetailData {
            private GetSelfSavingsPaymentDetailData() {
            }

            final class GetSelfSavingsPaymentType {
                private GetSelfSavingsPaymentType() {
                }

                @ApiModelProperty(example = "11")
                public Integer id;
                @ApiModelProperty(example = "cash")
                public String name;
            }

            @ApiModelProperty(example = "62")
            public Integer id;
            public GetSelfSavingsPaymentType paymentType;
            @ApiModelProperty(example = "")
            public Integer accountNumber;
            @ApiModelProperty(example = "")
            public Integer checkNumber;
            @ApiModelProperty(example = "")
            public Integer routingCode;
            @ApiModelProperty(example = "")
            public Integer receiptNumber;
            @ApiModelProperty(example = "")
            public Integer bankNumber;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        public GetSelfSavingsTransactionType transactionType;
        @ApiModelProperty(example = "1")
        public Integer accountId;
        @ApiModelProperty(example = "000000001")
        public Long accountNo;
        @ApiModelProperty(example = "[2013, 8, 7]")
        public LocalDate date;
        public GetSelfSavingsTransactionCurrency currency;
        public GetSelfSavingsPaymentDetailData paymentDetailData;
        @ApiModelProperty(example = "5000")
        public Integer amount;
        @ApiModelProperty(example = "0")
        public Integer runningBalance;
        @ApiModelProperty(example = "true")
        public Boolean reversed;
    }

    @ApiModel(value = "GetSelfSavingsAccountsAccountIdChargesResponse")
    public final static class GetSelfSavingsAccountsAccountIdChargesResponse {
        private GetSelfSavingsAccountsAccountIdChargesResponse() {
        }

        final class GetSelfSavingsChargeTimeType {
            private GetSelfSavingsChargeTimeType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @ApiModelProperty(example = "Specified due date")
            public String value;
        }

        final class GetSelfSavingsChargeCalculationType {
            private GetSelfSavingsChargeCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeCalculationType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "3")
        public Integer chargeId;
        @ApiModelProperty(example = "57")
        public Integer accountId;
        @ApiModelProperty(example = "Savings account maintenance fee")
        public String name;
        public GetSelfSavingsChargeTimeType chargeTimeType;
        public GetSelfSavingsChargeCalculationType chargeCalculationType;
        @ApiModelProperty(example = "0")
        public Double percentage;
        @ApiModelProperty(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSelfSavingsAccountsResponse.GetSelfSavingsCurrency currency;
        @ApiModelProperty(example = "100")
        public Integer amount;
        @ApiModelProperty(example = "0")
        public Integer amountPaid;
        @ApiModelProperty(example = "0")
        public Integer amountWaived;
        @ApiModelProperty(example = "0")
        public Integer amountWrittenOff;
        @ApiModelProperty(example = "100")
        public Integer amountOutstanding;
        @ApiModelProperty(example = "100")
        public Integer amountOrPercentage;
        @ApiModelProperty(example = "false")
        public Boolean penalty;
    }

    @ApiModel(value = "GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse")
    public final static class GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse {
        private GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer chargeId;
        @ApiModelProperty(example = "Passbook fee")
        public String name;
        public GetSelfSavingsAccountsAccountIdChargesResponse.GetSelfSavingsChargeTimeType chargeTimeType;
        public GetSelfSavingsAccountsAccountIdChargesResponse.GetSelfSavingsChargeCalculationType chargeCalculationType;
        @ApiModelProperty(example = "0")
        public Double percentage;
        @ApiModelProperty(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSelfSavingsAccountsResponse.GetSelfSavingsCurrency currency;
        @ApiModelProperty(example = "100")
        public Integer amount;
        @ApiModelProperty(example = "0")
        public Integer amountPaid;
        @ApiModelProperty(example = "0")
        public Integer amountWaived;
        @ApiModelProperty(example = "0")
        public Integer amountWrittenOff;
        @ApiModelProperty(example = "100")
        public Integer amountOutstanding;
        @ApiModelProperty(example = "100")
        public Integer amountOrPercentage;
        @ApiModelProperty(example = "false")
        public Boolean penalty;
    }
}
