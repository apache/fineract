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
import java.util.List;

/**
 * Created by Chirag Gupta on 12/31/17.
 */
final class RecurringDepositAccountTransactionsApiResourceSwagger {

    private RecurringDepositAccountTransactionsApiResourceSwagger() {}

    @Schema(description = "GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse")
    public static final class GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse {

        private GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse() {}

        static final class GetRecurringTransactionType {

            private GetRecurringTransactionType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "savingsAccountTransactionType.deposit")
            public String code;
            @Schema(example = "Deposit")
            public String description;
            @Schema(example = "true")
            public Boolean deposit;
            @Schema(example = "false")
            public Boolean withdrawal;
            @Schema(example = "false")
            public Boolean interestPosting;
            @Schema(example = "false")
            public Boolean feeDeduction;
            @Schema(example = "false")
            public Boolean initiateTransfer;
            @Schema(example = "false")
            public Boolean approveTransfer;
            @Schema(example = "false")
            public Boolean withdrawTransfer;
            @Schema(example = "false")
            public Boolean rejectTransfer;
            @Schema(example = "false")
            public Boolean overdraftInterest;
            @Schema(example = "false")
            public Boolean writtenoff;
            @Schema(example = "true")
            public Boolean overdraftFee;
        }

        static final class GetRecurringCurrency {

            private GetRecurringCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "4")
            public Integer decimalPlaces;
            @Schema(example = "100")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        @Schema(example = "1")
        public Long id;
        public GetRecurringTransactionType transactionType;
        @Schema(example = "1")
        public Long accountId;
        @Schema(example = "000000001")
        public String accountNo;
        @Schema(example = "[2014, 6, 25]")
        public LocalDate date;
        public GetRecurringCurrency currency;
        @Schema(example = "100000.000000")
        public BigDecimal amount;
        @Schema(example = "false")
        public Boolean reversed;
        @Schema(example = "[]")
        public List<Integer> paymentTypeOptions;
    }

    @Schema(description = "GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse")
    public static final class GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse {

        private GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse() {}

        static final class GetRecurringTransactionsCurrency {

            private GetRecurringTransactionsCurrency() {}

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

        static final class GetRecurringTransactionsTransactionType {

            private GetRecurringTransactionsTransactionType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "savingsAccountTransactionType.withdrawal")
            public String code;
            @Schema(example = "Withdrawal")
            public String description;
            @Schema(example = "false")
            public Boolean deposit;
            @Schema(example = "true")
            public Boolean withdrawal;
            @Schema(example = "false")
            public Boolean interestPosting;
            @Schema(example = "false")
            public Boolean feeDeduction;
        }

        static final class GetRecurringPaymentDetailData {

            private GetRecurringPaymentDetailData() {}

            static final class GetRecurringPaymentType {

                private GetRecurringPaymentType() {}

                @Schema(example = "11")
                public Long id;
                @Schema(example = "cash")
                public String name;
            }

            @Schema(example = "62")
            public Long id;
            public GetRecurringPaymentType paymentType;
            @Schema(example = "")
            public Integer accountNumber;
            @Schema(example = "")
            public Integer checkNumber;
            @Schema(example = "")
            public Integer routingCode;
            @Schema(example = "")
            public Integer receiptNumber;
            @Schema(example = "")
            public Integer bankNumber;
        }

        @Schema(example = "1")
        public Long id;
        public GetRecurringTransactionsTransactionType transactionType;
        @Schema(example = "1")
        public Long accountId;
        @Schema(example = "000000001")
        public String accountNo;
        @Schema(example = "[2013, 8, 7]")
        public LocalDate date;
        public GetRecurringTransactionsCurrency currency;
        public GetRecurringPaymentDetailData paymentDetailData;
        @Schema(example = "5000")
        public Float amount;
        @Schema(example = "0")
        public Integer runningBalance;
        @Schema(example = "true")
        public Boolean reversed;
    }

    @Schema(description = "PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest")
    public static final class PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest {

        private PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "27 May 2013")
        public String transactionDate;
        @Schema(example = "500")
        public Double transactionAmount;
        @Schema(example = "14")
        public Integer paymentTypeId;
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

    @Schema(description = "PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse")
    public static final class PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse {

        private PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse() {}

        static final class PostRecurringChanges {

            private PostRecurringChanges() {}

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

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "2")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "47")
        public Long resourceId;
        public PostRecurringChanges changes;
    }

    @Schema(description = "PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse")
    public static final class PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse {

        private PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "2")
        public Long clientId;
        @Schema(example = "1")
        public Long savingsId;
        @Schema(example = "48")
        public Long resourceId;
        public PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse.PostRecurringChanges changes;
    }
}
