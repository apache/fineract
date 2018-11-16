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
package org.apache.fineract.portfolio.self.client.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/21/17.
 */

final class SelfClientsApiResourceSwagger {
    private SelfClientsApiResourceSwagger() {
    }

    @ApiModel(value = "GetSelfClientsResponse")
    public final static class GetSelfClientsResponse {
        private GetSelfClientsResponse() {
        }

        final class GetSelfClientsPageItems {
            private GetSelfClientsPageItems() {
            }

            final class GetSelfClientsStatus {
                private GetSelfClientsStatus() {
                }

                @ApiModelProperty(example = "300")
                public Integer id;
                @ApiModelProperty(example = "clientStatusType.active")
                public String code;
                @ApiModelProperty(example = "Active")
                public String value;
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "000000001")
            public Long accountNo;
            public GetSelfClientsStatus status;
            @ApiModelProperty(example = "true")
            public Boolean active;
            @ApiModelProperty(example = "[2013, 3, 1]")
            public LocalDate activationDate;
            @ApiModelProperty(example = "Small shop")
            public String fullname;
            @ApiModelProperty(example = "Small shop")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
        }

        @ApiModelProperty(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetSelfClientsPageItems> pageItems;
    }

    @ApiModel(value = "GetSelfClientsClientIdResponse")
    public final static class GetSelfClientsClientIdResponse {
        private GetSelfClientsClientIdResponse() {
        }

        final class GetSelfClientsTimeline {
            private GetSelfClientsTimeline() {
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
        public GetSelfClientsResponse.GetSelfClientsPageItems.GetSelfClientsStatus status;
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
        public GetSelfClientsTimeline timeline;
        @ApiModelProperty(example = "4")
        public Integer savingsProductId;
        @ApiModelProperty(example = "account overdraft")
        public String savingsProductName;
        @ApiModelProperty(example = "")
        public List<String> groups;
    }

    @ApiModel(value = "GetSelfClientsClientIdAccountsResponse")
    public final static class GetSelfClientsClientIdAccountsResponse {
        private GetSelfClientsClientIdAccountsResponse() {
        }

        final class GetSelfClientsLoanAccounts {
            private GetSelfClientsLoanAccounts() {
            }

            final class GetSelfClientsLoanAccountsStatus {
                private GetSelfClientsLoanAccountsStatus() {
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

            final class GetSelfClientsLoanAccountsType {
                private GetSelfClientsLoanAccountsType() {
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
            public GetSelfClientsLoanAccountsStatus status;
            public GetSelfClientsLoanAccountsType loanType;
            @ApiModelProperty(example = "1")
            public Integer loanCycle;
        }

        final class GetSelfClientsSavingsAccounts {
            private GetSelfClientsSavingsAccounts() {
            }

            final class GetSelfClientsSavingsAccountsCurrency {
                private GetSelfClientsSavingsAccountsCurrency() {
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

            final class GetSelfClientsSavingsAccountsStatus {
                private GetSelfClientsSavingsAccountsStatus() {
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
            public GetSelfClientsSavingsAccountsStatus status;
            public GetSelfClientsSavingsAccountsCurrency currency;
        }

        public Set<GetSelfClientsLoanAccounts> loanAccounts;
        public Set<GetSelfClientsSavingsAccounts> savingsAccounts;
    }

    @ApiModel(value = "GetSelfClientsClientIdChargesResponse")
    public final static class GetSelfClientsClientIdChargesResponse {
        private GetSelfClientsClientIdChargesResponse() {
        }

        final class GetSelfClientsChargesPageItems {
            private GetSelfClientsChargesPageItems() {
            }

            final class GetSelfClientsChargeTimeType {
                private GetSelfClientsChargeTimeType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @ApiModelProperty(example = "Specified due date")
                public String value;
            }

            final class GetSelfClientsChargeCalculationType {
                private GetSelfClientsChargeCalculationType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "chargeCalculationType.flat")
                public String code;
                @ApiModelProperty(example = "Flat")
                public String value;
            }

            @ApiModelProperty(example = "5")
            public Integer id;
            @ApiModelProperty(example = "1")
            public Integer clientId;
            @ApiModelProperty(example = "6")
            public Integer chargeId;
            @ApiModelProperty(example = "Client Fees 2")
            public String name;
            public GetSelfClientsChargeTimeType chargeTimeType;
            @ApiModelProperty(example = "[2015, 9, 1]")
            public LocalDate dueDate;
            public GetSelfClientsChargeCalculationType chargeCalculationType;
            public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
            @ApiModelProperty(example = "550")
            public Float amount;
            @ApiModelProperty(example = "0")
            public Float amountPaid;
            @ApiModelProperty(example = "0")
            public Float amountWaived;
            @ApiModelProperty(example = "0")
            public Float amountWrittenOff;
            @ApiModelProperty(example = "550")
            public Float amountOutstanding;
            @ApiModelProperty(example = "false")
            public Boolean penalty;
            @ApiModelProperty(example = "true")
            public Boolean isActive;
            @ApiModelProperty(example = "false")
            public Boolean isPaid;
            @ApiModelProperty(example = "false")
            public Boolean isWaived;
        }

        @ApiModelProperty(example = "4")
        public Integer totalFilteredRecords;
        public Set<GetSelfClientsChargesPageItems> pageItems;
    }

    @ApiModel(value = "GetSelfClientsClientIdChargesChargeIdResponse")
    public final static class GetSelfClientsClientIdChargesChargeIdResponse {
        private GetSelfClientsClientIdChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "3")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "5")
        public Integer chargeId;
        @ApiModelProperty(example = "Client Fee 1")
        public String name;
        public GetSelfClientsClientIdChargesResponse.GetSelfClientsChargesPageItems.GetSelfClientsChargeTimeType chargeTimeType;
        @ApiModelProperty(example = "[2015, 8, 17]")
        public LocalDate dueDate;
        public GetSelfClientsClientIdChargesResponse.GetSelfClientsChargesPageItems.GetSelfClientsChargeCalculationType chargeCalculationType;
        public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
        @ApiModelProperty(example = "100")
        public Float amount;
        @ApiModelProperty(example = "0")
        public Float amountPaid;
        @ApiModelProperty(example = "100")
        public Float amountWaived;
        @ApiModelProperty(example = "0")
        public Float amountWrittenOff;
        @ApiModelProperty(example = "0")
        public Float amountOutstanding;
        @ApiModelProperty(example = "true")
        public Boolean penalty;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "false")
        public Boolean isPaid;
        @ApiModelProperty(example = "true")
        public Boolean isWaived;
    }

    @ApiModel(value = "GetSelfClientsClientIdTransactionsResponse")
    public final static class GetSelfClientsClientIdTransactionsResponse {
        private GetSelfClientsClientIdTransactionsResponse() {
        }

        final class GetSelfClientsClientIdTransactionsPageItems {
            private GetSelfClientsClientIdTransactionsPageItems() {
            }

            final class GetSelfClientsClientIdTransactionsType {
                private GetSelfClientsClientIdTransactionsType() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "clientTransactionType.payCharge")
                public String code;
                @ApiModelProperty(example = "PAY_CHARGE")
                public String value;
            }

            @ApiModelProperty(example = "226")
            public Integer id;
            @ApiModelProperty(example = "1")
            public Integer officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
            public GetSelfClientsClientIdTransactionsType type;
            @ApiModelProperty(example = "[2015, 9, 2]")
            public LocalDate date;
            public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
            @ApiModelProperty(example = "22")
            public Double amount;
            @ApiModelProperty(example = "[2015, 9, 2]")
            public LocalDate submittedOnDate;
            @ApiModelProperty(example = "false")
            public Boolean reversed;
        }

        @ApiModelProperty(example = "20")
        public Integer totalFilteredRecords;
        public Set<GetSelfClientsClientIdTransactionsPageItems> pageItems;
    }

    @ApiModel(value = "GetSelfClientsClientIdTransactionsTransactionIdResponse")
    public final static class GetSelfClientsClientIdTransactionsTransactionIdResponse {
        private GetSelfClientsClientIdTransactionsTransactionIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        public GetSelfClientsClientIdTransactionsResponse.GetSelfClientsClientIdTransactionsPageItems.GetSelfClientsClientIdTransactionsType type;
        @ApiModelProperty(example = "[2015, 8, 17]")
        public LocalDate date;
        public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
        @ApiModelProperty(example = "60.000000")
        public Float amount;
        @ApiModelProperty(example = "[2015, 8, 19]")
        public LocalDate submittedOnDate;
        @ApiModelProperty(example = "true")
        public Boolean reversed;
    }
}
