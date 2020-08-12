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
package org.apache.fineract.portfolio.self.client.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/21/17.
 */

final class SelfClientsApiResourceSwagger {

    private SelfClientsApiResourceSwagger() {}

    @Schema(description = "GetSelfClientsResponse")
    public static final class GetSelfClientsResponse {

        private GetSelfClientsResponse() {}

        static final class GetSelfClientsPageItems {

            private GetSelfClientsPageItems() {}

            static final class GetSelfClientsStatus {

                private GetSelfClientsStatus() {}

                @Schema(example = "300")
                public Integer id;
                @Schema(example = "clientStatusType.active")
                public String code;
                @Schema(example = "Active")
                public String description;
            }

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "000000001")
            public Long accountNo;
            public GetSelfClientsStatus status;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "[2013, 3, 1]")
            public LocalDate activationDate;
            @Schema(example = "Small shop")
            public String fullname;
            @Schema(example = "Small shop")
            public String displayName;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
        }

        @Schema(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetSelfClientsPageItems> pageItems;
    }

    @Schema(description = "GetSelfClientsClientIdResponse")
    public static final class GetSelfClientsClientIdResponse {

        private GetSelfClientsClientIdResponse() {}

        static final class GetSelfClientsTimeline {

            private GetSelfClientsTimeline() {}

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

        @Schema(example = "27")
        public Integer id;
        @Schema(example = "000000027")
        public Long accountNo;
        public GetSelfClientsResponse.GetSelfClientsPageItems.GetSelfClientsStatus status;
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
        public GetSelfClientsTimeline timeline;
        @Schema(example = "4")
        public Integer savingsProductId;
        @Schema(example = "account overdraft")
        public String savingsProductName;
        @Schema(example = "")
        public List<String> groups;
    }

    @Schema(description = "GetSelfClientsClientIdAccountsResponse")
    public static final class GetSelfClientsClientIdAccountsResponse {

        private GetSelfClientsClientIdAccountsResponse() {}

        static final class GetSelfClientsLoanAccounts {

            private GetSelfClientsLoanAccounts() {}

            static final class GetSelfClientsLoanAccountsStatus {

                private GetSelfClientsLoanAccountsStatus() {}

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

            static final class GetSelfClientsLoanAccountsType {

                private GetSelfClientsLoanAccountsType() {}

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
            public Long accountNo;
            @Schema(example = "456")
            public Integer externalId;
            @Schema(example = "1")
            public Integer productId;
            @Schema(example = "TestOne")
            public String productName;
            public GetSelfClientsLoanAccountsStatus status;
            public GetSelfClientsLoanAccountsType loanType;
            @Schema(example = "1")
            public Integer loanCycle;
        }

        static final class GetSelfClientsSavingsAccounts {

            private GetSelfClientsSavingsAccounts() {}

            static final class GetSelfClientsSavingsAccountsCurrency {

                private GetSelfClientsSavingsAccountsCurrency() {}

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

            static final class GetSelfClientsSavingsAccountsStatus {

                private GetSelfClientsSavingsAccountsStatus() {}

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
            }

            @Schema(example = "7")
            public Integer id;
            @Schema(example = "000000007")
            public Long accountNo;
            @Schema(example = "2")
            public Integer productId;
            @Schema(example = "Other product")
            public String productName;
            public GetSelfClientsSavingsAccountsStatus status;
            public GetSelfClientsSavingsAccountsCurrency currency;
        }

        public Set<GetSelfClientsLoanAccounts> loanAccounts;
        public Set<GetSelfClientsSavingsAccounts> savingsAccounts;
    }

    @Schema(description = "GetSelfClientsClientIdChargesResponse")
    public static final class GetSelfClientsClientIdChargesResponse {

        private GetSelfClientsClientIdChargesResponse() {}

        static final class GetSelfClientsChargesPageItems {

            private GetSelfClientsChargesPageItems() {}

            static final class GetSelfClientsChargeTimeType {

                private GetSelfClientsChargeTimeType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @Schema(example = "Specified due date")
                public String description;
            }

            static final class GetSelfClientsChargeCalculationType {

                private GetSelfClientsChargeCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            @Schema(example = "5")
            public Integer id;
            @Schema(example = "1")
            public Integer clientId;
            @Schema(example = "6")
            public Integer chargeId;
            @Schema(example = "Client Fees 2")
            public String name;
            public GetSelfClientsChargeTimeType chargeTimeType;
            @Schema(example = "[2015, 9, 1]")
            public LocalDate dueDate;
            public GetSelfClientsChargeCalculationType chargeCalculationType;
            public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
            @Schema(example = "550")
            public Float amount;
            @Schema(example = "0")
            public Float amountPaid;
            @Schema(example = "0")
            public Float amountWaived;
            @Schema(example = "0")
            public Float amountWrittenOff;
            @Schema(example = "550")
            public Float amountOutstanding;
            @Schema(example = "false")
            public Boolean penalty;
            @Schema(example = "true")
            public Boolean isActive;
            @Schema(example = "false")
            public Boolean isPaid;
            @Schema(example = "false")
            public Boolean isWaived;
        }

        @Schema(example = "4")
        public Integer totalFilteredRecords;
        public Set<GetSelfClientsChargesPageItems> pageItems;
    }

    @Schema(description = "GetSelfClientsClientIdChargesChargeIdResponse")
    public static final class GetSelfClientsClientIdChargesChargeIdResponse {

        private GetSelfClientsClientIdChargesChargeIdResponse() {}

        @Schema(example = "3")
        public Integer id;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "5")
        public Integer chargeId;
        @Schema(example = "Client Fee 1")
        public String name;
        public GetSelfClientsClientIdChargesResponse.GetSelfClientsChargesPageItems.GetSelfClientsChargeTimeType chargeTimeType;
        @Schema(example = "[2015, 8, 17]")
        public LocalDate dueDate;
        public GetSelfClientsClientIdChargesResponse.GetSelfClientsChargesPageItems.GetSelfClientsChargeCalculationType chargeCalculationType;
        public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
        @Schema(example = "100")
        public Float amount;
        @Schema(example = "0")
        public Float amountPaid;
        @Schema(example = "100")
        public Float amountWaived;
        @Schema(example = "0")
        public Float amountWrittenOff;
        @Schema(example = "0")
        public Float amountOutstanding;
        @Schema(example = "true")
        public Boolean penalty;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "false")
        public Boolean isPaid;
        @Schema(example = "true")
        public Boolean isWaived;
    }

    @Schema(description = "GetSelfClientsClientIdTransactionsResponse")
    public static final class GetSelfClientsClientIdTransactionsResponse {

        private GetSelfClientsClientIdTransactionsResponse() {}

        static final class GetSelfClientsClientIdTransactionsPageItems {

            private GetSelfClientsClientIdTransactionsPageItems() {}

            static final class GetSelfClientsClientIdTransactionsType {

                private GetSelfClientsClientIdTransactionsType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "clientTransactionType.payCharge")
                public String code;
                @Schema(example = "PAY_CHARGE")
                public String description;
            }

            @Schema(example = "226")
            public Integer id;
            @Schema(example = "1")
            public Integer officeId;
            @Schema(example = "Head Office")
            public String officeName;
            public GetSelfClientsClientIdTransactionsType type;
            @Schema(example = "[2015, 9, 2]")
            public LocalDate date;
            public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
            @Schema(example = "22")
            public Double amount;
            @Schema(example = "[2015, 9, 2]")
            public LocalDate submittedOnDate;
            @Schema(example = "false")
            public Boolean reversed;
        }

        @Schema(example = "20")
        public Integer totalFilteredRecords;
        public Set<GetSelfClientsClientIdTransactionsPageItems> pageItems;
    }

    @Schema(description = "GetSelfClientsClientIdTransactionsTransactionIdResponse")
    public static final class GetSelfClientsClientIdTransactionsTransactionIdResponse {

        private GetSelfClientsClientIdTransactionsTransactionIdResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "Head Office")
        public String officeName;
        public GetSelfClientsClientIdTransactionsResponse.GetSelfClientsClientIdTransactionsPageItems.GetSelfClientsClientIdTransactionsType type;
        @Schema(example = "[2015, 8, 17]")
        public LocalDate date;
        public GetSelfClientsClientIdAccountsResponse.GetSelfClientsSavingsAccounts.GetSelfClientsSavingsAccountsCurrency currency;
        @Schema(example = "60.000000")
        public Float amount;
        @Schema(example = "[2015, 8, 19]")
        public LocalDate submittedOnDate;
        @Schema(example = "true")
        public Boolean reversed;
    }
}
