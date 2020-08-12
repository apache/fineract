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
package org.apache.fineract.portfolio.group.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/18/17.
 */
final class CentersApiResourceSwagger {

    private CentersApiResourceSwagger() {}

    @Schema(description = "GetCentersTemplateResponse")
    public static final class GetCentersTemplateResponse {

        private GetCentersTemplateResponse() {}

        static final class GetCentersOfficeOptions {

            private GetCentersOfficeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
        }

        static final class GetCentersStaffOptions {

            private GetCentersStaffOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "D, Mary")
            public String displayName;
        }

        @Schema(example = "false")
        public Boolean active;
        @Schema(example = "[2013, 4, 18]")
        public LocalDate activationDate;
        @Schema(example = "2")
        public Integer officeId;
        public Set<GetCentersOfficeOptions> officeOptions;
        public Set<GetCentersStaffOptions> staffOptions;
    }

    @Schema(description = "GetCentersResponse")
    public static final class GetCentersResponse {

        private GetCentersResponse() {}

        static final class GetCentersPageItems {

            private GetCentersPageItems() {}

            static final class GetCentersStatus {

                private GetCentersStatus() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "groupingStatusType.pending")
                public String code;
                @Schema(example = "Pending")
                public String description;
            }

            @Schema(example = "2")
            public Integer id;
            public GetCentersStatus status;
            @Schema(example = "false")
            public Boolean active;
            @Schema(example = "Center 1")
            public String name;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
            @Schema(example = ".2.")
            public String hierarchy;
        }

        @Schema(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetCentersPageItems> pageItems;
    }

    @Schema(description = "GetCentersCenterIdResponse")
    public static final class GetCentersCenterIdResponse {

        private GetCentersCenterIdResponse() {}

        @Schema(example = "8")
        public Integer id;
        public GetCentersResponse.GetCentersPageItems.GetCentersStatus status;
        @Schema(example = "false")
        public Boolean active;
        @Schema(example = "First Center (No groups)")
        public String name;
        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = ".8.")
        public String hierarchy;
    }

    @Schema(description = "PostCentersRequest")
    public static final class PostCentersRequest {

        private PostCentersRequest() {}

        @Schema(example = "First Center (No groups)")
        public String name;
        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "false")
        public Boolean active;
    }

    @Schema(description = "PostCentersResponse")
    public static final class PostCentersResponse {

        private PostCentersResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "8")
        public Integer groupId;
        @Schema(example = "8")
        public Integer resourceId;
    }

    @Schema(description = "PutCentersCenterIdRequest")
    public static final class PutCentersCenterIdRequest {

        private PutCentersCenterIdRequest() {}

        @Schema(example = "First Center (No groups)")
        public String name;
    }

    @Schema(description = "PutCentersCenterIdResponse")
    public static final class PutCentersCenterIdResponse {

        private PutCentersCenterIdResponse() {}

        static final class PutCentersChanges {

            private PutCentersChanges() {}

            @Schema(example = "First Center (No groups) - modified")
            public String name;
        }

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "8")
        public Integer groupId;
        @Schema(example = "8")
        public Integer resourceId;
        public PutCentersChanges changes;
    }

    @Schema(description = "DeleteCentersCenterIdResponse")
    public static final class DeleteCentersCenterIdResponse {

        private DeleteCentersCenterIdResponse() {}

        static final class DeleteCentersChanges {

            private DeleteCentersChanges() {}
        }

        @Schema(example = "1")
        public Integer resourceId;
        public DeleteCentersChanges changes;
    }

    @Schema(description = "PostCentersCenterIdRequest")
    public static final class PostCentersCenterIdRequest {

        private PostCentersCenterIdRequest() {}

        @Schema(example = "32")
        public Integer closureReasonId;
        @Schema(example = "05 May 2014")
        public String closureDate;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PostCentersCenterIdResponse")
    public static final class PostCentersCenterIdResponse {

        private PostCentersCenterIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetCentersCenterIdAccountsResponse")
    public static final class GetCentersCenterIdAccountsResponse {

        private GetCentersCenterIdAccountsResponse() {}

        static final class GetCentersSavingsAccounts {

            private GetCentersSavingsAccounts() {}

            static final class GetCentersCenterIdStatus {

                private GetCentersCenterIdStatus() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "savingsAccountStatusType.submitted.and.pending.approval")
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
                public Boolean withdrawnByApplicant;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean closed;
                @Schema(example = "false")
                public Boolean prematureClosed;
                @Schema(example = "false")
                public Boolean transferInProgress;
                @Schema(example = "false")
                public Boolean transferOnHold;
            }

            static final class GetCentersCenterIdCurrency {

                private GetCentersCenterIdCurrency() {}

                @Schema(example = "USD")
                public String code;
                @Schema(example = "US Dollar")
                public String name;
                @Schema(example = "2")
                public Integer decimalPlaces;
                @Schema(example = "0")
                public Integer inMultiplesOf;
                @Schema(example = "$")
                public String displaySymbol;
                @Schema(example = "currency.USD")
                public String nameCode;
                @Schema(example = "US Dollar ($)")
                public String displayLabel;
            }

            static final class GetCentersAccountType {

                private GetCentersAccountType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "accountType.group")
                public String code;
                @Schema(example = "Group")
                public String description;
            }

            static final class GetCentersTimeline {

                private GetCentersTimeline() {}

                @Schema(example = "[2014, 5, 1]")
                public LocalDate submittedOnDate;
                @Schema(example = "mifos")
                public String submittedByUsername;
                @Schema(example = "App")
                public String submittedByFirstname;
                @Schema(example = "Administrator")
                public String submittedByLastname;
            }

            static final class GetCentersDepositType {

                private GetCentersDepositType() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "depositAccountType.savingsDeposit")
                public String code;
                @Schema(example = "Savings")
                public String description;
            }

            @Schema(example = "16")
            public Integer id;
            @Schema(example = "000000016")
            public Long accountNo;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "Voluntary savings")
            public String productName;
            public GetCentersCenterIdStatus status;
            public GetCentersCenterIdCurrency currency;
            public GetCentersAccountType accountType;
            public GetCentersTimeline timeline;
            public GetCentersDepositType depositType;
        }

        public Set<GetCentersSavingsAccounts> savingsAccounts;
    }
}
