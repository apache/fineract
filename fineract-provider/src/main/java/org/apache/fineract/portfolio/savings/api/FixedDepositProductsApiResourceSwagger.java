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
 * Created by Chirag Gupta on 12/23/17.
 */
final class FixedDepositProductsApiResourceSwagger {
    private FixedDepositProductsApiResourceSwagger() {
    }

    @ApiModel(value = "PostFixedDepositProductsRequest")
    public final static class PostFixedDepositProductsRequest {
        private PostFixedDepositProductsRequest() {
        }

        final class PostFixedDepositProductsCharts {
            private PostFixedDepositProductsCharts() {
            }

            final class PostFixedDepositProductsChartSlabs {
                private PostFixedDepositProductsChartSlabs() {
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
            public Set<PostFixedDepositProductsChartSlabs> chartSlabs;
        }

        @ApiModelProperty(example = "Fixed deposit product")
        public String name;
        @ApiModelProperty(example = "FD01")
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
        public Set<PostFixedDepositProductsCharts> charts;
    }

    @ApiModel(value = "PostFixedDepositProductsResponse")
    public final static class PostFixedDepositProductsResponse {
        private PostFixedDepositProductsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutFixedDepositProductsProductIdRequest")
    public final static class PutFixedDepositProductsProductIdRequest {
        private PutFixedDepositProductsProductIdRequest() {
        }

        @ApiModelProperty(example = "Fixed deposit product new offerings")
        public String description;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "5")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "1")
        public Integer minDepositTermTypeId;
    }

    @ApiModel(value = "PutFixedDepositProductsProductIdResponse")
    public final static class PutFixedDepositProductsProductIdResponse {
        private PutFixedDepositProductsProductIdResponse() {
        }

        final class PutFixedDepositProductsChanges {
            private PutFixedDepositProductsChanges() {
            }

            @ApiModelProperty(example = "Fixed deposit product new offerings")
            public String description;
            @ApiModelProperty(example = "5")
            public Integer minDepositTerm;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutFixedDepositProductsChanges changes;
    }

    @ApiModel(value = "GetFixedDepositProductsResponse")
    public final static class GetFixedDepositProductsResponse {
        private GetFixedDepositProductsResponse() {
        }

        final class GetFixedDepositProductsCurrency {
            private GetFixedDepositProductsCurrency() {
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

        final class GetFixedDepositProductsMinDepositTermType {
            private GetFixedDepositProductsMinDepositTermType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @ApiModelProperty(example = "Months")
            public String value;
        }

        final class GetFixedDepositProductsMaxDepositTermType {
            private GetFixedDepositProductsMaxDepositTermType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Years")
            public String value;
        }

        final class GetFixedDepositProductsInterestCompoundingPeriodType {
            private GetFixedDepositProductsInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetFixedDepositProductsInterestPostingPeriodType {
            private GetFixedDepositProductsInterestPostingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetFixedDepositProductsInterestCalculationType {
            private GetFixedDepositProductsInterestCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @ApiModelProperty(example = "Daily Balance")
            public String value;
        }

        final class GetFixedDepositProductsInterestCalculationDaysInYearType {
            private GetFixedDepositProductsInterestCalculationDaysInYearType() {
            }

            @ApiModelProperty(example = "365")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @ApiModelProperty(example = "365 Days")
            public String value;
        }

        final class GetFixedDepositProductsAccountingRule {
            private GetFixedDepositProductsAccountingRule() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountingRuleType.none")
            public String code;
            @ApiModelProperty(example = "NONE")
            public String value;
        }

        @ApiModelProperty(example = "3")
        public Integer id;
        @ApiModelProperty(example = "FD01")
        public String name;
        @ApiModelProperty(example = "FD01")
        public String shortName;
        @ApiModelProperty(example = "FD01")
        public String description;
        public GetFixedDepositProductsCurrency currency;
        @ApiModelProperty(example = "false")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "3")
        public Integer minDepositTerm;
        @ApiModelProperty(example = "4")
        public Integer maxDepositTerm;
        public GetFixedDepositProductsMinDepositTermType minDepositTermType;
        public GetFixedDepositProductsMaxDepositTermType maxDepositTermType;
        @ApiModelProperty(example = "0")
        public Double nominalAnnualInterestRate;
        public GetFixedDepositProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositProductsInterestCalculationType interestCalculationType;
        public GetFixedDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetFixedDepositProductsAccountingRule accountingRule;
    }

    @ApiModel(value = "GetFixedDepositProductsProductIdResponse")
    public final static class GetFixedDepositProductsProductIdResponse {
        private GetFixedDepositProductsProductIdResponse() {
        }

        final class GetFixedDepositProductsProductIdCurrency {
            private GetFixedDepositProductsProductIdCurrency() {
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

        final class GetFixedDepositProductsProductIdInterestCompoundingPeriodType {
            private GetFixedDepositProductsProductIdInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @ApiModelProperty(example = "Daily")
            public String value;
        }

        final class GetFixedDepositProductsProductIdAccountingMappings {
            private GetFixedDepositProductsProductIdAccountingMappings() {
            }

            final class GetFixedDepositProductsProductIdSavingsReferenceAccount {
                private GetFixedDepositProductsProductIdSavingsReferenceAccount() {
                }

                @ApiModelProperty(example = "12")
                public Integer id;
                @ApiModelProperty(example = "savings ref")
                public String name;
                @ApiModelProperty(example = "20")
                public Integer glCode;
            }

            final class GetFixedDepositProductsProductIdIncomeFromFeeAccount {
                private GetFixedDepositProductsProductIdIncomeFromFeeAccount() {
                }

                @ApiModelProperty(example = "16")
                public Integer id;
                @ApiModelProperty(example = "income from savings fee")
                public String name;
                @ApiModelProperty(example = "24")
                public Integer glCode;
            }

            final class GetFixedDepositProductsProductIdIncomeFromPenaltyAccount {
                private GetFixedDepositProductsProductIdIncomeFromPenaltyAccount() {
                }

                @ApiModelProperty(example = "17")
                public Integer id;
                @ApiModelProperty(example = "income from sav penalties")
                public String name;
                @ApiModelProperty(example = "25")
                public Integer glCode;
            }

            final class GetFixedDepositProductsProductIdInterestOnSavingsAccount {
                private GetFixedDepositProductsProductIdInterestOnSavingsAccount() {
                }

                @ApiModelProperty(example = "15")
                public Integer id;
                @ApiModelProperty(example = "interest on savings")
                public String name;
                @ApiModelProperty(example = "23")
                public Integer glCode;
            }

            final class GetFixedDepositProductsProductIdSavingsControlAccount {
                private GetFixedDepositProductsProductIdSavingsControlAccount() {
                }

                @ApiModelProperty(example = "13")
                public Integer id;
                @ApiModelProperty(example = "savings ref tool kit")
                public String name;
                @ApiModelProperty(example = "21")
                public Integer glCode;
            }

            final class GetFixedDepositProductsProductIdTransfersInSuspenseAccount {
                private GetFixedDepositProductsProductIdTransfersInSuspenseAccount() {
                }

                @ApiModelProperty(example = "14")
                public Integer id;
                @ApiModelProperty(example = "saving transfers")
                public String name;
                @ApiModelProperty(example = "22")
                public Integer glCode;
            }

            public GetFixedDepositProductsProductIdSavingsReferenceAccount savingsReferenceAccount;
            public GetFixedDepositProductsProductIdIncomeFromFeeAccount incomeFromFeeAccount;
            public GetFixedDepositProductsProductIdIncomeFromPenaltyAccount incomeFromPenaltyAccount;
            public GetFixedDepositProductsProductIdInterestOnSavingsAccount interestOnSavingsAccount;
            public GetFixedDepositProductsProductIdSavingsControlAccount savingsControlAccount;
            public GetFixedDepositProductsProductIdTransfersInSuspenseAccount transfersInSuspenseAccount;
        }

        final class GetFixedDepositProductsProductIdFeeToIncomeAccountMappings {
            private GetFixedDepositProductsProductIdFeeToIncomeAccountMappings() {
            }

            final class GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsCharge {
                private GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsCharge() {
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

            final class GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount {
                private GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount() {
                }

                @ApiModelProperty(example = "16")
                public Integer id;
                @ApiModelProperty(example = "income from savings fee")
                public String name;
                @ApiModelProperty(example = "24")
                public Integer glCode;
            }

            public GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsCharge charge;
            public GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount incomeAccount;
        }

        final class GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappings {
            private GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappings() {
            }

            final class GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge {
                private GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge() {
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

            public GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge charge;
            public GetFixedDepositProductsProductIdAccountingMappings.GetFixedDepositProductsProductIdIncomeFromPenaltyAccount incomeAccount;
        }

        final class GetFixedDepositProductsProductIdPreClosurePenalInterestOnType {
            private GetFixedDepositProductsProductIdPreClosurePenalInterestOnType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "preClosurePenalInterestOnType.wholeTerm")
            public String code;
            @ApiModelProperty(example = "Whole term")
            public String value;
        }

        final class GetFixedDepositProductsProductIdMinDepositTermType {
            private GetFixedDepositProductsProductIdMinDepositTermType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.weeks")
            public String code;
            @ApiModelProperty(example = "Weeks")
            public String value;
        }

        final class GetFixedDepositProductsProductIdMaxDepositTermType {
            private GetFixedDepositProductsProductIdMaxDepositTermType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @ApiModelProperty(example = "Years")
            public String value;
        }

        final class GetFixedDepositProductsProductIdActiveChart {
            private GetFixedDepositProductsProductIdActiveChart() {
            }

            final class GetFixedDepositProductsProductIdChartSlabs {
                private GetFixedDepositProductsProductIdChartSlabs() {
                }

                final class GetFixedDepositProductsProductIdPeriodType {
                    private GetFixedDepositProductsProductIdPeriodType() {
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
                public GetFixedDepositProductsProductIdPeriodType periodType;
                @ApiModelProperty(example = "0")
                public Integer fromPeriod;
                @ApiModelProperty(example = "90")
                public Integer toPeriod;
                @ApiModelProperty(example = "4.5")
                public Double annualInterestRate;
                public GetFixedDepositProductsProductIdCurrency currency;
            }

            @ApiModelProperty(example = "8")
            public Integer id;
            @ApiModelProperty(example = "[2014, 1, 1]")
            public LocalDate fromDate;
            @ApiModelProperty(example = "8")
            public Integer savingsProductId;
            @ApiModelProperty(example = "Fixed deposit product")
            public String savingsProductName;
            public Set<GetFixedDepositProductsProductIdChartSlabs> chartSlabs;
            public Set<GetFixedDepositProductsProductIdChartSlabs.GetFixedDepositProductsProductIdPeriodType> periodTypes;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "Fixed deposit product")
        public String name;
        @ApiModelProperty(example = "FD01")
        public String shortName;
        @ApiModelProperty(example = "Daily compounding using Daily Balance, 5% per year, 365 days in year")
        public String description;
        public GetFixedDepositProductsProductIdCurrency currency;
        public GetFixedDepositProductsProductIdInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositProductsResponse.GetFixedDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositProductsResponse.GetFixedDepositProductsInterestCalculationType interestCalculationType;
        public GetFixedDepositProductsResponse.GetFixedDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetFixedDepositProductsProductIdAccountingMappings accountingMappings;
        public Set<GetFixedDepositProductsProductIdFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public Set<GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappings> penaltyToIncomeAccountMappings;
        @ApiModelProperty(example = "true")
        public Boolean preClosurePenalApplicable;
        @ApiModelProperty(example = "1.75")
        public Double preClosurePenalInterest;
        public GetFixedDepositProductsProductIdPreClosurePenalInterestOnType preClosurePenalInterestOnType;
        @ApiModelProperty(example = "1")
        public Integer minDepositTerm;
        public GetFixedDepositProductsProductIdMinDepositTermType minDepositTermType;
        @ApiModelProperty(example = "5")
        public Integer maxDepositTerm;
        public GetFixedDepositProductsProductIdMaxDepositTermType maxDepositTermType;
        public GetFixedDepositProductsProductIdActiveChart activeChart;
    }

    @ApiModel(value = "DeleteFixedDepositProductsProductIdResponse")
    public final static class DeleteFixedDepositProductsProductIdResponse {
        private DeleteFixedDepositProductsProductIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
