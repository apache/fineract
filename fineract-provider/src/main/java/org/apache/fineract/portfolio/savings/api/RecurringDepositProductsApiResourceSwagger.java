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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/27/17.
 */
final class RecurringDepositProductsApiResourceSwagger {
    private RecurringDepositProductsApiResourceSwagger() {
    }

    @ApiModel(value = "PostRecurringDepositProductsRequest")
    public final static class PostRecurringDepositProductsRequest {
        private PostRecurringDepositProductsRequest() {
        }

        final class PostRecurringDepositProductsCharts {
            private PostRecurringDepositProductsCharts() {
            }

            final class PostRecurringDepositProductsChartSlabs {
                private PostRecurringDepositProductsChartSlabs() {
                }

                @ApiModelProperty(example = "from 0 to 90 days")
                public String description;
                @ApiModelProperty(example = "1")
                public Integer periodType;
                @ApiModelProperty(example = "0")
                public Integer fromPeriod;
                @ApiModelProperty(example = "90")
                public Integer toPeriod;
                @ApiModelProperty(example = "4.5")
                public Double annualInterestRate;
            }

            @ApiModelProperty(example = "01 Jan 2014")
            public String fromDate;
            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd MMMM yyyy")
            public String dateFormat;
            public Set<PostRecurringDepositProductsChartSlabs> chartSlabs;
        }

        @ApiModelProperty(example = "Recurring deposit product")
        public String name;
        @ApiModelProperty(example = "RD01")
        public String shortName;
        @ApiModelProperty(example = "Daily compounding using Daily Balance, 5% per year, 365 days in year")
        public String description;
        @ApiModelProperty(example = "USD")
        public String currencyCode;
        @ApiModelProperty(example = "2")
        public Integer digitsAfterDecimal;
        @ApiModelProperty(example = "0")
        public Integer inMultiplesOf;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "1")
        public Integer interestCompoundingPeriodType;
        @ApiModelProperty(example = "4")
        public Integer interestPostingPeriodType;
        @ApiModelProperty(example = "1")
        public Integer interestCalculationType;
        @ApiModelProperty(example = "365")
        public Integer interestCalculationDaysInYearType;
        @ApiModelProperty(example = "1")
        public Integer accountingRule;
        @ApiModelProperty(example = "1")
        public Integer recurringDepositFrequency;
        @ApiModelProperty(example = "2")
        public Integer recurringDepositFrequencyTypeId;
        @ApiModelProperty(example = "true")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "1.75")
        public Double preClosurePenalInterest;
        @ApiModelProperty(example = "1")
        public Integer preClosurePenalInterestOnTypeId;
        @ApiModelProperty(example = "1")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "1")
        public Integer minDepositTermTypeId;
        @ApiModelProperty(example = "5")
        public Integer maxDepositTerm;
        @ApiModelProperty(example = "3")
        public Integer maxDepositTermTypeId;
        @ApiModelProperty(example = "10000")
        public Long depositAmount;
        @ApiModelProperty(example = "100")
        public Long minDepositAmount;
        @ApiModelProperty(example = "1000000")
        public Long maxDepositAmount;
        public Set<PostRecurringDepositProductsCharts> charts;
    }

    @ApiModel(value = "PostRecurringDepositProductsResponse")
    public final static class PostRecurringDepositProductsResponse {
        private PostRecurringDepositProductsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutRecurringDepositProductsRequest")
    public final static class PutRecurringDepositProductsRequest {
        private PutRecurringDepositProductsRequest() {
        }

        @ApiModelProperty(example = "Recurring deposit product new offerings")
        public String description;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "5")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "1")
        public Integer minDepositTermTypeId;
    }

    @ApiModel(value = "PutRecurringDepositProductsResponse")
    public final static class PutRecurringDepositProductsResponse {
        private PutRecurringDepositProductsResponse() {
        }

        final class PutRecurringDepositProductsChanges {
            private PutRecurringDepositProductsChanges() {
            }

            @ApiModelProperty(example = "Recurring deposit product new offerings")
            public String description;
            @ApiModelProperty(example = "5")
            public Integer minDepositTerm;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutRecurringDepositProductsChanges changes;
    }

    @ApiModel(value = "GetRecurringDepositProductsResponse")
    public final static class GetRecurringDepositProductsResponse {
        private GetRecurringDepositProductsResponse() {
        }

        final class GetRecurringDepositProductsCurrency {
            private GetRecurringDepositProductsCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "1")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetRecurringDepositProductsMinDepositTermType {
            private GetRecurringDepositProductsMinDepositTermType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetRecurringDepositProductsMaxDepositTermType {
            private GetRecurringDepositProductsMaxDepositTermType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Years")
            public String value;
        }

        final class GetRecurringDepositProductsInterestCompoundingPeriodType {
            private GetRecurringDepositProductsInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetRecurringDepositProductsInterestPostingPeriodType {
            private GetRecurringDepositProductsInterestPostingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetRecurringDepositProductsInterestCalculationType {
            private GetRecurringDepositProductsInterestCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @ApiModelProperty(example = "Daily Balance")
            public String value;
        }

        final class GetRecurringDepositProductsInterestCalculationDaysInYearType {
            private GetRecurringDepositProductsInterestCalculationDaysInYearType() {
            }

            @ApiModelProperty(example = "365")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @ApiModelProperty(example = "365 Days")
            public String value;
        }

        final class GetRecurringDepositProductsAccountingRule {
            private GetRecurringDepositProductsAccountingRule() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountingRuleType.none")
            public String code;
            @ApiModelProperty(example = "NONE")
            public String value;
        }

        final class GetRecurringDepositProductsRecurringDepositFrequencyType {
            private GetRecurringDepositProductsRecurringDepositFrequencyType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "recurring.deposit.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        @ApiModelProperty(example = "3")
        public Integer id;
        @ApiModelProperty(example = "RD01")
        public String name;
        @ApiModelProperty(example = "RD01")
        public String shortName;
        @ApiModelProperty(example = "RD01")
        public String description;
        public GetRecurringDepositProductsCurrency currency;
        @ApiModelProperty(example = "1")
        public Integer recurringDepositFrequency;
        public GetRecurringDepositProductsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @ApiModelProperty(example = "false")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "3")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "4")
        public Integer maxDepositTerm;
        public GetRecurringDepositProductsMinDepositTermType minDepositTermType;
        public GetRecurringDepositProductsMaxDepositTermType maxDepositTermType;
        @ApiModelProperty(example = "0")
        public Double nominalAnnualInterestRate;
        public GetRecurringDepositProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositProductsInterestCalculationType interestCalculationType;
        public GetRecurringDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetRecurringDepositProductsAccountingRule accountingRule;
    }

    @ApiModel(value = "GetRecurringDepositProductsProductIdResponse")
    public final static class GetRecurringDepositProductsProductIdResponse {
        private GetRecurringDepositProductsProductIdResponse() {
        }

        final class GetRecurringDepositProductsProductIdCurrency {
            private GetRecurringDepositProductsProductIdCurrency() {
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

        final class GetRecurringDepositProductsProductIdInterestCompoundingPeriodType {
            private GetRecurringDepositProductsProductIdInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @ApiModelProperty(example = "Daily")
            public String value;
        }

        final class GetRecurringDepositProductsProductIdAccountingMappings {
            private GetRecurringDepositProductsProductIdAccountingMappings() {
            }

            final class GetRecurringDepositProductsProductIdSavingsReferenceAccount {
                private GetRecurringDepositProductsProductIdSavingsReferenceAccount() {
                }

                @ApiModelProperty(example = "12")
                public Integer id;
                @ApiModelProperty(example = "savings ref")
                public String name;
                @ApiModelProperty(example = "20")
                public Integer glCode;
            }

            final class GetRecurringDepositProductsProductIdIncomeFromFeeAccount {
                private GetRecurringDepositProductsProductIdIncomeFromFeeAccount() {
                }

                @ApiModelProperty(example = "16")
                public Integer id;
                @ApiModelProperty(example = "income from savings fee")
                public String name;
                @ApiModelProperty(example = "24")
                public Integer glCode;
            }

            final class GetRecurringDepositProductsProductIdIncomeFromPenaltyAccount {
                private GetRecurringDepositProductsProductIdIncomeFromPenaltyAccount() {
                }

                @ApiModelProperty(example = "17")
                public Integer id;
                @ApiModelProperty(example = "income from sav penalties")
                public String name;
                @ApiModelProperty(example = "25")
                public Integer glCode;
            }

            final class GetRecurringDepositProductsProductIdInterestOnSavingsAccount {
                private GetRecurringDepositProductsProductIdInterestOnSavingsAccount() {
                }

                @ApiModelProperty(example = "15")
                public Integer id;
                @ApiModelProperty(example = "interest on savings")
                public String name;
                @ApiModelProperty(example = "23")
                public Integer glCode;
            }

            final class GetRecurringDepositProductsProductIdSavingsControlAccount {
                private GetRecurringDepositProductsProductIdSavingsControlAccount() {
                }

                @ApiModelProperty(example = "13")
                public Integer id;
                @ApiModelProperty(example = "savings ref tool kit")
                public String name;
                @ApiModelProperty(example = "21")
                public Integer glCode;
            }

            final class GetRecurringDepositProductsProductIdTransfersInSuspenseAccount {
                private GetRecurringDepositProductsProductIdTransfersInSuspenseAccount() {
                }

                @ApiModelProperty(example = "14")
                public Integer id;
                @ApiModelProperty(example = "saving transfers")
                public String name;
                @ApiModelProperty(example = "22")
                public Integer glCode;
            }

            public GetRecurringDepositProductsProductIdSavingsReferenceAccount savingsReferenceAccount;
            public GetRecurringDepositProductsProductIdIncomeFromFeeAccount incomeFromFeeAccount;
            public GetRecurringDepositProductsProductIdIncomeFromPenaltyAccount incomeFromPenaltyAccount;
            public GetRecurringDepositProductsProductIdInterestOnSavingsAccount interestOnSavingsAccount;
            public GetRecurringDepositProductsProductIdSavingsControlAccount savingsControlAccount;
            public GetRecurringDepositProductsProductIdTransfersInSuspenseAccount transfersInSuspenseAccount;
        }

        final class GetRecurringDepositProductsProductIdFeeToIncomeAccountMappings {
            private GetRecurringDepositProductsProductIdFeeToIncomeAccountMappings() {
            }

            final class GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsCharge {
                private GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsCharge() {
                }

                @ApiModelProperty(example = "11")
                public Integer id;
                @ApiModelProperty(example = "sav charge")
                public String name;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean penalty;
            }

            final class GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount {
                private GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount() {
                }

                @ApiModelProperty(example = "16")
                public Integer id;
                @ApiModelProperty(example = "income from savings fee")
                public String name;
                @ApiModelProperty(example = "24")
                public Integer glCode;
            }

            public GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsCharge charge;
            public GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount incomeAccount;
        }

        final class GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappings {
            private GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappings() {
            }

            final class GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge {
                private GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge() {
                }

                @ApiModelProperty(example = "12")
                public Integer id;
                @ApiModelProperty(example = "sav 2")
                public String name;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "true")
                public Boolean penalty;
            }

            public GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge charge;
            public GetRecurringDepositProductsProductIdAccountingMappings.GetRecurringDepositProductsProductIdIncomeFromPenaltyAccount incomeAccount;
        }

        final class GetRecurringDepositProductsProductIdPreClosurePenalInterestOnType {
            private GetRecurringDepositProductsProductIdPreClosurePenalInterestOnType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "preClosurePenalInterestOnType.wholeTerm")
            public String code;
            @ApiModelProperty(example = "Whole term")
            public String value;
        }

        final class GetRecurringDepositProductsProductIdMinDepositTermType {
            private GetRecurringDepositProductsProductIdMinDepositTermType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.weeks")
            public String code;
            @ApiModelProperty(example = "Weeks")
            public String value;
        }

        final class GetRecurringDepositProductsProductIdMaxDepositTermType {
            private GetRecurringDepositProductsProductIdMaxDepositTermType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Years")
            public String value;
        }

        final class GetRecurringDepositProductsProductIdActiveChart {
            private GetRecurringDepositProductsProductIdActiveChart() {
            }

            final class GetRecurringDepositProductsProductIdChartSlabs {
                private GetRecurringDepositProductsProductIdChartSlabs() {
                }

                final class GetRecurringDepositProductsProductIdPeriodType {
                    private GetRecurringDepositProductsProductIdPeriodType() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "interestChartPeriodType.weeks")
                    public String code;
                    @ApiModelProperty(example = "Weeks")
                    public String value;
                }

                @ApiModelProperty(example = "18")
                public Integer id;
                @ApiModelProperty(example = "from 0 to 90 days")
                public String description;
                public GetRecurringDepositProductsProductIdPeriodType periodType;
                @ApiModelProperty(example = "0")
                public Integer fromPeriod;
                @ApiModelProperty(example = "90")
                public Integer toPeriod;
                @ApiModelProperty(example = "4.5")
                public Double annualInterestRate;
                public GetRecurringDepositProductsProductIdCurrency currency;
            }

            @ApiModelProperty(example = "8")
            public Integer id;
            @ApiModelProperty(example = "[2014, 1, 1]")
            public LocalDate fromDate;
            @ApiModelProperty(example = "8")
            public Integer savingsProductId;
            @ApiModelProperty(example = "Recurring deposit product")
            public String savingsProductName;
            public Set<GetRecurringDepositProductsProductIdChartSlabs> chartSlabs;
            public Set<GetRecurringDepositProductsProductIdChartSlabs.GetRecurringDepositProductsProductIdPeriodType> periodTypes;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "Recurring deposit product")
        public String name;
        @ApiModelProperty(example = "RD01")
        public String shortName;
        @ApiModelProperty(example = "Daily compounding using Daily Balance, 5% per year, 365 days in year")
        public String description;
        public GetRecurringDepositProductsProductIdCurrency currency;
        public GetRecurringDepositProductsProductIdInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsInterestCalculationType interestCalculationType;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetRecurringDepositProductsProductIdAccountingMappings accountingMappings;
        public Set<GetRecurringDepositProductsProductIdFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public Set<GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappings> penaltyToIncomeAccountMappings;
        @ApiModelProperty(example = "1")
        public Integer recurringDepositFrequency;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @ApiModelProperty(example = "true")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "1.75")
        public Double preClosurePenalInterest;
        public GetRecurringDepositProductsProductIdPreClosurePenalInterestOnType preClosurePenalInterestOnType;
        @ApiModelProperty(example = "1")
        public Integer minDepositTerm;
        public GetRecurringDepositProductsProductIdMinDepositTermType minDepositTermType;
        @ApiModelProperty(example = "5")
        public Integer maxDepositTerm;
        public GetRecurringDepositProductsProductIdMaxDepositTermType maxDepositTermType;
        public GetRecurringDepositProductsProductIdActiveChart activeChart;
    }

    @ApiModel(value = "DeleteRecurringDepositProductsProductIdResponse")
    public final static class DeleteRecurringDepositProductsProductIdResponse {
        private DeleteRecurringDepositProductsProductIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}

