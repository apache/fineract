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
package org.apache.fineract.portfolio.self.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/19/17.
 */
@SuppressWarnings({ "MemberName" })
final class SelfLoansApiResourceSwagger {

    private SelfLoansApiResourceSwagger() {}

    @Schema(description = "GetSelfLoansLoanIdResponse")
    public static final class GetSelfLoansLoanIdResponse {

        private GetSelfLoansLoanIdResponse() {}

        static final class GetLoansLoanIdStatus {

            private GetLoansLoanIdStatus() {}

            @Schema(example = "300")
            public Integer id;
            @Schema(example = "loanStatusType.active")
            public String code;
            @Schema(example = "Active")
            public String description;
            @Schema(example = "false")
            public Boolean pendingApproval;
            @Schema(example = "false")
            public Boolean waitingForDisbursal;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean closedObligationsMet;
            @Schema(example = "false")
            public Boolean closedWrittenOff;
            @Schema(example = "false")
            public Boolean closedRescheduled;
            @Schema(example = "false")
            public Boolean closed;
            @Schema(example = "false")
            public Boolean overpaid;
        }

        static final class GetLoansLoanIdLoanType {

            private GetLoansLoanIdLoanType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "loanType.individual")
            public String code;
            @Schema(example = "Individual")
            public String description;
        }

        static final class GetLoansLoanIdCurrency {

            private GetLoansLoanIdCurrency() {}

            @Schema(example = "UGX")
            public String code;
            @Schema(example = "Uganda Shilling")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "USh")
            public String displaySymbol;
            @Schema(example = "currency.UGX")
            public String nameCode;
            @Schema(example = "Uganda Shilling (USh)")
            public String displayLabel;
        }

        static final class GetLoansLoanIdTermPeriodFrequencyType {

            private GetLoansLoanIdTermPeriodFrequencyType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "termFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetLoansLoanIdRepaymentFrequencyType {

            private GetLoansLoanIdRepaymentFrequencyType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "repaymentFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetLoansLoanIdInterestRateFrequencyType {

            private GetLoansLoanIdInterestRateFrequencyType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.years")
            public String code;
            @Schema(example = "Per year")
            public String description;
        }

        static final class GetLoansLoanIdAmortizationType {

            private GetLoansLoanIdAmortizationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "amortizationType.equal.installments")
            public String code;
            @Schema(example = "Equal installments")
            public String description;
        }

        static final class GetLoansLoanIdInterestType {

            private GetLoansLoanIdInterestType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "interestType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetLoansLoanIdInterestCalculationPeriodType {

            private GetLoansLoanIdInterestCalculationPeriodType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "interestCalculationPeriodType.same.as.repayment.period")
            public String code;
            @Schema(example = "Same as repayment period")
            public String description;
        }

        static final class GetLoansLoanIdTimeline {

            private GetLoansLoanIdTimeline() {}

            @Schema(example = "[2012, 4, 3]")
            public LocalDate submittedOnDate;
            @Schema(example = "admin")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
            @Schema(example = "[2012, 4, 3]")
            public LocalDate approvedOnDate;
            @Schema(example = "admin")
            public String approvedByUsername;
            @Schema(example = "App")
            public String approvedByFirstname;
            @Schema(example = "Administrator")
            public String approvedByLastname;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate expectedDisbursementDate;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate actualDisbursementDate;
            @Schema(example = "admin")
            public String disbursedByUsername;
            @Schema(example = "App")
            public String disbursedByFirstname;
            @Schema(example = "Administrator")
            public String disbursedByLastname;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate expectedMaturityDate;
        }

        static final class GetLoansLoanIdSummary {

            private GetLoansLoanIdSummary() {}

            static final class GetLoansLoanIdEmiVariations {

                private GetLoansLoanIdEmiVariations() {}
            }

            static final class GetLoansLoanIdLinkedAccount {

                private GetLoansLoanIdLinkedAccount() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "000000001")
                public Long accountNo;
            }

            static final class GetLoansLoanIdDisbursementDetails {

                private GetLoansLoanIdDisbursementDetails() {}

                @Schema(example = "71")
                public Integer id;
                @Schema(example = "[2013, 11, 1]")
                public LocalDate expectedDisbursementDate;
                @Schema(example = "22000.000000")
                public Float principal;
                @Schema(example = "22000.000000")
                public Float approvedPrincipal;
            }

            static final class GetLoansLoanIdOverdueCharges {

                private GetLoansLoanIdOverdueCharges() {}

                static final class GetLoansLoanIdChargeTimeType {

                    private GetLoansLoanIdChargeTimeType() {}

                    @Schema(example = "9")
                    public Integer id;
                    @Schema(example = "chargeTimeType.overdueInstallment")
                    public String code;
                    @Schema(example = "overdue fees")
                    public String description;
                }

                static final class GetLoansLoanIdChargeCalculationType {

                    private GetLoansLoanIdChargeCalculationType() {}

                    @Schema(example = "2")
                    public Integer id;
                    @Schema(example = "chargeCalculationType.percent.of.amount")
                    public String code;
                    @Schema(example = "% Amount")
                    public String description;
                }

                static final class GetLoansLoanIdChargePaymentMode {

                    private GetLoansLoanIdChargePaymentMode() {}

                    @Schema(example = "0")
                    public Integer id;
                    @Schema(example = "chargepaymentmode.regular")
                    public String code;
                    @Schema(example = "Regular")
                    public String description;
                }

                static final class GetLoansLoanIdFeeFrequency {

                    private GetLoansLoanIdFeeFrequency() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "feeFrequencyperiodFrequencyType.weeks")
                    public String code;
                    @Schema(example = "Weeks")
                    public String description;
                }

                static final class GetLoanCurrency {

                    private GetLoanCurrency() {}

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

                static final class GetLoanChargeTemplateChargeAppliesTo {

                    private GetLoanChargeTemplateChargeAppliesTo() {}

                    @Schema(example = "1  ")
                    public Integer id;
                    @Schema(example = "chargeAppliesTo.loan")
                    public String code;
                    @Schema(example = "Loan")
                    public String description;
                }

                @Schema(example = "20")
                public Integer id;
                @Schema(example = "overdraft penality")
                public String name;
                @Schema(example = "true")
                public Boolean active;
                @Schema(example = "true")
                public Boolean penalty;
                public GetLoanCurrency currency;
                @Schema(example = "3.000000")
                public Float amount;
                public GetLoansLoanIdChargeTimeType chargeTimeType;
                public GetLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
                public GetLoansLoanIdChargeCalculationType chargeCalculationType;
                public GetLoansLoanIdChargePaymentMode chargePaymentMode;
                @Schema(example = "2")
                public Integer feeInterval;
                public GetLoansLoanIdFeeFrequency feeFrequency;
            }

            public GetLoansLoanIdCurrency currency;
            @Schema(example = "1000000")
            public Long principalDisbursed;
            @Schema(example = "0")
            public Long principalPaid;
            @Schema(example = "0")
            public Long principalWrittenOff;
            @Schema(example = "1000000")
            public Long principalOutstanding;
            @Schema(example = "833333.3")
            public Double principalOverdue;
            @Schema(example = "240000")
            public Long interestCharged;
            @Schema(example = "0")
            public Long interestPaid;
            @Schema(example = "0")
            public Long interestWaived;
            @Schema(example = "0")
            public Long interestWrittenOff;
            @Schema(example = "240000")
            public Long interestOutstanding;
            @Schema(example = "200000")
            public Long interestOverdue;
            @Schema(example = "18000")
            public Long feeChargesCharged;
            @Schema(example = "0")
            public Long feeChargesDueAtDisbursementCharged;
            @Schema(example = "0")
            public Long feeChargesPaid;
            @Schema(example = "0")
            public Long feeChargesWaived;
            @Schema(example = "0")
            public Long feeChargesWrittenOff;
            @Schema(example = "18000")
            public Long feeChargesOutstanding;
            @Schema(example = "15000")
            public Long feeChargesOverdue;
            @Schema(example = "0")
            public Long penaltyChargesCharged;
            @Schema(example = "0")
            public Long penaltyChargesPaid;
            @Schema(example = "0")
            public Long penaltyChargesWaived;
            @Schema(example = "0")
            public Long penaltyChargesWrittenOff;
            @Schema(example = "0")
            public Long penaltyChargesOutstanding;
            @Schema(example = "0")
            public Long penaltyChargesOverdue;
            @Schema(example = "1258000")
            public Long totalExpectedRepayment;
            @Schema(example = "0")
            public Long totalRepayment;
            @Schema(example = "258000")
            public Long totalExpectedCostOfLoan;
            @Schema(example = "0")
            public Long totalCostOfLoan;
            @Schema(example = "0")
            public Long totalWaived;
            @Schema(example = "0")
            public Long totalWrittenOff;
            @Schema(example = "1258000")
            public Long totalOutstanding;
            @Schema(example = "1048333.3")
            public Double totalOverdue;
            @Schema(example = "[2012, 5, 10]")
            public LocalDate overdueSinceDate;
            public GetLoansLoanIdLinkedAccount linkedAccount;
            public Set<GetLoansLoanIdDisbursementDetails> disbursementDetails;
            @Schema(example = "1100.000000")
            public Float fixedEmiAmount;
            @Schema(example = "35000")
            public Long maxOutstandingLoanBalance;
            @Schema(example = "false")
            public Boolean canDisburse;
            public Set<GetLoansLoanIdEmiVariations> emiAmountVariations;
            @Schema(example = "true")
            public Boolean inArrears;
            @Schema(example = "false")
            public Boolean isNPA;
            public Set<GetLoansLoanIdOverdueCharges> overdueCharges;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "000000001")
        public Long accountNo;
        public GetLoansLoanIdStatus status;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "Kampala first Client")
        public String clientName;
        @Schema(example = "2")
        public Integer clientOfficeId;
        @Schema(example = "1")
        public Integer loanProductId;
        @Schema(example = "Kampala Product (with cash accounting)")
        public String loanProductName;
        @Schema(example = "Typical Kampala loan product with cash accounting enabled for testing.")
        public String loanProductDescription;
        @Schema(example = "22")
        public Integer loanPurposeId;
        @Schema(example = "option.HousingImprovement")
        public String loanPurposeName;
        @Schema(example = "2")
        public Integer loanOfficerId;
        @Schema(example = "LoanOfficer, Kampala")
        public String loanOfficerName;
        public GetLoansLoanIdLoanType loanType;
        public GetLoansLoanIdCurrency currency;
        @Schema(example = "1000000")
        public Long principal;
        @Schema(example = "12")
        public Integer termFrequency;
        public GetLoansLoanIdTermPeriodFrequencyType termPeriodFrequencyType;
        @Schema(example = "12")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        public GetLoansLoanIdRepaymentFrequencyType repaymentFrequencyType;
        @Schema(example = "24")
        public Integer interestRatePerPeriod;
        public GetLoansLoanIdInterestRateFrequencyType interestRateFrequencyType;
        @Schema(example = "24")
        public Integer annualInterestRate;
        public GetLoansLoanIdAmortizationType amortizationType;
        public GetLoansLoanIdInterestType interestType;
        public GetLoansLoanIdInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "2")
        public Integer transactionProcessingStrategyId;
        public GetLoansLoanIdTimeline timeline;
        public GetLoansLoanIdSummary summary;
    }

    @Schema(description = "GetSelfLoansLoanIdTransactionsTransactionIdResponse")
    public static final class GetSelfLoansLoanIdTransactionsTransactionIdResponse {

        private GetSelfLoansLoanIdTransactionsTransactionIdResponse() {}

        static final class GetSelfLoansLoanIdTransactionsType {

            private GetSelfLoansLoanIdTransactionsType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "loanTransactionType.repayment")
            public String code;
            @Schema(example = "Repayment")
            public String description;
            @Schema(example = "false")
            public Boolean disbursement;
            @Schema(example = "false")
            public Boolean repaymentAtDisbursement;
            @Schema(example = "true")
            public Boolean repayment;
            @Schema(example = "false")
            public Boolean contra;
            @Schema(example = "false")
            public Boolean waiveInterest;
            @Schema(example = "false")
            public Boolean waiveCharges;
            @Schema(example = "false")
            public Boolean writeOff;
            @Schema(example = "false")
            public Boolean recoveryRepayment;
        }

        @Schema(example = "3")
        public Integer id;
        public GetSelfLoansLoanIdTransactionsType type;
        @Schema(example = "[2012, 5, 14]")
        public LocalDate date;
        @Schema(example = "false")
        public Boolean manuallyReversed;
        public GetSelfLoansLoanIdResponse.GetLoansLoanIdSummary.GetLoansLoanIdOverdueCharges.GetLoanCurrency currency;
        @Schema(example = "559.88")
        public Float amount;
        @Schema(example = "559.88")
        public Float interestPortion;
    }

    @Schema(description = "GetSelfLoansLoanIdChargesResponse")
    public static final class GetSelfLoansLoanIdChargesResponse {

        private GetSelfLoansLoanIdChargesResponse() {}

        static final class GetSelfLoansChargeTimeType {

            private GetSelfLoansChargeTimeType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeTimeType.disbursement")
            public String code;
            @Schema(example = "Disbursement")
            public String description;
        }

        static final class GetSelfLoansChargeCalculationType {

            private GetSelfLoansChargeCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "1")
        public Integer chargeId;
        @Schema(example = "Loan Processing fee")
        public String name;
        public GetSelfLoansChargeTimeType chargeTimeType;
        public GetSelfLoansChargeCalculationType chargeCalculationType;
        @Schema(example = "0")
        public Double percentage;
        @Schema(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSelfLoansLoanIdResponse.GetLoansLoanIdSummary.GetLoansLoanIdOverdueCharges.GetLoanCurrency currency;
        @Schema(example = "100")
        public Float amount;
        @Schema(example = "0")
        public Float amountPaid;
        @Schema(example = "0")
        public Float amountWaived;
        @Schema(example = "0")
        public Float amountWrittenOff;
        @Schema(example = "100")
        public Float amountOutstanding;
        @Schema(example = "100")
        public Float amountOrPercentage;
        @Schema(example = "false")
        public Boolean penalty;
    }

    @Schema(description = "GetSelfLoansTemplateResponse")
    public static final class GetSelfLoansTemplateResponse {

        private GetSelfLoansTemplateResponse() {}

        static final class GetSelfLoansTimeline {

            private GetSelfLoansTimeline() {}

            @Schema(example = "[2013, 3, 8]")
            public LocalDate expectedDisbursementDate;
        }

        static final class GetSelfLoansProductOptions {

            private GetSelfLoansProductOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Kampala Product (with cash accounting)")
            public String name;
        }

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "Kampala first Client")
        public String clientName;
        @Schema(example = "2")
        public Integer clientOfficeId;
        public GetSelfLoansTimeline timeline;
        public Set<GetSelfLoansProductOptions> productOptions;
    }

    @Schema(description = "PostSelfLoansRequest")
    public static final class PostSelfLoansRequest {

        private PostSelfLoansRequest() {}

        static final class PostSelfLoansDisbursementData {

            private PostSelfLoansDisbursementData() {}

            @Schema(example = "01 November 2013")
            public String expectedDisbursementDate;
            @Schema(example = "22000")
            public Long principal;
            @Schema(example = "22000")
            public Long approvedPrincipal;
        }

        static final class PostSelfLoansDatatables {

            private PostSelfLoansDatatables() {}

            static final class PostSelfLoansData {

                private PostSelfLoansData() {}

                @Schema(example = "en")
                public String locale;
                @Schema(example = "01 December 2016 00:00")
                public String Activation_Date;
                @Schema(example = "dd MMMM yyyy HH:mm")
                public String dateFormat;
            }

            @Schema(example = "Date Loan Field")
            public String registeredTableName;
            public PostSelfLoansData data;
        }

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "10,000.00")
        public Double principal;
        @Schema(example = "12")
        public Integer loanTermFrequency;
        @Schema(example = "2")
        public Integer loanTermFrequencyType;
        @Schema(example = "individual")
        public String loanType;
        @Schema(example = "10")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "2")
        public Integer repaymentFrequencyType;
        @Schema(example = "10")
        public Integer interestRatePerPeriod;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "1")
        public Integer interestCalculationPeriodType;
        @Schema(example = "1")
        public Integer transactionProcessingStrategyId;
        @Schema(example = "10 Jun 2013")
        public String expectedDisbursementDate;
        @Schema(example = "10 Jun 2013")
        public String submittedOnDate;
        @Schema(example = "1")
        public Integer linkAccountId;
        @Schema(example = "1100")
        public Integer fixedEmiAmount;
        @Schema(example = "35000")
        public Long maxOutstandingLoanBalance;
        public Set<PostSelfLoansDisbursementData> disbursementData;
        public Set<PostSelfLoansDatatables> datatables;
    }

    @Schema(description = "PostSelfLoansResponse")
    public static final class PostSelfLoansResponse {

        private PostSelfLoansResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer loanId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutSelfLoansLoanIdRequest")
    public static final class PutSelfLoansLoanIdRequest {

        private PutSelfLoansLoanIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "5000")
        public Long principal;
        @Schema(example = "10")
        public Integer loanTermFrequency;
        @Schema(example = "0")
        public Integer loanTermFrequencyType;
        @Schema(example = "10")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "0")
        public Integer repaymentFrequencyType;
        @Schema(example = "2")
        public Integer interestRatePerPeriod;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "0")
        public Integer interestCalculationPeriodType;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "04 March 2014")
        public String expectedDisbursementDate;
        @Schema(example = "1")
        public Integer transactionProcessingStrategyId;
    }

    @Schema(description = "PutSelfLoansLoanIdResponse")
    public static final class PutSelfLoansLoanIdResponse {

        private PutSelfLoansLoanIdResponse() {}

        static final class PutSelfLoansChanges {

            private PutSelfLoansChanges() {}

            @Schema(example = "5000")
            public Long principal;
            @Schema(example = "en")
            public String locale;
        }

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer loanId;
        @Schema(example = "1")
        public Integer resourceId;
        public PutSelfLoansChanges changes;
    }

    @Schema(description = "PostSelfLoansLoanIdRequest")
    public static final class PostSelfLoansLoanIdRequest {

        private PostSelfLoansLoanIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "20 September 2011")
        public String withdrawnOnDate;
        @Schema(example = "Reason loan applicant withdrew from application.")
        public String note;
    }

    @Schema(description = "PostSelfLoansLoanIdResponse")
    public static final class PostSelfLoansLoanIdResponse {

        private PostSelfLoansLoanIdResponse() {}

        static final class PostSelfLoansLoanIdChanges {

            private PostSelfLoansLoanIdChanges() {}

            static final class PostSelfLoansLoanIdStatus {

                private PostSelfLoansLoanIdStatus() {}

                @Schema(example = "400")
                public Integer id;
                @Schema(example = "loanStatusType.withdrawn.by.client")
                public String code;
                @Schema(example = "Withdrawn by applicant")
                public String description;
                @Schema(example = "false")
                public Boolean pendingApproval;
                @Schema(example = "false")
                public Boolean waitingForDisbursal;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean closedObligationsMet;
                @Schema(example = "false")
                public Boolean closedWrittenOff;
                @Schema(example = "false")
                public Boolean closedRescheduled;
                @Schema(example = "false")
                public Boolean closed;
                @Schema(example = "false")
                public Boolean overpaid;
            }

            public PostSelfLoansLoanIdStatus status;
            @Schema(example = "en")
            public String locale;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "20 September 2011")
            public String withdrawnOnDate;
            @Schema(example = "20 September 2011")
            public String closedOnDate;
        }

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "2")
        public Integer loanId;
        @Schema(example = "2")
        public Integer resourceId;
        public PostSelfLoansLoanIdChanges changes;
    }
}
