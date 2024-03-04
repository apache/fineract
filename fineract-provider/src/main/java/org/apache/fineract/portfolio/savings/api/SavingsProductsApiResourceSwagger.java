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
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/31/17.
 */
final class SavingsProductsApiResourceSwagger {

    private SavingsProductsApiResourceSwagger() {}

    @Schema(description = "PostSavingsProductsRequest")
    public static final class PostSavingsProductsRequest {

        private PostSavingsProductsRequest() {}

        static final class PostSavingsCharges {

            private PostSavingsCharges() {}

            @Schema(example = "1")
            public Integer id;
        }

        @Schema(example = "Passbook Savings")
        public String name;
        @Schema(example = "PBSV")
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
        @Schema(example = "5.0")
        public Double nominalAnnualInterestRate;
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
        public Set<PostSavingsCharges> charges;
        @Schema(example = "accountMappingForPayment")
        public String accountMappingForPayment;
    }

    @Schema(description = "PostSavingsProductsResponse")
    public static final class PostSavingsProductsResponse {

        private PostSavingsProductsResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutSavingsProductsProductIdRequest")
    public static final class PutSavingsProductsProductIdRequest {

        private PutSavingsProductsProductIdRequest() {}

        @Schema(example = "Passbook Savings Lite.")
        public String description;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "5.73")
        public Double interestRate;
    }

    @Schema(description = "PutSavingsProductsProductIdResponse")
    public static final class PutSavingsProductsProductIdResponse {

        private PutSavingsProductsProductIdResponse() {}

        static final class PutSavingsChanges {

            private PutSavingsChanges() {}

            @Schema(example = "Passbook Savings Lite.")
            public String description;
            @Schema(example = "5.73")
            public Double interestRate;
            @Schema(example = "en")
            public String locale;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutSavingsChanges changes;
    }

    @Schema(description = "GetSavingsProductsResponse")
    public static final class GetSavingsProductsResponse {

        private GetSavingsProductsResponse() {}

        static final class GetSavingsCurrency {

            private GetSavingsCurrency() {}

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

        static final class GetSavingsProductsInterestCompoundingPeriodType {

            private GetSavingsProductsInterestCompoundingPeriodType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @Schema(example = "Daily")
            public String value;
        }

        static final class GetSavingsProductsInterestPostingPeriodType {

            private GetSavingsProductsInterestPostingPeriodType() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @Schema(example = "Monthly")
            public String value;
        }

        static final class GetSavingsProductsInterestCalculationType {

            private GetSavingsProductsInterestCalculationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @Schema(example = "Daily Balance")
            public String value;
        }

        static final class GetSavingsProductsInterestCalculationDaysInYearType {

            private GetSavingsProductsInterestCalculationDaysInYearType() {}

            @Schema(example = "365")
            public Integer id;
            @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @Schema(example = "365 Days")
            public String value;
        }

        static final class GetSavingsProductsAccountingRule {

            private GetSavingsProductsAccountingRule() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountingRuleType.cash")
            public String code;
            @Schema(example = "CASH BASED")
            public String value;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Savings product")
        public String name;
        @Schema(example = "sa1")
        public String shortName;
        @Schema(example = "gtasga")
        public String description;
        public GetSavingsCurrency currency;
        @Schema(example = "5.000000")
        public BigDecimal nominalAnnualInterestRate;
        public GetSavingsProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsProductsInterestCalculationType interestCalculationType;
        public GetSavingsProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @Schema(example = "false")
        public Boolean withdrawalFeeForTransfers;
        public GetSavingsProductsAccountingRule accountingRule;
    }

    @Schema(description = "GetSavingsProductsProductIdResponse")
    public static final class GetSavingsProductsProductIdResponse {

        private GetSavingsProductsProductIdResponse() {}

        static final class GetSavingsProductsGlAccount {

            private GetSavingsProductsGlAccount() {}

            @Schema(example = "12")
            public Integer id;
            @Schema(example = "savings control")
            public String name;
            @Schema(example = "2000001")
            public String glCode;
        }

        static final class GetSavingsProductsAccountingMappings {

            private GetSavingsProductsAccountingMappings() {}

            public GetSavingsProductsGlAccount savingsReferenceAccount;
            public GetSavingsProductsGlAccount overdraftPortfolioControl;
            public GetSavingsProductsGlAccount feeReceivableAccount;
            public GetSavingsProductsGlAccount penaltyReceivableAccount;
            public GetSavingsProductsGlAccount incomeFromFeeAccount;
            public GetSavingsProductsGlAccount incomeFromPenaltyAccount;
            public GetSavingsProductsGlAccount incomeFromInterest;
            public GetSavingsProductsGlAccount interestOnSavingsAccount;
            public GetSavingsProductsGlAccount writeOffAccount;
            public GetSavingsProductsGlAccount savingsControlAccount;
            public GetSavingsProductsGlAccount transfersInSuspenseAccount;
            public GetSavingsProductsGlAccount interestPayableAccount;
        }

        static final class GetSavingsProductsPaymentChannelToFundSourceMappings {

            private GetSavingsProductsPaymentChannelToFundSourceMappings() {}

            static final class GetSavingsProductsPaymentType {

                private GetSavingsProductsPaymentType() {}

                @Schema(example = "10")
                public Integer id;
                @Schema(example = "check")
                public String name;
            }

            static final class GetSavingsProductsFundSourceAccount {

                private GetSavingsProductsFundSourceAccount() {}

                @Schema(example = "12")
                public Integer id;
                @Schema(example = "savings ref")
                public String name;
                @Schema(example = "20")
                public String glCode;
            }

            public GetSavingsProductsPaymentType paymentType;
            public GetSavingsProductsFundSourceAccount fundSourceAccount;
        }

        static final class GetSavingsProductsFeeToIncomeAccountMappings {

            private GetSavingsProductsFeeToIncomeAccountMappings() {}

            static final class GetSavingsProductsFeeToIncomeAccountMappingsCharge {

                private GetSavingsProductsFeeToIncomeAccountMappingsCharge() {}

                @Schema(example = "11")
                public Integer id;
                @Schema(example = "sav charge")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean penalty;
            }

            static final class GetSavingsProductsFeeToIncomeAccountMappingsIncomeAccount {

                private GetSavingsProductsFeeToIncomeAccountMappingsIncomeAccount() {}

                @Schema(example = "16")
                public Integer id;
                @Schema(example = "income from savings fee")
                public String name;
                @Schema(example = "24")
                public String glCode;
            }

            public GetSavingsProductsFeeToIncomeAccountMappingsCharge charge;
            public GetSavingsProductsFeeToIncomeAccountMappingsIncomeAccount incomeAccount;
        }

        static final class GetSavingsProductsPenaltyToIncomeAccountMappings {

            private GetSavingsProductsPenaltyToIncomeAccountMappings() {}

            static final class GetSavingsProductsPenaltyToIncomeAccountMappingsCharge {

                private GetSavingsProductsPenaltyToIncomeAccountMappingsCharge() {}

                @Schema(example = "12")
                public Integer id;
                @Schema(example = "sav 2")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "true")
                public Boolean penalty;
            }

            public GetSavingsProductsPenaltyToIncomeAccountMappingsCharge charge;
            public GetSavingsProductsGlAccount incomeAccount;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "savings product")
        public String name;
        @Schema(example = "sa1")
        public String shortName;
        @Schema(example = "gtasga")
        public String description;
        public GetSavingsProductsResponse.GetSavingsCurrency currency;
        @Schema(example = "5")
        public BigDecimal nominalAnnualInterestRate;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCalculationType interestCalculationType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @Schema(example = "false")
        public Boolean withdrawalFeeForTransfers;
        public GetSavingsProductsResponse.GetSavingsProductsAccountingRule accountingRule;
        public GetSavingsProductsAccountingMappings accountingMappings;
        public Set<GetSavingsProductsPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public Set<GetSavingsProductsFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public Set<GetSavingsProductsPenaltyToIncomeAccountMappings> penaltyToIncomeAccountMappings;
        @Schema(example = "[]")
        public List<Integer> charges;
    }

    @Schema(description = "GetSavingsProductsTemplateResponse")
    public static final class GetSavingsProductsTemplateResponse {

        private GetSavingsProductsTemplateResponse() {}

        static final class GetSavingsProductsTemplateAccountingRule {

            private GetSavingsProductsTemplateAccountingRule() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "accountingRuleType.none")
            public String code;
            @Schema(example = "NONE")
            public String value;
        }

        static final class GetSavingsProductsLockinPeriodFrequencyTypeOptions {

            private GetSavingsProductsLockinPeriodFrequencyTypeOptions() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "savings.lockin.savingsPeriodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String value;
        }

        static final class GetSavingsProductsWithdrawalFeeTypeOptions {

            private GetSavingsProductsWithdrawalFeeTypeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsWithdrawalFeesType.flat")
            public String code;
            @Schema(example = "Flat")
            public String value;
        }

        static final class GetSavingsProductsPaymentTypeOptions {

            private GetSavingsProductsPaymentTypeOptions() {}

            @Schema(example = "14")
            public Integer id;
            @Schema(example = "Wire Transfer")
            public String name;
            @Schema(example = "0")
            public Integer position;
            @Schema(example = "Money Transfer")
            public String description;
            @Schema(example = "true")
            public Boolean isCashPayment;
        }

        static final class GetSavingsProductsAccountingMappingOptions {

            private GetSavingsProductsAccountingMappingOptions() {}

            static final class GetSavingsProductsLiabilityAccountOptions {

                private GetSavingsProductsLiabilityAccountOptions() {}

                static final class GetSavingsProductsLiabilityType {

                    private GetSavingsProductsLiabilityType() {}

                    @Schema(example = "2")
                    public Integer id;
                    @Schema(example = "accountType.liability")
                    public String code;
                    @Schema(example = "LIABILITY")
                    public String description;
                }

                static final class GetSavingsProductsLiabilityUsage {

                    private GetSavingsProductsLiabilityUsage() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "accountUsage.detail")
                    public String code;
                    @Schema(example = "DETAIL")
                    public String description;
                }

                static final class GetSavingsProductsLiabilityTagId {

                    private GetSavingsProductsLiabilityTagId() {}

                    @Schema(example = "0")
                    public Integer id;
                }

                @Schema(example = "15")
                public Integer id;
                @Schema(example = "Savings Control")
                public String name;
                @Schema(example = "50001")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsProductsLiabilityType type;
                public GetSavingsProductsLiabilityUsage usage;
                @Schema(example = "Savings Control")
                public String nameDecorated;
                public GetSavingsProductsLiabilityTagId tagId;
            }

            static final class GetSavingsProductsAssetAccountOptions {

                private GetSavingsProductsAssetAccountOptions() {}

                static final class GetSavingsAssetLiabilityType {

                    private GetSavingsAssetLiabilityType() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "accountType.asset")
                    public String code;
                    @Schema(example = "ASSET")
                    public String description;
                }

                static final class GetSavingsAssetTagId {

                    private GetSavingsAssetTagId() {}
                }

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "Cash")
                public String name;
                @Schema(example = "100001")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsAssetLiabilityType type;
                public GetSavingsProductsLiabilityAccountOptions.GetSavingsProductsLiabilityUsage usage;
                public GetSavingsAssetTagId tagId;
            }

            static final class GetSavingsProductsExpenseAccountOptions {

                private GetSavingsProductsExpenseAccountOptions() {}

                static final class GetSavingsProductsExpenseType {

                    private GetSavingsProductsExpenseType() {}

                    @Schema(example = "5")
                    public Integer id;
                    @Schema(example = "accountType.expense")
                    public String code;
                    @Schema(example = "EXPENSE")
                    public String description;
                }

                @Schema(example = "6")
                public Integer id;
                @Schema(example = "Write Off Expenses")
                public String name;
                @Schema(example = "60001")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsProductsExpenseType type;
                public GetSavingsProductsLiabilityAccountOptions.GetSavingsProductsLiabilityUsage usage;
                public GetSavingsProductsAssetAccountOptions.GetSavingsAssetTagId tagId;
            }

            static final class GetSavingsProductsIncomeAccountOptions {

                private GetSavingsProductsIncomeAccountOptions() {}

                static final class GetSavingsProductsIncomeType {

                    private GetSavingsProductsIncomeType() {}

                    @Schema(example = "4")
                    public Integer id;
                    @Schema(example = "accountType.income")
                    public String code;
                    @Schema(example = "INCOME")
                    public String description;
                }

                @Schema(example = "3")
                public Integer id;
                @Schema(example = "income from interest")
                public String name;
                @Schema(example = "40001")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsProductsIncomeType type;
                public GetSavingsProductsLiabilityAccountOptions.GetSavingsProductsLiabilityUsage usage;
                public GetSavingsProductsAssetAccountOptions.GetSavingsAssetTagId tagId;

            }

            public Set<GetSavingsProductsLiabilityAccountOptions> liabilityAccountOptions;
            public Set<GetSavingsProductsAssetAccountOptions> assetAccountOptions;
            public Set<GetSavingsProductsExpenseAccountOptions> expenseAccountOptions;
            public Set<GetSavingsProductsIncomeAccountOptions> incomeAccountOptions;
        }

        static final class GetSavingsProductsChargeOptions {

            private GetSavingsProductsChargeOptions() {}

            static final class GetSavingsChargeTimeType {

                private GetSavingsChargeTimeType() {}

                @Schema(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @Schema(example = "1")
                public Integer id;
                @Schema(example = "Specified due date")
                public String description;
            }

            static final class GetSavingsProductsChargeAppliesTo {

                private GetSavingsProductsChargeAppliesTo() {}

                @Schema(example = "chargeAppliesTo.savings")
                public String code;
                @Schema(example = "2")
                public Integer id;
                @Schema(example = "Savings")
                public String description;
            }

            static final class GetSavingsChargeCalculationType {

                private GetSavingsChargeCalculationType() {}

                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "1")
                public Integer id;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetSavingsChargePaymentMode {

                private GetSavingsChargePaymentMode() {}

                @Schema(example = "chargepaymentmode.regular")
                public String code;
                @Schema(example = "0")
                public Integer id;
                @Schema(example = "chargepaymentmode.regular")
                public String description;
            }

            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "200")
            public Long amount;
            public GetSavingsProductsChargeAppliesTo chargeAppliesTo;
            public GetSavingsChargeCalculationType chargeCalculationType;
            public GetSavingsChargePaymentMode chargePaymentMode;
            public GetSavingsChargeTimeType chargeTimeType;
            public GetSavingsProductsResponse.GetSavingsCurrency currency;
            @Schema(example = "4")
            public Integer id;
            @Schema(example = "Savings charge 1")
            public String name;
            @Schema(example = "false")
            public Boolean penalty;
        }

        public GetSavingsProductsResponse.GetSavingsCurrency currency;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCalculationType interestCalculationType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetSavingsProductsTemplateAccountingRule accountingRule;
        public Set<GetSavingsProductsResponse.GetSavingsCurrency> currencyOptions;
        public Set<GetSavingsProductsResponse.GetSavingsProductsInterestCompoundingPeriodType> interestCompoundingPeriodTypeOptions;
        public Set<GetSavingsProductsResponse.GetSavingsProductsInterestPostingPeriodType> interestPostingPeriodTypeOptions;
        public Set<GetSavingsProductsResponse.GetSavingsProductsInterestCalculationType> interestCalculationTypeOptions;
        public Set<GetSavingsProductsResponse.GetSavingsProductsInterestCalculationDaysInYearType> interestCalculationDaysInYearTypeOptions;
        public Set<GetSavingsProductsLockinPeriodFrequencyTypeOptions> lockinPeriodFrequencyTypeOptions;
        public Set<GetSavingsProductsWithdrawalFeeTypeOptions> withdrawalFeeTypeOptions;
        public Set<GetSavingsProductsPaymentTypeOptions> paymentTypeOptions;
        public Set<GetSavingsProductsTemplateAccountingRule> accountingRuleOptions;
        public GetSavingsProductsAccountingMappingOptions accountingMappingOptions;
        public Set<GetSavingsProductsChargeOptions> chargeOptions;
        public GetSavingsProductsResponse.GetSavingsCurrency accountMapping;

    }

    @Schema(description = "DeleteSavingsProductsProductIdResponse")
    public static final class DeleteSavingsProductsProductIdResponse {

        private DeleteSavingsProductsProductIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }
}
