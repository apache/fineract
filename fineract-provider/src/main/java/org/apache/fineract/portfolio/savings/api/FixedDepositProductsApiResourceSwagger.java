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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/23/17.
 */
final class FixedDepositProductsApiResourceSwagger {

    private FixedDepositProductsApiResourceSwagger() {}

    @Schema(description = "PostFixedDepositProductsRequest")
    public static final class PostFixedDepositProductsRequest {

        private PostFixedDepositProductsRequest() {}

        static final class PostFixedDepositProductsCharts {

            private PostFixedDepositProductsCharts() {}

            static final class PostFixedDepositProductsChartSlabs {

                private PostFixedDepositProductsChartSlabs() {}

                @Schema(example = "from 0 to 90 days")
                public String description;
                @Schema(example = "1")
                public Integer periodType;
                @Schema(example = "0")
                public Integer fromPeriod;
                @Schema(example = "90")
                public Integer toPeriod;
                @Schema(example = "4.5")
                public Double annualInterestRate;
            }

            @Schema(example = "01 Jan 2014")
            public String fromDate;
            @Schema(example = "en")
            public String locale;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            public Set<PostFixedDepositProductsChartSlabs> chartSlabs;
        }

        @Schema(example = "Fixed deposit product")
        public String name;
        @Schema(example = "FD01")
        public String shortName;
        @Schema(example = "Daily compounding using Daily Balance, 5% per year, 365 days in year")
        public String description;
        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "2")
        public Integer digitsAfterDecimal;
        @Schema(example = "0")
        public Integer inMultiplesOf;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "1")
        public Integer interestCompoundingPeriodType;
        @Schema(example = "4")
        public Integer interestPostingPeriodType;
        @Schema(example = "1")
        public Integer interestCalculationType;
        @Schema(example = "365")
        public Integer interestCalculationDaysInYearType;
        @Schema(example = "1")
        public Integer accountingRule;
        @Schema(example = "true")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "1.75")
        public Double preClosurePenalInterest;
        @Schema(example = "1")
        public Integer preClosurePenalInterestOnTypeId;
        @Schema(example = "1")
        public Integer minDepositTerm;
        @Schema(example = "1")
        public Integer minDepositTermTypeId;
        @Schema(example = "5")
        public Integer maxDepositTerm;
        @Schema(example = "3")
        public Integer maxDepositTermTypeId;
        public Set<PostFixedDepositProductsCharts> charts;
    }

    @Schema(description = "PostFixedDepositProductsResponse")
    public static final class PostFixedDepositProductsResponse {

        private PostFixedDepositProductsResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutFixedDepositProductsProductIdRequest")
    public static final class PutFixedDepositProductsProductIdRequest {

        private PutFixedDepositProductsProductIdRequest() {}

        @Schema(example = "Fixed deposit product new offerings")
        public String description;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "5")
        public Integer minDepositTerm;
        @Schema(example = "1")
        public Integer minDepositTermTypeId;
    }

    @Schema(description = "PutFixedDepositProductsProductIdResponse")
    public static final class PutFixedDepositProductsProductIdResponse {

        private PutFixedDepositProductsProductIdResponse() {}

        static final class PutFixedDepositProductsChanges {

            private PutFixedDepositProductsChanges() {}

            @Schema(example = "Fixed deposit product new offerings")
            public String description;
            @Schema(example = "5")
            public Integer minDepositTerm;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutFixedDepositProductsChanges changes;
    }

    @Schema(description = "GetFixedDepositProductsResponse")
    public static final class GetFixedDepositProductsResponse {

        private GetFixedDepositProductsResponse() {}

        static final class GetFixedDepositProductsCurrency {

            private GetFixedDepositProductsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "1")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetFixedDepositProductsMinDepositTermType {

            private GetFixedDepositProductsMinDepositTermType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetFixedDepositProductsMaxDepositTermType {

            private GetFixedDepositProductsMaxDepositTermType() {}

            @Schema(example = "3")
            public Long id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @Schema(example = "Years")
            public String description;
        }

        static final class GetFixedDepositProductsInterestCompoundingPeriodType {

            private GetFixedDepositProductsInterestCompoundingPeriodType() {}

            @Schema(example = "4")
            public Long id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetFixedDepositProductsInterestPostingPeriodType {

            private GetFixedDepositProductsInterestPostingPeriodType() {}

            @Schema(example = "4")
            public Long id;
            @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetFixedDepositProductsInterestCalculationType {

            private GetFixedDepositProductsInterestCalculationType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @Schema(example = "Daily Balance")
            public String description;
        }

        static final class GetFixedDepositProductsInterestCalculationDaysInYearType {

            private GetFixedDepositProductsInterestCalculationDaysInYearType() {}

            @Schema(example = "365")
            public Long id;
            @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @Schema(example = "365 Days")
            public String description;
        }

        static final class GetFixedDepositProductsAccountingRule {

            private GetFixedDepositProductsAccountingRule() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "accountingRuleType.none")
            public String code;
            @Schema(example = "NONE")
            public String description;
        }

        @Schema(example = "3")
        public Long id;
        @Schema(example = "FD01")
        public String name;
        @Schema(example = "FD01")
        public String shortName;
        @Schema(example = "FD01")
        public String description;
        public GetFixedDepositProductsCurrency currency;
        @Schema(example = "false")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "3")
        public Integer minDepositTerm;
        @Schema(example = "4")
        public Integer maxDepositTerm;
        public GetFixedDepositProductsMinDepositTermType minDepositTermType;
        public GetFixedDepositProductsMaxDepositTermType maxDepositTermType;
        @Schema(example = "0")
        public Double nominalAnnualInterestRate;
        public GetFixedDepositProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositProductsInterestCalculationType interestCalculationType;
        public GetFixedDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetFixedDepositProductsAccountingRule accountingRule;
    }

    @Schema(description = "GetFixedDepositProductsProductIdResponse")
    public static final class GetFixedDepositProductsProductIdResponse {

        private GetFixedDepositProductsProductIdResponse() {}

        static final class GetFixedDepositProductsProductIdCurrency {

            private GetFixedDepositProductsProductIdCurrency() {}

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

        static final class GetFixedDepositProductsProductIdInterestCompoundingPeriodType {

            private GetFixedDepositProductsProductIdInterestCompoundingPeriodType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @Schema(example = "Daily")
            public String description;
        }

        static final class GetFixedDepositProductsGlAccount {

            private GetFixedDepositProductsGlAccount() {}

            @Schema(example = "12")
            public Long id;
            @Schema(example = "savings ref")
            public String name;
            @Schema(example = "20")
            public Integer glCode;
        }

        static final class GetFixedDepositProductsProductIdAccountingMappings {

            private GetFixedDepositProductsProductIdAccountingMappings() {}

            public GetFixedDepositProductsGlAccount savingsReferenceAccount;
            public GetFixedDepositProductsGlAccount feeReceivableAccount;
            public GetFixedDepositProductsGlAccount penaltyReceivableAccount;
            public GetFixedDepositProductsGlAccount incomeFromFeeAccount;
            public GetFixedDepositProductsGlAccount incomeFromPenaltyAccount;
            public GetFixedDepositProductsGlAccount interestOnSavingsAccount;
            public GetFixedDepositProductsGlAccount savingsControlAccount;
            public GetFixedDepositProductsGlAccount transfersInSuspenseAccount;
            public GetFixedDepositProductsGlAccount interestPayableAccount;
        }

        static final class GetFixedDepositProductsProductIdFeeToIncomeAccountMappings {

            private GetFixedDepositProductsProductIdFeeToIncomeAccountMappings() {}

            static final class GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsCharge {

                private GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsCharge() {}

                @Schema(example = "11")
                public Long id;
                @Schema(example = "sav charge")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean penalty;
            }

            static final class GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount {

                private GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount() {}

                @Schema(example = "16")
                public Long id;
                @Schema(example = "income from savings fee")
                public String name;
                @Schema(example = "24")
                public String glCode;
            }

            public GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsCharge charge;
            public GetFixedDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount incomeAccount;
        }

        static final class GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappings {

            private GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappings() {}

            static final class GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge {

                private GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge() {}

                @Schema(example = "12")
                public Long id;
                @Schema(example = "sav 2")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "true")
                public Boolean penalty;
            }

            public GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge charge;
            public GetFixedDepositProductsGlAccount incomeAccount;
        }

        static final class GetFixedDepositProductsProductIdPreClosurePenalInterestOnType {

            private GetFixedDepositProductsProductIdPreClosurePenalInterestOnType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "preClosurePenalInterestOnType.wholeTerm")
            public String code;
            @Schema(example = "Whole term")
            public String description;
        }

        static final class GetFixedDepositProductsProductIdMinDepositTermType {

            private GetFixedDepositProductsProductIdMinDepositTermType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.weeks")
            public String code;
            @Schema(example = "Weeks")
            public String description;
        }

        static final class GetFixedDepositProductsProductIdMaxDepositTermType {

            private GetFixedDepositProductsProductIdMaxDepositTermType() {}

            @Schema(example = "3")
            public Long id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @Schema(example = "Years")
            public String description;
        }

        static final class GetFixedDepositProductsProductIdActiveChart {

            private GetFixedDepositProductsProductIdActiveChart() {}

            static final class GetFixedDepositProductsProductIdChartSlabs {

                private GetFixedDepositProductsProductIdChartSlabs() {}

                static final class GetFixedDepositProductsProductIdPeriodType {

                    private GetFixedDepositProductsProductIdPeriodType() {}

                    @Schema(example = "1")
                    public Long id;
                    @Schema(example = "interestChartPeriodType.weeks")
                    public String code;
                    @Schema(example = "Weeks")
                    public String description;
                }

                @Schema(example = "18")
                public Long id;
                @Schema(example = "from 0 to 90 days")
                public String description;
                public GetFixedDepositProductsProductIdPeriodType periodType;
                @Schema(example = "0")
                public Integer fromPeriod;
                @Schema(example = "90")
                public Integer toPeriod;
                @Schema(example = "4.5")
                public Double annualInterestRate;
                public GetFixedDepositProductsProductIdCurrency currency;
            }

            @Schema(example = "8")
            public Long id;
            @Schema(example = "[2014, 1, 1]")
            public LocalDate fromDate;
            @Schema(example = "8")
            public Long savingsProductId;
            @Schema(example = "Fixed deposit product")
            public String savingsProductName;
            public Set<GetFixedDepositProductsProductIdChartSlabs> chartSlabs;
            public Set<GetFixedDepositProductsProductIdChartSlabs.GetFixedDepositProductsProductIdPeriodType> periodTypes;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Fixed deposit product")
        public String name;
        @Schema(example = "FD01")
        public String shortName;
        @Schema(example = "Daily compounding using Daily Balance, 5% per year, 365 days in year")
        public String description;
        public GetFixedDepositProductsProductIdCurrency currency;
        public GetFixedDepositProductsProductIdInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetFixedDepositProductsResponse.GetFixedDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetFixedDepositProductsResponse.GetFixedDepositProductsInterestCalculationType interestCalculationType;
        public GetFixedDepositProductsResponse.GetFixedDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetFixedDepositProductsProductIdAccountingMappings accountingMappings;
        public Set<GetFixedDepositProductsProductIdFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public Set<GetFixedDepositProductsProductIdPenaltyToIncomeAccountMappings> penaltyToIncomeAccountMappings;
        @Schema(example = "true")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "1.75")
        public Double preClosurePenalInterest;
        public GetFixedDepositProductsProductIdPreClosurePenalInterestOnType preClosurePenalInterestOnType;
        @Schema(example = "1")
        public Integer minDepositTerm;
        public GetFixedDepositProductsProductIdMinDepositTermType minDepositTermType;
        @Schema(example = "5")
        public Integer maxDepositTerm;
        public GetFixedDepositProductsProductIdMaxDepositTermType maxDepositTermType;
        public GetFixedDepositProductsProductIdActiveChart activeChart;
    }

    @Schema(description = "DeleteFixedDepositProductsProductIdResponse")
    public static final class DeleteFixedDepositProductsProductIdResponse {

        private DeleteFixedDepositProductsProductIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }
}
