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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.portfolio.TransactionEntryType;

final class SavingsAccountTransactionsApiResourceSwagger {

    private SavingsAccountTransactionsApiResourceSwagger() {}

    @Schema(description = "SavingsAccountTransactionsSearchResponse")
    public static final class SavingsAccountTransactionsSearchResponse {

        private SavingsAccountTransactionsSearchResponse() {}

        static final class GetSavingsAccountTransactionsPageItem {

            private GetSavingsAccountTransactionsPageItem() {}

            static final class GetTranscationEnumData {

                private GetTranscationEnumData() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "savingsAccountTransactionType.deposit")
                public String code;
                @Schema(example = "Deposit")
                public String value;

                @Schema(example = "true")
                public boolean deposit;
                @Schema(example = "false")
                public boolean dividendPayout;
                @Schema(example = "false")
                public boolean withdrawal;
                @Schema(example = "false")
                public boolean interestPosting;
                @Schema(example = "false")
                public boolean feeDeduction;
                @Schema(example = "false")
                public boolean initiateTransfer;
                @Schema(example = "false")
                public boolean approveTransfer;
                @Schema(example = "false")
                public boolean withdrawTransfer;
                @Schema(example = "false")
                public boolean rejectTransfer;
                @Schema(example = "false")
                public boolean overdraftInterest;
                @Schema(example = "false")
                public boolean writtenoff;
                @Schema(example = "true")
                public boolean overdraftFee;
                @Schema(example = "false")
                public boolean withholdTax;
                @Schema(example = "false")
                public boolean escheat;
                @Schema(example = "false")
                public boolean amountHold;
                @Schema(example = "false")
                public boolean amountRelease;
            }

            static final class GetTransactionsCurrency {

                private GetTransactionsCurrency() {}

                @Schema(example = "USD")
                public String code;
                @Schema(example = "US Dollar")
                public String name;
                @Schema(example = "2")
                public Integer decimalPlaces;
                @Schema(example = "0")
                public Integer isMultiplesOf;
                @Schema(example = "$")
                public String displaySymbol;
                @Schema(example = "currency.USD")
                public String nameCode;
                @Schema(example = "US Dollar ($)")
                public String displayLabel;
            }

            static final class GetTransactionsPaymentDetailData {

                private GetTransactionsPaymentDetailData() {}

                static final class GetPaymentTypeData {

                    private GetPaymentTypeData() {}

                    @Schema(example = "1")
                    public Long id;
                    @Schema(example = "Money Transfer")
                    public String name;
                    @Schema(example = "false")
                    public Boolean isSystemDefined;

                }

                @Schema(example = "1")
                public Long id;
                public GetPaymentTypeData paymentType;
                @Schema(example = "acc123")
                public String accountNumber;
                @Schema(example = "che123")
                public String checkNumber;
                @Schema(example = "rou123")
                public String routingCode;
                @Schema(example = "rec123")
                public String receiptNumber;
                @Schema(example = "ban123")
                public String bankNumber;
            }

            static final class GetSavingsAccountChargesPaidByData {

                private GetSavingsAccountChargesPaidByData() {}

                @Schema(example = "1")
                public Long chargeId;
                @Schema(example = "0")
                public BigDecimal amount;
            }

            @Schema(example = "1")
            public Long id;
            public GetTranscationEnumData transactionType;
            @Schema(example = "CREDIT")
            public TransactionEntryType entryType;
            @Schema(example = "1")
            public Long accountId;
            @Schema(example = "000000001")
            public String accountNo;
            @Schema(example = "[2023, 05, 01]")
            public LocalDate date;
            public GetTransactionsCurrency currency;
            public GetTransactionsPaymentDetailData paymentDetailData;
            @Schema(example = "500")
            public BigDecimal amount;
            @Schema(example = "500")
            public BigDecimal runningBalance;
            @Schema(example = "false")
            public boolean reversed;
            @Schema(example = "[2023, 05, 01]")
            public LocalDate submittedOnDate;
            @Schema(example = "false")
            public boolean interestedPostedAsOn;
            @Schema(example = "mifos")
            public String submittedByUsername;
            @Schema(example = "false")
            public boolean isManualTransaction;
            @Schema(example = "false")
            public Boolean isReversal;
            @Schema(example = "0")
            public Long originalTransactionId;
            @Schema(example = "false")
            public Boolean lienTransaction;
            @Schema(example = "0")
            public Long releaseTransactionId;
            public Set<GetSavingsAccountChargesPaidByData> chargesPaidByData = new HashSet<>();

        }

        @Schema(example = "2")
        public Long total;
        public Set<GetSavingsAccountTransactionsPageItem> content;
    }

    @Schema(description = "PostSavingsAccountTransactionsRequest")
    public static final class PostSavingsAccountTransactionsRequest {

        private PostSavingsAccountTransactionsRequest() {}

        @Schema(example = "27 March 2022")
        public String transactionDate;
        @Schema(example = "1000")
        public BigDecimal transactionAmount;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "true")
        public String lienAllowed;
        @Schema(example = "String")
        public String reasonForBlock;
        @Schema(example = "1")
        public Integer paymentTypeId;
    }

    @Schema(description = "PostSavingsAccountTransactionsResponse")
    public static final class PostSavingsAccountTransactionsResponse {

        private PostSavingsAccountTransactionsResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PostSavingsAccountBulkReversalTransactionsRequest")
    public static final class PostSavingsAccountBulkReversalTransactionsRequest {

        private PostSavingsAccountBulkReversalTransactionsRequest() {}

        @Schema(example = "true")
        public String isBulk;
    }
}
