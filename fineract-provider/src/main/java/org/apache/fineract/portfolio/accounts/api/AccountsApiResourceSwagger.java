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
package org.apache.fineract.portfolio.accounts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class AccountsApiResourceSwagger {

    private AccountsApiResourceSwagger() {}

    @Schema(description = "GetAccountsTypeTemplateResponse")
    public static final class GetAccountsTypeTemplateResponse {

        private GetAccountsTypeTemplateResponse() {}

        static final class GetAccountsTypeProductOptions {

            private GetAccountsTypeProductOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Share Product")
            public String name;
            @Schema(example = "SP")
            public String shortName;
            @Schema(example = "100")
            public Long totalShares;
        }

        @Schema(example = "7")
        public Integer clientId;
        @Schema(example = "Client Name")
        public String clientName;
        public Set<GetAccountsTypeProductOptions> productOptions;
    }

    @Schema(description = "GetAccountsTypeAccountIdResponse")
    public static final class GetAccountsTypeAccountIdResponse {

        private GetAccountsTypeAccountIdResponse() {}

        static final class GetAccountsStatus {

            private GetAccountsStatus() {}

            @Schema(example = "300")
            public Integer id;
            @Schema(example = "shareAccountStatusType.active")
            public String code;
            @Schema(example = "Active")
            public String description;
            @Schema(example = "false")
            public Boolean submittedAndPendingApproval;
            @Schema(example = "false")
            public Boolean approved;
            @Schema(example = "false")
            public Boolean rejected;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean closed;
        }

        static final class GetAccountsTimeline {

            private GetAccountsTimeline() {}

            @Schema(example = "[2016, 4, 1]")
            public LocalDate submittedOnDate;
            @Schema(example = "mifos")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
            @Schema(example = "[2016, 4, 1]")
            public LocalDate approvedDate;
            @Schema(example = "mifos")
            public String approvedByUsername;
            @Schema(example = "App")
            public String approvedByFirstname;
            @Schema(example = "Administrator")
            public String approvedByLastname;
            @Schema(example = "[2016, 4, 1]")
            public LocalDate activatedDate;
        }

        static final class GetAccountsCurrency {

            private GetAccountsCurrency() {}

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

        static final class GetAccountsSummary {

            private GetAccountsSummary() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "000000002")
            public Long accountNo;
            @Schema(example = "1")
            public Integer totalApprovedShares;
            @Schema(example = "0")
            public Integer totalPendingForApprovalShares;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "Conflux Share Product")
            public String productName;
            public GetAccountsStatus status;
            public GetAccountsTimeline timeline;
            public GetAccountsCurrency currency;
        }

        static final class GetAccountsPurchasedShares {

            private GetAccountsPurchasedShares() {}

            static final class GetAccountsPurchasedSharesStatus {

                private GetAccountsPurchasedSharesStatus() {}

                @Schema(example = "300")
                public Integer id;
                @Schema(example = "purchasedSharesStatusType.approved")
                public String code;
                @Schema(example = "Approved")
                public String description;
            }

            static final class GetAccountsPurchasedSharesType {

                private GetAccountsPurchasedSharesType() {}

                @Schema(example = "500")
                public Integer id;
                @Schema(example = "purchasedSharesType.purchased")
                public String code;
                @Schema(example = "Purchase")
                public String description;
            }

            @Schema(example = "6")
            public Integer id;
            @Schema(example = "2")
            public Integer accountId;
            @Schema(example = "[2016, 4, 1]")
            public LocalDate purchasedDate;
            @Schema(example = "10")
            public Integer numberOfShares;
            @Schema(example = "0.5")
            public Double purchasedPrice;
            public GetAccountsPurchasedSharesStatus status;
            public GetAccountsPurchasedSharesType type;
            @Schema(example = "5.05")
            public Double amount;
            @Schema(example = "0.05")
            public Double chargeAmount;
            @Schema(example = "5.05")
            public Double amountPaid;
        }

        static final class GetAccountsLockPeriodTypeEnum {

            private GetAccountsLockPeriodTypeEnum() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "savings.lockin.sharePeriodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        static final class GetAccountsCharges {

            private GetAccountsCharges() {}

            static final class GetAccountsChargeTimeType {

                private GetAccountsChargeTimeType() {}

                @Schema(example = "13")
                public Integer id;
                @Schema(example = "chargeTimeType.activation")
                public String code;
                @Schema(example = "Share Account Activate")
                public String description;
            }

            static final class GetAccountsChargeCalculationType {

                private GetAccountsChargeCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetAccountsChargesCurrency {

                private GetAccountsChargesCurrency() {}

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

            @Schema(example = "9")
            public Integer id;
            @Schema(example = "20")
            public Integer chargeId;
            @Schema(example = "2")
            public Integer accountId;
            @Schema(example = "Share Account Activation Flat")
            public String name;
            public GetAccountsChargeTimeType chargeTimeType;
            public GetAccountsChargeCalculationType chargeCalculationType;
            @Schema(example = "0")
            public Double percentage;
            @Schema(example = "0")
            public Double amountPercentageAppliedTo;
            public GetAccountsChargesCurrency currency;
            @Schema(example = "1")
            public Float amount;
            @Schema(example = "1")
            public Float amountPaid;
            @Schema(example = "0")
            public Float amountWaived;
            @Schema(example = "0")
            public Float amountWrittenOff;
            @Schema(example = "0")
            public Float amountOutstanding;
            @Schema(example = "1")
            public Float amountOrPercentage;
            @Schema(example = "true")
            public Boolean isActive;
        }

        @Schema(example = "2")
        public Integer id;
        @Schema(example = "000000002")
        public Long accountNo;
        @Schema(example = "000000013")
        public Long savingsAccountNumber;
        @Schema(example = "7")
        public Integer clientId;
        @Schema(example = "Client_FirstName_2KX8C Client_LastName_NWNG")
        public String clientName;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "Share Product")
        public String productName;
        public GetAccountsStatus status;
        public GetAccountsTimeline timeline;
        public GetAccountsCurrency currency;
        public GetAccountsSummary summary;
        public Set<GetAccountsPurchasedShares> purchasedShares;
        @Schema(example = "13")
        public Integer savingsAccountId;
        @Schema(example = "5")
        public Integer currentMarketPrice;
        @Schema(example = "1")
        public Integer lockinPeriod;
        public GetAccountsLockPeriodTypeEnum lockPeriodTypeEnum;
        @Schema(example = "1")
        public Integer minimumActivePeriod;
        public GetAccountsLockPeriodTypeEnum minimumActivePeriodTypeEnum;
        @Schema(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        public Set<GetAccountsCharges> charges;
        @Schema(example = "")
        public List<String> dividends;
    }

    @Schema(description = "GetAccountsTypeResponse")
    public static final class GetAccountsTypeResponse {

        private GetAccountsTypeResponse() {}

        static final class GetAccountsPageItems {

            private GetAccountsPageItems() {}

            static final class GetAccountsTypeStatus {

                private GetAccountsTypeStatus() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "shareAccountStatusType.submitted.and.pending.approval")
                public String code;
                @Schema(example = "Submitted and pending approval")
                public String description;
                @Schema(example = "true")
                public Boolean submittedAndPendingApproval;
                @Schema(example = "false")
                public Boolean approved;
                @Schema(example = "false")
                public Boolean rejected;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean closed;
            }

            static final class GetAccountsTypeTimeline {

                private GetAccountsTypeTimeline() {}

                @Schema(example = "[2013, 3, 1]")
                public LocalDate submittedOnDate;
            }

            static final class GetAccountsTypePurchasedShares {

                private GetAccountsTypePurchasedShares() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "01 May 2013")
                public String purchasedDate;
                @Schema(example = "10")
                public Integer numberOfShares;
                @Schema(example = "5")
                public Integer purchasedPrice;
            }

            static final class GetAccountsTypeSummary {

                private GetAccountsTypeSummary() {}

                public GetAccountsTypeAccountIdResponse.GetAccountsCharges.GetAccountsChargesCurrency currency;
            }

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "000000001")
            public Long accountNo;
            @Schema(example = "1")
            public Integer clientId;
            @Schema(example = "Client Name")
            public String clientName;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "Share Product Name")
            public String productName;
            public GetAccountsTypeStatus status;
            public GetAccountsTypeTimeline timeline;
            public GetAccountsTypeAccountIdResponse.GetAccountsCharges.GetAccountsChargesCurrency currency;
            public Set<GetAccountsTypePurchasedShares> purchasedShares;
            public GetAccountsTypeSummary summary;
        }

        @Schema(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetAccountsPageItems> pageItems;
    }

    @Schema(description = "PostAccountsTypeRequest")
    public static final class PostAccountsTypeRequest {

        private PostAccountsTypeRequest() {}

        static final class PostAccountsCharges {

            private PostAccountsCharges() {}

            @Schema(example = "20")
            public Integer chargeId;
            @Schema(example = "1")
            public Integer amount;
        }

        @Schema(example = "7")
        public Integer clientId;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "100")
        public Integer requestedShares;
        @Schema(example = "1")
        public Integer externalId;
        @Schema(example = "01 May 2016")
        public String submittedDate;
        @Schema(example = "1")
        public Integer minimumActivePeriod;
        @Schema(example = "0")
        public Integer minimumActivePeriodFrequencyType;
        @Schema(example = "1")
        public Integer lockinPeriodFrequency;
        @Schema(example = "0")
        public Integer lockinPeriodFrequencyType;
        @Schema(example = "01 May 2016")
        public String applicationDate;
        @Schema(example = "true")
        public Boolean allowDividendCalculationForInactiveClients;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        public Set<PostAccountsCharges> charges;
        @Schema(example = "13")
        public Integer savingsAccountId;
    }

    @Schema(description = "PostAccountsTypeResponse")
    public static final class PostAccountsTypeResponse {

        private PostAccountsTypeResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PostAccountsTypeAccountIdRequest")
    public static final class PostAccountsTypeAccountIdRequest {

        private PostAccountsTypeAccountIdRequest() {}

        static final class PostAccountsRequestedShares {

            private PostAccountsRequestedShares() {}

            @Schema(example = "35")
            public Integer id;
        }

        public Set<PostAccountsRequestedShares> requestedShares;
    }

    @Schema(description = "PostAccountsTypeAccountIdResponse")
    public static final class PostAccountsTypeAccountIdResponse {

        private PostAccountsTypeAccountIdResponse() {}

        @Schema(example = "5")
        public Integer resourceId;
    }

    @Schema(description = "PutAccountsTypeAccountIdRequest")
    public static final class PutAccountsTypeAccountIdRequest {

        private PutAccountsTypeAccountIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "01 April 2016")
        public String applicationDate;
        @Schema(example = "20")
        public Integer requestedShares;
    }

    @Schema(description = "PutAccountsTypeAccountIdResponse")
    public static final class PutAccountsTypeAccountIdResponse {

        private PutAccountsTypeAccountIdResponse() {}

        static final class PutAccountsChanges {

            private PutAccountsChanges() {}

            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "01 April 2016")
            public String applicationDate;
            @Schema(example = "20")
            public Integer requestedShares;
            @Schema(example = "en")
            public String locale;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutAccountsChanges changes;
    }
}
