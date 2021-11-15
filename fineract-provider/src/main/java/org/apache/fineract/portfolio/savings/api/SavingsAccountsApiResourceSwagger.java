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
 * Created by Chirag Gupta on 12/29/17.
 */
final class SavingsAccountsApiResourceSwagger {

    private SavingsAccountsApiResourceSwagger() {}

    @Schema(description = "GetSavingsAccountsTemplateResponse")
    public static final class GetSavingsAccountsTemplateResponse {

        private GetSavingsAccountsTemplateResponse() {}

        static final class GetSavingsProductOptions {

            private GetSavingsProductOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Passbook Savings")
            public String name;
        }

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "small business")
        public String clientName;
        public Set<GetSavingsProductOptions> productOptions;
    }

    @Schema(description = "GetSavingsAccountsResponse")
    public static final class GetSavingsAccountsResponse {

        private GetSavingsAccountsResponse() {}

        static final class GetSavingsPageItems {

            private GetSavingsPageItems() {}

            static final class GetSavingsStatus {

                private GetSavingsStatus() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "savingsAccountStatusType.submitted.and.pending.approval")
                public String code;
                @Schema(example = "Submitted and pending approval")
                public String value;
                @Schema(example = "true")
                public Boolean submittedAndPendingApproval;
                @Schema(example = "false")
                public Boolean approved;
                @Schema(example = "false")
                public Boolean rejected;
                @Schema(example = "false")
                public Boolean withdrawnByApplicant;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean closed;

            }

            static final class GetSavingsTimeline {

                private GetSavingsTimeline() {}

                @Schema(example = "[2013, 3, 1]")
                public LocalDate submittedOnDate;
                @Schema(example = "[2013, 4, 8]")
                public LocalDate approvedOnDate;
                @Schema(example = "[2014, 3, 1]")
                public LocalDate activatedOnDate;
                @Schema(example = "username")
                public String submittedByUsername;
                @Schema(example = "name")
                public String submittedByFirstname;
                @Schema(example = "lastname")
                public String submittedByLastname;
                @Schema(example = "mifos")
                public String approvedByUsername;
                @Schema(example = "name")
                public String approvedByFirstname;
                @Schema(example = "lastname")
                public String approvedByLastname;
            }

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

            static final class GetSavingsInterestCompoundingPeriodType {

                private GetSavingsInterestCompoundingPeriodType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "savings.interest.period.savingsCompoundingInterestPeriodType.daily")
                public String code;
                @Schema(example = "Daily")
                public String value;
            }

            static final class GetSavingsInterestPostingPeriodType {

                private GetSavingsInterestPostingPeriodType() {}

                @Schema(example = "4")
                public Integer id;
                @Schema(example = "savings.interest.posting.period.savingsPostingInterestPeriodType.monthly")
                public String code;
                @Schema(example = "Monthly")
                public String value;
            }

            static final class GetSavingsInterestCalculationType {

                private GetSavingsInterestCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "savingsInterestCalculationType.dailybalance")
                public String code;
                @Schema(example = "Daily Balance")
                public String value;
            }

            static final class GetSavingsInterestCalculationDaysInYearType {

                private GetSavingsInterestCalculationDaysInYearType() {}

                @Schema(example = "365")
                public Integer id;
                @Schema(example = "savingsInterestCalculationDaysInYearType.days365")
                public String code;
                @Schema(example = "365 Days")
                public String value;
            }

            static final class GetSavingsSummary {

                private GetSavingsSummary() {}

                public GetSavingsCurrency currency;
                @Schema(example = "0")
                public Integer accountBalance;
            }

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "000000001")
            public String accountNo;
            @Schema(example = "1")
            public Integer clientId;
            @Schema(example = "small business")
            public String clientName;
            @Schema(example = "1")
            public Integer savingsProductId;
            @Schema(example = "Passbook Savings")
            public String savingsProductName;
            @Schema(example = "0")
            public Integer fieldOfficerId;
            public GetSavingsStatus status;
            public GetSavingsTimeline timeline;
            public GetSavingsCurrency currency;
            @Schema(example = "5")
            public Double nominalAnnualInterestRate;
            public GetSavingsInterestCompoundingPeriodType interestCompoundingPeriodType;
            public GetSavingsInterestPostingPeriodType interestPostingPeriodType;
            public GetSavingsInterestCalculationType interestCalculationType;
            public GetSavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
            public GetSavingsSummary summary;
        }

        @Schema(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetSavingsPageItems> pageItems;
    }

    @Schema(description = "PostSavingsAccountsRequest")
    public static final class PostSavingsAccountsRequest {

        private PostSavingsAccountsRequest() {}

        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer productId;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "01 March 2011")
        public String submittedOnDate;
    }

    @Schema(description = "PostSavingsAccountsResponse")
    public static final class PostSavingsAccountsResponse {

        private PostSavingsAccountsResponse() {}

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetSavingsAccountsAccountIdResponse")
    public static final class GetSavingsAccountsAccountIdResponse {

        private GetSavingsAccountsAccountIdResponse() {}

        static final class GetSavingsAccountsSummary {

            private GetSavingsAccountsSummary() {}

            public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsCurrency currency;
            @Schema(example = "0")
            public Integer accountBalance;
            @Schema(example = "0")
            public Integer availableBalance;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "000000001")
        public String accountNo;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "small business")
        public String clientName;
        @Schema(example = "1")
        public Integer savingsProductId;
        @Schema(example = "Passbook Savings")
        public String savingsProductName;
        @Schema(example = "0")
        public Integer fieldOfficerId;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsStatus status;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsTimeline timeline;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsCurrency currency;
        @Schema(example = "5")
        public Double nominalAnnualInterestRate;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestCompoundingPeriodType interestCompoundingPeriodType;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestPostingPeriodType interestPostingPeriodType;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestCalculationType interestCalculationType;
        public GetSavingsAccountsResponse.GetSavingsPageItems.GetSavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType;
        public GetSavingsAccountsSummary summary;
    }

    @Schema(description = "PutSavingsAccountsAccountIdRequest")
    public static final class PutSavingsAccountsAccountIdRequest {

        private PutSavingsAccountsAccountIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "5.9999999999")
        public Double nominalAnnualInterestRate;
    }

    @Schema(description = "PutSavingsAccountsAccountIdResponse")
    public static final class PutSavingsAccountsAccountIdResponse {

        private PutSavingsAccountsAccountIdResponse() {}

        static final class PutSavingsAccountsChanges {

            private PutSavingsAccountsChanges() {}

            @Schema(example = "5.9999999999")
            public Double nominalAnnualInterestRate;
            @Schema(example = "en")
            public String locale;
        }

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
        public PutSavingsAccountsChanges changes;
    }

    @Schema(description = "PostSavingsAccountsAccountIdRequest")
    public static final class PostSavingsAccountsAccountIdRequest {

        private PostSavingsAccountsAccountIdRequest() {}

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "05 September 2014")
        public String approvedOnDate;
        @Schema(example = "05 September 2014")
        public String activatedOnDate;
    }

    @Schema(description = "PostSavingsAccountsAccountIdResponse")
    public static final class PostSavingsAccountsAccountIdResponse {

        private PostSavingsAccountsAccountIdResponse() {}

        static final class PostSavingsAccountsAccountIdChanges {

            private PostSavingsAccountsAccountIdChanges() {}
        }

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "8")
        public Integer clientId;
        @Schema(example = "8")
        public Integer resourceId;
        public PostSavingsAccountsAccountIdChanges changes;
    }

    @Schema(description = "DeleteSavingsAccountsAccountIdResponse")
    public static final class DeleteSavingsAccountsAccountIdResponse {

        private DeleteSavingsAccountsAccountIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer resourceId;
    }
}
