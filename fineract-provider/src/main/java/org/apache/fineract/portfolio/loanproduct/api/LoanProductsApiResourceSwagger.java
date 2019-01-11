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
package org.apache.fineract.portfolio.loanproduct.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/27/17.
 */
final class LoanProductsApiResourceSwagger {
    private LoanProductsApiResourceSwagger() {
    }

    @ApiModel(value = "PostLoanProductsRequest")
    public final static class PostLoanProductsRequest {
        private PostLoanProductsRequest() {
        }

        @ApiModelProperty(example = "LP Accrual Accounting")
        public String name;
        @ApiModelProperty(example = "LPAA")
        public String shortName;
        @ApiModelProperty(example = "USD")
        public String currencyCode;
        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "2")
        public Integer digitsAfterDecimal;
        @ApiModelProperty(example = "0")
        public Integer inMultiplesOf;
        @ApiModelProperty(example = "100,000.00")
        public Double principal;
        @ApiModelProperty(example = "12")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "1")
        public Integer repaymentEvery;
        @ApiModelProperty(example = "2")
        public Integer repaymentFrequencyType;
        @ApiModelProperty(example = "1")
        public Integer transactionProcessingStrategyId;
        @ApiModelProperty(example = "1.75")
        public Double interestRatePerPeriod;
        @ApiModelProperty(example = "2")
        public Integer interestRateFrequencyType;
        @ApiModelProperty(example = "1")
        public Integer amortizationType;
        @ApiModelProperty(example = "0")
        public Integer interestType;
        @ApiModelProperty(example = "1")
        public Integer interestCalculationPeriodType;
        @ApiModelProperty(example = "1")
        public Integer daysInMonthType;
        @ApiModelProperty(example = "1")
        public Integer daysInYearType;
        @ApiModelProperty(example = "false")
        public Boolean isInterestRecalculationEnabled;
        @ApiModelProperty(example = "3")
        public Integer accountingRule;
        @ApiModelProperty(example = "4")
        public Integer fundSourceAccountId;
        @ApiModelProperty(example = "8")
        public Integer loanPortfolioAccountId;
        @ApiModelProperty(example = "9")
        public Integer receivableInterestAccountId;
        @ApiModelProperty(example = "11")
        public Integer receivableFeeAccountId;
        @ApiModelProperty(example = "10")
        public Integer receivablePenaltyAccountId;
        @ApiModelProperty(example = "34")
        public Integer interestOnLoanAccountId;
        @ApiModelProperty(example = "37")
        public Integer incomeFromFeeAccountId;
        @ApiModelProperty(example = "35")
        public Integer incomeFromPenaltyAccountId;
        @ApiModelProperty(example = "2")
        public Integer overpaymentLiabilityAccountId;
        @ApiModelProperty(example = "41")
        public Integer writeOffAccountId;
    }

    @ApiModel(value = "PostLoanProductsResponse")
    public final static class PostLoanProductsResponse {
        private PostLoanProductsResponse() {
        }

        @ApiModelProperty(example = "3")
        public Integer resourceId;
    }

    @ApiModel(value = "GetLoanProductsResponse")
    public final static class GetLoanProductsResponse {
        private GetLoanProductsResponse() {
        }

        final class GetLoanProductsCurrency {
            private GetLoanProductsCurrency() {
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

        final class GetLoanProductsRepaymentFrequencyType {
            private GetLoanProductsRepaymentFrequencyType() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "repaymentFrequency.periodFrequencyType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
        }

        final class GetLoanProductsInterestRateFrequencyType {
            private GetLoanProductsInterestRateFrequencyType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "interestRateFrequency.periodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Per year")
            public String value;
        }

        final class GetLoanProductsAmortizationType {
            private GetLoanProductsAmortizationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "amortizationType.equal.installments")
            public String code;
            @ApiModelProperty(example = "Equal installments")
            public String value;
        }

        final class GetLoanProductsInterestType {
            private GetLoanProductsInterestType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "interestType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        final class GetLoansProductsInterestCalculationPeriodType {
            private GetLoansProductsInterestCalculationPeriodType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "interestCalculationPeriodType.same.as.repayment.period")
            public String code;
            @ApiModelProperty(example = "Same as repayment period")
            public String value;
        }

        final class GetLoansProductsDaysInMonthType {
            private GetLoansProductsDaysInMonthType() {
            }

            @ApiModelProperty(example = "30")
            public Integer id;
            @ApiModelProperty(example = "DaysInMonthType.days360")
            public String code;
            @ApiModelProperty(example = "30 Days")
            public String value;
        }

        final class GetLoansProductsDaysInYearType {
            private GetLoansProductsDaysInYearType() {
            }

            @ApiModelProperty(example = "360")
            public Integer id;
            @ApiModelProperty(example = "DaysInYearType.days360")
            public String code;
            @ApiModelProperty(example = "360 Days")
            public String value;
        }

        final class GetLoanProductsInterestRecalculationData {
            private GetLoanProductsInterestRecalculationData() {
            }

            final class GetLoanProductsInterestRecalculationCompoundingType {
                private GetLoanProductsInterestRecalculationCompoundingType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "interestRecalculationCompoundingMethod.fee")
                public String code;
                @ApiModelProperty(example = "Fee")
                public String value;
            }

            final class GetLoanProductsInterestRecalculationCompoundingFrequencyType {
                private GetLoanProductsInterestRecalculationCompoundingFrequencyType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "interestRecalculationFrequencyType.same.as.repayment.period")
                public String code;
                @ApiModelProperty(example = "Same as repayment period")
                public String value;
            }

            final class GetLoanProductsRescheduleStrategyType {
                private GetLoanProductsRescheduleStrategyType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "loanRescheduleStrategyMethod.reduce.number.of.installments")
                public String code;
                @ApiModelProperty(example = "Reduce number of installments")
                public String value;
            }

            final class GetLoanProductsPreClosureInterestCalculationStrategy {
                private GetLoanProductsPreClosureInterestCalculationStrategy() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "loanPreClosureInterestCalculationStrategy.tillPreClosureDate")
                public String code;
                @ApiModelProperty(example = "Till preclose Date")
                public String value;
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "1")
            public Integer productId;
            public GetLoanProductsInterestRecalculationCompoundingType interestRecalculationCompoundingType;
            public GetLoanProductsInterestRecalculationCompoundingFrequencyType interestRecalculationCompoundingFrequencyType;
            public GetLoanProductsRescheduleStrategyType rescheduleStrategyType;
            public GetLoanProductsInterestRecalculationCompoundingFrequencyType recalculationRestFrequencyType;
            public GetLoanProductsPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;
            @ApiModelProperty(example = "true")
            public Boolean isArrearsBasedOnOriginalSchedule;
        }

        final class GetLoanProductsAccountingRule {
            private GetLoanProductsAccountingRule() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountingRuleType.cash")
            public String code;
            @ApiModelProperty(example = "CASH BASED")
            public String value;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "personal loan product")
        public String name;
        @ApiModelProperty(example = "pe1")
        public String shortName;
        @ApiModelProperty(example = "false")
        public Boolean includeInBorrowerCycle;
        @ApiModelProperty(example = "false")
        public Boolean useBorrowerCycle;
        @ApiModelProperty(example = "[2013, 9, 2]")
        public LocalDate startDate;
        @ApiModelProperty(example = "[2014, 2, 7]")
        public LocalDate endDate;
        @ApiModelProperty(example = "loanProduct.active")
        public String status;
        public GetLoanProductsCurrency currency;
        @ApiModelProperty(example = "10000.000000")
        public Float principal;
        @ApiModelProperty(example = "5000.000000")
        public Float minPrincipal;
        @ApiModelProperty(example = "15000.000000")
        public Float maxPrincipal;
        @ApiModelProperty(example = "10")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "5")
        public Integer minNumberOfRepayments;
        @ApiModelProperty(example = "15")
        public Integer maxNumberOfRepayments;
        @ApiModelProperty(example = "7")
        public Integer repaymentEvery;
        public GetLoanProductsRepaymentFrequencyType repaymentFrequencyType;
        @ApiModelProperty(example = "15.000000")
        public Float interestRatePerPeriod;
        public GetLoanProductsInterestRateFrequencyType interestRateFrequencyType;
        @ApiModelProperty(example = "15.000000")
        public Float annualInterestRate;
        public GetLoanProductsAmortizationType amortizationType;
        public GetLoanProductsInterestType interestType;
        public GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @ApiModelProperty(example = "1")
        public Integer transactionProcessingStrategyId;
        @ApiModelProperty(example = "Mifos style")
        public String transactionProcessingStrategyName;
        @ApiModelProperty(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @ApiModelProperty(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @ApiModelProperty(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoansProductsDaysInMonthType daysInMonthType;
        public GetLoansProductsDaysInYearType daysInYearType;
        @ApiModelProperty(example = "true")
        public Boolean isInterestRecalculationEnabled;
        public GetLoanProductsInterestRecalculationData interestRecalculationData;
        public GetLoanProductsAccountingRule accountingRule;
        @ApiModelProperty(example = "0")
        public Integer principalThresholdForLastInstalment;
    }

    @ApiModel(value = "GetLoanProductsTemplateResponse")
    public final static class GetLoanProductsTemplateResponse {
        private GetLoanProductsTemplateResponse() {
        }

        final class GetLoanProductsTemplateCurrency {
            private GetLoanProductsTemplateCurrency() {
            }

            @ApiModelProperty(example = "")
            public String code;
            @ApiModelProperty(example = "")
            public String name;
            @ApiModelProperty(example = "0")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "0")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "")
            public String displaySymbol;
            @ApiModelProperty(example = "")
            public String nameCode;
            @ApiModelProperty(example = "[]")
            public String displayLabel;
        }

        final class GetLoanProductsRepaymentTemplateFrequencyType {
            private GetLoanProductsRepaymentTemplateFrequencyType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "repaymentFrequency.periodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetLoanProductsInterestRateTemplateFrequencyType {
            private GetLoanProductsInterestRateTemplateFrequencyType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "interestRateFrequency.periodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Per month")
            public String value;
        }

        final class GetLoanProductsInterestTemplateType {
            private GetLoanProductsInterestTemplateType() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "interestType.declining.balance")
            public String code;
            @ApiModelProperty(example = "Declining Balance")
            public String value;
        }

        final class GetLoanProductsAccountingRule {
            private GetLoanProductsAccountingRule() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountingRuleType.none")
            public String code;
            @ApiModelProperty(example = "NONE")
            public String value;
        }

        final class GetLoansProductsDaysInMonthTemplateType {
            private GetLoansProductsDaysInMonthTemplateType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "DaysInMonthType.actual")
            public String code;
            @ApiModelProperty(example = "Actual")
            public String value;
        }

        final class GetLoanProductsDaysInYearTemplateType {
            private GetLoanProductsDaysInYearTemplateType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "DaysInYearType.actual")
            public String code;
            @ApiModelProperty(example = "Actual")
            public String value;
        }

        final class GetLoanProductsInterestRecalculationTemplateData {
            private GetLoanProductsInterestRecalculationTemplateData() {
            }

            final class GetLoanProductsInterestRecalculationCompoundingType {
                private GetLoanProductsInterestRecalculationCompoundingType() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "interestRecalculationCompoundingMethod.none")
                public String code;
                @ApiModelProperty(example = "None")
                public String value;
            }

            final class GetLoanProductsRescheduleStrategyType {
                private GetLoanProductsRescheduleStrategyType() {
                }

                @ApiModelProperty(example = "3")
                public Integer id;
                @ApiModelProperty(example = "loanRescheduleStrategyMethod.reduce.emi.amount")
                public String code;
                @ApiModelProperty(example = "Reduce EMI amount")
                public String value;
            }

            public GetLoanProductsInterestRecalculationCompoundingType interestRecalculationCompoundingType;
            public GetLoanProductsRescheduleStrategyType rescheduleStrategyType;
            public GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;
        }

        final class GetLoanProductsPaymentTypeOptions {
            private GetLoanProductsPaymentTypeOptions() {
            }

            @ApiModelProperty(example = "10")
            public Integer id;
            @ApiModelProperty(example = "check")
            public String name;
            @ApiModelProperty(example = "1")
            public Integer position;
        }

        final class GetLoanProductsCurrencyOptions {
            private GetLoanProductsCurrencyOptions() {
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

        final class GetLoanProductsTransactionProcessingStrategyOptions {
            private GetLoanProductsTransactionProcessingStrategyOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "mifos-standard-strategy")
            public String code;
            @ApiModelProperty(example = "Penalties, Fees, Interest, Principal order")
            public String name;
        }

        final class GetLoanProductsChargeOptions {
            private GetLoanProductsChargeOptions() {
            }

            final class GetLoanChargeTimeType {
                private GetLoanChargeTimeType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.disbursement")
                public String code;
                @ApiModelProperty(example = "Disbursement")
                public String value;
            }

            final class GetLoanProductsChargeAppliesTo {
                private GetLoanProductsChargeAppliesTo() {
                }

                @ApiModelProperty(example = "1  ")
                public Integer id;
                @ApiModelProperty(example = "chargeAppliesTo.loan")
                public String code;
                @ApiModelProperty(example = "Loan")
                public String value;
            }

            final class GetLoanChargeCalculationType {
                private GetLoanChargeCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            final class GetLoansChargePaymentMode {
                private GetLoansChargePaymentMode() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "chargepaymentmode.regular")
                public String code;
                @ApiModelProperty(example = "Regular")
                public String value;
            }

            @ApiModelProperty(example = "5")
            public Integer id;
            @ApiModelProperty(example = "des charges")
            public String name;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean penalty;
            public GetLoanProductsCurrencyOptions currency;
            @ApiModelProperty(example = "100")
            public Long amount;
            public GetLoanChargeTimeType chargeTimeType;
            public GetLoanProductsChargeAppliesTo chargeAppliesTo;
            public GetLoanChargeCalculationType chargeCalculationType;
            public GetLoansChargePaymentMode chargePaymentMode;
        }

        final class GetLoanProductsAccountingMappingOptions {
            private GetLoanProductsAccountingMappingOptions() {
            }

            final class GetLoanProductsLiabilityAccountOptions {
                private GetLoanProductsLiabilityAccountOptions() {
                }

                final class GetLoanProductsLiabilityType {
                    private GetLoanProductsLiabilityType() {
                    }

                    @ApiModelProperty(example = "2")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.liability")
                    public String code;
                    @ApiModelProperty(example = "LIABILITY")
                    public String value;
                }

                final class GetLoanProductsLiabilityUsage {
                    private GetLoanProductsLiabilityUsage() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "accountUsage.detail")
                    public String code;
                    @ApiModelProperty(example = "DETAIL")
                    public String value;
                }

                final class GetLoanProductsLiabilityTagId {
                    private GetLoanProductsLiabilityTagId() {
                    }

                    @ApiModelProperty(example = "0")
                    public Integer id;
                }

                @ApiModelProperty(example = "11")
                public Integer id;
                @ApiModelProperty(example = "over payment")
                public String name;
                @ApiModelProperty(example = "13")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsLiabilityType type;
                public GetLoanProductsLiabilityUsage usage;
                @ApiModelProperty(example = "over payment")
                public String nameDecorated;
                public GetLoanProductsLiabilityTagId tagId;
                @ApiModelProperty(example = "0")
                public Integer organizationRunningBalance;
            }

            final class GetLoanProductsAssetAccountOptions {
                private GetLoanProductsAssetAccountOptions() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "Loan portfolio")
                public String name;
                @ApiModelProperty(example = "02")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @ApiModelProperty(example = "Loan portfolio")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @ApiModelProperty(example = "60000")
                public Integer organizationRunningBalance;
            }

            final class GetLoanProductsExpenseAccountOptions {
                private GetLoanProductsExpenseAccountOptions() {
                }

                final class GetLoanProductsExpenseType {
                    private GetLoanProductsExpenseType() {
                    }

                    @ApiModelProperty(example = "5")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.expense")
                    public String code;
                    @ApiModelProperty(example = "EXPENSE")
                    public String value;
                }

                @ApiModelProperty(example = "10")
                public Integer id;
                @ApiModelProperty(example = "loans written off 2")
                public String name;
                @ApiModelProperty(example = "12")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsExpenseType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @ApiModelProperty(example = "loans written off 2")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @ApiModelProperty(example = "0")
                public Integer organizationRunningBalance;
            }

            final class GetLoanProductsIncomeAccountOptions {
                private GetLoanProductsIncomeAccountOptions() {
                }

                final class GetLoanProductsIncomeType {
                    private GetLoanProductsIncomeType() {
                    }

                    @ApiModelProperty(example = "4")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.income")
                    public String code;
                    @ApiModelProperty(example = "INCOME")
                    public String value;
                }

                @ApiModelProperty(example = "4")
                public Integer id;
                @ApiModelProperty(example = "income from interest")
                public String name;
                @ApiModelProperty(example = "04")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsIncomeType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @ApiModelProperty(example = "income from interest")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @ApiModelProperty(example = "19")
                public Integer organizationRunningBalance;
            }

            public Set<GetLoanProductsLiabilityAccountOptions> liabilityAccountOptions;
            public Set<GetLoanProductsAssetAccountOptions> assetAccountOptions;
            public Set<GetLoanProductsExpenseAccountOptions> expenseAccountOptions;
            public Set<GetLoanProductsIncomeAccountOptions> incomeAccountOptions;
        }

        final class GetLoanProductsValueConditionTypeOptions {
            private GetLoanProductsValueConditionTypeOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "mifos-standard-strategyLoanProductValueConditionType.equal")
            public String code;
            @ApiModelProperty(example = "equals")
            public String value;
        }

        @ApiModelProperty(example = "false")
        public Boolean includeInBorrowerCycle;
        @ApiModelProperty(example = "false")
        public Boolean useBorrowerCycle;
        public GetLoanProductsTemplateCurrency currency;
        public GetLoanProductsRepaymentTemplateFrequencyType repaymentFrequencyType;
        public GetLoanProductsInterestRateTemplateFrequencyType interestRateFrequencyType;
        public GetLoanProductsResponse.GetLoanProductsAmortizationType amortizationType;
        public GetLoanProductsInterestTemplateType interestType;
        public GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @ApiModelProperty(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @ApiModelProperty(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @ApiModelProperty(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoanProductsAccountingRule accountingRule;
        public GetLoansProductsDaysInMonthTemplateType daysInMonthType;
        public GetLoanProductsDaysInYearTemplateType daysInYearType;
        @ApiModelProperty(example = "false")
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
    }

    @ApiModel(value = "GetLoanProductsProductIdResponse")
    public final static class GetLoanProductsProductIdResponse {
        private GetLoanProductsProductIdResponse() {
        }

        final class GetLoanProductsInterestRateFrequencyType {
            private GetLoanProductsInterestRateFrequencyType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "interestRateFrequency.periodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Per month")
            public String value;
        }

        final class GetLoanProductsPrincipalVariationsForBorrowerCycle {
            private GetLoanProductsPrincipalVariationsForBorrowerCycle() {
            }

            final class GetLoanProductsParamType {
                private GetLoanProductsParamType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "LoanProductParamType.principal")
                public String code;
                @ApiModelProperty(example = "principal")
                public String value;
            }

            final class GetLoanProductsValueConditionType {
                private GetLoanProductsValueConditionType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "LoanProductValueConditionType.equal")
                public String code;
                @ApiModelProperty(example = "equals")
                public String value;
            }

            @ApiModelProperty(example = "21")
            public Integer id;
            @ApiModelProperty(example = "1")
            public Integer borrowerCycleNumber;
            public GetLoanProductsParamType paramType;
            public GetLoanProductsValueConditionType valueConditionType;
            @ApiModelProperty(example = "2000.000000")
            public Float minValue;
            @ApiModelProperty(example = "20000.000000")
            public Float maxValue;
            @ApiModelProperty(example = "15000.000000")
            public Float defaultValue;
        }

        final class GetLoanAccountingMappings {
            private GetLoanAccountingMappings() {
            }

            final class GetLoanFundSourceAccount {
                private GetLoanFundSourceAccount() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "fund source")
                public String name;
                @ApiModelProperty(example = "01")
                public Integer glCode;
            }

            final class GetLoanPortfolioAccount {
                private GetLoanPortfolioAccount() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "Loan portfolio")
                public String name;
                @ApiModelProperty(example = "02")
                public Integer glCode;
            }

            final class GetLoanTransfersInSuspenseAccount {
                private GetLoanTransfersInSuspenseAccount() {
                }

                @ApiModelProperty(example = "3")
                public Integer id;
                @ApiModelProperty(example = "transfers")
                public String name;
                @ApiModelProperty(example = "03")
                public Integer glCode;
            }

            final class GetLoanInterestOnLoanAccount {
                private GetLoanInterestOnLoanAccount() {
                }

                @ApiModelProperty(example = "4")
                public Integer id;
                @ApiModelProperty(example = "income from interest")
                public String name;
                @ApiModelProperty(example = "04")
                public Integer glCode;
            }

            final class GetLoanIncomeFromFeeAccount {
                private GetLoanIncomeFromFeeAccount() {
                }

                @ApiModelProperty(example = "8")
                public Integer id;
                @ApiModelProperty(example = "income from fees 2")
                public String name;
                @ApiModelProperty(example = "10")
                public Integer glCode;
            }

            final class GetLoanIncomeFromPenaltyAccount {
                private GetLoanIncomeFromPenaltyAccount() {
                }

                @ApiModelProperty(example = "9")
                public Integer id;
                @ApiModelProperty(example = "income from penalities 2")
                public String name;
                @ApiModelProperty(example = "11")
                public Integer glCode;
            }

            final class GetLoanWriteOffAccount {
                private GetLoanWriteOffAccount() {
                }

                @ApiModelProperty(example = "10")
                public Integer id;
                @ApiModelProperty(example = "loans written off 2")
                public String name;
                @ApiModelProperty(example = "12")
                public Integer glCode;
            }

            final class GetLoanOverpaymentLiabilityAccount {
                private GetLoanOverpaymentLiabilityAccount() {
                }

                @ApiModelProperty(example = "11")
                public Integer id;
                @ApiModelProperty(example = "over payment")
                public String name;
                @ApiModelProperty(example = "13")
                public Integer glCode;
            }

            public GetLoanFundSourceAccount fundSourceAccount;
            public GetLoanPortfolioAccount loanPortfolioAccount;
            public GetLoanTransfersInSuspenseAccount transfersInSuspenseAccount;
            public GetLoanInterestOnLoanAccount interestOnLoanAccount;
            public GetLoanIncomeFromFeeAccount incomeFromFeeAccount;
            public GetLoanIncomeFromPenaltyAccount incomeFromPenaltyAccount;
            public GetLoanWriteOffAccount writeOffAccount;
            public GetLoanOverpaymentLiabilityAccount overpaymentLiabilityAccount;
        }

        final class GetLoanPaymentChannelToFundSourceMappings {
            private GetLoanPaymentChannelToFundSourceMappings() {
            }

            final class GetLoanPaymentType {
                private GetLoanPaymentType() {
                }

                @ApiModelProperty(example = "10")
                public Integer id;
                @ApiModelProperty(example = "check")
                public String name;
            }

            public GetLoanPaymentType paymentType;
            public GetLoanAccountingMappings.GetLoanFundSourceAccount fundSourceAccount;
        }

        final class GetLoanFeeToIncomeAccountMappings {
            private GetLoanFeeToIncomeAccountMappings() {
            }

            final class GetLoanCharge {
                private GetLoanCharge() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "flat install")
                public String name;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean penalty;
            }

            public GetLoanCharge charge;
            public GetLoanAccountingMappings.GetLoanIncomeFromFeeAccount incomeAccount;
        }

        @ApiModelProperty(example = "11")
        public Integer id;
        @ApiModelProperty(example = "advanced accounting")
        public String name;
        @ApiModelProperty(example = "ad11")
        public String shortName;
        @ApiModelProperty(example = "true")
        public Boolean includeInBorrowerCycle;
        @ApiModelProperty(example = "true")
        public Boolean useBorrowerCycle;
        @ApiModelProperty(example = "loanProduct.active")
        public String status;
        public GetLoanProductsResponse.GetLoanProductsCurrency currency;
        @ApiModelProperty(example = "10000.000000")
        public Float principal;
        @ApiModelProperty(example = "2000.000000")
        public Float minPrincipal;
        @ApiModelProperty(example = "15000.000000")
        public Float maxPrincipal;
        @ApiModelProperty(example = "7")
        public Integer numberOfRepayments;
        @ApiModelProperty(example = "7")
        public Integer repaymentEvery;
        public GetLoanProductsResponse.GetLoanProductsRepaymentFrequencyType repaymentFrequencyType;
        @ApiModelProperty(example = "5.000000")
        public Float interestRatePerPeriod;
        public GetLoanProductsInterestRateFrequencyType interestRateFrequencyType;
        @ApiModelProperty(example = "60.000000")
        public Float annualInterestRate;
        public GetLoanProductsResponse.GetLoanProductsAmortizationType amortizationType;
        public GetLoanProductsTemplateResponse.GetLoanProductsInterestTemplateType interestType;
        public GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @ApiModelProperty(example = "1")
        public Integer transactionProcessingStrategyId;
        @ApiModelProperty(example = "Mifos style")
        public String transactionProcessingStrategyName;
        @ApiModelProperty(example = "[]")
        public List<Integer> charges;
        public Set<GetLoanProductsPrincipalVariationsForBorrowerCycle> productsPrincipalVariationsForBorrowerCycle;
        @ApiModelProperty(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @ApiModelProperty(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoanProductsResponse.GetLoanProductsAccountingRule accountingRule;
        public GetLoanAccountingMappings accountingMappings;
        public Set<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public Set<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        @ApiModelProperty(example = "true")
        public Boolean multiDisburseLoan;
        @ApiModelProperty(example = "3")
        public Integer maxTrancheCount;
        @ApiModelProperty(example = "36000.000000")
        public Float outstandingLoanBalance;
        @ApiModelProperty(example = "2")
        public Integer overdueDaysForNPA;
        @ApiModelProperty(example = "50")
        public Integer principalThresholdForLastInstalment;
    }

    @ApiModel(value = "PutLoanProductsProductIdRequest")
    public final static class PutLoanProductsProductIdRequest {
        private PutLoanProductsProductIdRequest() {
        }

        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "70,000.00")
        public Double principal;
    }

    @ApiModel(value = "PutLoanProductsProductIdResponse")
    public final static class PutLoanProductsProductIdResponse {
        private PutLoanProductsProductIdResponse() {
        }

        final class PutLoanChanges {
            private PutLoanChanges() {
            }

            @ApiModelProperty(example = "70,000.00")
            public Double principal;
            @ApiModelProperty(example = "en_GB")
            public String locale;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutLoanChanges changes;
    }
}

