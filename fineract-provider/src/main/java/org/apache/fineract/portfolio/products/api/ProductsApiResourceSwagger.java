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
package org.apache.fineract.portfolio.products.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 01/01/18.
 */
final class ProductsApiResourceSwagger {

    private ProductsApiResourceSwagger() {}

    @Schema(description = "GetProductsTypeProductIdResponse")
    public static final class GetProductsTypeProductIdResponse {

        private GetProductsTypeProductIdResponse() {}

        static final class GetProductsCurrency {

            private GetProductsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "100")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetProductsMarketPrice {

            private GetProductsMarketPrice() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "Feb 1, 2016")
            public String fromDate;
            @Schema(example = "1")
            public Integer shareValue;
        }

        static final class GetProductsCharges {

            private GetProductsCharges() {}

            static final class GetChargesCurrency {

                private GetChargesCurrency() {}

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

            static final class GetChargeTimeType {

                private GetChargeTimeType() {}

                @Schema(example = "13")
                public Integer id;
                @Schema(example = "chargeTimeType.activation")
                public String code;
                @Schema(example = "Share Account Activate")
                public String description;
            }

            static final class GetChargeAppliesTo {

                private GetChargeAppliesTo() {}

                @Schema(example = "4")
                public Integer id;
                @Schema(example = "chargeAppliesTo.shares")
                public String code;
                @Schema(example = "Shares")
                public String description;
            }

            static final class GetChargeCalculationType {

                private GetChargeCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetChargePaymentMode {

                private GetChargePaymentMode() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "chargepaymentmode.regular")
                public String code;
                @Schema(example = "Regular")
                public String description;
            }

            @Schema(example = "20")
            public Integer id;
            @Schema(example = "Share Account Activation Flat")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean penalty;
            public GetChargesCurrency currency;
            @Schema(example = "1")
            public Integer amount;
            public GetChargeTimeType chargeTimeType;
            public GetChargeAppliesTo chargeAppliesTo;
            public GetChargeCalculationType chargeCalculationType;
            public GetChargePaymentMode chargePaymentMode;
        }

        static final class GetLockPeriodTypeEnum {

            private GetLockPeriodTypeEnum() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "Days")
            public String description;
        }

        static final class GetProductsAccountingRule {

            private GetProductsAccountingRule() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountingRuleType.cash")
            public String code;
            @Schema(example = "CASH BASED")
            public String description;
        }

        static final class GetProductsAccountingMappings {

            private GetProductsAccountingMappings() {}

            static final class GetShareReferenceId {

                private GetShareReferenceId() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @Schema(example = "ASSET_ED1461237837829")
                public String glCode;
            }

            static final class GetIncomeFromFeeAccountId {

                private GetIncomeFromFeeAccountId() {}

                @Schema(example = "14")
                public Integer id;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @Schema(example = "INCOME_OY1461237869836")
                public String glCode;
            }

            static final class GetShareEquityId {

                private GetShareEquityId() {}

                @Schema(example = "66")
                public Integer id;
                @Schema(example = "Equity Account")
                public String name;
                @Schema(example = "EQUITY1")
                public String glCode;
            }

            static final class GetShareSuspenseId {

                private GetShareSuspenseId() {}

                @Schema(example = "8")
                public Integer id;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @Schema(example = "LIABILITY_MA1461237860198")
                public String glCode;
            }

            public GetShareReferenceId shareReferenceId;
            public GetIncomeFromFeeAccountId incomeFromFeeAccountId;
            public GetShareEquityId shareEquityId;
            public GetShareSuspenseId shareSuspenseId;
        }

        static final class GetProductsMinimumActivePeriodFrequencyTypeOptions {

            private GetProductsMinimumActivePeriodFrequencyTypeOptions() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "savings.lockin.sharePeriodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        static final class GetProductsAccountingMappingOptions {

            private GetProductsAccountingMappingOptions() {}

            static final class GetProductsLiabilityAccountOptions {

                private GetProductsLiabilityAccountOptions() {}

                static final class GetProductsLiabilityType {

                    private GetProductsLiabilityType() {}

                    @Schema(example = "2")
                    public Integer id;
                    @Schema(example = "accountType.liability")
                    public String code;
                    @Schema(example = "LIABILITY")
                    public String description;
                }

                static final class GetProductsLiabilityUsage {

                    private GetProductsLiabilityUsage() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "accountUsage.detail")
                    public String code;
                    @Schema(example = "DETAIL")
                    public String description;
                }

                static final class GetProductsTagId {

                    private GetProductsTagId() {}

                    @Schema(example = "0")
                    public Integer id;
                    @Schema(example = "false")
                    public Boolean isActive;
                }

                @Schema(example = "4")
                public Integer id;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @Schema(example = "LIABILITY_2T1461237838897")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetProductsLiabilityType type;
                public GetProductsLiabilityUsage usage;
                @Schema(example = "DEFAULT_DESCRIPTION")
                public String description;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String nameDecorated;
                public GetProductsTagId tagId;
            }

            static final class GetProductsAssetAccountOptions {

                private GetProductsAssetAccountOptions() {}

                static final class GetAssetType {

                    private GetAssetType() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "accountType.asset")
                    public String code;
                    @Schema(example = "ASSET")
                    public String description;
                }

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @Schema(example = "ASSET_ED1461237837829")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetAssetType type;
                public GetProductsLiabilityAccountOptions.GetProductsLiabilityUsage usage;
                @Schema(example = "DEFAULT_DESCRIPTION")
                public String description;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String nameDecorated;
                public GetProductsLiabilityAccountOptions.GetProductsTagId tagId;
            }

            static final class GetProductsIncomeAccountOptions {

                private GetProductsIncomeAccountOptions() {}

                static final class GetIncomeType {

                    private GetIncomeType() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "accountType.income")
                    public String code;
                    @Schema(example = "INCOME")
                    public String description;
                }

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @Schema(example = "INCOME_9O1461237838422")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetIncomeType type;
                public GetProductsLiabilityAccountOptions.GetProductsLiabilityUsage usage;
                @Schema(example = "DEFAULT_DESCRIPTION")
                public String description;
                @Schema(example = "ACCOUNT_NAME_1FJBQ")
                public String nameDecorated;
                public GetProductsLiabilityAccountOptions.GetProductsTagId tagId;
            }

            static final class GetProductsEquityAccountOptions {

                private GetProductsEquityAccountOptions() {}

                static final class GetEquityType {

                    private GetEquityType() {}

                    @Schema(example = "3")
                    public Integer id;
                    @Schema(example = "accountType.equity")
                    public String code;
                    @Schema(example = "EQUITY")
                    public String description;
                }

                @Schema(example = "66")
                public Integer id;
                @Schema(example = "Equity Account")
                public String name;
                @Schema(example = "EQUITY1")
                public String glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetEquityType type;
                public GetProductsLiabilityAccountOptions.GetProductsLiabilityUsage usage;
                @Schema(example = "Equity Account")
                public String nameDecorated;
                public GetProductsLiabilityAccountOptions.GetProductsTagId tagId;
            }

            public Set<GetProductsLiabilityAccountOptions> liabilityAccountOptions;
            public Set<GetProductsAssetAccountOptions> assetAccountOptions;
            public Set<GetProductsIncomeAccountOptions> incomeAccountOptions;
            public Set<GetProductsEquityAccountOptions> equityAccountOptions;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Share Product")
        public String name;
        @Schema(example = "SP")
        public String shortName;
        @Schema(example = "SP")
        public String description;
        public GetProductsCurrency currency;
        @Schema(example = "100")
        public Integer totalShares;
        @Schema(example = "50")
        public Integer totalSharesIssued;
        @Schema(example = "1")
        public Integer unitPrice;
        @Schema(example = "50")
        public Integer shareCapital;
        @Schema(example = "1")
        public Integer minimumShares;
        @Schema(example = "10")
        public Integer nominalShares;
        @Schema(example = "50")
        public Integer maximumShares;
        public Set<GetProductsMarketPrice> marketPrice;
        public Set<GetProductsCharges> charges;
        @Schema(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        @Schema(example = "1")
        public Integer lockinPeriod;
        public GetLockPeriodTypeEnum lockPeriodTypeEnum;
        @Schema(example = "1")
        public Integer minimumActivePeriod;
        public GetLockPeriodTypeEnum minimumActivePeriodForDividendsTypeEnum;
        public GetProductsAccountingRule accountingRule;
        public GetProductsAccountingMappings accountingMappings;
        public Set<GetProductsCharges.GetChargesCurrency> currencyOptions;
        public Set<GetProductsCharges> chargeOptions;
        public Set<GetProductsMinimumActivePeriodFrequencyTypeOptions> minimumActivePeriodFrequencyTypeOptions;
        public Set<GetProductsMinimumActivePeriodFrequencyTypeOptions> lockinPeriodFrequencyTypeOptions;
        public GetProductsAccountingMappingOptions accountingMappingOptions;
    }

    @Schema(description = "GetProductsTypeResponse")
    public static final class GetProductsTypeResponse {

        private GetProductsTypeResponse() {}

        static final class GetProductsPageItems {

            private GetProductsPageItems() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Share Product")
            public String name;
            @Schema(example = "Share Product Description")
            public String shortName;
            @Schema(example = "100")
            public Integer totalShares;
        }

        @Schema(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetProductsPageItems> pageItems;
    }

    @Schema(description = "PostProductsTypeRequest")
    public static final class PostProductsTypeRequest {

        private PostProductsTypeRequest() {}

        static final class PostProductsMarketPricePeriods {

            private PostProductsMarketPricePeriods() {}

            @Schema(example = "en")
            public String locale;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "04 May 2016")
            public String fromDate;
            @Schema(example = "2")
            public Integer shareValue;
        }

        static final class PostProductsChargesSelected {

            private PostProductsChargesSelected() {}

            @Schema(example = "20")
            public Integer id;
        }

        @Schema(example = "Share Product")
        public String name;
        @Schema(example = "SP")
        public String shortName;
        @Schema(example = "Description")
        public String description;
        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "2")
        public Integer digitsAfterDecimal;
        @Schema(example = "1")
        public Integer inMultiplesOf;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "1000")
        public Integer totalShares;
        @Schema(example = "1000")
        public Integer sharesIssued;
        @Schema(example = "1")
        public Integer unitPrice;
        @Schema(example = "10")
        public Integer minimumShares;
        @Schema(example = "20")
        public Integer nominalShares;
        @Schema(example = "30")
        public Integer maximumShares;
        @Schema(example = "1")
        public Integer minimumActivePeriodForDividends;
        @Schema(example = "0")
        public Integer minimumactiveperiodFrequencyType;
        @Schema(example = "1")
        public Integer lockinPeriodFrequency;
        @Schema(example = "1")
        public Integer lockinPeriodFrequencyType;
        @Schema(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        public Set<PostProductsMarketPricePeriods> marketPricePeriods;
        public Set<PostProductsChargesSelected> chargesSelected;
        @Schema(example = "1")
        public Integer accountingRule;
    }

    @Schema(description = "PostProductsTypeResponse")
    public static final class PostProductsTypeResponse {

        private PostProductsTypeResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutProductsTypeProductIdRequest")
    public static final class PutProductsTypeProductIdRequest {

        private PutProductsTypeProductIdRequest() {}

        @Schema(example = "Share Product Description.")
        public String description;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "5.0")
        public Double unitPrice;
    }

    @Schema(description = "PutProductsTypeProductIdResponse")
    public static final class PutProductsTypeProductIdResponse {

        private PutProductsTypeProductIdResponse() {}

        static final class PutProductsChanges {

            private PutProductsChanges() {}

            @Schema(example = "Share Product Description.")
            public String description;
            @Schema(example = "5.0")
            public Double unitPrice;
            @Schema(example = "en")
            public String locale;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutProductsChanges changes;
    }
}
