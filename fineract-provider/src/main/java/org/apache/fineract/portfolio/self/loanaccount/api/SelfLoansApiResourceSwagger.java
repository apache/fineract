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
package org.apache.fineract.portfolio.self.loanaccount.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/19/17.
 */
final class SelfLoansApiResourceSwagger {
    private SelfLoansApiResourceSwagger() {
    }

    @ApiModel(value = "GetSelfLoansLoanIdResponse")
    public final static class GetSelfLoansLoanIdResponse {
        private GetSelfLoansLoanIdResponse() {
        }

        final class GetLoansLoanIdStatus {
            private GetLoansLoanIdStatus() {
            }

            @ApiModelProperty(example = "300")
            public Integer id;
            @ApiModelProperty(example = "loanStatusType.active")
            public String code;
            @ApiModelProperty(example = "Active")
            public String value;
            @ApiModelProperty(example = "false")
            public Boolean pendingApproval;
            @ApiModelProperty(example = "false")
            public Boolean waitingForDisbursal;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean closedObligationsMet;
            @ApiModelProperty(example = "false")
            public Boolean closedWrittenOff;
            @ApiModelProperty(example = "false")
            public Boolean closedRescheduled;
            @ApiModelProperty(example = "false")
            public Boolean closed;
            @ApiModelProperty(example = "false")
            public Boolean overpaid;
        }

        final class GetLoansLoanIdLoanType {
            private GetLoansLoanIdLoanType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "loanType.individual")
            public String code;
            @ApiModelProperty(example = "Individual")
            public String value;
        }

        final class GetLoansLoanIdCurrency {
            private GetLoansLoanIdCurrency() {
            }

            @ApiModelProperty(example = "UGX")
            public String code;
            @ApiModelProperty(example = "Uganda Shilling")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "USh")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.UGX")
            public String nameCode;
            @ApiModelProperty(example = "Uganda Shilling (USh)")
            public String displayLabel;
        }

        final class GetLoansLoanIdTermPeriodFrequencyType {
            private GetLoansLoanIdTermPeriodFrequencyType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "termFrequency.periodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetLoansLoanIdRepaymentFrequencyType {
            private GetLoansLoanIdRepaymentFrequencyType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "repaymentFrequency.periodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetLoansLoanIdInterestRateFrequencyType {
            private GetLoansLoanIdInterestRateFrequencyType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "interestRateFrequency.periodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Per year")
            public String value;
        }

        final class GetLoansLoanIdAmortizationType {
            private GetLoansLoanIdAmortizationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "amortizationType.equal.installments")
            public String code;
            @ApiModelProperty(example = "Equal installments")
            public String value;
        }

        final class GetLoansLoanIdInterestType {
            private GetLoansLoanIdInterestType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "interestType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        final class GetLoansLoanIdInterestCalculationPeriodType {
            private GetLoansLoanIdInterestCalculationPeriodType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "interestCalculationPeriodType.same.as.repayment.period")
            public String code;
            @ApiModelProperty(example = "Same as repayment period")
            public String value;
        }

        final class GetLoansLoanIdTimeline {
            private GetLoansLoanIdTimeline() {
            }

            @ApiModelProperty(example = "[2012, 4, 3]")
            public LocalDate submittedOnDate;
            @ApiModelProperty(example = "admin")
            public String submittedByUsername;
            @ApiModelProperty(example = "App")
            public String submittedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String submittedByLastname;
            @ApiModelProperty(example = "[2012, 4, 3]")
            public LocalDate approvedOnDate;
            @ApiModelProperty(example = "admin")
            public String approvedByUsername;
            @ApiModelProperty(example = "App")
            public String approvedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String approvedByLastname;
            @ApiModelProperty(example = "[2012, 4, 10]")
            public LocalDate expectedDisbursementDate;
            @ApiModelProperty(example = "[2012, 4, 10]")
            public LocalDate actualDisbursementDate;
            @ApiModelProperty(example = "admin")
            public String disbursedByUsername;
            @ApiModelProperty(example = "App")
            public String disbursedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String disbursedByLastname;
            @ApiModelProperty(example = "[2012, 4, 10]")
            public LocalDate expectedMaturityDate;
        }

        final class GetLoansLoanIdSummary {
            private GetLoansLoanIdSummary() {
            }

            final class GetLoansLoanIdEmiVariations {
                private GetLoansLoanIdEmiVariations() {
                }
            }

            final class GetLoansLoanIdLinkedAccount {
                private GetLoansLoanIdLinkedAccount() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "000000001")
                public Long accountNo;
            }

            final class GetLoansLoanIdDisbursementDetails {
                private GetLoansLoanIdDisbursementDetails() {
                }

                @ApiModelProperty(example = "71")
                public Integer id;
                @ApiModelProperty(example = "[2013, 11, 1]")
                public LocalDate expectedDisbursementDate;
                @ApiModelProperty(example = "22000.000000")
                public Float principal;
                @ApiModelProperty(example = "22000.000000")
                public Float approvedPrincipal;
            }

            final class GetLoansLoanIdOverdueCharges {
                private GetLoansLoanIdOverdueCharges() {
                }

                final class GetLoansLoanIdChargeTimeType {
                    private GetLoansLoanIdChargeTimeType() {
                    }

                    @ApiModelProperty(example = "9")
                    public Integer id;
                    @ApiModelProperty(example = "chargeTimeType.overdueInstallment")
                    public String code;
                    @ApiModelProperty(example = "overdue fees")
                    public String value;
                }

                final class GetLoansLoanIdChargeCalculationType {
                    private GetLoansLoanIdChargeCalculationType() {
                    }

                    @ApiModelProperty(example = "2")
                    public Integer id;
                    @ApiModelProperty(example = "chargeCalculationType.percent.of.amount")
                    public String code;
                    @ApiModelProperty(example = "% Amount")
                    public String value;
                }

                final class GetLoansLoanIdChargePaymentMode {
                    private GetLoansLoanIdChargePaymentMode() {
                    }

                    @ApiModelProperty(example = "0")
                    public Integer id;
                    @ApiModelProperty(example = "chargepaymentmode.regular")
                    public String code;
                    @ApiModelProperty(example = "Regular")
                    public String value;
                }

                final class GetLoansLoanIdFeeFrequency {
                    private GetLoansLoanIdFeeFrequency() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "feeFrequencyperiodFrequencyType.weeks")
                    public String code;
                    @ApiModelProperty(example = "Weeks")
                    public String value;
                }

                final class GetLoanCurrency {
                    private GetLoanCurrency() {
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

                final class GetLoanChargeTemplateChargeAppliesTo {
                    private GetLoanChargeTemplateChargeAppliesTo() {
                    }

                    @ApiModelProperty(example = "1  ")
                    public Integer id;
                    @ApiModelProperty(example = "chargeAppliesTo.loan")
                    public String code;
                    @ApiModelProperty(example = "Loan")
                    public String value;
                }

                @ApiModelProperty(example = "20")
                public Integer id;
                @ApiModelProperty(example = "overdraft penality")
                public String name;
                @ApiModelProperty(example = "true")
                public Boolean active;
                @ApiModelProperty(example = "true")
                public Boolean penalty;
                public GetLoanCurrency currency;
                @ApiModelProperty(example = "3.000000")
                public Float amount;
                public GetLoansLoanIdChargeTimeType chargeTimeType;
                public GetLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
                public GetLoansLoanIdChargeCalculationType chargeCalculationType;
                public GetLoansLoanIdChargePaymentMode chargePaymentMode;
                @ApiModelProperty(example = "2")
                public Integer feeInterval;
                public GetLoansLoanIdFeeFrequency feeFrequency;
            }

            public GetLoansLoanIdCurrency currency;
            @ApiModelProperty(example = "1000000")
            public Long principalDisbursed;
            @ApiModelProperty(example = "0")
            public Long principalPaid;
            @ApiModelProperty(example = "0")
            public Long principalWrittenOff;
            @ApiModelProperty(example = "1000000")
            public Long principalOutstanding;
            @ApiModelProperty(example = "833333.3")
            public Double principalOverdue;
            @ApiModelProperty(example = "240000")
            public Long interestCharged;
            @ApiModelProperty(example = "0")
            public Long interestPaid;
            @ApiModelProperty(example = "0")
            public Long interestWaived;
            @ApiModelProperty(example = "0")
            public Long interestWrittenOff;
            @ApiModelProperty(example = "240000")
            public Long interestOutstanding;
            @ApiModelProperty(example = "200000")
            public Long interestOverdue;
            @ApiModelProperty(example = "18000")
            public Long feeChargesCharged;
            @ApiModelProperty(example = "0")
            public Long feeChargesDueAtDisbursementCharged;
            @ApiModelProperty(example = "0")
            public Long feeChargesPaid;
            @ApiModelProperty(example = "0")
            public Long feeChargesWaived;
            @ApiModelProperty(example = "0")
            public Long feeChargesWrittenOff;
            @ApiModelProperty(example = "18000")
            public Long feeChargesOutstanding;
            @ApiModelProperty(example = "15000")
            public Long feeChargesOverdue;
            @ApiModelProperty(example = "0")
            public Long penaltyChargesCharged;
            @ApiModelProperty(example = "0")
            public Long penaltyChargesPaid;
            @ApiModelProperty(example = "0")
            public Long penaltyChargesWaived;
            @ApiModelProperty(example = "0")
            public Long penaltyChargesWrittenOff;
            @ApiModelProperty(example = "0")
            public Long penaltyChargesOutstanding;
            @ApiModelProperty(example = "0")
            public Long penaltyChargesOverdue;
            @ApiModelProperty(example = "1258000")
            public Long totalExpectedRepayment;
            @ApiModelProperty(example = "0")
            public Long totalRepayment;
            @ApiModelProperty(example = "258000")
            public Long totalExpectedCostOfLoan;
            @ApiModelProperty(example = "0")
            public Long totalCostOfLoan;
            @ApiModelProperty(example = "0")
            public Long totalWaived;
            @ApiModelProperty(example = "0")
            public Long totalWrittenOff;
            @ApiModelProperty(example = "1258000")
            public Long totalOutstanding;
            @ApiModelProperty(example = "1048333.3")
            public Double totalOverdue;
            @ApiModelProperty(example = "[2012, 5, 10]")
            public LocalDate overdueSinceDate;
            public GetLoansLoanIdLinkedAccount linkedAccount;
            public Set<GetLoansLoanIdDisbursementDetails> disbursementDetails;
            @ApiModelProperty(example = "1100.000000")
            public Float fixedEmiAmount;
            @ApiModelProperty(example = "35000")
            public Long maxOutstandingLoanBalance;
            @ApiModelProperty(example = "false")
            public Boolean canDisburse;
            public Set<GetLoansLoanIdEmiVariations> emiAmountVariations;
            @ApiModelProperty(example = "true")
            public Boolean inArrears;
            @ApiModelProperty(example = "false")
            public Boolean isNPA;
            public Set<GetLoansLoanIdOverdueCharges> overdueCharges;
        }

        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "000000001")
        public Long accountNo;
        public GetLoansLoanIdStatus status;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "Kampala first Client")
        public String clientName;
        @ApiModelProperty(example = "2")
        public Integer clientOfficeId;
        @ApiModelProperty(example = "1")
        public Integer loanProductId;
        @ApiModelProperty(example = "Kampala Product (with cash accounting)")
        public String loanProductName;
        @ApiModelProperty(example = "Typical Kampala loan product with cash accounting enabled for testing.")
        public String loanProductDescription;
        @ApiModelProperty(example = "22")
        public Integer loanPurposeId;
        @ApiModelProperty(example = "option.HousingImprovement")
        public String loanPurposeName;
        @ApiModelProperty(example = "2")
        public Integer loanOfficerId;
        @ApiModelProperty(example = "LoanOfficer, Kampala")
        public String loanOfficerName;
        public GetLoansLoanIdLoanType loanType;
        public GetLoansLoanIdCurrency currency;
        @ApiModelProperty(example = "1000000")
        public Long principal;
        @ApiModelProperty(example = "12")
        public Integer termFrequency;
        public GetLoansLoanIdTermPeriodFrequencyType termPeriodFrequencyType;
        @ApiModelProperty(example = "12")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "1")
        public Integer repaymentEvery;
        public GetLoansLoanIdRepaymentFrequencyType repaymentFrequencyType;
        @ApiModelProperty(example = "24")
        public Integer interestRatePerPeriod;
        public GetLoansLoanIdInterestRateFrequencyType interestRateFrequencyType;
        @ApiModelProperty(example = "24")
        public Integer annualInterestRate;
        public GetLoansLoanIdAmortizationType amortizationType;
        public GetLoansLoanIdInterestType interestType;
        public GetLoansLoanIdInterestCalculationPeriodType interestCalculationPeriodType;
        @ApiModelProperty(example = "2")
        public Integer transactionProcessingStrategyId;
        public GetLoansLoanIdTimeline timeline;
        public GetLoansLoanIdSummary summary;
    }

    @ApiModel(value = "GetSelfLoansLoanIdTransactionsTransactionIdResponse")
    public final static class GetSelfLoansLoanIdTransactionsTransactionIdResponse {
        private GetSelfLoansLoanIdTransactionsTransactionIdResponse() {
        }

        final class GetSelfLoansLoanIdTransactionsType {
            private GetSelfLoansLoanIdTransactionsType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "loanTransactionType.repayment")
            public String code;
            @ApiModelProperty(example = "Repayment")
            public String value;
            @ApiModelProperty(example = "false")
            public Boolean disbursement;
            @ApiModelProperty(example = "false")
            public Boolean repaymentAtDisbursement;
            @ApiModelProperty(example = "true")
            public Boolean repayment;
            @ApiModelProperty(example = "false")
            public Boolean contra;
            @ApiModelProperty(example = "false")
            public Boolean waiveInterest;
            @ApiModelProperty(example = "false")
            public Boolean waiveCharges;
            @ApiModelProperty(example = "false")
            public Boolean writeOff;
            @ApiModelProperty(example = "false")
            public Boolean recoveryRepayment;
        }

        @ApiModelProperty(example = "3")
        public Integer id;
        public GetSelfLoansLoanIdTransactionsType type;
        @ApiModelProperty(example = "[2012, 5, 14]")
        public LocalDate date;
        @ApiModelProperty(example = "false")
        public Boolean manuallyReversed;
        public GetSelfLoansLoanIdResponse.GetLoansLoanIdSummary.GetLoansLoanIdOverdueCharges.GetLoanCurrency currency;
        @ApiModelProperty(example = "559.88")
        public Float amount;
        @ApiModelProperty(example = "559.88")
        public Float interestPortion;
    }

    @ApiModel(value = "GetSelfLoansLoanIdChargesResponse")
    public final static class GetSelfLoansLoanIdChargesResponse {
        private GetSelfLoansLoanIdChargesResponse() {
        }

        final class GetSelfLoansChargeTimeType {
            private GetSelfLoansChargeTimeType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeTimeType.disbursement")
            public String code;
            @ApiModelProperty(example = "Disbursement")
            public String value;
        }

        final class GetSelfLoansChargeCalculationType {
            private GetSelfLoansChargeCalculationType() {
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
        @ApiModelProperty(example = "1")
        public Integer chargeId;
        @ApiModelProperty(example = "Loan Processing fee")
        public String name;
        public GetSelfLoansChargeTimeType chargeTimeType;
        public GetSelfLoansChargeCalculationType chargeCalculationType;
        @ApiModelProperty(example = "0")
        public Double percentage;
        @ApiModelProperty(example = "0")
        public Double amountPercentageAppliedTo;
        public GetSelfLoansLoanIdResponse.GetLoansLoanIdSummary.GetLoansLoanIdOverdueCharges.GetLoanCurrency currency;
        @ApiModelProperty(example = "100")
        public Float amount;
        @ApiModelProperty(example = "0")
        public Float amountPaid;
        @ApiModelProperty(example = "0")
        public Float amountWaived;
        @ApiModelProperty(example = "0")
        public Float amountWrittenOff;
        @ApiModelProperty(example = "100")
        public Float amountOutstanding;
        @ApiModelProperty(example = "100")
        public Float amountOrPercentage;
        @ApiModelProperty(example = "false")
        public Boolean penalty;
    }

    @ApiModel(value = "GetSelfLoansTemplateResponse")
    public final static class GetSelfLoansTemplateResponse {
        private GetSelfLoansTemplateResponse() {
        }

        final class GetSelfLoansTimeline {
            private GetSelfLoansTimeline() {
            }

            @ApiModelProperty(example = "[2013, 3, 8]")
            public LocalDate expectedDisbursementDate;
        }

        final class GetSelfLoansProductOptions {
            private GetSelfLoansProductOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Kampala Product (with cash accounting)")
            public String name;
        }

        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "Kampala first Client")
        public String clientName;
        @ApiModelProperty(example = "2")
        public Integer clientOfficeId;
        public GetSelfLoansTimeline timeline;
        public Set<GetSelfLoansProductOptions> productOptions;
    }

    @ApiModel(value = "PostSelfLoansRequest")
    public final static class PostSelfLoansRequest {
        private PostSelfLoansRequest() {
        }

        final class PostSelfLoansDisbursementData {
            private PostSelfLoansDisbursementData() {
            }

            @ApiModelProperty(example = "01 November 2013")
            public String expectedDisbursementDate;
            @ApiModelProperty(example = "22000")
            public Long principal;
            @ApiModelProperty(example = "22000")
            public Long approvedPrincipal;
        }

        final class PostSelfLoansDatatables {
            private PostSelfLoansDatatables() {
            }

            final class PostSelfLoansData {
                private PostSelfLoansData() {
                }

                @ApiModelProperty(example = "en")
                public String locale;
                @ApiModelProperty(example = "01 December 2016 00:00")
                public String Activation_Date;
                @ApiModelProperty(example = "dd MMMM yyyy HH:mm")
                public String dateFormat;
            }

            @ApiModelProperty(example = "Date Loan Field")
            public String registeredTableName;
            public PostSelfLoansData data;
        }

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "10,000.00")
        public Double principal;
        @ApiModelProperty(example = "12")
        public Integer loanTermFrequency;
        @ApiModelProperty(example = "2")
        public Integer loanTermFrequencyType;
        @ApiModelProperty(example = "individual")
        public String loanType;
        @ApiModelProperty(example = "10")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "1")
        public Integer repaymentEvery;
        @ApiModelProperty(example = "2")
        public Integer repaymentFrequencyType;
        @ApiModelProperty(example = "10")
        public Integer interestRatePerPeriod;
        @ApiModelProperty(example = "1")
        public Integer amortizationType;
        @ApiModelProperty(example = "0")
        public Integer interestType;
        @ApiModelProperty(example = "1")
        public Integer interestCalculationPeriodType;
        @ApiModelProperty(example = "1")
        public Integer transactionProcessingStrategyId;
        @ApiModelProperty(example = "10 Jun 2013")
        public String expectedDisbursementDate;
        @ApiModelProperty(example = "10 Jun 2013")
        public String submittedOnDate;
        @ApiModelProperty(example = "1")
        public Integer linkAccountId;
        @ApiModelProperty(example = "1100")
        public Integer fixedEmiAmount;
        @ApiModelProperty(example = "35000")
        public Long maxOutstandingLoanBalance;
        public Set<PostSelfLoansDisbursementData> disbursementData;
        public Set<PostSelfLoansDatatables> datatables;
    }

    @ApiModel(value = "PostSelfLoansResponse")
    public final static class PostSelfLoansResponse {
        private PostSelfLoansResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer loanId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutSelfLoansLoanIdRequest")
    public final static class PutSelfLoansLoanIdRequest {
        private PutSelfLoansLoanIdRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "5000")
        public Long principal;
        @ApiModelProperty(example = "10")
        public Integer loanTermFrequency;
        @ApiModelProperty(example = "0")
        public Integer loanTermFrequencyType;
        @ApiModelProperty(example = "10")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "1")
        public Integer repaymentEvery;
        @ApiModelProperty(example = "0")
        public Integer repaymentFrequencyType;
        @ApiModelProperty(example = "2")
        public Integer interestRatePerPeriod;
        @ApiModelProperty(example = "0")
        public Integer interestType;
        @ApiModelProperty(example = "0")
        public Integer interestCalculationPeriodType;
        @ApiModelProperty(example = "1")
        public Integer amortizationType;
        @ApiModelProperty(example = "04 March 2014")
        public String expectedDisbursementDate;
        @ApiModelProperty(example = "1")
        public Integer transactionProcessingStrategyId;
    }

    @ApiModel(value = "PutSelfLoansLoanIdResponse")
    public final static class PutSelfLoansLoanIdResponse {
        private PutSelfLoansLoanIdResponse() {
        }

        final class PutSelfLoansChanges {
            private PutSelfLoansChanges() {
            }

            @ApiModelProperty(example = "5000")
            public Long principal;
            @ApiModelProperty(example = "en")
            public String locale;
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer loanId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutSelfLoansChanges changes;
    }

    @ApiModel(value = "PostSelfLoansLoanIdRequest")
    public final static class PostSelfLoansLoanIdRequest {
        private PostSelfLoansLoanIdRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "20 September 2011")
        public String withdrawnOnDate;
        @ApiModelProperty(example = "Reason loan applicant withdrew from application.")
        public String note;
    }

    @ApiModel(value = "PostSelfLoansLoanIdResponse")
    public final static class PostSelfLoansLoanIdResponse {
        private PostSelfLoansLoanIdResponse() {
        }

        final class PostSelfLoansLoanIdChanges {
            private PostSelfLoansLoanIdChanges() {
            }

            final class PostSelfLoansLoanIdStatus {
                private PostSelfLoansLoanIdStatus() {
                }

                @ApiModelProperty(example = "400")
                public Integer id;
                @ApiModelProperty(example = "loanStatusType.withdrawn.by.client")
                public String code;
                @ApiModelProperty(example = "Withdrawn by applicant")
                public String value;
                @ApiModelProperty(example = "false")
                public Boolean pendingApproval;
                @ApiModelProperty(example = "false")
                public Boolean waitingForDisbursal;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean closedObligationsMet;
                @ApiModelProperty(example = "false")
                public Boolean closedWrittenOff;
                @ApiModelProperty(example = "false")
                public Boolean closedRescheduled;
                @ApiModelProperty(example = "false")
                public Boolean closed;
                @ApiModelProperty(example = "false")
                public Boolean overpaid;
            }

            public PostSelfLoansLoanIdStatus status;
            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd MMMM yyyy")
            public String dateFormat;
            @ApiModelProperty(example = "20 September 2011")
            public String withdrawnOnDate;
            @ApiModelProperty(example = "20 September 2011")
            public String closedOnDate;
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "2")
        public Integer loanId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
        public PostSelfLoansLoanIdChanges changes;
    }
}
