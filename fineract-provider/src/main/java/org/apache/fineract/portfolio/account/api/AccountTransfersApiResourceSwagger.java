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
package org.apache.fineract.portfolio.account.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/16/17.
 */
final class AccountTransfersApiResourceSwagger {

    private AccountTransfersApiResourceSwagger() {}

    @Schema(description = "GetAccountTransfersTemplateResponse")
    public static final class GetAccountTransfersTemplateResponse {

        private GetAccountTransfersTemplateResponse() {}

        static final class GetAccountTransfersFromOffice {

            private GetAccountTransfersFromOffice() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "HO")
            public String name;
            @Schema(example = "HO")
            public String nameDecorated;
            @Schema(example = "1")
            public Integer externalId;
            @Schema(example = "[2009, 1, 1]")
            public LocalDate openingDate;
            @Schema(example = ".")
            public String hierarchy;
        }

        static final class GetAccountTransfersFromAccountType {

            private GetAccountTransfersFromAccountType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        static final class GetAccountTransfersFromOfficeOptions {

            private GetAccountTransfersFromOfficeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "HO")
            public String name;
            @Schema(example = "HO")
            public String nameDecorated;
        }

        static final class GetAccountTransfersFromClientOptions {

            private GetAccountTransfersFromClientOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Small shop")
            public String displayName;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "HO")
            public String officeName;
        }

        static final class GetAccountTransfersFromAccountTypeOptions {

            private GetAccountTransfersFromAccountTypeOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        static final class GetAccountTransfersToOfficeOptions {

            private GetAccountTransfersToOfficeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "HO")
            public String name;
            @Schema(example = "HO")
            public String nameDecorated;
        }

        static final class GetAccountTransfersToAccountTypeOptions {

            private GetAccountTransfersToAccountTypeOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        @Schema(example = "0")
        public Long transferAmount;
        @Schema(example = "[2013, 8, 15]")
        public LocalDate transferDate;
        public GetAccountTransfersFromOffice fromOffice;
        public GetAccountTransfersFromAccountType fromAccountType;
        public Set<GetAccountTransfersFromOfficeOptions> fromOfficeOptions;
        public Set<GetAccountTransfersFromClientOptions> fromClientOptions;
        public Set<GetAccountTransfersFromAccountTypeOptions> fromAccountTypeOptions;
        public Set<GetAccountTransfersToOfficeOptions> toOfficeOptions;
        public Set<GetAccountTransfersToAccountTypeOptions> toAccountTypeOptions;
    }

    @Schema(description = "PostAccountTransfersRequest")
    public static final class PostAccountTransfersRequest {

        private PostAccountTransfersRequest() {}

        @Schema(example = "1")
        public Integer fromOfficeId;
        @Schema(example = "1")
        public Integer fromClientId;
        @Schema(example = "2")
        public Integer fromAccountType;
        @Schema(example = "1")
        public Integer fromAccountId;
        @Schema(example = "1")
        public Integer toOfficeId;
        @Schema(example = "1")
        public Integer toClientId;
        @Schema(example = "2")
        public Integer toAccountType;
        @Schema(example = "2")
        public Integer toAccountId;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "01 August 2011")
        public String transferDate;
        @Schema(example = "112.45")
        public Float transferAmount;
        @Schema(example = "A description of the transfer")
        public String transferDescription;
    }

    @Schema(description = "PostAccountTransfersResponse")
    public static final class PostAccountTransfersResponse {

        private PostAccountTransfersResponse() {}

        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetAccountTransfersResponse")
    public static final class GetAccountTransfersResponse {

        private GetAccountTransfersResponse() {}

        static final class GetAccountTransfersPageItems {

            private GetAccountTransfersPageItems() {}

            static final class GetAccountTransfersPageItemsCurrency {

                private GetAccountTransfersPageItemsCurrency() {}

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

            static final class GetAccountTransfersPageItemsFromOffice {

                private GetAccountTransfersPageItemsFromOffice() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "HO")
                public String name;
            }

            static final class GetAccountTransfersPageItemsFromAccount {

                private GetAccountTransfersPageItemsFromAccount() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "000000001")
                public Long accountNo;
            }

            static final class GetAccountTransfersPageItemsToAccountType {

                private GetAccountTransfersPageItemsToAccountType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "accountType.loan")
                public String code;
                @Schema(example = "Loan Account")
                public String description;
            }

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "false")
            public Boolean reversed;
            public GetAccountTransfersPageItemsCurrency currency;
            @Schema(example = "200")
            public Float transferAmount;
            @Schema(example = "[2013, 4, 1]")
            public LocalDate transferDate;
            @Schema(example = "pay off loan from savings.")
            public String transferDescription;
            public GetAccountTransfersPageItemsFromOffice fromOffice;
            public GetAccountTransfersTemplateResponse.GetAccountTransfersFromClientOptions fromClient;
            public GetAccountTransfersTemplateResponse.GetAccountTransfersFromAccountType fromAccountType;
            public GetAccountTransfersPageItemsFromAccount fromAccount;
            public GetAccountTransfersPageItemsFromOffice toOffice;
            public GetAccountTransfersTemplateResponse.GetAccountTransfersFromClientOptions toClient;
            public GetAccountTransfersPageItemsToAccountType toAccountType;
            public GetAccountTransfersPageItemsFromAccount toAccount;
        }

        @Schema(example = "4")
        public Integer totalFilteredRecords;
        public Set<GetAccountTransfersPageItems> pageItems;
    }

    @Schema(description = "GetAccountTransfersTemplateRefundByTransferResponse")
    public static final class GetAccountTransfersTemplateRefundByTransferResponse {

        private GetAccountTransfersTemplateRefundByTransferResponse() {}

        static final class GetAccountTransfersTemplateRefundByTransferCurrency {

            private GetAccountTransfersTemplateRefundByTransferCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "0")
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

        static final class GetAccountTransfersTemplateRefundByTransferFromOffice {

            private GetAccountTransfersTemplateRefundByTransferFromOffice() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
            @Schema(example = "1")
            public Integer externalId;
            @Schema(example = "[2009, 1, 1]")
            public LocalDate openingDate;
            @Schema(example = ".")
            public String hierarchy;
        }

        static final class GetAccountTransfersTemplateRefundByTransferFromClient {

            private GetAccountTransfersTemplateRefundByTransferFromClient() {}

            static final class GetAccountTransfersStatus {

                private GetAccountTransfersStatus() {}

                @Schema(example = "300")
                public Integer id;
                @Schema(example = "clientStatusType.active")
                public String code;
                @Schema(example = "Active")
                public String description;
            }

            static final class GetAccountTransfersGender {

                private GetAccountTransfersGender() {}
            }

            static final class GetAccountTransfersClientType {

                private GetAccountTransfersClientType() {}
            }

            static final class GetAccountTransfersClientClassification {

                private GetAccountTransfersClientClassification() {}
            }

            static final class GetAccountTransfersTimeline {

                private GetAccountTransfersTimeline() {}

                @Schema(example = "[2012, 2, 1]")
                public LocalDate submittedOnDate;
                @Schema(example = "mifos")
                public String submittedByUsername;
                @Schema(example = "App")
                public String submittedByFirstname;
                @Schema(example = "Administrator")
                public String submittedByLastname;
                @Schema(example = "[2012, 2, 1]")
                public LocalDate activatedOnDate;
                @Schema(example = "mifos")
                public String activatedByUsername;
                @Schema(example = "App")
                public String activatedByFirstname;
                @Schema(example = "Administrator")
                public String activatedByLastname;
            }

            static final class GetAccountTransfersGroups {

                private GetAccountTransfersGroups() {}
            }

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "000000001")
            public Long accountNo;
            public GetAccountTransfersStatus status;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "[2012, 2, 1]")
            public LocalDate activationDate;
            @Schema(example = "Daniel")
            public String firstname;
            @Schema(example = "Owusu")
            public String lastname;
            @Schema(example = "Daniel Owusu")
            public String displayName;
            public GetAccountTransfersGender gender;
            public GetAccountTransfersClientType clientType;
            public GetAccountTransfersClientClassification clientClassification;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
            public GetAccountTransfersTimeline timeline;
            public GetAccountTransfersGroups groups;
        }

        static final class GetAccountTransfersTemplateRefundByTransferFromAccount {

            private GetAccountTransfersTemplateRefundByTransferFromAccount() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "000000002")
            public Long accountNo;
            @Schema(example = "1")
            public Integer clientId;
            @Schema(example = "Daniel Owusu")
            public String clientName;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "CTRL")
            public String productName;
            @Schema(example = "0")
            public Integer fieldOfficerId;
            public GetAccountTransfersTemplateRefundByTransferCurrency currency;
            @Schema(example = "130")
            public Float amtForTransfer;
        }

        static final class GetAccountTransfersTemplateRefundByTransferToClient {

            private GetAccountTransfersTemplateRefundByTransferToClient() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Daniel Owusu")
            public String displayName;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
        }

        static final class GetAccountTransfersTemplateRefundByTransferToAccount {

            private GetAccountTransfersTemplateRefundByTransferToAccount() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "000000001")
            public Long accountNo;
            @Schema(example = "1")
            public Integer clientId;
            @Schema(example = "Daniel Owusu")
            public String clientName;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "TEST")
            public String productName;
            @Schema(example = "0")
            public Integer fieldOfficerId;
            public GetAccountTransfersTemplateRefundByTransferCurrency currency;
        }

        static final class GetAccountTransfersTemplateRefundByTransferFromOfficeOptions {

            private GetAccountTransfersTemplateRefundByTransferFromOfficeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
        }

        static final class GetAccountTransfersTemplateRefundByTransferFromClientOptions {

            private GetAccountTransfersTemplateRefundByTransferFromClientOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Daniel Owusu")
            public String displayName;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
        }

        static final class GetAccountTransfersTemplateRefundByTransferFromAccountOptions {

            private GetAccountTransfersTemplateRefundByTransferFromAccountOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "000000002")
            public Long accountNo;
            @Schema(example = "1")
            public Integer clientId;
            @Schema(example = "Daniel Owusu")
            public String clientName;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "CTRL")
            public String productName;
            @Schema(example = "0")
            public Integer fieldOfficerId;
            public GetAccountTransfersTemplateRefundByTransferCurrency currency;
        }

        public GetAccountTransfersTemplateRefundByTransferCurrency currency;
        @Schema(example = "130")
        public Float transferAmount;
        @Schema(example = "[2014, 11, 1]")
        public LocalDate transferDate;
        public GetAccountTransfersTemplateRefundByTransferFromOffice fromOffice;
        public GetAccountTransfersTemplateRefundByTransferFromClient fromClient;
        public GetAccountTransfersResponse.GetAccountTransfersPageItems.GetAccountTransfersPageItemsToAccountType fromAccountType;
        public GetAccountTransfersTemplateRefundByTransferFromAccount fromAccount;
        public GetAccountTransfersTemplateRefundByTransferFromOffice toOffice;
        public GetAccountTransfersTemplateRefundByTransferToClient toClient;
        public GetAccountTransfersTemplateResponse.GetAccountTransfersFromAccountType toAccountType;
        public GetAccountTransfersTemplateRefundByTransferToAccount toAccount;
        public Set<GetAccountTransfersTemplateRefundByTransferFromOfficeOptions> fromOfficeOptions;
        public Set<GetAccountTransfersTemplateRefundByTransferFromClientOptions> fromClientOptions;
        public Set<GetAccountTransfersTemplateResponse.GetAccountTransfersFromAccountType> fromAccountTypeOptions;
        public Set<GetAccountTransfersTemplateRefundByTransferFromAccountOptions> fromAccountOptions;
        public Set<GetAccountTransfersTemplateRefundByTransferFromOfficeOptions> toOfficeOptions;
        public Set<GetAccountTransfersTemplateRefundByTransferFromClientOptions> toClientOptions;
        public Set<GetAccountTransfersTemplateResponse.GetAccountTransfersFromAccountType> toAccountTypeOptions;
        public Set<GetAccountTransfersTemplateRefundByTransferToAccount> toAccountOptions;
    }

    @Schema(description = "PostAccountTransfersRefundByTransferRequest")
    public static final class PostAccountTransfersRefundByTransferRequest {

        private PostAccountTransfersRefundByTransferRequest() {}

        @Schema(example = "2")
        public Integer fromAccountId;
        @Schema(example = "1")
        public Integer fromAccountType;
        @Schema(example = "1")
        public Integer toOfficeId;
        @Schema(example = "1")
        public Integer toClientId;
        @Schema(example = "2")
        public Integer toAccountType;
        @Schema(example = "1")
        public Integer toAccountId;
        @Schema(example = "130")
        public Float transferAmount;
        @Schema(example = "31 October 2014")
        public String transferDate;
        @Schema(example = "Transfer refund to my savings account")
        public String transferDescription;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "1")
        public Integer fromClientId;
        @Schema(example = "1")
        public Integer fromOfficeId;
    }

    @Schema(description = "PostAccountTransfersRefundByTransferResponse")
    public static final class PostAccountTransfersRefundByTransferResponse {

        private PostAccountTransfersRefundByTransferResponse() {}

        @Schema(example = "1")
        public Integer savingsId;
        @Schema(example = "1")
        public Integer resourceId;
    }
}
