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
package org.apache.fineract.portfolio.products.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 01/01/18.
 */
final class ProductsApiResourceSwagger {
    private ProductsApiResourceSwagger() {
    }

    @ApiModel(value = "GetProductsTypeProductIdResponse")
    public final static class GetProductsTypeProductIdResponse {
        private GetProductsTypeProductIdResponse() {
        }

        final class GetProductsCurrency {
            private GetProductsCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "100")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetProductsMarketPrice {
            private GetProductsMarketPrice() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "Feb 1, 2016")
            public String fromDate;
            @ApiModelProperty(example = "1")
            public Integer shareValue;
        }

        final class GetProductsCharges {
            private GetProductsCharges() {
            }

            final class GetChargesCurrency {
                private GetChargesCurrency() {
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

            final class GetChargeTimeType {
                private GetChargeTimeType() {
                }

                @ApiModelProperty(example = "13")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.activation")
                public String code;
                @ApiModelProperty(example = "Share Account Activate")
                public String value;
            }

            final class GetChargeAppliesTo {
                private GetChargeAppliesTo() {
                }

                @ApiModelProperty(example = "4")
                public Integer id;
                @ApiModelProperty(example = "chargeAppliesTo.shares")
                public String code;
                @ApiModelProperty(example = "Shares")
                public String value;
            }

            final class GetChargeCalculationType {
                private GetChargeCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            final class GetChargePaymentMode {
                private GetChargePaymentMode() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "chargepaymentmode.regular")
                public String code;
                @ApiModelProperty(example = "Regular")
                public String value;
            }

            @ApiModelProperty(example = "20")
            public Integer id;
            @ApiModelProperty(example = "Share Account Activation Flat")
            public String name;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean penalty;
            public GetChargesCurrency currency;
            @ApiModelProperty(example = "1")
            public Integer amount;
            public GetChargeTimeType chargeTimeType;
            public GetChargeAppliesTo chargeAppliesTo;
            public GetChargeCalculationType chargeCalculationType;
            public GetChargePaymentMode chargePaymentMode;
        }

        final class GetLockPeriodTypeEnum {
            private GetLockPeriodTypeEnum() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "Days")
            public String value;
        }

        final class GetProductsAccountingRule {
            private GetProductsAccountingRule() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountingRuleType.cash")
            public String code;
            @ApiModelProperty(example = "CASH BASED")
            public String value;
        }

        final class GetProductsAccountingMappings {
            private GetProductsAccountingMappings() {
            }

            final class GetShareReferenceId {
                private GetShareReferenceId() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @ApiModelProperty(example = "ASSET_ED1461237837829")
                public String glCode;
            }

            final class GetIncomeFromFeeAccountId {
                private GetIncomeFromFeeAccountId() {
                }

                @ApiModelProperty(example = "14")
                public Integer id;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @ApiModelProperty(example = "INCOME_OY1461237869836")
                public String glCode;
            }

            final class GetShareEquityId {
                private GetShareEquityId() {
                }

                @ApiModelProperty(example = "66")
                public Integer id;
                @ApiModelProperty(example = "Equity Account")
                public String name;
                @ApiModelProperty(example = "EQUITY1")
                public String glCode;
            }

            final class GetShareSuspenseId {
                private GetShareSuspenseId() {
                }

                @ApiModelProperty(example = "8")
                public Integer id;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @ApiModelProperty(example = "LIABILITY_MA1461237860198")
                public String glCode;
            }

            public GetShareReferenceId shareReferenceId;
            public GetIncomeFromFeeAccountId incomeFromFeeAccountId;
            public GetShareEquityId shareEquityId;
            public GetShareSuspenseId shareSuspenseId;
        }

        final class GetProductsMinimumActivePeriodFrequencyTypeOptions {
            private GetProductsMinimumActivePeriodFrequencyTypeOptions() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "savings.lockin.sharePeriodFrequencyType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
        }

        final class GetProductsAccountingMappingOptions {
            private GetProductsAccountingMappingOptions() {
            }

            final class GetProductsLiabilityAccountOptions {
                private GetProductsLiabilityAccountOptions() {
                }

                final class GetProductsLiabilityType {
                    private GetProductsLiabilityType() {
                    }

                    @ApiModelProperty(example = "2")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.liability")
                    public String code;
                    @ApiModelProperty(example = "LIABILITY")
                    public String value;
                }

                final class GetProductsLiabilityUsage {
                    private GetProductsLiabilityUsage() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "accountUsage.detail")
                    public String code;
                    @ApiModelProperty(example = "DETAIL")
                    public String value;
                }

                final class GetProductsTagId {
                    private GetProductsTagId() {
                    }

                    @ApiModelProperty(example = "0")
                    public Integer id;
                    @ApiModelProperty(example = "false")
                    public Boolean isActive;
                }

                @ApiModelProperty(example = "4")
                public Integer id;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @ApiModelProperty(example = "LIABILITY_2T1461237838897")
                public String glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetProductsLiabilityType type;
                public GetProductsLiabilityUsage usage;
                @ApiModelProperty(example = "DEFAULT_DESCRIPTION")
                public String description;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String nameDecorated;
                public GetProductsTagId tagId;
            }

            final class GetProductsAssetAccountOptions {
                private GetProductsAssetAccountOptions() {
                }

                final class GetAssetType {
                    private GetAssetType() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.asset")
                    public String code;
                    @ApiModelProperty(example = "ASSET")
                    public String value;
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @ApiModelProperty(example = "ASSET_ED1461237837829")
                public String glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetAssetType type;
                public GetProductsLiabilityAccountOptions.GetProductsLiabilityUsage usage;
                @ApiModelProperty(example = "DEFAULT_DESCRIPTION")
                public String description;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String nameDecorated;
                public GetProductsLiabilityAccountOptions.GetProductsTagId tagId;
            }

            final class GetProductsIncomeAccountOptions {
                private GetProductsIncomeAccountOptions() {
                }

                final class GetIncomeType {
                    private GetIncomeType() {
                    }

                    @ApiModelProperty(example = "1")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.income")
                    public String code;
                    @ApiModelProperty(example = "INCOME")
                    public String value;
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String name;
                @ApiModelProperty(example = "INCOME_9O1461237838422")
                public String glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetIncomeType type;
                public GetProductsLiabilityAccountOptions.GetProductsLiabilityUsage usage;
                @ApiModelProperty(example = "DEFAULT_DESCRIPTION")
                public String description;
                @ApiModelProperty(example = "ACCOUNT_NAME_1FJBQ")
                public String nameDecorated;
                public GetProductsLiabilityAccountOptions.GetProductsTagId tagId;
            }

            final class GetProductsEquityAccountOptions {
                private GetProductsEquityAccountOptions() {
                }

                final class GetEquityType {
                    private GetEquityType() {
                    }

                    @ApiModelProperty(example = "3")
                    public Integer id;
                    @ApiModelProperty(example = "accountType.equity")
                    public String code;
                    @ApiModelProperty(example = "EQUITY")
                    public String value;
                }

                @ApiModelProperty(example = "66")
                public Integer id;
                @ApiModelProperty(example = "Equity Account")
                public String name;
                @ApiModelProperty(example = "EQUITY1")
                public String glCode;
                @ApiModelProperty(example = "false")
                public Boolean disabled;
                @ApiModelProperty(example = "true")
                public Boolean manualEntriesAllowed;
                public GetEquityType type;
                public GetProductsLiabilityAccountOptions.GetProductsLiabilityUsage usage;
                @ApiModelProperty(example = "Equity Account")
                public String nameDecorated;
                public GetProductsLiabilityAccountOptions.GetProductsTagId tagId;
            }

            public Set<GetProductsLiabilityAccountOptions> liabilityAccountOptions;
            public Set<GetProductsAssetAccountOptions> assetAccountOptions;
            public Set<GetProductsIncomeAccountOptions> incomeAccountOptions;
            public Set<GetProductsEquityAccountOptions> equityAccountOptions;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "Share Product")
        public String name;
        @ApiModelProperty(example = "SP")
        public String shortName;
        @ApiModelProperty(example = "SP")
        public String description;
        public GetProductsCurrency currency;
        @ApiModelProperty(example = "100")
        public Integer totalShares;
        @ApiModelProperty(example = "50")
        public Integer totalSharesIssued;
        @ApiModelProperty(example = "1")
        public Integer unitPrice;
        @ApiModelProperty(example = "50")
        public Integer shareCapital;
        @ApiModelProperty(example = "1")
        public Integer minimumShares;
        @ApiModelProperty(example = "10")
        public Integer nominalShares;
        @ApiModelProperty(example = "50")
        public Integer maximumShares;
        public Set<GetProductsMarketPrice> marketPrice;
        public Set<GetProductsCharges> charges;
        @ApiModelProperty(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        @ApiModelProperty(example = "1")
        public Integer lockinPeriod;
        public GetLockPeriodTypeEnum lockPeriodTypeEnum;
        @ApiModelProperty(example = "1")
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

    @ApiModel(value = "GetProductsTypeResponse")
    public static final class GetProductsTypeResponse {
        private GetProductsTypeResponse() {
        }

        final class GetProductsPageItems {
            private GetProductsPageItems() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Share Product")
            public String name;
            @ApiModelProperty(example = "Share Product Description")
            public String shortName;
            @ApiModelProperty(example = "100")
            public Integer totalShares;
        }

        @ApiModelProperty(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetProductsPageItems> pageItems;
    }

    @ApiModel(value = "PostProductsTypeRequest")
    public final static class PostProductsTypeRequest {
        private PostProductsTypeRequest() {
        }

        final class PostProductsMarketPricePeriods {
            private PostProductsMarketPricePeriods() {
            }

            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd MMMM yyyy")
            public String dateFormat;
            @ApiModelProperty(example = "04 May 2016")
            public String fromDate;
            @ApiModelProperty(example = "2")
            public Integer shareValue;
        }

        final class PostProductsChargesSelected {
            private PostProductsChargesSelected() {
            }

            @ApiModelProperty(example = "20")
            public Integer id;
        }

        @ApiModelProperty(example = "Share Product")
        public String name;
        @ApiModelProperty(example = "SP")
        public String shortName;
        @ApiModelProperty(example = "Description")
        public String description;
        @ApiModelProperty(example = "USD")
        public String currencyCode;
        @ApiModelProperty(example = "2")
        public Integer digitsAfterDecimal;
        @ApiModelProperty(example = "1")
        public Integer inMultiplesOf;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "1000")
        public Integer totalShares;
        @ApiModelProperty(example = "1000")
        public Integer sharesIssued;
        @ApiModelProperty(example = "1")
        public Integer unitPrice;
        @ApiModelProperty(example = "10")
        public Integer minimumShares;
        @ApiModelProperty(example = "20")
        public Integer nominalShares;
        @ApiModelProperty(example = "30")
        public Integer maximumShares;
        @ApiModelProperty(example = "1")
        public Integer minimumActivePeriodForDividends;
        @ApiModelProperty(example = "0")
        public Integer minimumactiveperiodFrequencyType;
        @ApiModelProperty(example = "1")
        public Integer lockinPeriodFrequency;
        @ApiModelProperty(example = "1")
        public Integer lockinPeriodFrequencyType;
        @ApiModelProperty(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        public Set<PostProductsMarketPricePeriods> marketPricePeriods;
        public Set<PostProductsChargesSelected> chargesSelected;
        @ApiModelProperty(example = "1")
        public Integer accountingRule;
    }

    @ApiModel(value = "PostProductsTypeResponse")
    public final static class PostProductsTypeResponse {
        private PostProductsTypeResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutProductsTypeProductIdRequest")
    public final static class PutProductsTypeProductIdRequest {
        private PutProductsTypeProductIdRequest() {
        }

        @ApiModelProperty(example = "Share Product Description.")
        public String description;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "5.0")
        public Double unitPrice;
    }

    @ApiModel(value = "PutProductsTypeProductIdResponse")
    public final static class PutProductsTypeProductIdResponse {
        private PutProductsTypeProductIdResponse() {
        }

        final class PutProductsChanges {
            private PutProductsChanges() {
            }

            @ApiModelProperty(example = "Share Product Description.")
            public String description;
            @ApiModelProperty(example = "5.0")
            public Double unitPrice;
            @ApiModelProperty(example = "en")
            public String locale;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutProductsChanges changes;
    }
}
