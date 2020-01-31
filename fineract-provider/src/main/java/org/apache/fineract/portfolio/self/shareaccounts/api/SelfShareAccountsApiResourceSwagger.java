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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;


/**
 * Created by Kang Breder on 05/08/19.
 */

final class SelfShareAccountsApiResourceSwagger {
    private SelfShareAccountsApiResourceSwagger(){

    }

    @ApiModel(value = "GetShareAccountsClientIdResponse")
    public final static class GetShareAccountsClientIdResponse {
        private GetShareAccountsClientIdResponse() {
        }

        final class GetShareAccountsProductOptions {
            private GetShareAccountsProductOptions() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "SP")
            public String name;
            @ApiModelProperty(example = "SP")
            public String shortName;
            @ApiModelProperty(example = "1000")
            public Integer totalShares;
        }

        final class GetShareAccountsChargeOptions {
            private GetShareAccountsChargeOptions() {
            }

            final class GetShareAccountsCurrency {
                private GetShareAccountsCurrency() {
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

            final class GetShareAccountsChargeTimeType {
                private GetShareAccountsChargeTimeType() {
                }

                @ApiModelProperty(example = "14")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.sharespurchase")
                public String code;
                @ApiModelProperty(example = "Share purchase")
                public String value;
            }

            final class GetShareAccountsChargeAppliesTo {
                private GetShareAccountsChargeAppliesTo() {
                }

                @ApiModelProperty(example = "4")
                public Integer id;
                @ApiModelProperty(example = "chargeAppliesTo.shares")
                public String code;
                @ApiModelProperty(example = "Shares")
                public String value;
            }

            final class GetShareAccountsChargeCalculationType {
                private GetShareAccountsChargeCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            final class GetShareAccountsChargePaymentMode {
                private GetShareAccountsChargePaymentMode() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "chargepaymentmode.regular")
                public String code;
                @ApiModelProperty(example = "Regular")
                public String value;
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "Activation fee")
            public String name;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean penalty;
            public GetShareAccountsCurrency currency;
            @ApiModelProperty(example = "2")
            public Integer amount;
            public GetShareAccountsChargeTimeType chargeTimeType;
            public GetShareAccountsChargeAppliesTo chargeAppliesTo;
            public GetShareAccountsChargeCalculationType calculationType;
            public GetShareAccountsChargePaymentMode paymentMode;

        }

        @ApiModelProperty(example = "14")
        public Integer clientId;
        public Set<GetShareAccountsProductOptions> productOptions;
        public Set<GetShareAccountsChargeOptions> chargeOptions;
    }

    @ApiModel(value = "GetShareAccountsClientIdProductIdResponse")
    public final static class GetShareAccountsClientIdProductIdResponse {
        private GetShareAccountsClientIdProductIdResponse() {
        }

        final class GetClientIdProductIdProductOptions {
            private GetClientIdProductIdProductOptions() {
            }

            final class GetShareAccountsClientIdProductIdLockPeriodTypeEnum {
                private GetShareAccountsClientIdProductIdLockPeriodTypeEnum() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "shares.lockin.sharePeriodFrequencyType.days")
                public String code;
                @ApiModelProperty(example = "days")
                public String value;

            }

            final class GetShareAccountsClientIdProductIdMinimumActivePeriodForDividendsTypeEnum {
                private GetShareAccountsClientIdProductIdMinimumActivePeriodForDividendsTypeEnum() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "shares.minimumactive.sharePeriodFrequencyType.days")
                public String code;
                @ApiModelProperty(example = "days")
                public String value;

            }

            final class GetShareAccountsClientIdProductIdAccountingRule {
                private GetShareAccountsClientIdProductIdAccountingRule() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "accountingRuleType.cash")
                public String code;
                @ApiModelProperty(example = "CASH BASED")
                public String value;

            }

            final class GetClientIdProductIdAccountingMappings {
                private GetClientIdProductIdAccountingMappings() {
                }

                final class GetShareAccountsShareReferenceId {
                    private GetShareAccountsShareReferenceId() {
                    }

                    @ApiModelProperty(example = "32")
                    public Integer id;
                    @ApiModelProperty(example = "Cash in Hand")
                    public String name;
                    @ApiModelProperty(example = "20301")
                    public Integer glCode;

                }

                final class GetShareAccountsIncomeFromFeeAccountId {
                    private GetShareAccountsIncomeFromFeeAccountId() {
                    }

                    @ApiModelProperty(example = "40")
                    public Integer id;
                    @ApiModelProperty(example = "Other Operating Income")
                    public String name;
                    @ApiModelProperty(example = "30105")
                    public Integer glCode;

                }

                final class GetShareAccountsShareEquityId {
                    private GetShareAccountsShareEquityId() {
                    }

                    @ApiModelProperty(example = "56")
                    public Integer id;
                    @ApiModelProperty(example = "Share Equity")
                    public String name;
                    @ApiModelProperty(example = "00098")
                    public Integer glCode;

                }

                final class GetShareAccountsShareSuspenseId {
                    private GetShareAccountsShareSuspenseId() {
                    }

                    @ApiModelProperty(example = "2")
                    public Integer id;
                    @ApiModelProperty(example = "Overpayment Liability")
                    public String name;
                    @ApiModelProperty(example = "10200")
                    public Integer glCode;

                }

                public GetShareAccountsShareReferenceId shareReferenceId;
                public GetShareAccountsIncomeFromFeeAccountId incomeFromFeeAccountId;
                public GetShareAccountsShareEquityId ShareEquityId;
                public GetShareAccountsShareSuspenseId shareSuspenseId;
            }

            final class GetClientIdProductIdMinimumActivePeriodFrequencyTypeOptions {
                private GetClientIdProductIdMinimumActivePeriodFrequencyTypeOptions() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "shares.lockin.sharePeriodFrequencyType.days")
                public String code;
                @ApiModelProperty(example = "Days")
                public Integer value;

            }

            final class GetClientIdProductIdLockinPeriodFrequencyTypeOptions {
                private GetClientIdProductIdLockinPeriodFrequencyTypeOptions() {
                }

                @ApiModelProperty(example = "0")
                public Integer id;
                @ApiModelProperty(example = "shares.lockin.sharePeriodFrequencyType.days")
                public String code;
                @ApiModelProperty(example = "Days")
                public Integer value;

            }

            final class GetAccountingMappingOptions {
                private GetAccountingMappingOptions() {
                }

                final class GetAccountingMappingLiabilityAccountOptions {
                    private GetAccountingMappingLiabilityAccountOptions() {
                    }

                    final class GetLiabilityAccountType {
                        private GetLiabilityAccountType() {
                        }

                        @ApiModelProperty(example = "2")
                        public Integer id;
                        @ApiModelProperty(example = "accountType.liability")
                        public String code;
                        @ApiModelProperty(example = "LIABILITY")
                        public Integer value;

                    }

                    final class GetLiabilityAccountUsage {
                        private GetLiabilityAccountUsage() {
                        }

                        @ApiModelProperty(example = "1")
                        public Integer id;
                        @ApiModelProperty(example = "accountUsage.detail")
                        public String code;
                        @ApiModelProperty(example = "DETAIL")
                        public Integer value;

                    }

                    final class GetLiabilityAccountTagId {
                        private GetLiabilityAccountTagId() {
                        }

                        @ApiModelProperty(example = "0")
                        public Integer id;
                        @ApiModelProperty(example = "false")
                        public Boolean active;
                        @ApiModelProperty(example = "false")
                        public Boolean mandatory;

                    }

                    @ApiModelProperty(example = "30")
                    public Integer id;
                    @ApiModelProperty(example = "Recurring Deposits")
                    public String name;
                    @ApiModelProperty(example = "1")
                    public Integer parentId;
                    @ApiModelProperty(example = "10104")
                    public Integer glCode;
                    @ApiModelProperty(example = "false")
                    public Boolean disabled;
                    @ApiModelProperty(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetLiabilityAccountType type;
                    public GetLiabilityAccountUsage usage;
                    @ApiModelProperty(example = " ....Recurring Deposits")
                    public String nameDecorated;
                    public GetLiabilityAccountTagId tagId;

                }

                final class GetAccountingMappingAssetAccountOptions {
                    private GetAccountingMappingAssetAccountOptions() {
                    }

                    final class GetAssetAccountType {
                        private GetAssetAccountType() {
                        }

                        @ApiModelProperty(example = "1")
                        public Integer id;
                        @ApiModelProperty(example = "accountType.asset")
                        public String code;
                        @ApiModelProperty(example = "ASSET")
                        public Integer value;

                    }

                    final class GetAssetAccountUsage {
                        private GetAssetAccountUsage() {
                        }

                        @ApiModelProperty(example = "1")
                        public Integer id;
                        @ApiModelProperty(example = "accountUsage.detail")
                        public String code;
                        @ApiModelProperty(example = "DETAIL")
                        public Integer value;

                    }

                    final class GetAssetAccountTagId {
                        private GetAssetAccountTagId() {
                        }

                        @ApiModelProperty(example = "0")
                        public Integer id;
                        @ApiModelProperty(example = "false")
                        public Boolean active;
                        @ApiModelProperty(example = "false")
                        public Boolean mandatory;

                    }

                    @ApiModelProperty(example = "31")
                    public Integer id;
                    @ApiModelProperty(example = "Furniture and Fixtures")
                    public String name;
                    @ApiModelProperty(example = "8")
                    public Integer parentId;
                    @ApiModelProperty(example = "20101")
                    public Integer glCode;
                    @ApiModelProperty(example = "false")
                    public Boolean disabled;
                    @ApiModelProperty(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetAssetAccountType type;
                    public GetAssetAccountUsage usage;
                    @ApiModelProperty(example = " ....Furniture and Fixtures")
                    public String nameDecorated;
                    public GetAssetAccountTagId tagId;

                }

                final class GetAccountingMappingIncomeAccountOptions {
                    private GetAccountingMappingIncomeAccountOptions() {
                    }

                    final class GetIncomeAccountType {
                        private GetIncomeAccountType() {
                        }

                        @ApiModelProperty(example = "4")
                        public Integer id;
                        @ApiModelProperty(example = "accountType.income")
                        public String code;
                        @ApiModelProperty(example = "INCOME")
                        public Integer value;

                    }

                    final class GetIncomeAccountUsage {
                        private GetIncomeAccountUsage() {
                        }

                        @ApiModelProperty(example = "1")
                        public Integer id;
                        @ApiModelProperty(example = "accountUsage.detail")
                        public String code;
                        @ApiModelProperty(example = "DETAIL")
                        public Integer value;

                    }

                    final class GetIncomeAccountTagId {
                        private GetIncomeAccountTagId() {
                        }

                        @ApiModelProperty(example = "0")
                        public Integer id;
                        @ApiModelProperty(example = "false")
                        public Boolean active;
                        @ApiModelProperty(example = "false")
                        public Boolean mandatory;

                    }

                    @ApiModelProperty(example = "54")
                    public Integer id;
                    @ApiModelProperty(example = "Loan Recovery (Temp)")
                    public String name;
                    @ApiModelProperty(example = "220002-Temp")
                    public Integer glCode;
                    @ApiModelProperty(example = "false")
                    public Boolean disabled;
                    @ApiModelProperty(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetIncomeAccountType type;
                    public GetIncomeAccountUsage usage;
                    @ApiModelProperty(example = "Temporary account to track income from Loan recovery")
                    public String Description;
                    @ApiModelProperty(example = "Loan Recovery (Temp)")
                    public String nameDecorated;
                    public GetIncomeAccountTagId tagId;
                }

                final class GetShareAccountAccountingMappingEquityAccountOptions {
                    private GetShareAccountAccountingMappingEquityAccountOptions() {
                    }

                    final class GetShareAccountsEquityAccountType {
                        private GetShareAccountsEquityAccountType() {
                        }

                        @ApiModelProperty(example = "3")
                        public Integer id;
                        @ApiModelProperty(example = "accountType.equity")
                        public String code;
                        @ApiModelProperty(example = "EQUITY")
                        public Integer value;

                    }

                    final class GetShareAccountsEquityAccountUsage {
                        private GetShareAccountsEquityAccountUsage() {
                        }

                        @ApiModelProperty(example = "1")
                        public Integer id;
                        @ApiModelProperty(example = "accountUsage.detail")
                        public String code;
                        @ApiModelProperty(example = "DETAIL")
                        public Integer value;

                    }

                    final class GetShareAccountsEquityAccountTagId {
                        private GetShareAccountsEquityAccountTagId() {
                        }

                        @ApiModelProperty(example = "0")
                        public Integer id;
                        @ApiModelProperty(example = "false")
                        public Boolean active;
                        @ApiModelProperty(example = "false")
                        public Boolean mandatory;

                    }

                    @ApiModelProperty(example = "56")
                    public Integer id;
                    @ApiModelProperty(example = "Share Equity")
                    public String name;
                    @ApiModelProperty(example = "25")
                    public Integer parentId;
                    @ApiModelProperty(example = "00098")
                    public Integer glCode;
                    @ApiModelProperty(example = "false")
                    public Boolean disabled;
                    @ApiModelProperty(example = "true")
                    public Boolean manualEntriesAllowed;
                    public GetShareAccountsEquityAccountType type;
                    public GetShareAccountsEquityAccountUsage usage;
                    @ApiModelProperty(example = "....Share Equity")
                    public String nameDecorated;
                    public GetShareAccountsEquityAccountTagId tagId;

                }

            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "SP")
            public String name;
            @ApiModelProperty(example = "SP")
            public String shortName;
            @ApiModelProperty(example = "SP1")
            public String description;
            public GetShareAccountsClientIdResponse.GetShareAccountsChargeOptions.GetShareAccountsCurrency currency;
            @ApiModelProperty(example = "1000")
            public Integer totalShares;
            @ApiModelProperty(example = "900")
            public Integer totalSharesIssued;
            @ApiModelProperty(example = "1")
            public Integer unitPrice;
            @ApiModelProperty(example = "900")
            public Integer shareCapital;
            @ApiModelProperty(example = "8")
            public Integer minimumShares;
            @ApiModelProperty(example = "500")
            public Integer norminalShares;
            @ApiModelProperty(example = "500")
            public Integer maximumShares;
            @ApiModelProperty(example = "[]")
            public String marketPrice;
            @ApiModelProperty(example = "[]")
            public String charges;
            @ApiModelProperty(example = "False")
            public Boolean allowDividendCalculationForInactiveClients;
            @ApiModelProperty(example = "50")
            public Integer lockinPeriod;
            public GetShareAccountsClientIdProductIdLockPeriodTypeEnum lockinPeriodEnum;
            @ApiModelProperty(example = "10")
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

        final class GetClientIdProductIdChargeOptions {
            private GetClientIdProductIdChargeOptions() {
            }

            public GetShareAccountsClientIdResponse.GetShareAccountsChargeOptions chargeOptions;
        }

        public Set<GetClientIdProductIdProductOptions> productOptions;
        public Set<GetClientIdProductIdChargeOptions> chargeOptions;
    }

    @ApiModel(value = "PostNewShareApplicationRequest")
    public final static class PostNewShareApplicationRequest {
        private PostNewShareApplicationRequest() {
        }

        final class GetShareAccountsCharges {
            private GetShareAccountsCharges() {
            }

            @ApiModelProperty(example = "2")
            public Integer chargeId;
            @ApiModelProperty(example = "2")
            public Integer amount;
        }

        @ApiModelProperty(example = "3")
        public Integer productId;
        @ApiModelProperty(example = "1")
        public Integer unitPrice;
        @ApiModelProperty(example = "500")
        public Integer requestedShares;
        @ApiModelProperty(example = "31 July 2018")
        public String submittedDate;
        @ApiModelProperty(example = "2")
        public Integer savingsAccountId;
        @ApiModelProperty(example = "31 July 2018")
        public String applicationDate;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM YYYY")
        public String dateFormat;
        public GetShareAccountsCharges charges;
        @ApiModelProperty(example = "14")
        public Integer clientId;
    }

    @ApiModel(value = "PostNewShareApplicationResponse")
    public final static class PostNewShareApplicationResponse {
        private PostNewShareApplicationResponse() {
        }

        @ApiModelProperty(example = "12")
        public Integer resourceId;
    }
}
