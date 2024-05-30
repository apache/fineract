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
package org.apache.fineract.portfolio.loanproduct.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiResourceSwagger.GetDelinquencyBucketsResponse;

/**
 * Created by Chirag Gupta on 12/27/17.
 */
final class LoanProductsApiResourceSwagger {

    private LoanProductsApiResourceSwagger() {}

    @Schema(description = "PostLoanProductsRequest")
    public static final class PostLoanProductsRequest {

        private PostLoanProductsRequest() {}

        @Schema(example = "LP Accrual Accounting")
        public String name;
        @Schema(example = "2075e308-d4a8-44d9-8203-f5a947b8c2f4")
        public String externalId;
        @Schema(example = "LPAA")
        public String shortName;
        @Schema(example = "non-interest bearing product")
        public String description;
        @Schema(example = "10 July 2022")
        public String startDate;
        @Schema(example = "10 July 2022")
        public String closeDate;
        @Schema(example = "3")
        public Long fundId;

        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "2")
        public Integer digitsAfterDecimal;
        @Schema(example = "1")
        public Integer installmentAmountInMultiplesOf;
        @Schema(example = "1")
        public Integer inMultiplesOf;

        // Terms
        @Schema(example = "5000.00")
        public Double minPrincipal;
        @Schema(example = "10000.00")
        public Double principal;
        @Schema(example = "15000.00")
        public Double maxPrincipal;

        @Schema(example = "1")
        public Integer minNumberOfRepayments;
        @Schema(example = "1")
        public Integer maxNumberOfRepayments;
        @Schema(example = "12")
        public Integer numberOfRepayments;
        @Schema(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        @Schema(example = "0")
        public Double minInterestRatePerPeriod;
        @Schema(example = "1.75")
        public Double interestRatePerPeriod;
        @Schema(example = "23.4")
        public Double maxInterestRatePerPeriod;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        @Schema(example = "true")
        public Boolean canDefineInstallmentAmount;
        @Schema(example = "10")
        public Integer fixedLength;

        // Settings
        @Schema(example = "false")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "false")
        public Boolean useBorrowerCycle;
        @Schema(example = "2")
        public Long repaymentFrequencyType;
        @Schema(example = "2")
        public Integer interestRateFrequencyType;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "1")
        public Integer interestCalculationPeriodType;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategyCode;
        public List<AdvancedPaymentData> paymentAllocation;
        public List<CreditAllocationData> creditAllocation;
        @Schema(example = "false")
        public Boolean isLinkedToFloatingInterestRates;
        @Schema(example = "false")
        public Boolean allowVariableInstallments;
        @Schema(example = "30")
        public Integer minimumDaysBetweenDisbursalAndFirstRepayment;
        @Schema(example = "true")
        public Boolean allowApprovedDisbursedAmountsOverApplied;
        @Schema(example = "percentage")
        public String overAppliedCalculationType;
        @Schema(example = "50")
        public Integer overAppliedNumber;
        @Schema(example = "1")
        public Integer daysInMonthType;
        @Schema(example = "1")
        public Integer daysInYearType;
        @Schema(example = "true")
        public Boolean allowPartialPeriodInterestCalcualtion;
        @Schema(example = "179")
        public Integer overdueDaysForNPA;
        @Schema(example = "3")
        public Integer graceOnPrincipalPayment;
        @Schema(example = "3")
        public Integer graceOnInterestPayment;
        @Schema(example = "90")
        public Integer inArrearsTolerance;
        @Schema(example = "3")
        public Integer graceOnArrearsAgeing;
        @Schema(example = "false")
        public Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;
        @Schema(example = "false")
        public Boolean isEqualAmortization;
        @Schema(example = "false")
        public Boolean canUseForTopup;
        @Schema(example = "false")
        public Boolean holdGuaranteeFunds;
        @Schema(example = "1")
        public Long delinquencyBucketId;
        @Schema(example = "false")
        public Boolean enableInstallmentLevelDelinquency;
        @Schema(example = "3")
        public Integer dueDaysForRepaymentEvent;
        @Schema(example = "3")
        public Integer overDueDaysForRepaymentEvent;
        @Schema(example = "false")
        public Boolean enableDownPayment;
        @Schema(example = "5.5")
        public BigDecimal disbursedAmountPercentageForDownPayment;
        @Schema(example = "false")
        public Boolean enableAutoRepaymentForDownPayment;
        @Schema(example = "1")
        public Integer repaymentStartDateType;

        // Interest Recalculation
        @Schema(example = "false")
        public Boolean isInterestRecalculationEnabled;
        @Schema(example = "1")
        public Integer interestRecalculationCompoundingMethod;
        @Schema(example = "2")
        public Integer rescheduleStrategyMethod;
        @Schema(example = "1")
        public Integer preClosureInterestCalculationStrategy;
        @Schema(example = "1")
        public Integer recalculationCompoundingFrequencyType;
        @Schema(example = "50")
        public Integer recalculationRestFrequencyType;
        @Schema(example = "1")
        public Integer recalculationRestFrequencyInterval;
        @Schema(example = "1")
        public Integer recalculationCompoundingFrequencyInterval;
        @Schema(example = "1")
        public Integer recalculationCompoundingFrequencyOnDayType;

        // Accounting
        @Schema(example = "3")
        public Integer accountingRule;
        @Schema(example = "4")
        public Long fundSourceAccountId;
        @Schema(example = "8")
        public Long loanPortfolioAccountId;
        @Schema(example = "9")
        public Long receivableInterestAccountId;
        @Schema(example = "11")
        public Long receivableFeeAccountId;
        @Schema(example = "10")
        public Long receivablePenaltyAccountId;
        @Schema(example = "34")
        public Long interestOnLoanAccountId;
        @Schema(example = "37")
        public Long incomeFromFeeAccountId;
        @Schema(example = "35")
        public Long incomeFromPenaltyAccountId;
        @Schema(example = "2")
        public Long overpaymentLiabilityAccountId;
        @Schema(example = "41")
        public Long writeOffAccountId;
        @Schema(example = "5")
        public Long transfersInSuspenseAccountId;
        @Schema(example = "15")
        public Long incomeFromRecoveryAccountId;
        @Schema(example = "48")
        public Long goodwillCreditAccountId;
        @Schema(example = "20")
        public Long incomeFromChargeOffInterestAccountId;
        @Schema(example = "11")
        public Long incomeFromChargeOffFeesAccountId;
        @Schema(example = "12")
        public Long chargeOffExpenseAccountId;
        @Schema(example = "13")
        public Long chargeOffFraudExpenseAccountId;
        @Schema(example = "11")
        public Long incomeFromChargeOffPenaltyAccountId;
        @Schema(example = "20")
        public Long incomeFromGoodwillCreditInterestAccountId;
        @Schema(example = "11")
        public Long incomeFromGoodwillCreditFeesAccountId;
        @Schema(example = "11")
        public Long incomeFromGoodwillCreditPenaltyAccountId;
        public List<GetLoanProductsProductIdResponse.GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public List<GetLoanProductsProductIdResponse.GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

        // Multi Disburse
        @Schema(example = "true")
        public Boolean multiDisburseLoan;
        @Schema(example = "50")
        public Integer principalThresholdForLastInstallment;
        @Schema(example = "true")
        public Boolean disallowExpectedDisbursements;
        @Schema(example = "3")
        public Integer maxTrancheCount;
        @Schema(example = "36000.00")
        public Double outstandingLoanBalance;

        public List<ChargeData> charges;
        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        public AllowAttributeOverrides allowAttributeOverrides;
        public List<RateData> rates;
        @Schema(example = "CUMULATIVE")
        public String loanScheduleType;
        @Schema(example = "HORIZONTAL")
        public String loanScheduleProcessingType;

        static final class AllowAttributeOverrides {

            private AllowAttributeOverrides() {}

            @Schema(example = "true")
            public boolean amortizationType;
            @Schema(example = "true")
            public boolean interestType;
            @Schema(example = "true")
            public boolean transactionProcessingStrategyCode;
            @Schema(example = "true")
            public boolean interestCalculationPeriodType;
            @Schema(example = "true")
            public boolean inArrearsTolerance;
            @Schema(example = "true")
            public boolean repaymentEvery;
            @Schema(example = "true")
            public boolean graceOnPrincipalAndInterestPayment;
            @Schema(example = "true")
            public boolean graceOnArrearsAgeing;
        }

        static final class ChargeData {

            private ChargeData() {}

            @Schema(example = "1")
            public Long id;
        }

        static final class RateData {

            private RateData() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "some name")
            public String name;
            @Schema(example = "20")
            public BigDecimal percentage;
            @Schema(description = "Apply specific product using its id, code, and value.")
            public EnumOptionData productApply;
            @Schema(example = "false")
            public boolean active;
        }

    }

    @Schema(description = "PostLoanProductsResponse")
    public static final class PostLoanProductsResponse {

        private PostLoanProductsResponse() {}

        @Schema(example = "3")
        public Long resourceId;
    }

    @Schema(description = "GetLoanProductsResponse")
    public static final class GetLoanProductsResponse {

        private GetLoanProductsResponse() {}

        static final class GetLoanProductsCurrency {

            private GetLoanProductsCurrency() {}

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

        static final class GetLoanProductsRepaymentFrequencyType {

            private GetLoanProductsRepaymentFrequencyType() {}

            @Schema(example = "0")
            public Long id;
            @Schema(example = "repaymentFrequency.periodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        static final class GetLoanProductsInterestRateFrequencyType {

            private GetLoanProductsInterestRateFrequencyType() {}

            @Schema(example = "3")
            public Long id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.years")
            public String code;
            @Schema(example = "Per year")
            public String description;
        }

        static final class GetLoanProductsAmortizationType {

            private GetLoanProductsAmortizationType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "amortizationType.equal.installments")
            public String code;
            @Schema(example = "Equal installments")
            public String description;
        }

        static final class GetLoanProductsInterestType {

            private GetLoanProductsInterestType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "interestType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetLoansProductsInterestCalculationPeriodType {

            private GetLoansProductsInterestCalculationPeriodType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "interestCalculationPeriodType.same.as.repayment.period")
            public String code;
            @Schema(example = "Same as repayment period")
            public String description;
        }

        static final class GetLoansProductsDaysInMonthType {

            private GetLoansProductsDaysInMonthType() {}

            @Schema(example = "30")
            public Long id;
            @Schema(example = "DaysInMonthType.days360")
            public String code;
            @Schema(example = "30 Days")
            public String description;
        }

        static final class GetLoansProductsDaysInYearType {

            private GetLoansProductsDaysInYearType() {}

            @Schema(example = "360")
            public Long id;
            @Schema(example = "DaysInYearType.days360")
            public String code;
            @Schema(example = "360 Days")
            public String description;
        }

        static final class GetLoanProductsRepaymentStartDateType {

            private GetLoanProductsRepaymentStartDateType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "repaymentStartDateType.disbursementDate")
            public String code;
            @Schema(example = "Disbursement Date")
            public String description;
        }

        static final class GetLoanProductsInterestRecalculationData {

            private GetLoanProductsInterestRecalculationData() {}

            static final class GetLoanProductsInterestRecalculationCompoundingType {

                private GetLoanProductsInterestRecalculationCompoundingType() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "interestRecalculationCompoundingMethod.fee")
                public String code;
                @Schema(example = "Fee")
                public String description;
            }

            static final class GetLoanProductsInterestRecalculationCompoundingFrequencyType {

                private GetLoanProductsInterestRecalculationCompoundingFrequencyType() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "interestRecalculationFrequencyType.same.as.repayment.period")
                public String code;
                @Schema(example = "Same as repayment period")
                public String description;
            }

            static final class GetLoanProductsRescheduleStrategyType {

                private GetLoanProductsRescheduleStrategyType() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "loanRescheduleStrategyMethod.reduce.number.of.installments")
                public String code;
                @Schema(example = "Reduce number of installments")
                public String description;
            }

            static final class GetLoanProductsPreClosureInterestCalculationStrategy {

                private GetLoanProductsPreClosureInterestCalculationStrategy() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "loanPreClosureInterestCalculationStrategy.tillPreClosureDate")
                public String code;
                @Schema(example = "Till preclose Date")
                public String description;
            }

            @Schema(example = "3")
            public Long id;
            @Schema(example = "1")
            public Long productId;
            public GetLoanProductsInterestRecalculationData.GetLoanProductsInterestRecalculationCompoundingType interestRecalculationCompoundingType;
            public GetLoanProductsInterestRecalculationCompoundingFrequencyType interestRecalculationCompoundingFrequencyType;
            public GetLoanProductsInterestRecalculationData.GetLoanProductsRescheduleStrategyType rescheduleStrategyType;
            public GetLoanProductsInterestRecalculationCompoundingFrequencyType recalculationRestFrequencyType;
            @Schema(example = "1")
            public Integer recalculationRestFrequencyInterval;
            public GetLoanProductsPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;
            @Schema(example = "true")
            public Boolean isArrearsBasedOnOriginalSchedule;
            @Schema(example = "1")
            public Integer recalculationCompoundingFrequencyInterval;
            @Schema(example = "1")
            public Integer recalculationCompoundingFrequencyOnDayType;
        }

        static final class GetLoanProductsAccountingRule {

            private GetLoanProductsAccountingRule() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "accountingRuleType.cash")
            public String code;
            @Schema(example = "CASH BASED")
            public String description;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "personal loan product")
        public String name;
        @Schema(example = "pe1")
        public String shortName;
        @Schema(example = "false")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "false")
        public Boolean useBorrowerCycle;
        @Schema(example = "[2013, 9, 2]")
        public LocalDate startDate;
        @Schema(example = "[2014, 2, 7]")
        public LocalDate endDate;
        @Schema(example = "loanProduct.active")
        public String status;
        public GetLoanProductsCurrency currency;
        @Schema(example = "10000.000000")
        public Double principal;
        @Schema(example = "5000.000000")
        public Double minPrincipal;
        @Schema(example = "15000.000000")
        public Double maxPrincipal;
        @Schema(example = "10")
        public Integer numberOfRepayments;
        @Schema(example = "5")
        public Integer minNumberOfRepayments;
        @Schema(example = "15")
        public Integer maxNumberOfRepayments;
        @Schema(example = "7")
        public Integer repaymentEvery;
        public GetLoanProductsRepaymentFrequencyType repaymentFrequencyType;
        @Schema(example = "10")
        public Integer fixedLength;
        @Schema(example = "15.000000")
        public Double interestRatePerPeriod;
        public GetLoanProductsResponse.GetLoanProductsInterestRateFrequencyType interestRateFrequencyType;
        @Schema(example = "15.000000")
        public Double annualInterestRate;
        public GetLoanProductsAmortizationType amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        public GetLoanProductsInterestType interestType;
        public GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategy;
        @Schema(example = "Mifos style")
        public String transactionProcessingStrategyName;
        @Schema(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoansProductsDaysInMonthType daysInMonthType;
        public GetLoansProductsDaysInYearType daysInYearType;
        @Schema(example = "true")
        public Boolean isInterestRecalculationEnabled;
        public GetLoanProductsInterestRecalculationData interestRecalculationData;
        public GetLoanProductsResponse.GetLoanProductsAccountingRule accountingRule;
        @Schema(example = "0")
        public Integer principalThresholdForLastInstalment;
        public GetLoanProductsResponse.GetLoanProductsRepaymentStartDateType repaymentStartDateType;
    }

    @Schema(description = "GetLoanProductsTemplateResponse")
    public static final class GetLoanProductsTemplateResponse {

        private GetLoanProductsTemplateResponse() {}

        static final class GetLoanProductsTemplateCurrency {

            private GetLoanProductsTemplateCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "Usa dollar")
            public String name;
            @Schema(example = "0")
            public Integer decimalPlaces;
            @Schema(example = "0")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "USD")
            public String nameCode;
            @Schema(example = "[]")
            public String displayLabel;
        }

        static final class GetLoanProductsRepaymentTemplateFrequencyType {

            private GetLoanProductsRepaymentTemplateFrequencyType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "repaymentFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetLoanProductsInterestRateTemplateFrequencyType {

            private GetLoanProductsInterestRateTemplateFrequencyType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Per month")
            public String description;
        }

        static final class GetLoanProductsInterestTemplateType {

            private GetLoanProductsInterestTemplateType() {}

            @Schema(example = "0")
            public Long id;
            @Schema(example = "interestType.declining.balance")
            public String code;
            @Schema(example = "Declining Balance")
            public String description;
        }

        static final class GetLoanProductsAccountingRule {

            private GetLoanProductsAccountingRule() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "accountingRuleType.none")
            public String code;
            @Schema(example = "NONE")
            public String description;
        }

        static final class GetLoansProductsDaysInMonthTemplateType {

            private GetLoansProductsDaysInMonthTemplateType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "DaysInMonthType.actual")
            public String code;
            @Schema(example = "Actual")
            public String description;
        }

        static final class GetLoanProductsDaysInYearTemplateType {

            private GetLoanProductsDaysInYearTemplateType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "DaysInYearType.actual")
            public String code;
            @Schema(example = "Actual")
            public String description;
        }

        static final class GetLoanProductsInterestRecalculationTemplateData {

            private GetLoanProductsInterestRecalculationTemplateData() {}

            static final class GetLoanProductsInterestRecalculationCompoundingType {

                private GetLoanProductsInterestRecalculationCompoundingType() {}

                @Schema(example = "0")
                public Long id;
                @Schema(example = "interestRecalculationCompoundingMethod.none")
                public String code;
                @Schema(example = "None")
                public String description;
            }

            static final class GetLoanProductsRescheduleStrategyType {

                private GetLoanProductsRescheduleStrategyType() {}

                @Schema(example = "3")
                public Long id;
                @Schema(example = "loanRescheduleStrategyMethod.reduce.emi.amount")
                public String code;
                @Schema(example = "Reduce EMI amount")
                public String description;
            }

            public GetLoanProductsInterestRecalculationTemplateData.GetLoanProductsInterestRecalculationCompoundingType interestRecalculationCompoundingType;
            public GetLoanProductsInterestRecalculationTemplateData.GetLoanProductsRescheduleStrategyType rescheduleStrategyType;
            public GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;
        }

        static final class GetLoanProductsPaymentTypeOptions {

            private GetLoanProductsPaymentTypeOptions() {}

            @Schema(example = "10")
            public Long id;
            @Schema(example = "check")
            public String name;
            @Schema(example = "1")
            public Integer position;
        }

        static final class GetLoanProductsCurrencyOptions {

            private GetLoanProductsCurrencyOptions() {}

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

        static final class GetLoanProductsTransactionProcessingStrategyOptions {

            private GetLoanProductsTransactionProcessingStrategyOptions() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "mifos-standard-strategy")
            public String code;
            @Schema(example = "Penalties, Fees, Interest, Principal order")
            public String name;
        }

        static final class GetLoanProductsChargeOptions {

            private GetLoanProductsChargeOptions() {}

            static final class GetLoanChargeTimeType {

                private GetLoanChargeTimeType() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "chargeTimeType.disbursement")
                public String code;
                @Schema(example = "Disbursement")
                public String description;
            }

            static final class GetLoanProductsChargeAppliesTo {

                private GetLoanProductsChargeAppliesTo() {}

                @Schema(example = "1  ")
                public Long id;
                @Schema(example = "chargeAppliesTo.loan")
                public String code;
                @Schema(example = "Loan")
                public String description;
            }

            static final class GetLoanChargeCalculationType {

                private GetLoanChargeCalculationType() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetLoansChargePaymentMode {

                private GetLoansChargePaymentMode() {}

                @Schema(example = "0")
                public Long id;
                @Schema(example = "chargepaymentmode.regular")
                public String code;
                @Schema(example = "Regular")
                public String description;
            }

            @Schema(example = "5")
            public Long id;
            @Schema(example = "des charges")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean penalty;
            public GetLoanProductsCurrencyOptions currency;
            @Schema(example = "100")
            public BigDecimal amount;
            public GetLoanChargeTimeType chargeTimeType;
            public GetLoanProductsChargeAppliesTo chargeAppliesTo;
            public GetLoanChargeCalculationType chargeCalculationType;
            public GetLoansChargePaymentMode chargePaymentMode;
        }

        static final class GetLoanProductsAccountingMappingOptions {

            private GetLoanProductsAccountingMappingOptions() {}

            static final class GetLoanProductsLiabilityAccountOptions {

                private GetLoanProductsLiabilityAccountOptions() {}

                static final class GetLoanProductsLiabilityType {

                    private GetLoanProductsLiabilityType() {}

                    @Schema(example = "2")
                    public Long id;
                    @Schema(example = "accountType.liability")
                    public String code;
                    @Schema(example = "LIABILITY")
                    public String description;
                }

                static final class GetLoanProductsLiabilityUsage {

                    private GetLoanProductsLiabilityUsage() {}

                    @Schema(example = "1")
                    public Long id;
                    @Schema(example = "accountUsage.detail")
                    public String code;
                    @Schema(example = "DETAIL")
                    public String description;
                }

                static final class GetLoanProductsLiabilityTagId {

                    private GetLoanProductsLiabilityTagId() {}

                    @Schema(example = "0")
                    public Long id;
                }

                @Schema(example = "11")
                public Long id;
                @Schema(example = "over payment")
                public String name;
                @Schema(example = "13")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsLiabilityType type;
                public GetLoanProductsLiabilityUsage usage;
                @Schema(example = "over payment")
                public String nameDecorated;
                public GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "0")
                public Integer organizationRunningBalance;
            }

            static final class GetLoanProductsAssetAccountOptions {

                private GetLoanProductsAssetAccountOptions() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "Loan portfolio")
                public String name;
                @Schema(example = "02")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @Schema(example = "Loan portfolio")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "60000")
                public Integer organizationRunningBalance;
            }

            static final class GetLoanProductsExpenseAccountOptions {

                private GetLoanProductsExpenseAccountOptions() {}

                static final class GetLoanProductsExpenseType {

                    private GetLoanProductsExpenseType() {}

                    @Schema(example = "5")
                    public Long id;
                    @Schema(example = "accountType.expense")
                    public String code;
                    @Schema(example = "EXPENSE")
                    public String description;
                }

                @Schema(example = "10")
                public Long id;
                @Schema(example = "loans written off 2")
                public String name;
                @Schema(example = "12")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsExpenseType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @Schema(example = "loans written off 2")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "0")
                public Integer organizationRunningBalance;
            }

            static final class GetLoanProductsIncomeAccountOptions {

                private GetLoanProductsIncomeAccountOptions() {}

                static final class GetLoanProductsIncomeType {

                    private GetLoanProductsIncomeType() {}

                    @Schema(example = "4")
                    public Long id;
                    @Schema(example = "accountType.income")
                    public String code;
                    @Schema(example = "INCOME")
                    public String description;
                }

                @Schema(example = "4")
                public Long id;
                @Schema(example = "income from interest")
                public String name;
                @Schema(example = "04")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsIncomeType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @Schema(example = "income from interest")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "19")
                public Integer organizationRunningBalance;
            }

            public Set<GetLoanProductsLiabilityAccountOptions> liabilityAccountOptions;
            public Set<GetLoanProductsAssetAccountOptions> assetAccountOptions;
            public Set<GetLoanProductsExpenseAccountOptions> expenseAccountOptions;
            public Set<GetLoanProductsIncomeAccountOptions> incomeAccountOptions;
        }

        static final class GetLoanProductsValueConditionTypeOptions {

            private GetLoanProductsValueConditionTypeOptions() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "mifos-standard-strategyLoanProductValueConditionType.equal")
            public String code;
            @Schema(example = "equals")
            public String description;
        }

        @Schema(example = "false")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "false")
        public Boolean useBorrowerCycle;
        public GetLoanProductsTemplateCurrency currency;
        public GetLoanProductsRepaymentTemplateFrequencyType repaymentFrequencyType;
        public GetLoanProductsInterestRateTemplateFrequencyType interestRateFrequencyType;
        public GetLoanProductsResponse.GetLoanProductsAmortizationType amortizationType;
        public GetLoanProductsInterestTemplateType interestType;
        public GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoanProductsTemplateResponse.GetLoanProductsAccountingRule accountingRule;
        public GetLoansProductsDaysInMonthTemplateType daysInMonthType;
        public GetLoanProductsDaysInYearTemplateType daysInYearType;
        @Schema(example = "false")
        public Boolean isInterestRecalculationEnabled;
        public GetLoanProductsInterestRecalculationTemplateData interestRecalculationData;
        public Set<GetLoanProductsPaymentTypeOptions> paymentTypeOptions;
        public Set<GetLoanProductsCurrencyOptions> currencyOptions;
        public Set<GetLoanProductsRepaymentTemplateFrequencyType> repaymentFrequencyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsPreClosureInterestCalculationStrategy> preClosureInterestCalculationStrategyOptions;
        public Set<GetLoanProductsInterestRateTemplateFrequencyType> interestRateFrequencyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsAmortizationType> amortizationTypeOptions;
        public Set<GetLoanProductsInterestTemplateType> interestTypeOptions;
        public Set<GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType> interestCalculationPeriodTypeOptions;
        public Set<GetLoanProductsTransactionProcessingStrategyOptions> transactionProcessingStrategyOptions;
        public Set<GetLoanProductsChargeOptions> chargeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsAccountingRule> accountingRuleOptions;
        public GetLoanProductsAccountingMappingOptions accountingMappingOptions;
        public Set<GetLoanProductsValueConditionTypeOptions> valueConditionTypeOptions;
        public Set<GetLoansProductsDaysInMonthTemplateType> daysInMonthTypeOptions;
        public Set<GetLoanProductsInterestTemplateType> daysInYearTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsInterestRecalculationCompoundingType> interestRecalculationCompoundingTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsRescheduleStrategyType> rescheduleStrategyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsInterestRecalculationCompoundingFrequencyType> interestRecalculationFrequencyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsRepaymentStartDateType> repaymentStartDateTypeOptions;
        public List<EnumOptionData> advancedPaymentAllocationTransactionTypes;
        public List<EnumOptionData> advancedPaymentAllocationFutureInstallmentAllocationRules;
        public List<EnumOptionData> advancedPaymentAllocationTypes;
        public List<EnumOptionData> loanScheduleTypeOptions;
        public List<EnumOptionData> loanScheduleProcessingTypeOptions;

        public List<EnumOptionData> creditAllocationAllocationTypes;
        public List<EnumOptionData> creditAllocationTransactionTypes;

    }

    @Schema(description = "GetLoanProductsProductIdResponse")
    public static final class GetLoanProductsProductIdResponse {

        private GetLoanProductsProductIdResponse() {}

        static final class GetLoanProductsInterestRateFrequencyType {

            private GetLoanProductsInterestRateFrequencyType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Per month")
            public String description;
        }

        static final class GetLoanProductsRepaymentStartDateType {

            private GetLoanProductsRepaymentStartDateType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "repaymentStartDateType.disbursementDate")
            public String code;
            @Schema(example = "Disbursement Date")
            public String description;
        }

        static final class GetLoanProductsPrincipalVariationsForBorrowerCycle {

            private GetLoanProductsPrincipalVariationsForBorrowerCycle() {}

            static final class GetLoanProductsParamType {

                private GetLoanProductsParamType() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "LoanProductParamType.principal")
                public String code;
                @Schema(example = "principal")
                public String description;
            }

            static final class GetLoanProductsValueConditionType {

                private GetLoanProductsValueConditionType() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "LoanProductValueConditionType.equal")
                public String code;
                @Schema(example = "equals")
                public String description;
            }

            @Schema(example = "21")
            public Long id;
            @Schema(example = "1")
            public Integer borrowerCycleNumber;
            public GetLoanProductsParamType paramType;
            public GetLoanProductsValueConditionType valueConditionType;
            @Schema(example = "2000.000000")
            public Double minValue;
            @Schema(example = "20000.000000")
            public Double maxValue;
            @Schema(example = "15000.000000")
            public Double defaultValue;
        }

        static final class GetLoanAccountingMappings {

            private GetLoanAccountingMappings() {}

            static final class GetGlAccountMapping {

                private GetGlAccountMapping() {}

                @Schema(example = "10")
                public Long id;
                @Schema(example = "Cash Account")
                public String name;
                @Schema(example = "012-34-65")
                public String glCode;
            }

            public GetGlAccountMapping fundSourceAccount;
            public GetGlAccountMapping loanPortfolioAccount;
            public GetGlAccountMapping transfersInSuspenseAccount;
            public GetGlAccountMapping receivableInterestAccount;
            public GetGlAccountMapping receivablePenaltyAccount;
            public GetGlAccountMapping interestOnLoanAccount;
            public GetGlAccountMapping incomeFromFeeAccount;
            public GetGlAccountMapping incomeFromPenaltyAccount;
            public GetGlAccountMapping incomeFromRecoveryAccount;
            public GetGlAccountMapping writeOffAccount;
            public GetGlAccountMapping goodwillCreditAccount;
            public GetGlAccountMapping overpaymentLiabilityAccount;
        }

        static final class GetLoanPaymentChannelToFundSourceMappings {

            private GetLoanPaymentChannelToFundSourceMappings() {}

            @Schema(example = "10")
            public Long paymentTypeId;
            @Schema(example = "39")
            public Long fundSourceAccountId;
        }

        static final class GetLoanFeeToIncomeAccountMappings {

            private GetLoanFeeToIncomeAccountMappings() {}

            static final class GetLoanCharge {

                private GetLoanCharge() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "flat install")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean penalty;
            }

            public GetLoanCharge charge;
            public GetLoanAccountingMappings.GetGlAccountMapping incomeAccount;
            @Schema(example = "10")
            public Long chargeId;
            @Schema(example = "39")
            public Long incomeAccountId;
        }

        @Schema(example = "11")
        public Long id;
        @Schema(example = "advanced accounting")
        public String name;
        @Schema(example = "ad11")
        public String shortName;
        @Schema(example = "true")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "true")
        public Boolean useBorrowerCycle;
        @Schema(example = "loanProduct.active")
        public String status;
        public GetLoanProductsResponse.GetLoanProductsCurrency currency;
        @Schema(example = "10000.000000")
        public Double principal;
        @Schema(example = "2000.000000")
        public Double minPrincipal;
        @Schema(example = "15000.000000")
        public Double maxPrincipal;
        @Schema(example = "7")
        public Integer numberOfRepayments;
        @Schema(example = "7")
        public Integer repaymentEvery;
        public GetLoanProductsResponse.GetLoanProductsRepaymentFrequencyType repaymentFrequencyType;
        @Schema(example = "10")
        public Integer fixedLength;
        @Schema(example = "5.000000")
        public Double interestRatePerPeriod;
        public GetLoanProductsProductIdResponse.GetLoanProductsInterestRateFrequencyType interestRateFrequencyType;
        @Schema(example = "60.000000")
        public Double annualInterestRate;
        public GetLoanProductsResponse.GetLoanProductsAmortizationType amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        public GetLoanProductsTemplateResponse.GetLoanProductsInterestTemplateType interestType;
        public GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategyCode;
        @Schema(example = "Mifos style")
        public String transactionProcessingStrategyName;
        @Schema(example = "[]")
        public List<AdvancedPaymentData> paymentAllocation;
        @Schema(example = "[]")
        public List<CreditAllocationData> creditAllocation;
        @Schema(example = "[]")
        public List<Integer> charges;
        public Set<GetLoanProductsPrincipalVariationsForBorrowerCycle> productsPrincipalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoanProductsResponse.GetLoanProductsAccountingRule accountingRule;
        public GetLoanAccountingMappings accountingMappings;
        public Set<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public Set<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        @Schema(example = "true")
        public Boolean multiDisburseLoan;
        @Schema(example = "3")
        public Integer maxTrancheCount;
        @Schema(example = "36000.000000")
        public Double outstandingLoanBalance;
        @Schema(example = "2")
        public Integer overdueDaysForNPA;
        @Schema(example = "50")
        public Integer principalThresholdForLastInstalment;
        public GetDelinquencyBucketsResponse delinquencyBucket;
        @Schema(example = "false")
        public Boolean enableInstallmentLevelDelinquency;
        @Schema(example = "true")
        public Boolean disallowExpectedDisbursements;
        @Schema(example = "3")
        public Integer dueDaysForRepaymentEvent;
        @Schema(example = "3")
        public Integer overDueDaysForRepaymentEvent;
        @Schema(example = "3")
        public Integer inArrearsTolerance;
        @Schema(example = "false")
        public Boolean enableDownPayment;
        @Schema(example = "5.5")
        public BigDecimal disbursedAmountPercentageForDownPayment;
        @Schema(example = "false")
        public Boolean enableAutoRepaymentForDownPayment;
        public GetLoanProductsRepaymentStartDateType repaymentStartDateType;
        @Schema(example = "CUMULATIVE")
        public EnumOptionData loanScheduleType;
        @Schema(example = "HORIZONTAL")
        public EnumOptionData loanScheduleProcessingType;
    }

    @Schema(description = "PutLoanProductsProductIdRequest")
    public static final class PutLoanProductsProductIdRequest {

        private PutLoanProductsProductIdRequest() {}

        @Schema(example = "LP Accrual Accounting")
        public String name;
        @Schema(example = "LPAA")
        public String shortName;
        @Schema(example = "non-interest bearing product")
        public String description;
        @Schema(example = "10 July 2022")
        public String startDate;
        @Schema(example = "10 July 2022")
        public String closeDate;
        @Schema(example = "3")
        public Long fundId;

        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "2")
        public Integer digitsAfterDecimal;
        @Schema(example = "1")
        public Integer installmentAmountInMultiplesOf;
        @Schema(example = "1")
        public Integer inMultiplesOf;

        // Terms
        @Schema(example = "5000.00")
        public Double minPrincipal;
        @Schema(example = "10000.00")
        public Double principal;
        @Schema(example = "15000.00")
        public Double maxPrincipal;

        @Schema(example = "1")
        public Integer minNumberOfRepayments;
        @Schema(example = "1")
        public Integer maxNumberOfRepayments;
        @Schema(example = "12")
        public Integer numberOfRepayments;
        @Schema(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        @Schema(example = "0")
        public Double minInterestRatePerPeriod;
        @Schema(example = "1.75")
        public Double interestRatePerPeriod;
        @Schema(example = "23.4")
        public Double maxInterestRatePerPeriod;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        @Schema(example = "true")
        public Boolean canDefineInstallmentAmount;
        @Schema(example = "10.0")
        public Integer fixedLength;

        // Settings
        @Schema(example = "false")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "false")
        public Boolean useBorrowerCycle;
        @Schema(example = "2")
        public Integer repaymentFrequencyType;
        @Schema(example = "2")
        public Integer interestRateFrequencyType;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "1")
        public Integer interestCalculationPeriodType;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategyCode;
        @Schema(example = "[]")
        public List<AdvancedPaymentData> paymentAllocation;
        @Schema(example = "[]")
        public List<CreditAllocationData> creditAllocation;
        @Schema(example = "false")
        public Boolean isLinkedToFloatingInterestRates;
        @Schema(example = "false")
        public Boolean allowVariableInstallments;
        @Schema(example = "30")
        public Integer minimumDaysBetweenDisbursalAndFirstRepayment;
        @Schema(example = "true")
        public Boolean allowApprovedDisbursedAmountsOverApplied;
        @Schema(example = "percentage")
        public String overAppliedCalculationType;
        @Schema(example = "50")
        public Integer overAppliedNumber;
        @Schema(example = "1")
        public Long daysInMonthType;
        @Schema(example = "1")
        public Long daysInYearType;
        @Schema(example = "true")
        public Boolean allowPartialPeriodInterestCalcualtion;
        @Schema(example = "179")
        public Integer overdueDaysForNPA;
        @Schema(example = "3")
        public Integer graceOnPrincipalPayment;
        @Schema(example = "3")
        public Integer graceOnInterestPayment;
        @Schema(example = "90")
        public Integer inArrearsTolerance;
        @Schema(example = "3")
        public Integer graceOnArrearsAgeing;
        @Schema(example = "false")
        public Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;
        @Schema(example = "false")
        public Boolean isEqualAmortization;
        @Schema(example = "false")
        public Boolean canUseForTopup;
        @Schema(example = "false")
        public Boolean holdGuaranteeFunds;
        @Schema(example = "1")
        public Long delinquencyBucketId;
        @Schema(example = "false")
        public Boolean enableInstallmentLevelDelinquency;
        @Schema(example = "3")
        public Integer dueDaysForRepaymentEvent;
        @Schema(example = "3")
        public Integer overDueDaysForRepaymentEvent;
        @Schema(example = "false")
        public Boolean enableDownPayment;
        @Schema(example = "5.5")
        public BigDecimal disbursedAmountPercentageForDownPayment;
        @Schema(example = "false")
        public Boolean enableAutoRepaymentForDownPayment;
        @Schema(example = "1")
        public Integer repaymentStartDateType;

        // Interest Recalculation
        @Schema(example = "false")
        public Boolean isInterestRecalculationEnabled;
        @Schema(example = "1")
        public Integer interestRecalculationCompoundingMethod;
        @Schema(example = "2")
        public Integer rescheduleStrategyMethod;
        @Schema(example = "1")
        public Integer preClosureInterestCalculationStrategy;
        @Schema(example = "1")
        public Integer recalculationCompoundingFrequencyType;
        @Schema(example = "50")
        public Integer recalculationRestFrequencyType;
        @Schema(example = "1")
        public Integer recalculationRestFrequencyInterval;
        @Schema(example = "1")
        public Integer recalculationCompoundingFrequencyInterval;
        @Schema(example = "1")
        public Integer recalculationCompoundingFrequencyOnDayType;

        // Accounting
        @Schema(example = "3")
        public Integer accountingRule;
        @Schema(example = "4")
        public Long fundSourceAccountId;
        @Schema(example = "8")
        public Long loanPortfolioAccountId;
        @Schema(example = "9")
        public Long receivableInterestAccountId;
        @Schema(example = "11")
        public Long receivableFeeAccountId;
        @Schema(example = "10")
        public Long receivablePenaltyAccountId;
        @Schema(example = "34")
        public Long interestOnLoanAccountId;
        @Schema(example = "37")
        public Long incomeFromFeeAccountId;
        @Schema(example = "35")
        public Long incomeFromPenaltyAccountId;
        @Schema(example = "2")
        public Long overpaymentLiabilityAccountId;
        @Schema(example = "41")
        public Long writeOffAccountId;
        @Schema(example = "5")
        public Long transfersInSuspenseAccountId;
        @Schema(example = "15")
        public Long incomeFromRecoveryAccountId;
        @Schema(example = "48")
        public Long goodwillCreditAccountId;
        @Schema(example = "20")
        public Long incomeFromChargeOffInterestAccountId;
        @Schema(example = "11")
        public Long incomeFromChargeOffFeesAccountId;
        @Schema(example = "12")
        public Long chargeOffExpenseAccountId;
        @Schema(example = "13")
        public Long chargeOffFraudExpenseAccountId;

        @Schema(example = "20")
        public Long incomeFromGoodwillCreditInterestAccountId;

        @Schema(example = "11")
        public Long incomeFromGoodwillCreditFeesAccountId;

        @Schema(example = "11")
        public Long incomeFromGoodwillCreditPenaltyAccountId;

        @Schema(example = "11")
        public Long incomeFromChargeOffPenaltyAccountId;
        public List<GetLoanProductsProductIdResponse.GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public List<GetLoanProductsProductIdResponse.GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

        // Multi Disburse
        @Schema(example = "true")
        public Boolean multiDisburseLoan;
        @Schema(example = "50")
        public Integer principalThresholdForLastInstallment;
        @Schema(example = "true")
        public Boolean disallowExpectedDisbursements;
        @Schema(example = "3")
        public Integer maxTrancheCount;
        @Schema(example = "36000.00")
        public Double outstandingLoanBalance;

        public List<PostLoanProductsRequest.ChargeData> charges;
        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(example = "HORIZONTAL")
        public String loanScheduleProcessingType;
        @Schema(example = "CUMULATIVE")
        public String loanScheduleType;

        public PostLoanProductsRequest.AllowAttributeOverrides allowAttributeOverrides;
        public List<PostLoanProductsRequest.RateData> rates;

        static final class AllowAttributeOverrides {

            private AllowAttributeOverrides() {}

            @Schema(example = "true")
            public boolean amortizationType;
            @Schema(example = "true")
            public boolean interestType;
            @Schema(example = "true")
            public boolean transactionProcessingStrategyCode;
            @Schema(example = "true")
            public boolean interestCalculationPeriodType;
            @Schema(example = "true")
            public boolean inArrearsTolerance;
            @Schema(example = "true")
            public boolean repaymentEvery;
            @Schema(example = "true")
            public boolean graceOnPrincipalAndInterestPayment;
            @Schema(example = "true")
            public boolean graceOnArrearsAgeing;
        }

        static final class ChargeData {

            private ChargeData() {}

            @Schema(example = "1")
            public Long id;
        }

        static final class RateData {

            private RateData() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "some name")
            public String name;
            @Schema(example = "20")
            public BigDecimal percentage;
            @Schema(description = "Apply specific product using its id, code, and value.")
            public EnumOptionData productApply;
            @Schema(example = "false")
            public boolean active;
        }

    }

    public static final class AdvancedPaymentData {

        @Schema(example = "DEFAULT")
        public String transactionType;
        @Schema(example = "[]")
        public List<PaymentAllocationOrder> paymentAllocationOrder;

        @Schema(example = "NEXT_INSTALLMENT")
        public String futureInstallmentAllocationRule;
    }

    public static class PaymentAllocationOrder {

        @Schema(example = "DUE_PAST_PENALTY")
        public String paymentAllocationRule;

        @Schema(example = "1")
        public Integer order;
    }

    public static final class CreditAllocationData {

        @Schema(example = "Chargeback")
        public String transactionType;
        @Schema(example = "[]")
        public List<CreditAllocationOrder> creditAllocationOrder;
    }

    public static class CreditAllocationOrder {

        @Schema(example = "PENALTY")
        public String creditAllocationRule;

        @Schema(example = "1")
        public Integer order;
    }

    @Schema(description = "PutLoanProductsProductIdResponse")
    public static final class PutLoanProductsProductIdResponse {

        private PutLoanProductsProductIdResponse() {}

        static final class PutLoanChanges {

            private PutLoanChanges() {}

            @Schema(example = "70,000.00")
            public Double principal;
            @Schema(example = "en_GB")
            public String locale;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutLoanChanges changes;
    }
}
