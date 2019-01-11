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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/09/17.
 */
final class LoansApiResourceSwagger {
    private LoansApiResourceSwagger() {
    }

    @ApiModel(value = "GetLoansTemplateResponse")
    public final static class GetLoansTemplateResponse {
        private GetLoansTemplateResponse() {
        }

        final class GetLoansTemplateTimeline {
            private GetLoansTemplateTimeline() {
            }

            @ApiModelProperty(example = "[2013, 3, 8]")
            public LocalDate expectedDisbursementDate;
        }

        final class GetLoansTemplateProductOptions {
            private GetLoansTemplateProductOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Kampala Product (with cash accounting)")
            public String name;
        }

        @ApiModelProperty(example = "1")
        public Long clientId;
        @ApiModelProperty(example = "Kampala first Client")
        public String clientName;
        @ApiModelProperty(example = "2")
        public Integer clientOfficeId;
        public GetLoansTemplateTimeline timeline;
        public Set<GetLoansTemplateProductOptions> productOptions;
    }

    @ApiModel(value = "GetLoansLoanIdResponse")
    public final static class GetLoansLoanIdResponse {
        private GetLoansLoanIdResponse() {
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

                @ApiModelProperty(example = "20")
                public Integer id;
                @ApiModelProperty(example = "overdraft penality")
                public String name;
                @ApiModelProperty(example = "true")
                public Boolean active;
                @ApiModelProperty(example = "true")
                public Boolean penalty;
                public LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.GetLoanChargeCurrency currency;
                @ApiModelProperty(example = "3.000000")
                public Float amount;
                public GetLoansLoanIdChargeTimeType chargeTimeType;
                public LoanChargesApiResourceSwagger.GetLoansLoanIdChargesTemplateResponse.GetLoanChargeTemplateChargeOptions.GetLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
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

    @ApiModel(value = "GetLoansResponse")
    public final static class GetLoansResponse {
        private GetLoansResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetLoansLoanIdResponse> pageItems;
    }

    @ApiModel(value = "PostLoansRequest")
    public static final class PostLoansRequest {
        private PostLoansRequest() {
        }

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "100,000.00")
        public Double principal;
        @ApiModelProperty(example = "12")
        public Integer loanTermFrequency;
        @ApiModelProperty(example = "2")
        public Integer loanTermFrequencyType;
        @ApiModelProperty(example = "12")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "1")
        public Integer repaymentEvery;
        @ApiModelProperty(example = "2")
        public Integer repaymentFrequencyType;
        @ApiModelProperty(example = "2")
        public Integer interestRatePerPeriod;
        @ApiModelProperty(example = "1")
        public Integer amortizationType;
        @ApiModelProperty(example = "0")
        public Integer interestType;
        @ApiModelProperty(example = "1")
        public Integer interestCalculationPeriodType;
        @ApiModelProperty(example = "20 September 2011")
        public String expectedDisbursementDate;
        @ApiModelProperty(example = "2")
        public Integer transactionProcessingStrategyId;
    }

    @ApiModel(value = "PostLoansResponse")
    public static final class PostLoansResponse {
        private PostLoansResponse() {
        }

        final class PostLoansRepaymentSchedulePeriods {
            private PostLoansRepaymentSchedulePeriods() {
            }

            @ApiModelProperty(example = "0")
            public Integer period;
            @ApiModelProperty(example = "[2011, 9, 20]")
            public LocalDate dueDate;
            @ApiModelProperty(example = "100000")
            public Long principalDisbursed;
            @ApiModelProperty(example = "100000")
            public Long principalLoanBalanceOutstanding;
            @ApiModelProperty(example = "0")
            public Long feeChargesDue;
            @ApiModelProperty(example = "0")
            public Long feeChargesOutstanding;
            @ApiModelProperty(example = "0")
            public Long totalOriginalDueForPeriod;
            @ApiModelProperty(example = "0")
            public Long totalDueForPeriod;
            @ApiModelProperty(example = "0")
            public Long totalOutstandingForPeriod;
            @ApiModelProperty(example = "0")
            public Long totalOverdue;
            @ApiModelProperty(example = "0")
            public Long totalActualCostOfLoanForPeriod;
        }

        public GetLoansLoanIdResponse.GetLoansLoanIdCurrency currency;
        @ApiModelProperty(example = "366")
        public Integer loanTermInDays;
        @ApiModelProperty(example = "100000")
        public Long totalPrincipalDisbursed;
        @ApiModelProperty(example = "100000")
        public Long totalPrincipalExpected;
        @ApiModelProperty(example = "0")
        public Long totalPrincipalPaid;
        @ApiModelProperty(example = "13471.52")
        public Double totalInterestCharged;
        @ApiModelProperty(example = "0")
        public Long totalFeeChargesCharged;
        @ApiModelProperty(example = "0")
        public Long totalPenaltyChargesCharged;
        @ApiModelProperty(example = "0")
        public Long totalWaived;
        @ApiModelProperty(example = "0")
        public Long totalWrittenOff;
        @ApiModelProperty(example = "113471.52")
        public Double totalRepaymentExpected;
        @ApiModelProperty(example = "0")
        public Long totalRepayment;
        @ApiModelProperty(example = "0")
        public Long totalOutstanding;
        public Set<PostLoansRepaymentSchedulePeriods> periods;
    }

    @ApiModel(value = "PutLoansLoanIdRequest")
    public final static class PutLoansLoanIdRequest {
        private PutLoansLoanIdRequest() {
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

    @ApiModel(value = "PutLoansLoanIdResponse")
    public final static class PutLoansLoanIdResponse {
        private PutLoansLoanIdResponse() {
        }

        final class PutLoansLoanIdChanges {
            private PutLoansLoanIdChanges() {
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
        public PutLoansLoanIdChanges changes;
    }

    @ApiModel(value = "DeleteLoansLoanIdResponse")
    public static final class DeleteLoansLoanIdResponse {
        private DeleteLoansLoanIdResponse() {
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

    @ApiModel(value = "PostLoansLoanIdRequest")
    public final static class PostLoansLoanIdRequest {
        private PostLoansLoanIdRequest() {
        }

        @ApiModelProperty(example = "2")
        public Integer toLoanOfficerId;
        @ApiModelProperty(example = "02 September 2014")
        public String assignmentDate;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "")
        public Integer fromLoanOfficerId;
    }

    @ApiModel(value = "PostLoansLoanIdResponse")
    public static final class PostLoansLoanIdResponse {
        private PostLoansLoanIdResponse() {
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "6")
        public Integer clientId;
        @ApiModelProperty(example = "3")
        public Integer loanId;
        @ApiModelProperty(example = "3")
        public Integer resourceId;
    }
}
