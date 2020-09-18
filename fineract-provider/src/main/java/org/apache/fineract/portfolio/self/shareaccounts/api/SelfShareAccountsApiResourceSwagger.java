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

package org.apache.fineract.portfolio.self.shareaccounts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Kang Breder on 05/08/19.
 */
@SuppressWarnings({ "MemberName" })
final class SelfShareAccountsApiResourceSwagger {

    private SelfShareAccountsApiResourceSwagger() {

    }

    @Schema(description = "GetShareAccountsClientIdResponse")
    public static final class GetShareAccountsClientIdResponse {

        private GetShareAccountsClientIdResponse() {}

        static final class GetShareAccountsProductOptions {

            private GetShareAccountsProductOptions() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "SP")
            public String name;
            @Schema(example = "SP")
            public String shortName;
            @Schema(example = "1000")
            public Integer totalShares;
        }

        static final class GetShareAccountsChargeOptions {

            private GetShareAccountsChargeOptions() {}

            static final class GetShareAccountsCurrency {

                private GetShareAccountsCurrency() {}

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

            static final class GetShareAccountsChargeTimeType {

                private GetShareAccountsChargeTimeType() {}

                @Schema(example = "14")
                public Integer id;
                @Schema(example = "chargeTimeType.sharespurchase")
                public String code;
                @Schema(example = "Share purchase")
                public String description;
            }

            static final class GetShareAccountsChargeAppliesTo {

                private GetShareAccountsChargeAppliesTo() {}

                @Schema(example = "4")
                public Integer id;
                @Schema(example = "chargeAppliesTo.shares")
                public String code;
                @Schema(example = "Shares")
                public String description;
            }

            static final class GetShareAccountsChargeCalculationType {

                private GetShareAccountsChargeCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetShareAccountsChargePaymentMode {

                private GetShareAccountsChargePaymentMode() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "chargepaymentmode.regular")
                public String code;
                @Schema(example = "Regular")
                public String description;
            }

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "Activation fee")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean penalty;
            public GetShareAccountsCurrency currency;
            @Schema(example = "2")
            public Integer amount;
            public GetShareAccountsChargeTimeType chargeTimeType;
            public GetShareAccountsChargeAppliesTo chargeAppliesTo;
            public GetShareAccountsChargeCalculationType calculationType;
            public GetShareAccountsChargePaymentMode paymentMode;

        }

        @Schema(example = "14")
        public Integer clientId;
        public Set<GetShareAccountsProductOptions> productOptions;
        public Set<GetShareAccountsChargeOptions> chargeOptions;
    }

    @Schema(description = "GetShareAccountsClientIdProductIdResponse")
    public static final class GetShareAccountsClientIdProductIdResponse {

        private GetShareAccountsClientIdProductIdResponse() {}

        static final class GetClientIdProductIdProductOptions {

            private GetClientIdProductIdProductOptions() {}

            static final class GetShareAccountsClientIdProductIdLockPeriodTypeEnum {

                private GetShareAccountsClientIdProductIdLockPeriodTypeEnum() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "shares.lockin.sharePeriodFrequencyType.days")
                public String code;
                @Schema(example = "days")
                public String description;

            }

            static final class GetShareAccountsClientIdProductIdMinimumActivePeriodForDividendsTypeEnum {

                private GetShareAccountsClientIdProductIdMinimumActivePeriodForDividendsTypeEnum() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "shares.minimumactive.sharePeriodFrequencyType.days")
                public String code;
                @Schema(example = "days")
                public String description;

            }

            static final class GetShareAccountsClientIdProductIdAccountingRule {

                private GetShareAccountsClientIdProductIdAccountingRule() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "accountingRuleType.cash")
                public String code;
                @Schema(example = "CASH BASED")
                public String description;

            }

            static final class GetClientIdProductIdAccountingMappings {

                private GetClientIdProductIdAccountingMappings() {}

                static final class GetShareAccountsShareReferenceId {

                    private GetShareAccountsShareReferenceId() {}

                    @Schema(example = "32")
                    public Integer id;
                    @Schema(example = "Cash in Hand")
                    public String name;
                    @Schema(example = "20301")
                    public Integer glCode;

                }

                static final class GetShareAccountsIncomeFromFeeAccountId {

                    private GetShareAccountsIncomeFromFeeAccountId() {}

                    @Schema(example = "40")
                    public Integer id;
                    @Schema(example = "Other Operating Income")
                    public String name;
                    @Schema(example = "30105")
                    public Integer glCode;

                }

                static final class GetShareAccountsShareEquityId {

                    private GetShareAccountsShareEquityId() {}

                    @Schema(example = "56")
                    public Integer id;
                    @Schema(example = "Share Equity")
                    public String name;
                    @Schema(example = "00098")
                    public Integer glCode;

                }

                static final class GetShareAccountsShareSuspenseId {

                    private GetShareAccountsShareSuspenseId() {}

                    @Schema(example = "2")
                    public Integer id;
                    @Schema(example = "Overpayment Liability")
                    public String name;
                    @Schema(example = "10200")
                    public Integer glCode;

                }

                public GetShareAccountsShareReferenceId shareReferenceId;
                public GetShareAccountsIncomeFromFeeAccountId incomeFromFeeAccountId;
                public GetShareAccountsShareEquityId ShareEquityId;
                public GetShareAccountsShareSuspenseId shareSuspenseId;
            }

            static final class GetClientIdProductIdMinimumActivePeriodFrequencyTypeOptions {

                private GetClientIdProductIdMinimumActivePeriodFrequencyTypeOptions() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "shares.lockin.sharePeriodFrequencyType.days")
                public String code;
                @Schema(example = "Days")
                public Integer description;

            }

            static final class GetClientIdProductIdLockinPeriodFrequencyTypeOptions {

                private GetClientIdProductIdLockinPeriodFrequencyTypeOptions() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "shares.lockin.sharePeriodFrequencyType.days")
                public String code;
                @Schema(example = "Days")
                public Integer description;

            }

            static final class GetAccountingMappingOptions {

                private GetAccountingMappingOptions() {}

                static final class GetAccountingMappingLiabilityAccountOptions {

                    private GetAccountingMappingLiabilityAccountOptions() {}

                    static final class GetLiabilityAccountType {

                        private GetLiabilityAccountType() {}

                        @Schema(example = "2")
                        public Integer id;
                        @Schema(example = "accountType.liability")
                        public String code;
                        @Schema(example = "LIABILITY")
                        public Integer description;

                    }

                    static final class GetLiabilityAccountUsage {

                        private GetLiabilityAccountUsage() {}

                        @Schema(example = "1")
                        public Integer id;
                        @Schema(example = "accountUsage.detail")
                        public String code;
                        @Schema(example = "DETAIL")
                        public Integer description;

                    }

                    static final class GetLiabilityAccountTagId {

                        private GetLiabilityAccountTagId() {}

                        @Schema(example = "0")
                        public Integer id;
                        @Schema(example = "false")
                        public Boolean active;
                        @Schema(example = "false")
                        public Boolean mandatory;

                    }

                    @Schema(example = "30")
                    public Integer id;
                    @Schema(example = "Recurring Deposits")
                    public String name;
                    @Schema(example = "1")
                    public Integer parentId;
                    @Schema(example = "10104")
                    public Integer glCode;
                    @Schema(example = "false")
                    public Boolean disabled;
                    @Schema(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetLiabilityAccountType type;
                    public GetLiabilityAccountUsage usage;
                    @Schema(example = " ....Recurring Deposits")
                    public String nameDecorated;
                    public GetLiabilityAccountTagId tagId;

                }

                static final class GetAccountingMappingAssetAccountOptions {

                    private GetAccountingMappingAssetAccountOptions() {}

                    static final class GetAssetAccountType {

                        private GetAssetAccountType() {}

                        @Schema(example = "1")
                        public Integer id;
                        @Schema(example = "accountType.asset")
                        public String code;
                        @Schema(example = "ASSET")
                        public Integer description;

                    }

                    static final class GetAssetAccountUsage {

                        private GetAssetAccountUsage() {}

                        @Schema(example = "1")
                        public Integer id;
                        @Schema(example = "accountUsage.detail")
                        public String code;
                        @Schema(example = "DETAIL")
                        public Integer description;

                    }

                    static final class GetAssetAccountTagId {

                        private GetAssetAccountTagId() {}

                        @Schema(example = "0")
                        public Integer id;
                        @Schema(example = "false")
                        public Boolean active;
                        @Schema(example = "false")
                        public Boolean mandatory;

                    }

                    @Schema(example = "31")
                    public Integer id;
                    @Schema(example = "Furniture and Fixtures")
                    public String name;
                    @Schema(example = "8")
                    public Integer parentId;
                    @Schema(example = "20101")
                    public Integer glCode;
                    @Schema(example = "false")
                    public Boolean disabled;
                    @Schema(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetAssetAccountType type;
                    public GetAssetAccountUsage usage;
                    @Schema(example = " ....Furniture and Fixtures")
                    public String nameDecorated;
                    public GetAssetAccountTagId tagId;

                }

                static final class GetAccountingMappingIncomeAccountOptions {

                    private GetAccountingMappingIncomeAccountOptions() {}

                    static final class GetIncomeAccountType {

                        private GetIncomeAccountType() {}

                        @Schema(example = "4")
                        public Integer id;
                        @Schema(example = "accountType.income")
                        public String code;
                        @Schema(example = "INCOME")
                        public Integer description;

                    }

                    static final class GetIncomeAccountUsage {

                        private GetIncomeAccountUsage() {}

                        @Schema(example = "1")
                        public Integer id;
                        @Schema(example = "accountUsage.detail")
                        public String code;
                        @Schema(example = "DETAIL")
                        public Integer description;

                    }

                    static final class GetIncomeAccountTagId {

                        private GetIncomeAccountTagId() {}

                        @Schema(example = "0")
                        public Integer id;
                        @Schema(example = "false")
                        public Boolean active;
                        @Schema(example = "false")
                        public Boolean mandatory;

                    }

                    @Schema(example = "54")
                    public Integer id;
                    @Schema(example = "Loan Recovery (Temp)")
                    public String name;
                    @Schema(example = "220002-Temp")
                    public Integer glCode;
                    @Schema(example = "false")
                    public Boolean disabled;
                    @Schema(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetIncomeAccountType type;
                    public GetIncomeAccountUsage usage;
                    @Schema(example = "Temporary account to track income from Loan recovery")
                    public String Description;
                    @Schema(example = "Loan Recovery (Temp)")
                    public String nameDecorated;
                    public GetIncomeAccountTagId tagId;
                }

                static final class GetShareAccountAccountingMappingEquityAccountOptions {

                    private GetShareAccountAccountingMappingEquityAccountOptions() {}

                    static final class GetShareAccountsEquityAccountType {

                        private GetShareAccountsEquityAccountType() {}

                        @Schema(example = "3")
                        public Integer id;
                        @Schema(example = "accountType.equity")
                        public String code;
                        @Schema(example = "EQUITY")
                        public Integer description;

                    }

                    static final class GetShareAccountsEquityAccountUsage {

                        private GetShareAccountsEquityAccountUsage() {}

                        @Schema(example = "1")
                        public Integer id;
                        @Schema(example = "accountUsage.detail")
                        public String code;
                        @Schema(example = "DETAIL")
                        public Integer description;

                    }

                    static final class GetShareAccountsEquityAccountTagId {

                        private GetShareAccountsEquityAccountTagId() {}

                        @Schema(example = "0")
                        public Integer id;
                        @Schema(example = "false")
                        public Boolean active;
                        @Schema(example = "false")
                        public Boolean mandatory;

                    }

                    @Schema(example = "56")
                    public Integer id;
                    @Schema(example = "Share Equity")
                    public String name;
                    @Schema(example = "25")
                    public Integer parentId;
                    @Schema(example = "00098")
                    public Integer glCode;
                    @Schema(example = "false")
                    public Boolean disabled;
                    @Schema(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetShareAccountsEquityAccountType type;
                    public GetShareAccountsEquityAccountUsage usage;
                    @Schema(example = "....Share Equity")
                    public String nameDecorated;
                    public GetShareAccountsEquityAccountTagId tagId;

                }

            }

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "SP")
            public String name;
            @Schema(example = "SP")
            public String shortName;
            @Schema(example = "SP1")
            public String description;
            public GetShareAccountsClientIdResponse.GetShareAccountsChargeOptions.GetShareAccountsCurrency currency;
            @Schema(example = "1000")
            public Integer totalShares;
            @Schema(example = "900")
            public Integer totalSharesIssued;
            @Schema(example = "1")
            public Integer unitPrice;
            @Schema(example = "900")
            public Integer shareCapital;
            @Schema(example = "8")
            public Integer minimumShares;
            @Schema(example = "500")
            public Integer norminalShares;
            @Schema(example = "500")
            public Integer maximumShares;
            @Schema(example = "[]")
            public String marketPrice;
            @Schema(example = "[]")
            public String charges;
            @Schema(example = "False")
            public Boolean allowDividendCalculationForInactiveClients;
            @Schema(example = "50")
            public Integer lockinPeriod;
            public GetShareAccountsClientIdProductIdLockPeriodTypeEnum lockinPeriodEnum;
            @Schema(example = "10")
            public Integer minimumActivePeriod;
            public GetShareAccountsClientIdProductIdMinimumActivePeriodForDividendsTypeEnum minimumActivePeriodForDividendsTypeEnum;
            public GetShareAccountsClientIdProductIdAccountingRule accountingRule;
            public GetClientIdProductIdAccountingMappings accountingMappings;
            public GetShareAccountsClientIdResponse.GetShareAccountsChargeOptions.GetShareAccountsCurrency currencyOptions;
            public GetShareAccountsClientIdResponse.GetShareAccountsChargeOptions chargeOptions;
            public GetClientIdProductIdMinimumActivePeriodFrequencyTypeOptions minimumActivePeriodFrequencyTypeOptions;
            public GetClientIdProductIdLockinPeriodFrequencyTypeOptions lockinPeriodFrequencyTypeOptions;
            public GetAccountingMappingOptions accountingMappingOptions;
        }

        static final class GetClientIdProductIdChargeOptions {

            private GetClientIdProductIdChargeOptions() {}

            public GetShareAccountsClientIdResponse.GetShareAccountsChargeOptions chargeOptions;
        }

        public Set<GetClientIdProductIdProductOptions> productOptions;
        public Set<GetClientIdProductIdChargeOptions> chargeOptions;
    }

    @Schema(description = "PostNewShareApplicationRequest")
    public static final class PostNewShareApplicationRequest {

        private PostNewShareApplicationRequest() {}

        static final class GetShareAccountsCharges {

            private GetShareAccountsCharges() {}

            @Schema(example = "2")
            public Integer chargeId;
            @Schema(example = "2")
            public Integer amount;
        }

        @Schema(example = "3")
        public Integer productId;
        @Schema(example = "1")
        public Integer unitPrice;
        @Schema(example = "500")
        public Integer requestedShares;
        @Schema(example = "31 July 2018")
        public String submittedDate;
        @Schema(example = "2")
        public Integer savingsAccountId;
        @Schema(example = "31 July 2018")
        public String applicationDate;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM YYYY")
        public String dateFormat;
        public GetShareAccountsCharges charges;
        @Schema(example = "14")
        public Integer clientId;
    }

    @Schema(description = "PostNewShareApplicationResponse")
    public static final class PostNewShareApplicationResponse {

        private PostNewShareApplicationResponse() {}

        @Schema(example = "12")
        public Integer resourceId;
    }
}
