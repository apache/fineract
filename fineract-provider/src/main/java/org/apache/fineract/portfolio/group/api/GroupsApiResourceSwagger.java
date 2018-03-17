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
package org.apache.fineract.portfolio.group.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/10/17.
 */
final class GroupsApiResourceSwagger {
    private GroupsApiResourceSwagger() {
    }

    @ApiModel(value = "GetGroupsTemplateResponse")
    public final static class GetGroupsTemplateResponse {
        private GetGroupsTemplateResponse() {
        }

        final class GetGroupsTemplateOfficeOptions {
            private GetGroupsTemplateOfficeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String nameDecorated;
        }

        final class GetGroupsTemplateStaffOptions {
            private GetGroupsTemplateStaffOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "C, Mike")
            public String displayName;
        }

        final class GetGroupsTemplateClientOptions {
            private GetGroupsTemplateClientOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Petra Yton")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
        }

        final class GetGroupsTemplateDatatables {
            private GetGroupsTemplateDatatables() {
            }

            final class GetGroupsTemplateColumnHeaderData {
                private GetGroupsTemplateColumnHeaderData() {
                }

                final class GetGroupsTemplateColumnValues {
                    private GetGroupsTemplateColumnValues() {
                    }
                }

                @ApiModelProperty(example = "group_id")
                public String columnName;
                @ApiModelProperty(example = "bigint")
                public String columnType;
                @ApiModelProperty(example = "0")
                public Integer columnLength;
                @ApiModelProperty(example = "INTEGER")
                public String columnDisplayType;
                @ApiModelProperty(example = "false")
                public Boolean isColumnNullable;
                @ApiModelProperty(example = "true")
                public Boolean isColumnPrimaryKey;
                public Set<GetGroupsTemplateColumnValues> columnValues;
            }

            @ApiModelProperty(example = "m_group")
            public String applicationTableName;
            @ApiModelProperty(example = "Group Activation Data")
            public String registeredTableName;
            public Set<GetGroupsTemplateColumnHeaderData> columnHeaderData;
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        public Set<GetGroupsTemplateOfficeOptions> officeOptions;
        public Set<GetGroupsTemplateStaffOptions> staffOptions;
        public Set<GetGroupsTemplateClientOptions> clientOptions;
        public Set<GetGroupsTemplateDatatables> datatables;
    }

    @ApiModel(value = "GetGroupsResponse")
    public static final class GetGroupsResponse {
        private GetGroupsResponse() {
        }

        final class GetGroupsPageItems {
            private GetGroupsPageItems() {
            }

            final class GetGroupsStatus {
                private GetGroupsStatus() {
                }

                @ApiModelProperty(example = "100")
                public Integer id;
                @ApiModelProperty(example = "clientStatusType.pending")
                public String code;
                @ApiModelProperty(example = "Pending")
                public String value;
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "AnotherGroup")
            public String name;
            public GetGroupsStatus status;
            @ApiModelProperty(example = "false")
            public Boolean active;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
            @ApiModelProperty(example = ".4.")
            public String hierarchy;
        }

        @ApiModelProperty(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetGroupsPageItems> pageItems;
    }

    @ApiModel(value = "GetGroupsGroupIdResponse")
    public final static class GetGroupsGroupIdResponse {
        private GetGroupsGroupIdResponse() {
        }

        final class GetGroupsGroupIdTimeline {
            private GetGroupsGroupIdTimeline() {
            }

            @ApiModelProperty(example = "[2013, 11, 14]")
            public LocalDate activatedOnDate;
            @ApiModelProperty(example = "mifos")
            public String activatedByUsername;
            @ApiModelProperty(example = "App")
            public String activatedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String activatedByLastname;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "First Group")
        public String name;
        @ApiModelProperty(example = "000-1A")
        public String externalId;
        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = ".1.")
        public String hierarchy;
        public GetGroupsGroupIdTimeline timeline;
    }

    @ApiModel(value = "PostGroupsRequest")
    public final static class PostGroupsRequest {
        private PostGroupsRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Pending Group")
        public String name;
        @ApiModelProperty(example = "false")
        public Boolean active;
    }

    @ApiModel(value = "PostGroupsResponse")
    public final static class PostGroupsResponse {
        private PostGroupsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "2")
        public Integer groupId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
    }

    @ApiModel(value = "DeleteGroupsGroupIdResponse")
    public final static class DeleteGroupsGroupIdResponse {
        private DeleteGroupsGroupIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "2")
        public Integer groupId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
    }

    @ApiModel(value = "PostGroupsGroupIdCommandUnassignStaffRequest")
    public final static class PostGroupsGroupIdCommandUnassignStaffRequest {
        private PostGroupsGroupIdCommandUnassignStaffRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer staffId;
    }

    @ApiModel(value = "PostGroupsGroupIdCommandUnassignStaffResponse")
    public final static class PostGroupsGroupIdCommandUnassignStaffResponse {
        private PostGroupsGroupIdCommandUnassignStaffResponse() {
        }

        final class PostGroupsGroupIdCommandUnassignStaffChanges {
            private PostGroupsGroupIdCommandUnassignStaffChanges() {
            }
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer groupId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PostGroupsGroupIdCommandUnassignStaffChanges changes;
    }

    @ApiModel(value = "PutGroupsGroupIdRequest")
    public final static class PutGroupsGroupIdRequest {
        private PutGroupsGroupIdRequest() {
        }

        @ApiModelProperty(example = "First Group (changed)")
        public String name;
    }

    @ApiModel(value = "PutGroupsGroupIdResponse")
    public final static class PutGroupsGroupIdResponse {
        private PutGroupsGroupIdResponse() {
        }

        final class PutGroupsGroupIdChanges {
            private PutGroupsGroupIdChanges() {
            }

            @ApiModelProperty(example = "First Group (changed)")
            public String name;
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer groupId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutGroupsGroupIdChanges changes;
    }

    @ApiModel(value = "GetGroupsGroupIdAccountsResponse")
    public final static class GetGroupsGroupIdAccountsResponse {
        private GetGroupsGroupIdAccountsResponse() {
        }

        final class GetGroupsGroupIdAccountsLoanAccounts {
            private GetGroupsGroupIdAccountsLoanAccounts() {
            }

            final class GetGroupsGroupIdAccountsStatus {
                private GetGroupsGroupIdAccountsStatus() {
                }

                @ApiModelProperty(example = "100")
                public Integer id;
                @ApiModelProperty(example = "loanStatusType.submitted.and.pending.approval")
                public String code;
                @ApiModelProperty(example = "Submitted and pending approval")
                public String value;
                @ApiModelProperty(example = "true")
                public Boolean pendingApproval;
                @ApiModelProperty(example = "false")
                public Boolean waitingForDisbursal;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean closedObligationsMet;
                @ApiModelProperty(example = "false")
                public Boolean closedWrittenOff;
                @ApiModelProperty(example = "false")
                public Boolean closedRescheduled;
                @ApiModelProperty(example = "false")
                public Boolean closed;
                @ApiModelProperty(example = "false")
                public Boolean overpaid;
            }

            final class GetGroupsGroupIdAccountsLoanType {
                private GetGroupsGroupIdAccountsLoanType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "accountType.group")
                public String code;
                @ApiModelProperty(example = "Group")
                public String value;
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "000000003")
            public Long accountNo;
            @ApiModelProperty(example = "3")
            public Integer productId;
            @ApiModelProperty(example = "daily product")
            public String productName;
            public GetGroupsGroupIdAccountsStatus status;
            public GetGroupsGroupIdAccountsLoanType loanType;
        }

        final class GetGroupsGroupIdAccountsSavingAccounts {
            private GetGroupsGroupIdAccountsSavingAccounts() {
            }

            final class GetGroupsGroupIdAccountsSavingStatus {
                private GetGroupsGroupIdAccountsSavingStatus() {
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

            final class GetGroupsGroupIdAccountsSavingCurrency {
                private GetGroupsGroupIdAccountsSavingCurrency() {
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

            final class GetGroupsGroupIdAccountsSavingAccountType {
                private GetGroupsGroupIdAccountsSavingAccountType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "accountType.group")
                public String code;
                @ApiModelProperty(example = "Group")
                public String value;
            }

            @ApiModelProperty(example = "9")
            public Integer id;
            @ApiModelProperty(example = "000000009")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "p_sav")
            public String productName;
            public GetGroupsGroupIdAccountsSavingStatus status;
            public GetGroupsGroupIdAccountsSavingCurrency currency;
            public GetGroupsGroupIdAccountsSavingAccountType accountType;
        }

        final class GetGroupsGroupIdAccountsMemberLoanAccounts {
            private GetGroupsGroupIdAccountsMemberLoanAccounts() {
            }

            final class GetGroupsGroupIdAccountsMemberLoanStatus {
                private GetGroupsGroupIdAccountsMemberLoanStatus() {
                }

                @ApiModelProperty(example = "200")
                public Integer id;
                @ApiModelProperty(example = "loanStatusType.approved")
                public String code;
                @ApiModelProperty(example = "Approved")
                public String value;
                @ApiModelProperty(example = "false")
                public Boolean pendingApproval;
                @ApiModelProperty(example = "true")
                public Boolean waitingForDisbursal;
                @ApiModelProperty(example = "false")
                public Boolean active;
                @ApiModelProperty(example = "false")
                public Boolean closedObligationsMet;
                @ApiModelProperty(example = "false")
                public Boolean closedWrittenOff;
                @ApiModelProperty(example = "false")
                public Boolean closedRescheduled;
                @ApiModelProperty(example = "false")
                public Boolean closed;
                @ApiModelProperty(example = "false")
                public Boolean overpaid;
            }

            final class GetGroupsGroupIdAccountsMemberLoanType {
                private GetGroupsGroupIdAccountsMemberLoanType() {
                }

                @ApiModelProperty(example = "3")
                public Integer id;
                @ApiModelProperty(example = "accountType.jlg")
                public String code;
                @ApiModelProperty(example = "JLG")
                public String value;
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "000000004")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "testLoan")
            public String productName;
            public GetGroupsGroupIdAccountsMemberLoanStatus status;
            public GetGroupsGroupIdAccountsMemberLoanType loanType;
        }

        final class GetGroupsGroupIdAccountsMemberSavingsAccounts {
            private GetGroupsGroupIdAccountsMemberSavingsAccounts() {
            }


            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "000000003")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "p_sav")
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

    @ApiModel(value = "PostGroupsGroupIdRequest")
    public final static class PostGroupsGroupIdRequest {
        private PostGroupsGroupIdRequest() {
        }

        final class PostGroupsGroupIdClients {
            private PostGroupsGroupIdClients() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
        }

        @ApiModelProperty(example = "2")
        public Integer destinationGroupId;
        public Set<PostGroupsGroupIdClients> clients;
    }

    @ApiModel(value = "PostGroupsGroupIdResponse")
    public final static class PostGroupsGroupIdResponse {
        private PostGroupsGroupIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
