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
package org.apache.fineract.portfolio.account.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/16/17.
 */
final class AccountTransfersApiResourceSwagger {
    private AccountTransfersApiResourceSwagger() {
    }

    @ApiModel(value = "GetAccountTransfersTemplateResponse")
    public final static class GetAccountTransfersTemplateResponse {
        private GetAccountTransfersTemplateResponse() {
        }

        final class GetAccountTransfersFromOffice {
            private GetAccountTransfersFromOffice() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "HO")
            public String name;
            @ApiModelProperty(example = "HO")
            public String nameDecorated;
            @ApiModelProperty(example = "1")
            public Integer externalId;
            @ApiModelProperty(example = "[2009, 1, 1]")
            public LocalDate openingDate;
            @ApiModelProperty(example = ".")
            public String hierarchy;
        }

        final class GetAccountTransfersFromAccountType {
            private GetAccountTransfersFromAccountType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        final class GetAccountTransfersFromOfficeOptions {
            private GetAccountTransfersFromOfficeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "HO")
            public String name;
            @ApiModelProperty(example = "HO")
            public String nameDecorated;
        }

        final class GetAccountTransfersFromClientOptions {
            private GetAccountTransfersFromClientOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Small shop")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "HO")
            public String officeName;
        }

        final class GetAccountTransfersFromAccountTypeOptions {
            private GetAccountTransfersFromAccountTypeOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        final class GetAccountTransfersToOfficeOptions {
            private GetAccountTransfersToOfficeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "HO")
            public String name;
            @ApiModelProperty(example = "HO")
            public String nameDecorated;
        }

        final class GetAccountTransfersToAccountTypeOptions {
            private GetAccountTransfersToAccountTypeOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        @ApiModelProperty(example = "0")
        public Long transferAmount;
        @ApiModelProperty(example = "[2013, 8, 15]")
        public LocalDate transferDate;
        public GetAccountTransfersFromOffice fromOffice;
        public GetAccountTransfersFromAccountType fromAccountType;
        public Set<GetAccountTransfersFromOfficeOptions> fromOfficeOptions;
        public Set<GetAccountTransfersFromClientOptions> fromClientOptions;
        public Set<GetAccountTransfersFromAccountTypeOptions> fromAccountTypeOptions;
        public Set<GetAccountTransfersToOfficeOptions> toOfficeOptions;
        public Set<GetAccountTransfersToAccountTypeOptions> toAccountTypeOptions;
    }

    @ApiModel(value = "PostAccountTransfersRequest")
    public final static class PostAccountTransfersRequest {
        private PostAccountTransfersRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer fromOfficeId;
        @ApiModelProperty(example = "1")
        public Integer fromClientId;
        @ApiModelProperty(example = "2")
        public Integer fromAccountType;
        @ApiModelProperty(example = "1")
        public Integer fromAccountId;
        @ApiModelProperty(example = "1")
        public Integer toOfficeId;
        @ApiModelProperty(example = "1")
        public Integer toClientId;
        @ApiModelProperty(example = "2")
        public Integer toAccountType;
        @ApiModelProperty(example = "2")
        public Integer toAccountId;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "01 August 2011")
        public String transferDate;
        @ApiModelProperty(example = "112.45")
        public Float transferAmount;
        @ApiModelProperty(example = "A description of the transfer")
        public String transferDescription;
    }

    @ApiModel(value = "PostAccountTransfersResponse")
    public final static class PostAccountTransfersResponse {
        private PostAccountTransfersResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "GetAccountTransfersResponse")
    public final static class GetAccountTransfersResponse {
        private GetAccountTransfersResponse() {
        }

        final class GetAccountTransfersPageItems {
            private GetAccountTransfersPageItems() {
            }

            final class GetAccountTransfersPageItemsCurrency {
                private GetAccountTransfersPageItemsCurrency() {
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

            final class GetAccountTransfersPageItemsFromOffice {
                private GetAccountTransfersPageItemsFromOffice() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "HO")
                public String name;
            }

            final class GetAccountTransfersPageItemsFromAccount {
                private GetAccountTransfersPageItemsFromAccount() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "000000001")
                public Long accountNo;
            }

            final class GetAccountTransfersPageItemsToAccountType {
                private GetAccountTransfersPageItemsToAccountType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "accountType.loan")
                public String code;
                @ApiModelProperty(example = "Loan Account")
                public String value;
            }


            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "false")
            public Boolean reversed;
            public GetAccountTransfersPageItemsCurrency currency;
            @ApiModelProperty(example = "200")
            public Float transferAmount;
            @ApiModelProperty(example = "[2013, 4, 1]")
            public LocalDate transferDate;
            @ApiModelProperty(example = "pay off loan from savings.")
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

        @ApiModelProperty(example = "4")
        public Integer totalFilteredRecords;
        public Set<GetAccountTransfersPageItems> pageItems;
    }

    @ApiModel(value = "GetAccountTransfersTemplateRefundByTransferResponse")
    public static final class GetAccountTransfersTemplateRefundByTransferResponse {
        private GetAccountTransfersTemplateRefundByTransferResponse() {
        }

        final class GetAccountTransfersTemplateRefundByTransferCurrency {
            private GetAccountTransfersTemplateRefundByTransferCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "0")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "0")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        final class GetAccountTransfersTemplateRefundByTransferFromOffice {
            private GetAccountTransfersTemplateRefundByTransferFromOffice() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String nameDecorated;
            @ApiModelProperty(example = "1")
            public Integer externalId;
            @ApiModelProperty(example = "[2009, 1, 1]")
            public LocalDate openingDate;
            @ApiModelProperty(example = ".")
            public String hierarchy;
        }

        final class GetAccountTransfersTemplateRefundByTransferFromClient {
            private GetAccountTransfersTemplateRefundByTransferFromClient() {
            }

            final class GetAccountTransfersStatus {
                private GetAccountTransfersStatus() {
                }

                @ApiModelProperty(example = "300")
                public Integer id;
                @ApiModelProperty(example = "clientStatusType.active")
                public String code;
                @ApiModelProperty(example = "Active")
                public String value;
            }

            final class GetAccountTransfersGender {
                private GetAccountTransfersGender() {
                }
            }

            final class GetAccountTransfersClientType {
                private GetAccountTransfersClientType() {
                }
            }

            final class GetAccountTransfersClientClassification {
                private GetAccountTransfersClientClassification() {
                }
            }

            final class GetAccountTransfersTimeline {
                private GetAccountTransfersTimeline() {
                }

                @ApiModelProperty(example = "[2012, 2, 1]")
                public LocalDate submittedOnDate;
                @ApiModelProperty(example = "mifos")
                public String submittedByUsername;
                @ApiModelProperty(example = "App")
                public String submittedByFirstname;
                @ApiModelProperty(example = "Administrator")
                public String submittedByLastname;
                @ApiModelProperty(example = "[2012, 2, 1]")
                public LocalDate activatedOnDate;
                @ApiModelProperty(example = "mifos")
                public String activatedByUsername;
                @ApiModelProperty(example = "App")
                public String activatedByFirstname;
                @ApiModelProperty(example = "Administrator")
                public String activatedByLastname;
            }

            final class GetAccountTransfersGroups {
                private GetAccountTransfersGroups() {
                }
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "000000001")
            public Long accountNo;
            public GetAccountTransfersStatus status;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "[2012, 2, 1]")
            public LocalDate activationDate;
            @ApiModelProperty(example = "Daniel")
            public String firstname;
            @ApiModelProperty(example = "Owusu")
            public String lastname;
            @ApiModelProperty(example = "Daniel Owusu")
            public String displayName;
            public GetAccountTransfersGender gender;
            public GetAccountTransfersClientType clientType;
            public GetAccountTransfersClientClassification clientClassification;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
            public GetAccountTransfersTimeline timeline;
            public GetAccountTransfersGroups groups;
        }

        final class GetAccountTransfersTemplateRefundByTransferFromAccount {
            private GetAccountTransfersTemplateRefundByTransferFromAccount() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "000000002")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "Daniel Owusu")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "CTRL")
            public String productName;
            @ApiModelProperty(example = "0")
            public Integer fieldOfficerId;
            public GetAccountTransfersTemplateRefundByTransferCurrency currency;
            @ApiModelProperty(example = "130")
            public Float amtForTransfer;
        }

        final class GetAccountTransfersTemplateRefundByTransferToClient {
            private GetAccountTransfersTemplateRefundByTransferToClient() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Daniel Owusu")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
        }

        final class GetAccountTransfersTemplateRefundByTransferToAccount {
            private GetAccountTransfersTemplateRefundByTransferToAccount() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "000000001")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "Daniel Owusu")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "TEST")
            public String productName;
            @ApiModelProperty(example = "0")
            public Integer fieldOfficerId;
            public GetAccountTransfersTemplateRefundByTransferCurrency currency;
        }

        final class GetAccountTransfersTemplateRefundByTransferFromOfficeOptions {
            private GetAccountTransfersTemplateRefundByTransferFromOfficeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String nameDecorated;
        }

        final class GetAccountTransfersTemplateRefundByTransferFromClientOptions {
            private GetAccountTransfersTemplateRefundByTransferFromClientOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Daniel Owusu")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
        }

        final class GetAccountTransfersTemplateRefundByTransferFromAccountOptions {
            private GetAccountTransfersTemplateRefundByTransferFromAccountOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "000000002")
            public Long accountNo;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "Daniel Owusu")
            public String clientName;
            @ApiModelProperty(example = "1")
            public Integer productId;
            @ApiModelProperty(example = "CTRL")
            public String productName;
            @ApiModelProperty(example = "0")
            public Integer fieldOfficerId;
            public GetAccountTransfersTemplateRefundByTransferCurrency currency;
        }

        public GetAccountTransfersTemplateRefundByTransferCurrency currency;
        @ApiModelProperty(example = "130")
        public Float transferAmount;
        @ApiModelProperty(example = "[2014, 11, 1]")
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

    @ApiModel(value = "PostAccountTransfersRefundByTransferRequest")
    public static final class PostAccountTransfersRefundByTransferRequest {
        private PostAccountTransfersRefundByTransferRequest() {
        }

        @ApiModelProperty(example = "2")
        public Integer fromAccountId;
        @ApiModelProperty(example = "1")
        public Integer fromAccountType;
        @ApiModelProperty(example = "1")
        public Integer toOfficeId;
        @ApiModelProperty(example = "1")
        public Integer toClientId;
        @ApiModelProperty(example = "2")
        public Integer toAccountType;
        @ApiModelProperty(example = "1")
        public Integer toAccountId;
        @ApiModelProperty(example = "130")
        public Float transferAmount;
        @ApiModelProperty(example = "31 October 2014")
        public String transferDate;
        @ApiModelProperty(example = "Transfer refund to my savings account")
        public String transferDescription;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "1")
        public Integer fromClientId;
        @ApiModelProperty(example = "1")
        public Integer fromOfficeId;
    }

    @ApiModel(value = "PostAccountTransfersRefundByTransferResponse")
    public final static class PostAccountTransfersRefundByTransferResponse {
        private PostAccountTransfersRefundByTransferResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
