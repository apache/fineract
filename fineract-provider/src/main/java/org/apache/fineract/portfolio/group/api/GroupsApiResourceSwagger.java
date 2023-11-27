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
 * Created by Chirag Gupta on 12/10/17.
 */
final class GroupsApiResourceSwagger {

    private GroupsApiResourceSwagger() {}

    @Schema(description = "GetGroupsTemplateResponse")
    public static final class GetGroupsTemplateResponse {

        private GetGroupsTemplateResponse() {}

        static final class GetGroupsTemplateOfficeOptions {

            private GetGroupsTemplateOfficeOptions() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
        }

        static final class GetGroupsTemplateStaffOptions {

            private GetGroupsTemplateStaffOptions() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "C, Mike")
            public String displayName;
        }

        static final class GetGroupsTemplateClientOptions {

            private GetGroupsTemplateClientOptions() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Petra Yton")
            public String displayName;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "Head Office")
            public String officeName;
        }

        static final class GetGroupsTemplateDatatables {

            private GetGroupsTemplateDatatables() {}

            static final class GetGroupsTemplateColumnHeaderData {

                private GetGroupsTemplateColumnHeaderData() {}

                static final class GetGroupsTemplateColumnValues {

                    private GetGroupsTemplateColumnValues() {}
                }

                @Schema(example = "group_id")
                public String columnName;
                @Schema(example = "bigint")
                public String columnType;
                @Schema(example = "0")
                public Integer columnLength;
                @Schema(example = "INTEGER")
                public String columnDisplayType;
                @Schema(example = "false")
                public Boolean isColumnNullable;
                @Schema(example = "true")
                public Boolean isColumnPrimaryKey;
                public Set<GetGroupsTemplateColumnValues> columnValues;
            }

            @Schema(example = "m_group")
            public String applicationTableName;
            @Schema(example = "Group Activation Data")
            public String registeredTableName;
            public Set<GetGroupsTemplateColumnHeaderData> columnHeaderData;
        }

        @Schema(example = "1")
        public Long officeId;
        public Set<GetGroupsTemplateOfficeOptions> officeOptions;
        public Set<GetGroupsTemplateStaffOptions> staffOptions;
        public Set<GetGroupsTemplateClientOptions> clientOptions;
        public Set<GetGroupsTemplateDatatables> datatables;
    }

    @Schema(description = "GetGroupsResponse")
    public static final class GetGroupsResponse {

        private GetGroupsResponse() {}

        static final class GetGroupsPageItems {

            private GetGroupsPageItems() {}

            static final class GetGroupsStatus {

                private GetGroupsStatus() {}

                @Schema(example = "100")
                public Long id;
                @Schema(example = "clientStatusType.pending")
                public String code;
                @Schema(example = "Pending")
                public String description;
            }

            @Schema(example = "4")
            public Long id;
            @Schema(example = "AnotherGroup")
            public String name;
            public GetGroupsStatus status;
            @Schema(example = "false")
            public Boolean active;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "Head Office")
            public String officeName;
            @Schema(example = ".4.")
            public String hierarchy;
        }

        @Schema(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetGroupsPageItems> pageItems;
    }

    @Schema(description = "GetGroupsGroupIdResponse")
    public static final class GetGroupsGroupIdResponse {

        private GetGroupsGroupIdResponse() {}

        static final class GetGroupsGroupIdTimeline {

            private GetGroupsGroupIdTimeline() {}

            @Schema(example = "[2013, 11, 14]")
            public LocalDate activatedOnDate;
            @Schema(example = "mifos")
            public String activatedByUsername;
            @Schema(example = "App")
            public String activatedByFirstname;
            @Schema(example = "Administrator")
            public String activatedByLastname;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "First Group")
        public String name;
        @Schema(example = "000-1A")
        public String externalId;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = ".1.")
        public String hierarchy;
        public GetGroupsGroupIdTimeline timeline;
    }

    @Schema(description = "PostGroupsRequest")
    public static final class PostGroupsRequest {

        private PostGroupsRequest() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Pending Group")
        public String name;
        @Schema(example = "false")
        public Boolean active;
    }

    @Schema(description = "PostGroupsResponse")
    public static final class PostGroupsResponse {

        private PostGroupsResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "2")
        public Long groupId;
        @Schema(example = "2")
        public Long resourceId;
    }

    @Schema(description = "DeleteGroupsGroupIdResponse")
    public static final class DeleteGroupsGroupIdResponse {

        private DeleteGroupsGroupIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "2")
        public Long groupId;
        @Schema(example = "2")
        public Long resourceId;
    }

    @Schema(description = "PostGroupsGroupIdCommandUnassignStaffRequest")
    public static final class PostGroupsGroupIdCommandUnassignStaffRequest {

        private PostGroupsGroupIdCommandUnassignStaffRequest() {}

        @Schema(example = "1")
        public Long staffId;
    }

    @Schema(description = "PostGroupsGroupIdCommandUnassignStaffResponse")
    public static final class PostGroupsGroupIdCommandUnassignStaffResponse {

        private PostGroupsGroupIdCommandUnassignStaffResponse() {}

        static final class PostGroupsGroupIdCommandUnassignStaffChanges {

            private PostGroupsGroupIdCommandUnassignStaffChanges() {}
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long groupId;
        @Schema(example = "1")
        public Long resourceId;
        public PostGroupsGroupIdCommandUnassignStaffChanges changes;
    }

    @Schema(description = "PutGroupsGroupIdRequest")
    public static final class PutGroupsGroupIdRequest {

        private PutGroupsGroupIdRequest() {}

        @Schema(example = "First Group (changed)")
        public String name;
    }

    @Schema(description = "PutGroupsGroupIdResponse")
    public static final class PutGroupsGroupIdResponse {

        private PutGroupsGroupIdResponse() {}

        static final class PutGroupsGroupIdChanges {

            private PutGroupsGroupIdChanges() {}

            @Schema(example = "First Group (changed)")
            public String name;
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long groupId;
        @Schema(example = "1")
        public Long resourceId;
        public PutGroupsGroupIdChanges changes;
    }

    @Schema(description = "GetGroupsGroupIdAccountsResponse")
    public static final class GetGroupsGroupIdAccountsResponse {

        private GetGroupsGroupIdAccountsResponse() {}

        static final class GetGroupsGroupIdAccountsLoanAccounts {

            private GetGroupsGroupIdAccountsLoanAccounts() {}

            static final class GetGroupsGroupIdAccountsStatus {

                private GetGroupsGroupIdAccountsStatus() {}

                @Schema(example = "100")
                public Long id;
                @Schema(example = "loanStatusType.submitted.and.pending.approval")
                public String code;
                @Schema(example = "Submitted and pending approval")
                public String description;
                @Schema(example = "true")
                public Boolean pendingApproval;
                @Schema(example = "false")
                public Boolean waitingForDisbursal;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean closedObligationsMet;
                @Schema(example = "false")
                public Boolean closedWrittenOff;
                @Schema(example = "false")
                public Boolean closedRescheduled;
                @Schema(example = "false")
                public Boolean closed;
                @Schema(example = "false")
                public Boolean overpaid;
            }

            static final class GetGroupsGroupIdAccountsLoanType {

                private GetGroupsGroupIdAccountsLoanType() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "accountType.group")
                public String code;
                @Schema(example = "Group")
                public String description;
            }

            @Schema(example = "3")
            public Long id;
            @Schema(example = "000000003")
            public Long accountNo;
            @Schema(example = "3")
            public Long productId;
            @Schema(example = "daily product")
            public String productName;
            public GetGroupsGroupIdAccountsStatus status;
            public GetGroupsGroupIdAccountsLoanType loanType;
        }

        static final class GetGroupsGroupIdAccountsSavingAccounts {

            private GetGroupsGroupIdAccountsSavingAccounts() {}

            static final class GetGroupsGroupIdAccountsSavingStatus {

                private GetGroupsGroupIdAccountsSavingStatus() {}

                @Schema(example = "100")
                public Long id;
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
            }

            static final class GetGroupsGroupIdAccountsSavingCurrency {

                private GetGroupsGroupIdAccountsSavingCurrency() {}

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

            static final class GetGroupsGroupIdAccountsSavingAccountType {

                private GetGroupsGroupIdAccountsSavingAccountType() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "accountType.group")
                public String code;
                @Schema(example = "Group")
                public String description;
            }

            @Schema(example = "9")
            public Long id;
            @Schema(example = "000000009")
            public Long accountNo;
            @Schema(example = "1")
            public Long productId;
            @Schema(example = "p_sav")
            public String productName;
            public GetGroupsGroupIdAccountsSavingStatus status;
            public GetGroupsGroupIdAccountsSavingCurrency currency;
            public GetGroupsGroupIdAccountsSavingAccountType accountType;
        }

        static final class GetGroupsGroupIdAccountsMemberLoanAccounts {

            private GetGroupsGroupIdAccountsMemberLoanAccounts() {}

            static final class GetGroupsGroupIdAccountsMemberLoanStatus {

                private GetGroupsGroupIdAccountsMemberLoanStatus() {}

                @Schema(example = "200")
                public Long id;
                @Schema(example = "loanStatusType.approved")
                public String code;
                @Schema(example = "Approved")
                public String description;
                @Schema(example = "false")
                public Boolean pendingApproval;
                @Schema(example = "true")
                public Boolean waitingForDisbursal;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean closedObligationsMet;
                @Schema(example = "false")
                public Boolean closedWrittenOff;
                @Schema(example = "false")
                public Boolean closedRescheduled;
                @Schema(example = "false")
                public Boolean closed;
                @Schema(example = "false")
                public Boolean overpaid;
            }

            static final class GetGroupsGroupIdAccountsMemberLoanType {

                private GetGroupsGroupIdAccountsMemberLoanType() {}

                @Schema(example = "3")
                public Long id;
                @Schema(example = "accountType.jlg")
                public String code;
                @Schema(example = "JLG")
                public String description;
            }

            @Schema(example = "4")
            public Long id;
            @Schema(example = "000000004")
            public Long accountNo;
            @Schema(example = "1")
            public Long productId;
            @Schema(example = "testLoan")
            public String productName;
            public GetGroupsGroupIdAccountsMemberLoanStatus status;
            public GetGroupsGroupIdAccountsMemberLoanType loanType;
        }

        static final class GetGroupsGroupIdAccountsMemberSavingsAccounts {

            private GetGroupsGroupIdAccountsMemberSavingsAccounts() {}

            @Schema(example = "3")
            public Long id;
            @Schema(example = "000000003")
            public Long accountNo;
            @Schema(example = "1")
            public Long productId;
            @Schema(example = "p_sav")
            public String productName;
            public GetGroupsGroupIdAccountsSavingAccounts.GetGroupsGroupIdAccountsSavingStatus status;
            public GetGroupsGroupIdAccountsSavingAccounts.GetGroupsGroupIdAccountsSavingCurrency currency;
            public GetGroupsGroupIdAccountsMemberLoanAccounts.GetGroupsGroupIdAccountsMemberLoanType accountType;
        }

        public Set<GetGroupsGroupIdAccountsLoanAccounts> loanAccounts;
        public Set<GetGroupsGroupIdAccountsSavingAccounts> savingsAccounts;
        public Set<GetGroupsGroupIdAccountsMemberLoanAccounts> memberLoanAccounts;
        public Set<GetGroupsGroupIdAccountsMemberSavingsAccounts> memberSavingsAccounts;
    }

    @Schema(description = "PostGroupsGroupIdRequest")
    public static final class PostGroupsGroupIdRequest {

        private PostGroupsGroupIdRequest() {}

        static final class PostGroupsGroupIdClients {

            private PostGroupsGroupIdClients() {}

            @Schema(example = "1")
            public Long id;
        }

        @Schema(example = "2")
        public Long destinationGroupId;
        public Set<PostGroupsGroupIdClients> clients;
    }

    @Schema(description = "PostGroupsGroupIdResponse")
    public static final class PostGroupsGroupIdResponse {

        private PostGroupsGroupIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }
}
