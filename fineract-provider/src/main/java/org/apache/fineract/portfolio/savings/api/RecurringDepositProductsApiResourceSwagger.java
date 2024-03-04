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
 * Created by Chirag Gupta on 12/27/17.
 */
final class RecurringDepositProductsApiResourceSwagger {

    private RecurringDepositProductsApiResourceSwagger() {}

    @Schema(description = "PostRecurringDepositProductsRequest")
    public static final class PostRecurringDepositProductsRequest {

        private PostRecurringDepositProductsRequest() {}

        static final class PostRecurringDepositProductsCharts {

            private PostRecurringDepositProductsCharts() {}

            static final class PostRecurringDepositProductsChartSlabs {

                private PostRecurringDepositProductsChartSlabs() {}

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
            public Set<PostRecurringDepositProductsChartSlabs> chartSlabs;
        }

        @Schema(example = "Recurring deposit product")
        public String name;
        @Schema(example = "RD01")
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
        @Schema(example = "1")
        public Integer recurringDepositFrequency;
        @Schema(example = "2")
        public Integer recurringDepositFrequencyTypeId;
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
        @Schema(example = "10000")
        public Long depositAmount;
        @Schema(example = "100")
        public Long minDepositAmount;
        @Schema(example = "1000000")
        public Long maxDepositAmount;
        public Set<PostRecurringDepositProductsCharts> charts;
    }

    @Schema(description = "PostRecurringDepositProductsResponse")
    public static final class PostRecurringDepositProductsResponse {

        private PostRecurringDepositProductsResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutRecurringDepositProductsRequest")
    public static final class PutRecurringDepositProductsRequest {

        private PutRecurringDepositProductsRequest() {}

        @Schema(example = "Recurring deposit product new offerings")
        public String description;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "5")
        public Integer minDepositTerm;
        @Schema(example = "1")
        public Integer minDepositTermTypeId;
    }

    @Schema(description = "PutRecurringDepositProductsResponse")
    public static final class PutRecurringDepositProductsResponse {

        private PutRecurringDepositProductsResponse() {}

        static final class PutRecurringDepositProductsChanges {

            private PutRecurringDepositProductsChanges() {}

            @Schema(example = "Recurring deposit product new offerings")
            public String description;
            @Schema(example = "5")
            public Integer minDepositTerm;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutRecurringDepositProductsChanges changes;
    }

    @Schema(description = "GetRecurringDepositProductsResponse")
    public static final class GetRecurringDepositProductsResponse {

        private GetRecurringDepositProductsResponse() {}

        static final class GetRecurringDepositProductsCurrency {

            private GetRecurringDepositProductsCurrency() {}

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

        static final class GetRecurringDepositProductsMinDepositTermType {

            private GetRecurringDepositProductsMinDepositTermType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetRecurringDepositProductsMaxDepositTermType {

            private GetRecurringDepositProductsMaxDepositTermType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @Schema(example = "Years")
            public String description;
        }

        static final class GetRecurringDepositProductsInterestCompoundingPeriodType {

            private GetRecurringDepositProductsInterestCompoundingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetRecurringDepositProductsInterestPostingPeriodType {

            private GetRecurringDepositProductsInterestPostingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String description;
        }

        static final class GetRecurringDepositProductsInterestCalculationType {

            private GetRecurringDepositProductsInterestCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @Schema(example = "Daily Balance")
            public String description;
        }

        static final class GetRecurringDepositProductsInterestCalculationDaysInYearType {

            private GetRecurringDepositProductsInterestCalculationDaysInYearType() {}

            @Schema(example = "365")
            public Integer id;
            @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @Schema(example = "365 Days")
            public String description;
        }

        static final class GetRecurringDepositProductsAccountingRule {

            private GetRecurringDepositProductsAccountingRule() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "accountingRuleType.none")
            public String code;
            @Schema(example = "NONE")
            public String description;
        }

        static final class GetRecurringDepositProductsRecurringDepositFrequencyType {

            private GetRecurringDepositProductsRecurringDepositFrequencyType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "recurring.deposit.savingsPeriodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        @Schema(example = "3")
        public Long id;
        @Schema(example = "RD01")
        public String name;
        @Schema(example = "RD01")
        public String shortName;
        @Schema(example = "RD01")
        public String description;
        public GetRecurringDepositProductsCurrency currency;
        @Schema(example = "1")
        public Integer recurringDepositFrequency;
        public GetRecurringDepositProductsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @Schema(example = "false")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "3")
        public Integer minDepositTerm;
        @Schema(example = "4")
        public Integer maxDepositTerm;
        public GetRecurringDepositProductsMinDepositTermType minDepositTermType;
        public GetRecurringDepositProductsMaxDepositTermType maxDepositTermType;
        @Schema(example = "0")
        public Double nominalAnnualInterestRate;
        public GetRecurringDepositProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositProductsInterestCalculationType interestCalculationType;
        public GetRecurringDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetRecurringDepositProductsAccountingRule accountingRule;
    }

    @Schema(description = "GetRecurringDepositProductsProductIdResponse")
    public static final class GetRecurringDepositProductsProductIdResponse {

        private GetRecurringDepositProductsProductIdResponse() {}

        static final class GetRecurringDepositProductsProductIdCurrency {

            private GetRecurringDepositProductsProductIdCurrency() {}

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

        static final class GetRecurringDepositProductsProductIdInterestCompoundingPeriodType {

            private GetRecurringDepositProductsProductIdInterestCompoundingPeriodType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @Schema(example = "Daily")
            public String description;
        }

        static final class GetRecurringDepositProductsGlAccount {

            private GetRecurringDepositProductsGlAccount() {}

            @Schema(example = "12")
            public Long id;
            @Schema(example = "savings control")
            public String name;
            @Schema(example = "2000001")
            public String glCode;
        }

        static final class GetRecurringDepositProductsProductIdAccountingMappings {

            private GetRecurringDepositProductsProductIdAccountingMappings() {}

            public GetRecurringDepositProductsGlAccount incomeFromFeeAccount;
            public GetRecurringDepositProductsGlAccount incomeFromPenaltyAccount;
            public GetRecurringDepositProductsGlAccount interestOnSavingsAccount;
            public GetRecurringDepositProductsGlAccount savingsControlAccount;
            public GetRecurringDepositProductsGlAccount transfersInSuspenseAccount;
            public GetRecurringDepositProductsGlAccount feeReceivableAccount;
            public GetRecurringDepositProductsGlAccount penaltyReceivableAccount;
            public GetRecurringDepositProductsGlAccount interestPayableAccount;
        }

        static final class GetRecurringDepositProductsProductIdFeeToIncomeAccountMappings {

            private GetRecurringDepositProductsProductIdFeeToIncomeAccountMappings() {}

            static final class GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsCharge {

                private GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsCharge() {}

                @Schema(example = "11")
                public Long id;
                @Schema(example = "sav charge")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean penalty;
            }

            static final class GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount {

                private GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount() {}

                @Schema(example = "16")
                public Long id;
                @Schema(example = "income from savings fee")
                public String name;
                @Schema(example = "24")
                public String glCode;
            }

            public GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsCharge charge;
            public GetRecurringDepositProductsProductIdFeeToIncomeAccountMappingsIncomeAccount incomeAccount;
        }

        static final class GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappings {

            private GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappings() {}

            static final class GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge {

                private GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge() {}

                @Schema(example = "12")
                public Long id;
                @Schema(example = "sav 2")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "true")
                public Boolean penalty;
            }

            public GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappingsCharge charge;
            public GetRecurringDepositProductsGlAccount incomeAccount;
        }

        static final class GetRecurringDepositProductsProductIdPreClosurePenalInterestOnType {

            private GetRecurringDepositProductsProductIdPreClosurePenalInterestOnType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "preClosurePenalInterestOnType.wholeTerm")
            public String code;
            @Schema(example = "Whole term")
            public String description;
        }

        static final class GetRecurringDepositProductsProductIdMinDepositTermType {

            private GetRecurringDepositProductsProductIdMinDepositTermType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.weeks")
            public String code;
            @Schema(example = "Weeks")
            public String description;
        }

        static final class GetRecurringDepositProductsProductIdMaxDepositTermType {

            private GetRecurringDepositProductsProductIdMaxDepositTermType() {}

            @Schema(example = "3")
            public Long id;
            @Schema(example = "deposit.term.savingsPeriodFrequencyType.years")
            public String code;
            @Schema(example = "Years")
            public String description;
        }

        static final class GetRecurringDepositProductsProductIdActiveChart {

            private GetRecurringDepositProductsProductIdActiveChart() {}

            static final class GetRecurringDepositProductsProductIdChartSlabs {

                private GetRecurringDepositProductsProductIdChartSlabs() {}

                static final class GetRecurringDepositProductsProductIdPeriodType {

                    private GetRecurringDepositProductsProductIdPeriodType() {}

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
                public GetRecurringDepositProductsProductIdPeriodType periodType;
                @Schema(example = "0")
                public Integer fromPeriod;
                @Schema(example = "90")
                public Integer toPeriod;
                @Schema(example = "4.5")
                public Double annualInterestRate;
                public GetRecurringDepositProductsProductIdCurrency currency;
            }

            @Schema(example = "8")
            public Long id;
            @Schema(example = "[2014, 1, 1]")
            public LocalDate fromDate;
            @Schema(example = "8")
            public Long savingsProductId;
            @Schema(example = "Recurring deposit product")
            public String savingsProductName;
            public Set<GetRecurringDepositProductsProductIdChartSlabs> chartSlabs;
            public Set<GetRecurringDepositProductsProductIdChartSlabs.GetRecurringDepositProductsProductIdPeriodType> periodTypes;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Recurring deposit product")
        public String name;
        @Schema(example = "RD01")
        public String shortName;
        @Schema(example = "Daily compounding using Daily Balance, 5% per year, 365 days in year")
        public String description;
        public GetRecurringDepositProductsProductIdCurrency currency;
        public GetRecurringDepositProductsProductIdInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsInterestCalculationType interestCalculationType;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetRecurringDepositProductsProductIdAccountingMappings accountingMappings;
        public Set<GetRecurringDepositProductsProductIdFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public Set<GetRecurringDepositProductsProductIdPenaltyToIncomeAccountMappings> penaltyToIncomeAccountMappings;
        @Schema(example = "1")
        public Integer recurringDepositFrequency;
        public GetRecurringDepositProductsResponse.GetRecurringDepositProductsRecurringDepositFrequencyType recurringDepositFrequencyType;
        @Schema(example = "true")
        public Boolean preClosurePenalApplicable;
        @Schema(example = "1.75")
        public Double preClosurePenalInterest;
        public GetRecurringDepositProductsProductIdPreClosurePenalInterestOnType preClosurePenalInterestOnType;
        @Schema(example = "1")
        public Integer minDepositTerm;
        public GetRecurringDepositProductsProductIdMinDepositTermType minDepositTermType;
        @Schema(example = "5")
        public Integer maxDepositTerm;
        public GetRecurringDepositProductsProductIdMaxDepositTermType maxDepositTermType;
        public GetRecurringDepositProductsProductIdActiveChart activeChart;
    }

    @Schema(description = "DeleteRecurringDepositProductsProductIdResponse")
    public static final class DeleteRecurringDepositProductsProductIdResponse {

        private DeleteRecurringDepositProductsProductIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }
}
