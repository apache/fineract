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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/09/17.
 */
final class LoansApiResourceSwagger {

    private LoansApiResourceSwagger() {}

    @Schema(description = "GetLoansTemplateResponse")
    public static final class GetLoansTemplateResponse {

        private GetLoansTemplateResponse() {}

        static final class GetLoansTemplateTimeline {

            private GetLoansTemplateTimeline() {}

            @Schema(example = "[2013, 3, 8]")
            public LocalDate expectedDisbursementDate;
        }

        static final class GetLoansTemplateProductOptions {

            private GetLoansTemplateProductOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Kampala Product (with cash accounting)")
            public String name;
        }

        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "Kampala first Client")
        public String clientName;
        @Schema(example = "2")
        public Integer clientOfficeId;
        public GetLoansTemplateTimeline timeline;
        public Set<GetLoansTemplateProductOptions> productOptions;
    }

    @Schema(description = "GetLoansLoanIdResponse")
    public static final class GetLoansLoanIdResponse {

        private GetLoansLoanIdResponse() {}

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

                @Schema(example = "20")
                public Integer id;
                @Schema(example = "overdraft penality")
                public String name;
                @Schema(example = "true")
                public Boolean active;
                @Schema(example = "true")
                public Boolean penalty;
                public LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.GetLoanChargeCurrency currency;
                @Schema(example = "3.000000")
                public Float amount;
                public GetLoansLoanIdChargeTimeType chargeTimeType;
                public LoanChargesApiResourceSwagger.GetLoansLoanIdChargesTemplateResponse.GetLoanChargeTemplateChargeOptions.GetLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
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
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        public GetLoansLoanIdInterestType interestType;
        public GetLoansLoanIdInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "2")
        public Integer transactionProcessingStrategyId;
        public GetLoansLoanIdTimeline timeline;
        public GetLoansLoanIdSummary summary;
    }

    @Schema(description = "GetLoansResponse")
    public static final class GetLoansResponse {

        private GetLoansResponse() {}

        @Schema(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetLoansLoanIdResponse> pageItems;
    }

    @Schema(description = "PostLoansRequest")
    public static final class PostLoansRequest {

        private PostLoansRequest() {}

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "100,000.00")
        public Double principal;
        @Schema(example = "12")
        public Integer loanTermFrequency;
        @Schema(example = "2")
        public Integer loanTermFrequencyType;
        @Schema(example = "12")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "2")
        public Integer repaymentFrequencyType;
        @Schema(example = "2")
        public Integer interestRatePerPeriod;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "1")
        public Integer interestCalculationPeriodType;
        @Schema(example = "20 September 2011")
        public String expectedDisbursementDate;
        @Schema(example = "2")
        public Integer transactionProcessingStrategyId;
        @Schema(example = "360", allowableValues = "1, 360, 364, 36")
        public Integer daysInYearType;
    }

    @Schema(description = "PostLoansResponse")
    public static final class PostLoansResponse {

        private PostLoansResponse() {}

        static final class PostLoansRepaymentSchedulePeriods {

            private PostLoansRepaymentSchedulePeriods() {}

            @Schema(example = "0")
            public Integer period;
            @Schema(example = "[2011, 9, 20]")
            public LocalDate dueDate;
            @Schema(example = "100000")
            public Long principalDisbursed;
            @Schema(example = "100000")
            public Long principalLoanBalanceOutstanding;
            @Schema(example = "0")
            public Long feeChargesDue;
            @Schema(example = "0")
            public Long feeChargesOutstanding;
            @Schema(example = "0")
            public Long totalOriginalDueForPeriod;
            @Schema(example = "0")
            public Long totalDueForPeriod;
            @Schema(example = "0")
            public Long totalOutstandingForPeriod;
            @Schema(example = "0")
            public Long totalOverdue;
            @Schema(example = "0")
            public Long totalActualCostOfLoanForPeriod;
        }

        public GetLoansLoanIdResponse.GetLoansLoanIdCurrency currency;
        @Schema(example = "366")
        public Integer loanTermInDays;
        @Schema(example = "100000")
        public Long totalPrincipalDisbursed;
        @Schema(example = "100000")
        public Long totalPrincipalExpected;
        @Schema(example = "0")
        public Long totalPrincipalPaid;
        @Schema(example = "13471.52")
        public Double totalInterestCharged;
        @Schema(example = "0")
        public Long totalFeeChargesCharged;
        @Schema(example = "0")
        public Long totalPenaltyChargesCharged;
        @Schema(example = "0")
        public Long totalWaived;
        @Schema(example = "0")
        public Long totalWrittenOff;
        @Schema(example = "113471.52")
        public Double totalRepaymentExpected;
        @Schema(example = "0")
        public Long totalRepayment;
        @Schema(example = "0")
        public Long totalOutstanding;
        public Set<PostLoansRepaymentSchedulePeriods> periods;
    }

    @Schema(description = "PutLoansLoanIdRequest")
    public static final class PutLoansLoanIdRequest {

        private PutLoansLoanIdRequest() {}

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
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        @Schema(example = "04 March 2014")
        public String expectedDisbursementDate;
        @Schema(example = "1")
        public Integer transactionProcessingStrategyId;
    }

    @Schema(description = "PutLoansLoanIdResponse")
    public static final class PutLoansLoanIdResponse {

        private PutLoansLoanIdResponse() {}

        static final class PutLoansLoanIdChanges {

            private PutLoansLoanIdChanges() {}

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
        public PutLoansLoanIdChanges changes;
    }

    @Schema(description = "DeleteLoansLoanIdResponse")
    public static final class DeleteLoansLoanIdResponse {

        private DeleteLoansLoanIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer loanId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PostLoansLoanIdRequest")
    public static final class PostLoansLoanIdRequest {

        private PostLoansLoanIdRequest() {}

        @Schema(example = "2")
        public Integer toLoanOfficerId;
        @Schema(example = "02 September 2014")
        public String assignmentDate;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "")
        public Integer fromLoanOfficerId;
    }

    @Schema(description = "PostLoansLoanIdResponse")
    public static final class PostLoansLoanIdResponse {

        private PostLoansLoanIdResponse() {}

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "6")
        public Integer clientId;
        @Schema(example = "3")
        public Integer loanId;
        @Schema(example = "3")
        public Integer resourceId;
    }
}
