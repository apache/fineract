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

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/29/17.
 */
final class SavingsAccountsApiResourceSwagger {
    private SavingsAccountsApiResourceSwagger() {
    }

    @ApiModel(value = "GetSavingsAccountsTemplateResponse")
    public final static class GetSavingsAccountsTemplateResponse {
        private GetSavingsAccountsTemplateResponse() {
        }

        final class GetSavingsProductOptions {
            private GetSavingsProductOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Passbook Savings")
            public String name;
        }

        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "small business")
        public String clientName;
        public Set<GetSavingsProductOptions> productOptions;
    }

    @ApiModel(value = "GetSavingsAccountsResponse")
    public final static class GetSavingsAccountsResponse {
        private GetSavingsAccountsResponse() {
        }

        final class GetSavingsPageItems {
            private GetSavingsPageItems() {
            }

            final class GetSavingsStatus {
                private GetSavingsStatus() {
                }

                @ApiModelProperty(example = "100")
                public Integer id;
                @ApiModelProperty(example = "savingsAccountStatusType.submitted.and.pending.approval")
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
                public Boolean withdrawnByApplicant;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean closed;

            }

            final class GetSavingsTimeline {
                private GetSavingsTimeline() {
                }

                @ApiModelProperty(example = "[2013, 3, 1]")
                public LocalDate submittedOnDate;
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

            final class GetSavingsInterestCompoundingPeriodType {
                private GetSavingsInterestCompoundingPeriodType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
                public String code;
                @ApiModelProperty(example = "Daily")
                public String value;
            }

            final class GetSavingsInterestPostingPeriodType {
                private GetSavingsInterestPostingPeriodType() {
                }

                @ApiModelProperty(example = "4")
                public Integer id;
                @ApiModelProperty(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
                public String code;
                @ApiModelProperty(example = "Monthly")
                public String value;
            }

            final class GetSavingsInterestCalculationType {
                private GetSavingsInterestCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "savingsInterestCalculationType.dailybalance")
                public String code;
                @ApiModelProperty(example = "Daily Balance")
                public String value;
            }

            final class GetSavingsInterestCalculationDaysInYearType {
                private GetSavingsInterestCalculationDaysInYearType() {
                }

                @ApiModelProperty(example = "365")
                public Integer id;
                @ApiModelProperty(example = "savingsInterestCalculationDaysInYearType.days365")
                public String code;
                @ApiModelProperty(example = "365 Days")
                public String value;
            }

            final class GetSavingsSummary {
                private GetSavingsSummary() {
                }

                public GetSavingsCurrency currency;
                @ApiModelProperty(example = "0")
                public Integer accountBalance;
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "000000001")
            public String accountNo;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "small business")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer savingsProductId;
            @ApiModelProperty(example = "Passbook Savings")
            public String savingsProductName;
            @ApiModelProperty(example = "0")
            public Integer fieldOfficerId;
            public GetSavingsStatus status;
            public GetSavingsTimeline timeline;
            public GetSavingsCurrency currency;
            @ApiModelProperty(example = "5")
            public Double nominalAnnualInterestRate;
            public GetSavingsInterestCompoundingPeriodType interestCompoundingPeriodType;
            public GetSavingsInterestPostingPeriodType interestPostingPeriodType;
            public GetSavingsInterestCalculationType interestCalculationType;
            public GetSavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
            public GetSavingsSummary summary;
        }

        @ApiModelProperty(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetSavingsPageItems> pageItems;
    }

    @ApiModel(value = "PostSavingsAccountsRequest")
    public final static class PostSavingsAccountsRequest {
        private PostSavingsAccountsRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer productId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 March 2011")
        public String submittedOnDate;
    }

    @ApiModel(value = "PostSavingsAccountsResponse")
    public final static class PostSavingsAccountsResponse {
        private PostSavingsAccountsResponse() {
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "GetSavingsAccountsAccountIdResponse")
    public final static class GetSavingsAccountsAccountIdResponse {
        private GetSavingsAccountsAccountIdResponse() {
        }

        final class GetSavingsAccountsSummary {
            private GetSavingsAccountsSummary() {
            }

            public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsCurrency currency;
            @ApiModelProperty(example = "0")
            public Integer accountBalance;
            @ApiModelProperty(example = "0")
            public Integer availableBalance;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "000000001")
        public String accountNo;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "small business")
        public String clientName;
        @ApiModelProperty(example = "1")
        public Integer savingsProductId;
        @ApiModelProperty(example = "Passbook Savings")
        public String savingsProductName;
        @ApiModelProperty(example = "0")
        public Integer fieldOfficerId;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsStatus status;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsTimeline timeline;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsCurrency currency;
        @ApiModelProperty(example = "5")
        public Double nominalAnnualInterestRate;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestCalculationType interestCalculationType;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetSavingsAccountsSummary summary;
    }

    @ApiModel(value = "PutSavingsAccountsAccountIdRequest")
    public final static class PutSavingsAccountsAccountIdRequest {
        private PutSavingsAccountsAccountIdRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "5.9999999999")
        public Double nominalAnnualInterestRate;
    }

    @ApiModel(value = "PutSavingsAccountsAccountIdResponse")
    public final static class PutSavingsAccountsAccountIdResponse {
        private PutSavingsAccountsAccountIdResponse() {
        }

        final class PutSavingsAccountsChanges {
            private PutSavingsAccountsChanges() {
            }

            @ApiModelProperty(example = "5.9999999999")
            public Double nominalAnnualInterestRate;
            @ApiModelProperty(example = "en")
            public String locale;
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutSavingsAccountsChanges changes;
    }

    @ApiModel(value = "PostSavingsAccountsAccountIdRequest")
    public final static class PostSavingsAccountsAccountIdRequest {
        private PostSavingsAccountsAccountIdRequest() {
        }

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "05 September 2014")
        public String unassignedDate;
    }

    @ApiModel(value = "PostSavingsAccountsAccountIdResponse")
    public final static class PostSavingsAccountsAccountIdResponse {
        private PostSavingsAccountsAccountIdResponse() {
        }

        final class PostSavingsAccountsAccountIdChanges {
            private PostSavingsAccountsAccountIdChanges() {
            }
        }

        @ApiModelProperty(example = "2")
        public Integer officeId;
        @ApiModelProperty(example = "8")
        public Integer clientId;
        @ApiModelProperty(example = "8")
        public Integer resourceId;
        public PostSavingsAccountsAccountIdChanges changes;
    }

    @ApiModel(value = "DeleteSavingsAccountsAccountIdResponse")
    public final static class DeleteSavingsAccountsAccountIdResponse {
        private DeleteSavingsAccountsAccountIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
