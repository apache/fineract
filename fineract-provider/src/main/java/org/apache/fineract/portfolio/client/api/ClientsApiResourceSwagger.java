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
package org.apache.fineract.portfolio.client.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 01/13/18.
 */
final class ClientsApiResourceSwagger {
    private ClientsApiResourceSwagger() {
    }

    @ApiModel(value = "GetClientsTemplateResponse")
    public final static class GetClientsTemplateResponse {
        private GetClientsTemplateResponse() {
        }

        final class GetClientsOfficeOptions {
            private GetClientsOfficeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String nameDecorated;
        }

        final class GetClientsStaffOptions {
            private GetClientsStaffOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "xyz")
            public String firstname;
            @ApiModelProperty(example = "sjs")
            public String lastname;
            @ApiModelProperty(example = "sjs, xyz")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
            @ApiModelProperty(example = "true")
            public Boolean isLoanOfficer;
            @ApiModelProperty(example = "true")
            public Boolean isActive;
        }

        final class GetClientsSavingProductOptions {
            private GetClientsSavingProductOptions() {
            }

            @ApiModelProperty(example = "4")
            public Integer id;
            @ApiModelProperty(example = "account overdraft")
            public String name;
            @ApiModelProperty(example = "false")
            public Boolean withdrawalFeeForTransfers;
            @ApiModelProperty(example = "false")
            public Boolean allowOverdraft;
        }

        final class GetClientsDataTables {
            private GetClientsDataTables() {
            }

            final class GetClientsColumnHeaderData {
                private GetClientsColumnHeaderData() {
                }

                @ApiModelProperty(example = "client_id")
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
                @ApiModelProperty(example = "[]")
                public List<String> columnValues;
            }

            @ApiModelProperty(example = "m_client")
            public String applicationTableName;
            @ApiModelProperty(example = "Address Details")
            public String registeredTableName;
            public Set<GetClientsColumnHeaderData> columnHeaderData;
        }

        @ApiModelProperty(example = "[2014, 3, 4]")
        public LocalDate activationDate;
        @ApiModelProperty(example = "1")
        public Integer officeId;
        public Set<GetClientsOfficeOptions> officeOptions;
        public Set<GetClientsStaffOptions> staffOptions;
        public Set<GetClientsSavingProductOptions> savingProductOptions;
        public Set<GetClientsDataTables> datatables;
    }

    @ApiModel(value = "GetClientsResponse")
    public final static class GetClientsResponse {
        private GetClientsResponse() {
        }

        final class GetClientsPageItemsResponse {
            private GetClientsPageItemsResponse() {
            }

            final class GetClientStatus {
                private GetClientStatus() {
                }

                @ApiModelProperty(example = "100")
                public Integer id;
                @ApiModelProperty(example = "clientStatusType.pending")
                public String code;
                @ApiModelProperty(example = "Pending")
                public String value;
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "000000002")
            public String accountNo;
            public GetClientStatus status;
            @ApiModelProperty(example = "false")
            public Boolean active;
            @ApiModelProperty(example = "Home Farm Produce")
            public String fullname;
            @ApiModelProperty(example = "Home Farm Produce")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
        }

        @ApiModelProperty(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetClientsPageItemsResponse> pageItems;
    }

    @ApiModel(value = "GetClientsClientIdResponse")
    public final static class GetClientsClientIdResponse {
        private GetClientsClientIdResponse() {
        }

        final class GetClientsClientIdStatus {
            private GetClientsClientIdStatus() {
            }

            @ApiModelProperty(example = "300")
            public Integer id;
            @ApiModelProperty(example = "clientStatusType.active")
            public String code;
            @ApiModelProperty(example = "Active")
            public String value;
        }

        final class GetClientsTimeline {
            private GetClientsTimeline() {
            }

            @ApiModelProperty(example = "[2013, 1, 1]")
            public LocalDate submittedOnDate;
            @ApiModelProperty(example = "mifos")
            public String submittedByUsername;
            @ApiModelProperty(example = "App")
            public String submittedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String submittedByLastname;
            @ApiModelProperty(example = "[2013, 1, 1]")
            public LocalDate activatedOnDate;
            @ApiModelProperty(example = "mifos")
            public String activatedByUsername;
            @ApiModelProperty(example = "App")
            public String activatedByFirstname;
            @ApiModelProperty(example = "Administrator")
            public String activatedByLastname;

        }

        @ApiModelProperty(example = "27")
        public Integer id;
        @ApiModelProperty(example = "000000027")
        public Long accountNo;
        public GetClientsClientIdStatus status;
        @ApiModelProperty(example = "true")
        public Boolean active;
        @ApiModelProperty(example = "[2013, 1, 1]")
        public LocalDate activationDate;
        @ApiModelProperty(example = "savings")
        public String firstname;
        @ApiModelProperty(example = "test")
        public String lastname;
        @ApiModelProperty(example = "savings test")
        public String displayName;
        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        public GetClientsTimeline timeline;
        @ApiModelProperty(example = "4")
        public Integer savingsProductId;
        @ApiModelProperty(example = "account overdraft")
        public String savingsProductName;
        @ApiModelProperty(example = "[]")
        public List<String> groups;
    }

    @ApiModel(value = "PostClientsRequest")
    public final static class PostClientsRequest {
        private PostClientsRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Client of group")
        public String fullname;
        @ApiModelProperty(example = "1")
        public Integer groupId;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "true")
        public Boolean active;
        @ApiModelProperty(example = "04 March 2009")
        public String activationDate;
    }

    @ApiModel(value = "PostClientsResponse")
    public final static class PostClientsResponse {
        private PostClientsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer groupId;
        @ApiModelProperty(example = "2")
        public Integer clientId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
    }

    @ApiModel(value = "PutClientsClientIdRequest")
    public final static class PutClientsClientIdRequest {
        private PutClientsClientIdRequest() {
        }

        @ApiModelProperty(example = "786444UUUYYH7")
        public String externalId;
    }

    @ApiModel(value = "PutClientsClientIdResponse")
    public final static class PutClientsClientIdResponse {
        private PutClientsClientIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutClientsClientIdRequest changes;
    }

    @ApiModel(value = "DeleteClientsClientIdRequest")
    public final static class DeleteClientsClientIdRequest {
        private DeleteClientsClientIdRequest() {
        }
    }

    @ApiModel(value = "DeleteClientsClientIdResponse")
    public final static class DeleteClientsClientIdResponse {
        private DeleteClientsClientIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "3")
        public Integer clientId;
        @ApiModelProperty(example = "3")
        public Integer resourceId;
    }

    @ApiModel(value = "PostClientsClientIdRequest")
    public final static class PostClientsClientIdRequest {
        private PostClientsClientIdRequest() {
        }

        @ApiModelProperty(example = "We cannot accept transfers of clients having loans with less than 1 repayment left")
        public String note;
    }

    @ApiModel(value = "PostClientsClientIdResponse")
    public final static class PostClientsClientIdResponse {
        private PostClientsClientIdResponse() {
        }

        @ApiModelProperty(example = "2")
        public Integer clientId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
    }

    @ApiModel(value = "GetClientsClientIdAccountsResponse")
    public final static class GetClientsClientIdAccountsResponse {
        private GetClientsClientIdAccountsResponse() {
        }

        final class GetClientsLoanAccounts {
            private GetClientsLoanAccounts() {
            }

            final class GetClientsLoanAccountsStatus {
                private GetClientsLoanAccountsStatus() {
                }

                @ApiModelProperty(example = "300")
                public Integer id;
                @ApiModelProperty(example = "loanStatusType.active")
                public String code;
                @ApiModelProperty(example = "Active")
                public String value;
                @ApiModelProperty(example = "false")
                public Boolean pendingApproval;
                @ApiModelProperty(example = "false")
                public Boolean waitingForDisbursal;
                @ApiModelProperty(example = "true")
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

            final class GetClientsLoanAccountsType {
                private GetClientsLoanAccountsType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "loanType.individual")
                public String code;
                @ApiModelProperty(example = "Individual")
                public String value;
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "000000001")
            public Long accountNo;
            @ApiModelProperty(example = "456")
            public Integer externalId;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "TestOne")
            public String productName;
            public GetClientsLoanAccountsStatus status;
            public GetClientsLoanAccountsType loanType;
            @ApiModelProperty(example = "1")
            public Integer loanCycle;
        }

        final class GetClientsSavingsAccounts {
            private GetClientsSavingsAccounts() {
            }

            final class GetClientsSavingsAccountsCurrency {
                private GetClientsSavingsAccountsCurrency() {
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

            final class GetClientsSavingsAccountsStatus {
                private GetClientsSavingsAccountsStatus() {
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

            @ApiModelProperty(example = "7")
            public Integer id;
            @ApiModelProperty(example = "000000007")
            public Long accountNo;
            @ApiModelProperty(example = "2")
            public Integer productId;
            @ApiModelProperty(example = "Other product")
            public String productName;
            public GetClientsSavingsAccountsStatus status;
            public GetClientsSavingsAccountsCurrency currency;
        }

        public Set<GetClientsLoanAccounts> loanAccounts;
        public Set<GetClientsSavingsAccounts> savingsAccounts;
    }
}
