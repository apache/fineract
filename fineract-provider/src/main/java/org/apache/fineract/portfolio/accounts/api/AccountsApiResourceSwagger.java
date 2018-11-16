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
package org.apache.fineract.portfolio.accounts.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class AccountsApiResourceSwagger {
    private AccountsApiResourceSwagger() {
    }

    @ApiModel(value = "GetAccountsTypeTemplateResponse")
    public final static class GetAccountsTypeTemplateResponse {
        private GetAccountsTypeTemplateResponse() {
        }

        final class GetAccountsTypeProductOptions {
            private GetAccountsTypeProductOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Share Product")
            public String name;
            @ApiModelProperty(example = "SP")
            public String shortName;
            @ApiModelProperty(example = "100")
            public Long totalShares;
        }

        @ApiModelProperty(example = "7")
        public Integer clientId;
        @ApiModelProperty(example = "Client Name")
        public String clientName;
        public Set<GetAccountsTypeProductOptions> productOptions;
    }

    @ApiModel(value = "GetAccountsTypeAccountIdResponse")
    public final static class GetAccountsTypeAccountIdResponse {
        private GetAccountsTypeAccountIdResponse() {
        }

        final class GetAccountsStatus {
            private GetAccountsStatus() {
            }

            @ApiModelProperty(example = "300")
            public Integer id;
            @ApiModelProperty(example = "shareAccountStatusType.active")
            public String code;
            @ApiModelProperty(example = "Active")
            public String value;
            @ApiModelProperty(example = "false")
            public Boolean submittedAndPendingApproval;
            @ApiModelProperty(example = "false")
            public Boolean approved;
            @ApiModelProperty(example = "false")
            public Boolean rejected;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "false")
            public Boolean closed;
        }

        final class GetAccountsTimeline {
            private GetAccountsTimeline() {
            }

            @ApiModelProperty(example = "[2016, 4, 1]")
            public LocalDate submittedOnDate;
            @ApiModelProperty(example = "mifos")
            public String submittedByUsername;
            @ApiModelProperty(example = "App")
            public String submittedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String submittedByLastname;
            @ApiModelProperty(example = "[2016, 4, 1]")
            public LocalDate approvedDate;
            @ApiModelProperty(example = "mifos")
            public String approvedByUsername;
            @ApiModelProperty(example = "App")
            public String approvedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String approvedByLastname;
            @ApiModelProperty(example = "[2016, 4, 1]")
            public LocalDate activatedDate;
        }

        final class GetAccountsCurrency {
            private GetAccountsCurrency() {
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

        final class GetAccountsSummary {
            private GetAccountsSummary() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "000000002")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer totalApprovedShares;
            @ApiModelProperty(example = "0")
            public Integer totalPendingForApprovalShares;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "Conflux Share Product")
            public String productName;
            public GetAccountsStatus status;
            public GetAccountsTimeline timeline;
            public GetAccountsCurrency currency;
        }

        final class GetAccountsPurchasedShares {
            private GetAccountsPurchasedShares() {
            }

            final class GetAccountsPurchasedSharesStatus {
                private GetAccountsPurchasedSharesStatus() {
                }

                @ApiModelProperty(example = "300")
                public Integer id;
                @ApiModelProperty(example = "purchasedSharesStatusType.approved")
                public String code;
                @ApiModelProperty(example = "Approved")
                public String value;
            }

            final class GetAccountsPurchasedSharesType {
                private GetAccountsPurchasedSharesType() {
                }

                @ApiModelProperty(example = "500")
                public Integer id;
                @ApiModelProperty(example = "purchasedSharesType.purchased")
                public String code;
                @ApiModelProperty(example = "Purchase")
                public String value;
            }

            @ApiModelProperty(example = "6")
            public Integer id;
            @ApiModelProperty(example = "2")
            public Integer accountId;
            @ApiModelProperty(example = "[2016, 4, 1]")
            public LocalDate purchasedDate;
            @ApiModelProperty(example = "10")
            public Integer numberOfShares;
            @ApiModelProperty(example = "0.5")
            public Double purchasedPrice;
            public GetAccountsPurchasedSharesStatus status;
            public GetAccountsPurchasedSharesType type;
            @ApiModelProperty(example = "5.05")
            public Double amount;
            @ApiModelProperty(example = "0.05")
            public Double chargeAmount;
            @ApiModelProperty(example = "5.05")
            public Double amountPaid;
        }

        final class GetAccountsLockPeriodTypeEnum {
            private GetAccountsLockPeriodTypeEnum() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "savings.lockin.sharePeriodFrequencyType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
        }

        final class GetAccountsCharges {
            private GetAccountsCharges() {
            }

            final class GetAccountsChargeTimeType {
                private GetAccountsChargeTimeType() {
                }

                @ApiModelProperty(example = "13")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.activation")
                public String code;
                @ApiModelProperty(example = "Share Account Activate")
                public String value;
            }

            final class GetAccountsChargeCalculationType {
                private GetAccountsChargeCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            final class GetAccountsChargesCurrency {
                private GetAccountsChargesCurrency() {
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

            @ApiModelProperty(example = "9")
            public Integer id;
            @ApiModelProperty(example = "20")
            public Integer chargeId;
            @ApiModelProperty(example = "2")
            public Integer accountId;
            @ApiModelProperty(example = "Share Account Activation Flat")
            public String name;
            public GetAccountsChargeTimeType chargeTimeType;
            public GetAccountsChargeCalculationType chargeCalculationType;
            @ApiModelProperty(example = "0")
            public Double percentage;
            @ApiModelProperty(example = "0")
            public Double amountPercentageAppliedTo;
            public GetAccountsChargesCurrency currency;
            @ApiModelProperty(example = "1")
            public Float amount;
            @ApiModelProperty(example = "1")
            public Float amountPaid;
            @ApiModelProperty(example = "0")
            public Float amountWaived;
            @ApiModelProperty(example = "0")
            public Float amountWrittenOff;
            @ApiModelProperty(example = "0")
            public Float amountOutstanding;
            @ApiModelProperty(example = "1")
            public Float amountOrPercentage;
            @ApiModelProperty(example = "true")
            public Boolean isActive;
        }


        @ApiModelProperty(example = "2")
        public Integer id;
        @ApiModelProperty(example = "000000002")
        public Long accountNo;
        @ApiModelProperty(example = "000000013")
        public Long savingsAccountNumber;
        @ApiModelProperty(example = "7")
        public Integer clientId;
        @ApiModelProperty(example = "Client_FirstName_2KX8C Client_LastName_NWNG")
        public String clientName;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "Share Product")
        public String productName;
        public GetAccountsStatus status;
        public GetAccountsTimeline timeline;
        public GetAccountsCurrency currency;
        public GetAccountsSummary summary;
        public Set<GetAccountsPurchasedShares> purchasedShares;
        @ApiModelProperty(example = "13")
        public Integer savingsAccountId;
        @ApiModelProperty(example = "5")
        public Integer currentMarketPrice;
        @ApiModelProperty(example = "1")
        public Integer lockinPeriod;
        public GetAccountsLockPeriodTypeEnum lockPeriodTypeEnum;
        @ApiModelProperty(example = "1")
        public Integer minimumActivePeriod;
        public GetAccountsLockPeriodTypeEnum minimumActivePeriodTypeEnum;
        @ApiModelProperty(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        public Set<GetAccountsCharges> charges;
        @ApiModelProperty(example = "")
        public List<String> dividends;
    }

    @ApiModel(value = "GetAccountsTypeResponse")
    public final static class GetAccountsTypeResponse {
        private GetAccountsTypeResponse() {
        }

        final class GetAccountsPageItems {
            private GetAccountsPageItems() {
            }

            final class GetAccountsTypeStatus {
                private GetAccountsTypeStatus() {
                }

                @ApiModelProperty(example = "100")
                public Integer id;
                @ApiModelProperty(example = "shareAccountStatusType.submitted.and.pending.approval")
                public String code;
                @ApiModelProperty(example = "Submitted and pending approval")
                public String value;
                @ApiModelProperty(example = "true")
                public Boolean submittedAndPendingApproval;
                @ApiModelProperty(example = "false")
                public Boolean approved;
                @ApiModelProperty(example = "false")
                public Boolean rejected;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean closed;
            }

            final class GetAccountsTypeTimeline {
                private GetAccountsTypeTimeline() {
                }

                @ApiModelProperty(example = "[2013, 3, 1]")
                public LocalDate submittedOnDate;
            }

            final class GetAccountsTypePurchasedShares {
                private GetAccountsTypePurchasedShares() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "01 May 2013")
                public String purchasedDate;
                @ApiModelProperty(example = "10")
                public Integer numberOfShares;
                @ApiModelProperty(example = "5")
                public Integer purchasedPrice;
            }

            final class GetAccountsTypeSummary {
                private GetAccountsTypeSummary() {
                }

                public GetAccountsTypeAccountIdResponse.GetAccountsCharges.GetAccountsChargesCurrency currency;
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "000000001")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "Client Name")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "Share Product Name")
            public String productName;
            public GetAccountsTypeStatus status;
            public GetAccountsTypeTimeline timeline;
            public GetAccountsTypeAccountIdResponse.GetAccountsCharges.GetAccountsChargesCurrency currency;
            public Set<GetAccountsTypePurchasedShares> purchasedShares;
            public GetAccountsTypeSummary summary;
        }

        @ApiModelProperty(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetAccountsPageItems> pageItems;
    }

    @ApiModel(value = "PostAccountsTypeRequest")
    public final static class PostAccountsTypeRequest {
        private PostAccountsTypeRequest() {
        }

        final class PostAccountsCharges {
            private PostAccountsCharges() {
            }

            @ApiModelProperty(example = "20")
            public Integer chargeId;
            @ApiModelProperty(example = "1")
            public Integer amount;
        }

        @ApiModelProperty(example = "7")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "100")
        public Integer requestedShares;
        @ApiModelProperty(example = "1")
        public Integer externalId;
        @ApiModelProperty(example = "01 May 2016")
        public String submittedDate;
        @ApiModelProperty(example = "1")
        public Integer minimumActivePeriod;
        @ApiModelProperty(example = "0")
        public Integer minimumActivePeriodFrequencyType;
        @ApiModelProperty(example = "1")
        public Integer lockinPeriodFrequency;
        @ApiModelProperty(example = "0")
        public Integer lockinPeriodFrequencyType;
        @ApiModelProperty(example = "01 May 2016")
        public String applicationDate;
        @ApiModelProperty(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        public Set<PostAccountsCharges> charges;
        @ApiModelProperty(example = "13")
        public Integer savingsAccountId;
    }

    @ApiModel(value = "PostAccountsTypeResponse")
    public final static class PostAccountsTypeResponse {
        private PostAccountsTypeResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PostAccountsTypeAccountIdRequest")
    public final static class PostAccountsTypeAccountIdRequest {
        private PostAccountsTypeAccountIdRequest() {
        }

        final class PostAccountsRequestedShares {
            private PostAccountsRequestedShares() {
            }

            @ApiModelProperty(example = "35")
            public Integer id;
        }

        public Set<PostAccountsRequestedShares> requestedShares;
    }

    @ApiModel(value = "PostAccountsTypeAccountIdResponse")
    public final static class PostAccountsTypeAccountIdResponse {
        private PostAccountsTypeAccountIdResponse() {
        }

        @ApiModelProperty(example = "5")
        public Integer resourceId;
    }

    @ApiModel(value = "PutAccountsTypeAccountIdRequest")
    public final static class PutAccountsTypeAccountIdRequest {
        private PutAccountsTypeAccountIdRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 April 2016")
        public String applicationDate;
        @ApiModelProperty(example = "20")
        public Integer requestedShares;
    }

    @ApiModel(value = "PutAccountsTypeAccountIdResponse")
    public final static class PutAccountsTypeAccountIdResponse {
        private PutAccountsTypeAccountIdResponse() {
        }

        final class PutAccountsChanges {
            private PutAccountsChanges() {
            }

            @ApiModelProperty(example = "dd MMMM yyyy")
            public String dateFormat;
            @ApiModelProperty(example = "01 April 2016")
            public String applicationDate;
            @ApiModelProperty(example = "20")
            public Integer requestedShares;
            @ApiModelProperty(example = "en")
            public String locale;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutAccountsChanges changes;
    }
}
