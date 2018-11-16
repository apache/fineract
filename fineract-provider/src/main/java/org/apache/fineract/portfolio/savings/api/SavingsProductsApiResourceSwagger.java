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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/31/17.
 */
final class SavingsProductsApiResourceSwagger {
    private SavingsProductsApiResourceSwagger() {
    }

    @ApiModel(value = "PostSavingsProductsRequest")
    public final static class PostSavingsProductsRequest {
        private PostSavingsProductsRequest() {
        }

        final class PostSavingsCharges {
            private PostSavingsCharges() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
        }

        @ApiModelProperty(example = "Passbook Savings")
        public String name;
        @ApiModelProperty(example = "PBSV")
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
        @ApiModelProperty(example = "5.0")
        public Double nominalAnnualInterestRate;
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
        public Set<PostSavingsCharges> charges;
    }

    @ApiModel(value = "PostSavingsProductsResponse")
    public final static class PostSavingsProductsResponse {
        private PostSavingsProductsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutSavingsProductsProductIdRequest")
    public final static class PutSavingsProductsProductIdRequest {
        private PutSavingsProductsProductIdRequest() {
        }

        @ApiModelProperty(example = "Passbook Savings Lite.")
        public String description;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "5.73")
        public Double interestRate;
    }

    @ApiModel(value = "PutSavingsProductsProductIdResponse")
    public final static class PutSavingsProductsProductIdResponse {
        private PutSavingsProductsProductIdResponse() {
        }

        final class PutSavingsChanges {
            private PutSavingsChanges() {
            }

            @ApiModelProperty(example = "Passbook Savings Lite.")
            public String description;
            @ApiModelProperty(example = "5.73")
            public Double interestRate;
            @ApiModelProperty(example = "en")
            public String locale;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutSavingsChanges changes;
    }

    @ApiModel(value = "GetSavingsProductsResponse")
    public final static class GetSavingsProductsResponse {
        private GetSavingsProductsResponse() {
        }

        final class GetSavingsCurrency {
            private GetSavingsCurrency() {
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

        final class GetSavingsProductsInterestCompoundingPeriodType {
            private GetSavingsProductsInterestCompoundingPeriodType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
            public String code;
            @ApiModelProperty(example = "Daily")
            public String value;
        }

        final class GetSavingsProductsInterestPostingPeriodType {
            private GetSavingsProductsInterestPostingPeriodType() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
            public String code;
            @ApiModelProperty(example = "Monthly")
            public String value;
        }

        final class GetSavingsProductsInterestCalculationType {
            private GetSavingsProductsInterestCalculationType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
            public String code;
            @ApiModelProperty(example = "Daily Balance")
            public String value;
        }

        final class GetSavingsProductsInterestCalculationDaysInYearType {
            private GetSavingsProductsInterestCalculationDaysInYearType() {
            }

            @ApiModelProperty(example = "365")
            public Integer id;
            @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
            public String code;
            @ApiModelProperty(example = "365 Days")
            public String value;
        }

        final class GetSavingsProductsAccountingRule {
            private GetSavingsProductsAccountingRule() {
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
        @ApiModelProperty(example = "Savings product")
        public String name;
        @ApiModelProperty(example = "sa1")
        public String shortName;
        @ApiModelProperty(example = "gtasga")
        public String description;
        public GetSavingsCurrency currency;
        @ApiModelProperty(example = "5.000000")
        public BigDecimal nominalAnnualInterestRate;
        public GetSavingsProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsProductsInterestCalculationType interestCalculationType;
        public GetSavingsProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @ApiModelProperty(example = "false")
        public Boolean withdrawalFeeForTransfers;
        public GetSavingsProductsAccountingRule accountingRule;
    }

    @ApiModel(value = "GetSavingsProductsProductIdResponse")
    public final static class GetSavingsProductsProductIdResponse {
        private GetSavingsProductsProductIdResponse() {
        }

        final class GetSavingsProductsAccountingMappings {
            private GetSavingsProductsAccountingMappings() {
            }

            final class GetSavingsProductsSavingsReferenceAccount {
                private GetSavingsProductsSavingsReferenceAccount() {
                }

                @ApiModelProperty(example = "12")
                public Integer id;
                @ApiModelProperty(example = "savings ref")
                public String name;
                @ApiModelProperty(example = "20")
                public Integer glCode;
            }

            final class GetSavingsProductsIncomeFromFeeAccount {
                private GetSavingsProductsIncomeFromFeeAccount() {
                }

                @ApiModelProperty(example = "16")
                public Integer id;
                @ApiModelProperty(example = "income from savings fee")
                public String name;
                @ApiModelProperty(example = "24")
                public Integer glCode;
            }

            final class GetSavingsProductsIncomeFromPenaltyAccount {
                private GetSavingsProductsIncomeFromPenaltyAccount() {
                }

                @ApiModelProperty(example = "17")
                public Integer id;
                @ApiModelProperty(example = "income from sav penalties")
                public String name;
                @ApiModelProperty(example = "25")
                public Integer glCode;
            }

            final class GetSavingsProductsInterestOnSavingsAccount {
                private GetSavingsProductsInterestOnSavingsAccount() {
                }

                @ApiModelProperty(example = "15")
                public Integer id;
                @ApiModelProperty(example = "interest on savings")
                public String name;
                @ApiModelProperty(example = "23")
                public Integer glCode;
            }

            final class GetSavingsProductsSavingsControlAccount {
                private GetSavingsProductsSavingsControlAccount() {
                }

                @ApiModelProperty(example = "13")
                public Integer id;
                @ApiModelProperty(example = "savings ref tool kit")
                public String name;
                @ApiModelProperty(example = "21")
                public Integer glCode;
            }

            final class GetSavingsProductsTransfersInSuspenseAccount {
                private GetSavingsProductsTransfersInSuspenseAccount() {
                }

                @ApiModelProperty(example = "14")
                public Integer id;
                @ApiModelProperty(example = "saving transfers")
                public String name;
                @ApiModelProperty(example = "22")
                public Integer glCode;
            }

            public GetSavingsProductsSavingsReferenceAccount savingsReferenceAccount;
            public GetSavingsProductsIncomeFromFeeAccount incomeFromFeeAccount;
            public GetSavingsProductsIncomeFromPenaltyAccount incomeFromPenaltyAccount;
            public GetSavingsProductsInterestOnSavingsAccount interestOnSavingsAccount;
            public GetSavingsProductsSavingsControlAccount savingsControlAccount;
            public GetSavingsProductsTransfersInSuspenseAccount transfersInSuspenseAccount;
        }

        final class GetSavingsProductsPaymentChannelToFundSourceMappings {
            private GetSavingsProductsPaymentChannelToFundSourceMappings() {
            }

            final class GetSavingsProductsPaymentType {
                private GetSavingsProductsPaymentType() {
                }

                @ApiModelProperty(example = "10")
                public Integer id;
                @ApiModelProperty(example = "check")
                public String name;
            }

            final class GetSavingsProductsFundSourceAccount {
                private GetSavingsProductsFundSourceAccount() {
                }

                @ApiModelProperty(example = "12")
                public Integer id;
                @ApiModelProperty(example = "savings ref")
                public String name;
                @ApiModelProperty(example = "20")
                public Integer glCode;
            }

            public GetSavingsProductsPaymentType paymentType;
            public GetSavingsProductsFundSourceAccount fundSourceAccount;
        }

        final class GetSavingsProductsFeeToIncomeAccountMappings {
            private GetSavingsProductsFeeToIncomeAccountMappings() {
            }

            final class GetSavingsProductsFeeToIncomeAccountMappingsCharge {
                private GetSavingsProductsFeeToIncomeAccountMappingsCharge() {
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

            final class GetSavingsProductsFeeToIncomeAccountMappingsIncomeAccount {
                private GetSavingsProductsFeeToIncomeAccountMappingsIncomeAccount() {
                }

                @ApiModelProperty(example = "16")
                public Integer id;
                @ApiModelProperty(example = "income from savings fee")
                public String name;
                @ApiModelProperty(example = "24")
                public Integer glCode;
            }

            public GetSavingsProductsFeeToIncomeAccountMappingsCharge charge;
            public GetSavingsProductsFeeToIncomeAccountMappingsIncomeAccount incomeAccount;
        }

        final class GetSavingsProductsPenaltyToIncomeAccountMappings {
            private GetSavingsProductsPenaltyToIncomeAccountMappings() {
            }

            final class GetSavingsProductsPenaltyToIncomeAccountMappingsCharge {
                private GetSavingsProductsPenaltyToIncomeAccountMappingsCharge() {
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

            public GetSavingsProductsPenaltyToIncomeAccountMappingsCharge charge;
            public GetSavingsProductsAccountingMappings.GetSavingsProductsIncomeFromPenaltyAccount incomeAccount;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "savings product")
        public String name;
        @ApiModelProperty(example = "sa1")
        public String shortName;
        @ApiModelProperty(example = "gtasga")
        public String description;
        public GetSavingsProductsResponse.GetSavingsCurrency currency;
        @ApiModelProperty(example = "5")
        public BigDecimal nominalAnnualInterestRate;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCalculationType interestCalculationType;
        public GetSavingsProductsResponse.GetSavingsProductsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        @ApiModelProperty(example = "false")
        public Boolean withdrawalFeeForTransfers;
        public GetSavingsProductsResponse.GetSavingsProductsAccountingRule accountingRule;
        public GetSavingsProductsAccountingMappings accountingMappings;
        public Set<GetSavingsProductsPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public Set<GetSavingsProductsFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        public Set<GetSavingsProductsPenaltyToIncomeAccountMappings> penaltyToIncomeAccountMappings;
        @ApiModelProperty(example = "[]")
        public List<Integer> charges;
    }

    @ApiModel(value = "GetSavingsProductsTemplateResponse")
    public final static class GetSavingsProductsTemplateResponse {
        private GetSavingsProductsTemplateResponse() {
        }

        final class GetSavingsProductsTemplateAccountingRule {
            private GetSavingsProductsTemplateAccountingRule() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountingRuleType.none")
            public String code;
            @ApiModelProperty(example = "NONE")
            public String value;
        }

        final class GetSavingsProductsLockinPeriodFrequencyTypeOptions {
            private GetSavingsProductsLockinPeriodFrequencyTypeOptions() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "savings.lockin.savingsPeriodFrequencyType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
        }

        final class GetSavingsProductsWithdrawalFeeTypeOptions {
            private GetSavingsProductsWithdrawalFeeTypeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsWithdrawalFeesType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        final class GetSavingsProductsPaymentTypeOptions {
            private GetSavingsProductsPaymentTypeOptions() {
            }

            @ApiModelProperty(example = "14")
            public Integer id;
            @ApiModelProperty(example = "Wire Transfer")
            public String name;
            @ApiModelProperty(example = "0")
            public Integer position;
        }

        final class GetSavingsProductsAccountingMappingOptions {
            private GetSavingsProductsAccountingMappingOptions() {
            }

            final class GetSavingsProductsLiabilityAccountOptions {
                private GetSavingsProductsLiabilityAccountOptions() {
                }

                final class GetSavingsProductsLiabilityType {
                    private GetSavingsProductsLiabilityType() {
                    }

                    @ApiModelProperty(example = "2")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.liability")
                    public String code;
                    @ApiModelProperty(example = "LIABILITY")
                    public String value;
                }

                final class GetSavingsProductsLiabilityUsage {
                    private GetSavingsProductsLiabilityUsage() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "accountUsage.detail")
                    public String code;
                    @ApiModelProperty(example = "DETAIL")
                    public String value;
                }

                final class GetSavingsProductsLiabilityTagId {
                    private GetSavingsProductsLiabilityTagId() {
                    }

                    @ApiModelProperty(example = "0")
                    public Integer id;
                }

                @ApiModelProperty(example = "15")
                public Integer id;
                @ApiModelProperty(example = "Savings Control")
                public String name;
                @ApiModelProperty(example = "50001")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsProductsLiabilityType type;
                public GetSavingsProductsLiabilityUsage usage;
                @ApiModelProperty(example = "Savings Control")
                public String nameDecorated;
                public GetSavingsProductsLiabilityTagId tagId;
            }

            final class GetSavingsProductsAssetAccountOptions {
                private GetSavingsProductsAssetAccountOptions() {
                }

                final class GetSavingsAssetLiabilityType {
                    private GetSavingsAssetLiabilityType() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.asset")
                    public String code;
                    @ApiModelProperty(example = "ASSET")
                    public String value;
                }

                final class GetSavingsAssetTagId {
                    private GetSavingsAssetTagId() {
                    }
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "Cash")
                public String name;
                @ApiModelProperty(example = "100001")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsAssetLiabilityType type;
                public GetSavingsProductsLiabilityAccountOptions.GetSavingsProductsLiabilityUsage usage;
                public GetSavingsAssetTagId tagId;
            }

            final class GetSavingsProductsExpenseAccountOptions {
                private GetSavingsProductsExpenseAccountOptions() {
                }

                final class GetSavingsProductsExpenseType {
                    private GetSavingsProductsExpenseType() {
                    }

                    @ApiModelProperty(example = "5")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.expense")
                    public String code;
                    @ApiModelProperty(example = "EXPENSE")
                    public String value;
                }

                @ApiModelProperty(example = "6")
                public Integer id;
                @ApiModelProperty(example = "Write Off Expenses")
                public String name;
                @ApiModelProperty(example = "60001")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetSavingsProductsExpenseType type;
                public GetSavingsProductsLiabilityAccountOptions.GetSavingsProductsLiabilityUsage usage;
                public GetSavingsProductsAssetAccountOptions.GetSavingsAssetTagId tagId;
            }

            final class GetSavingsProductsIncomeAccountOptions {
                private GetSavingsProductsIncomeAccountOptions() {
                }

                final class GetSavingsProductsIncomeType {
                    private GetSavingsProductsIncomeType() {
                    }

                    @ApiModelProperty(example = "4")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.income")
                    public String code;
                    @ApiModelProperty(example = "INCOME")
                    public String value;
                }

                @ApiModelProperty(example = "3")
                public Integer id;
                @ApiModelProperty(example = "income from interest")
                public String name;
                @ApiModelProperty(example = "40001")
                public Integer glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
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

        final class GetSavingsProductsChargeOptions {
            private GetSavingsProductsChargeOptions() {
            }

            final class GetSavingsChargeTimeType {
                private GetSavingsChargeTimeType() {
                }

                @ApiModelProperty(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "Specified due date")
                public String value;
            }

            final class GetSavingsProductsChargeAppliesTo {
                private GetSavingsProductsChargeAppliesTo() {
                }

                @ApiModelProperty(example = "chargeAppliesTo.savings")
                public String code;
                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "Savings")
                public String value;
            }

            final class GetSavingsChargeCalculationType {
                private GetSavingsChargeCalculationType() {
                }

                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            final class GetSavingsChargePaymentMode {
                private GetSavingsChargePaymentMode() {
                }

                @ApiModelProperty(example = "chargepaymentmode.regular")
                public String code;
                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "chargepaymentmode.regular")
                public String value;
            }

            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "200")
            public Long amount;
            public GetSavingsProductsChargeAppliesTo chargeAppliesTo;
            public GetSavingsChargeCalculationType chargeCalculationType;
            public GetSavingsChargePaymentMode chargePaymentMode;
            public GetSavingsChargeTimeType chargeTimeType;
            public GetSavingsProductsResponse.GetSavingsCurrency currency;
            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "Savings charge 1")
            public String name;
            @ApiModelProperty(example = "false")
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
        public Set<GetSavingsProductsAccountingMappingOptions> accountingMappingOptions;
        public Set<GetSavingsProductsChargeOptions> chargeOptions;
    }

    @ApiModel(value = "DeleteSavingsProductsProductIdResponse")
    public final static class DeleteSavingsProductsProductIdResponse {
        private DeleteSavingsProductsProductIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
