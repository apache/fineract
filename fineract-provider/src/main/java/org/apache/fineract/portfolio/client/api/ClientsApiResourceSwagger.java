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
package org.apache.fineract.portfolio.client.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 01/13/18.
 */
final class ClientsApiResourceSwagger {

    private ClientsApiResourceSwagger() {}

    @Schema(description = "GetClientsTemplateResponse")
    public static final class GetClientsTemplateResponse {

        private GetClientsTemplateResponse() {}

        static final class GetClientsOfficeOptions {

            private GetClientsOfficeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
        }

        static final class GetClientsStaffOptions {

            private GetClientsStaffOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "xyz")
            public String firstname;
            @Schema(example = "sjs")
            public String lastname;
            @Schema(example = "sjs, xyz")
            public String displayName;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
            @Schema(example = "true")
            public Boolean isLoanOfficer;
            @Schema(example = "true")
            public Boolean isActive;
        }

        static final class GetClientsSavingProductOptions {

            private GetClientsSavingProductOptions() {}

            @Schema(example = "4")
            public Integer id;
            @Schema(example = "account overdraft")
            public String name;
            @Schema(example = "false")
            public Boolean withdrawalFeeForTransfers;
            @Schema(example = "false")
            public Boolean allowOverdraft;
        }

        static final class GetClientsDataTables {

            private GetClientsDataTables() {}

            static final class GetClientsColumnHeaderData {

                private GetClientsColumnHeaderData() {}

                @Schema(example = "client_id")
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
                @Schema(example = "[]")
                public List<String> columnValues;
            }

            @Schema(example = "m_client")
            public String applicationTableName;
            @Schema(example = "Address Details")
            public String registeredTableName;
            public Set<GetClientsColumnHeaderData> columnHeaderData;
        }

        @Schema(example = "[2014, 3, 4]")
        public LocalDate activationDate;
        @Schema(example = "1")
        public Integer officeId;
        public Set<GetClientsOfficeOptions> officeOptions;
        public Set<GetClientsStaffOptions> staffOptions;
        public Set<GetClientsSavingProductOptions> savingProductOptions;
        public Set<GetClientsDataTables> datatables;
    }

    @Schema(description = "GetClientsResponse")
    public static final class GetClientsResponse {

        private GetClientsResponse() {}

        static final class GetClientsPageItemsResponse {

            private GetClientsPageItemsResponse() {}

            static final class GetClientStatus {

                private GetClientStatus() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "clientStatusType.pending")
                public String code;
                @Schema(example = "Pending")
                public String description;
            }

            @Schema(example = "2")
            public Long id;
            @Schema(example = "000000002")
            public String accountNo;
            public GetClientStatus status;
            @Schema(example = "false")
            public Boolean active;
            @Schema(example = "Home Farm Produce")
            public String fullname;
            @Schema(example = "Home Farm Produce")
            public String displayName;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
        }

        @Schema(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetClientsPageItemsResponse> pageItems;
    }

    @Schema(description = "GetClientsClientIdResponse")
    public static final class GetClientsClientIdResponse {

        private GetClientsClientIdResponse() {}

        static final class GetClientsClientIdStatus {

            private GetClientsClientIdStatus() {}

            @Schema(example = "300")
            public Integer id;
            @Schema(example = "clientStatusType.active")
            public String code;
            @Schema(example = "Active")
            public String description;
        }

        static final class GetClientsTimeline {

            private GetClientsTimeline() {}

            @Schema(example = "[2013, 1, 1]")
            public LocalDate submittedOnDate;
            @Schema(example = "mifos")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
            @Schema(example = "[2013, 1, 1]")
            public LocalDate activatedOnDate;
            @Schema(example = "mifos")
            public String activatedByUsername;
            @Schema(example = "App")
            public String activatedByFirstname;
            @Schema(example = "Administrator")
            public String activatedByLastname;

        }

        static final class GetClientsGroups {

            private GetClientsGroups() {}

            @Schema(example = "000000001")
            public Long id;
            @Schema(example = "000000002")
            public String accountNo;
            @Schema(example = "Group name")
            public String name;
            @Schema(example = "000000003")
            public Long externalId;
        }

        @Schema(example = "27")
        public Integer id;
        @Schema(example = "000000027")
        public String accountNo;
        public GetClientsClientIdStatus status;
        @Schema(example = "true")
        public Boolean active;
        @Schema(example = "[2013, 1, 1]")
        public LocalDate activationDate;
        @Schema(example = "savings")
        public String firstname;
        @Schema(example = "test")
        public String lastname;
        @Schema(example = "savings test")
        public String displayName;
        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "Head Office")
        public String officeName;
        public GetClientsTimeline timeline;
        @Schema(example = "4")
        public Integer savingsProductId;
        @Schema(example = "account overdraft")
        public String savingsProductName;
        @Schema(example = "[]")
        public List<GetClientsGroups> groups;
    }

    @Schema(description = "PostClientsRequest")
    public static final class PostClientsRequest {

        private PostClientsRequest() {}

        static final class PostClientsDatatable {

            private PostClientsDatatable() {}

            @Schema(example = "Client Beneficiary information")
            public String registeredTableName;
            @Schema(example = "data")
            public HashMap<String, Object> data;

        }

        static final class PostClientsAddressRequest {

            @Schema(example = "Ipca")
            public String street;
            @Schema(example = "Kandivali")
            public String addressLine1;
            @Schema(example = "plot47")
            public String addressLine2;
            @Schema(example = "charkop")
            public String addressLine3;
            @Schema(example = "Mumbai")
            public String city;
            @Schema(example = "800")
            public Integer stateProvinceId;
            @Schema(example = "802")
            public Integer countryId;
            @Schema(example = "400064")
            public Long postalCode;
            @Schema(example = "1")
            public Long addressTypeId;
            @Schema(example = "true")
            public Boolean isActive;
        }

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer legalFormId;
        @Schema(example = "Client of group")
        public String fullname;
        @Schema(example = "Client_FirstName")
        public String firstname;
        @Schema(example = "123")
        public String externalId;
        @Schema(example = "Client_LastName")
        public String lastname;
        @Schema(example = "1")
        public Integer groupId;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "true")
        public Boolean active;
        @Schema(example = "04 March 2009")
        public String activationDate;
        @Schema(description = "List of PostClientsDatatable")
        public List<PostClientsDatatable> datatables;
        @Schema(description = "Address requests")
        public List<PostClientsAddressRequest> address;

    }

    @Schema(description = "PostClientsResponse")
    public static final class PostClientsResponse {

        private PostClientsResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer groupId;
        @Schema(example = "2")
        public Long clientId;
        @Schema(example = "2")
        public Integer resourceId;
    }

    @Schema(description = "PutClientsClientIdRequest")
    public static final class PutClientsClientIdRequest {

        private PutClientsClientIdRequest() {}

        @Schema(example = "786444UUUYYH7")
        public String externalId;
    }

    @Schema(description = "PutClientsClientIdResponse")
    public static final class PutClientsClientIdResponse {

        private PutClientsClientIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "1")
        public Integer resourceId;
        public PutClientsClientIdRequest changes;
    }

    @Schema(description = "DeleteClientsClientIdRequest")
    public static final class DeleteClientsClientIdRequest {

        private DeleteClientsClientIdRequest() {}
    }

    @Schema(description = "DeleteClientsClientIdResponse")
    public static final class DeleteClientsClientIdResponse {

        private DeleteClientsClientIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "3")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
    }

    @Schema(description = "PostClientsClientIdRequest")
    public static final class PostClientsClientIdRequest {

        private PostClientsClientIdRequest() {}

        @Schema(example = "03 August 2021")
        public String activationDate;
        @Schema(example = "dd MMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostClientsClientIdResponse")
    public static final class PostClientsClientIdResponse {

        private PostClientsClientIdResponse() {}

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "2")
        public Integer clientId;
        @Schema(example = "2")
        public Integer resourceId;
    }

    @Schema(description = "GetClientsClientIdAccountsResponse")
    public static final class GetClientsClientIdAccountsResponse {

        private GetClientsClientIdAccountsResponse() {}

        static final class GetClientsLoanAccounts {

            private GetClientsLoanAccounts() {}

            static final class GetClientsLoanAccountsStatus {

                private GetClientsLoanAccountsStatus() {}

                @Schema(example = "300")
                public Integer id;
                @Schema(example = "loanStatusType.active")
                public String code;
                @Schema(example = "Active")
                public String description;
                @Schema(example = "false")
                public Boolean pendingApproval;
                @Schema(example = "false")
                public Boolean waitingForDisbursal;
                @Schema(example = "true")
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

            static final class GetClientsLoanAccountsType {

                private GetClientsLoanAccountsType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "loanType.individual")
                public String code;
                @Schema(example = "Individual")
                public String description;
            }

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "000000001")
            public String accountNo;
            @Schema(example = "456")
            public Integer externalId;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "TestOne")
            public String productName;
            public GetClientsLoanAccountsStatus status;
            public GetClientsLoanAccountsType loanType;
            @Schema(example = "1")
            public Integer loanCycle;
        }

        static final class GetClientsSavingsAccounts {

            private GetClientsSavingsAccounts() {}

            static final class GetClientsSavingsAccountsCurrency {

                private GetClientsSavingsAccountsCurrency() {}

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

            static final class GetClientsSavingsAccountsStatus {

                private GetClientsSavingsAccountsStatus() {}

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
                @Schema(example = "false")
                public Boolean prematureClosed;
                @Schema(example = "false")
                public Boolean transferInProgress;
                @Schema(example = "false")
                public Boolean transferOnHold;
                @Schema(example = "false")
                public Boolean matured;
            }

            static final class GetClientsSavingsAccountsDepositType {

                private GetClientsSavingsAccountsDepositType() {}

                @Schema(example = "100")
                public Integer id;
                @Schema(example = "depositAccountType.savingsDeposit")
                public String code;
                @Schema(example = "Savings")
                public String value;
            }

            @Schema(example = "7")
            public Integer id;
            @Schema(example = "000000007")
            public String accountNo;
            @Schema(example = "2")
            public Integer productId;
            @Schema(example = "Other product")
            public String productName;
            @Schema(example = "OP")
            public String shortProductName;
            public GetClientsSavingsAccountsStatus status;
            public GetClientsSavingsAccountsCurrency currency;
            public GetClientsSavingsAccountsDepositType depositType;
        }

        public Set<GetClientsLoanAccounts> loanAccounts;
        public Set<GetClientsSavingsAccounts> savingsAccounts;
    }
}
