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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Chirag Gupta on 12/31/17.
 */
final class RecurringDepositAccountTransactionsApiResourceSwagger {
    private RecurringDepositAccountTransactionsApiResourceSwagger() {
    }

    @ApiModel(value = "GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse")
    public final static class GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse {
        private GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse() {
        }

        final class GetRecurringTransactionType {
            private GetRecurringTransactionType() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "savingsAccountTransactionType.deposit")
            public String code;
            @ApiModelProperty(example = "Deposit")
            public String value;
            @ApiModelProperty(example = "true")
            public Boolean deposit;
            @ApiModelProperty(example = "false")
            public Boolean withdrawal;
            @ApiModelProperty(example = "false")
            public Boolean interestPosting;
            @ApiModelProperty(example = "false")
            public Boolean feeDeduction;
            @ApiModelProperty(example = "false")
            public Boolean initiateTransfer;
            @ApiModelProperty(example = "false")
            public Boolean approveTransfer;
            @ApiModelProperty(example = "false")
            public Boolean withdrawTransfer;
            @ApiModelProperty(example = "false")
            public Boolean rejectTransfer;
            @ApiModelProperty(example = "false")
            public Boolean overdraftInterest;
            @ApiModelProperty(example = "false")
            public Boolean writtenoff;
            @ApiModelProperty(example = "true")
            public Boolean overdraftFee;
        }

        final class GetRecurringCurrency {
            private GetRecurringCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "4")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "100")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        public GetRecurringTransactionType transactionType;
        @ApiModelProperty(example = "1")
        public Integer accountId;
        @ApiModelProperty(example = "000000001")
        public String accountNo;
        @ApiModelProperty(example = "[2014, 6, 25]")
        public LocalDate date;
        public GetRecurringCurrency currency;
        @ApiModelProperty(example = "100000.000000")
        public BigDecimal amount;
        @ApiModelProperty(example = "false")
        public Boolean reversed;
        @ApiModelProperty(example = "[]")
        public List<Integer> paymentTypeOptions;
    }

    @ApiModel(value = "GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse")
    public final static class GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse {
        private GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse() {
        }

        final class GetRecurringTransactionsCurrency {
            private GetRecurringTransactionsCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
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

        final class GetRecurringTransactionsTransactionType {
            private GetRecurringTransactionsTransactionType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "savingsAccountTransactionType.withdrawal")
            public String code;
            @ApiModelProperty(example = "Withdrawal")
            public String value;
            @ApiModelProperty(example = "false")
            public Boolean deposit;
            @ApiModelProperty(example = "true")
            public Boolean withdrawal;
            @ApiModelProperty(example = "false")
            public Boolean interestPosting;
            @ApiModelProperty(example = "false")
            public Boolean feeDeduction;
        }

        final class GetRecurringPaymentDetailData {
            private GetRecurringPaymentDetailData() {
            }

            final class GetRecurringPaymentType {
                private GetRecurringPaymentType() {
                }

                @ApiModelProperty(example = "11")
                public Integer id;
                @ApiModelProperty(example = "cash")
                public String name;
            }

            @ApiModelProperty(example = "62")
            public Integer id;
            public GetRecurringPaymentType paymentType;
            @ApiModelProperty(example = "")
            public Integer accountNumber;
            @ApiModelProperty(example = "")
            public Integer checkNumber;
            @ApiModelProperty(example = "")
            public Integer routingCode;
            @ApiModelProperty(example = "")
            public Integer receiptNumber;
            @ApiModelProperty(example = "")
            public Integer bankNumber;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        public GetRecurringTransactionsTransactionType transactionType;
        @ApiModelProperty(example = "1")
        public Integer accountId;
        @ApiModelProperty(example = "000000001")
        public String accountNo;
        @ApiModelProperty(example = "[2013, 8, 7]")
        public LocalDate date;
        public GetRecurringTransactionsCurrency currency;
        public GetRecurringPaymentDetailData paymentDetailData;
        @ApiModelProperty(example = "5000")
        public Float amount;
        @ApiModelProperty(example = "0")
        public Integer runningBalance;
        @ApiModelProperty(example = "true")
        public Boolean reversed;
    }

    @ApiModel(value = "PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest")
    public final static class PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest {
        private PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest() {
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "27 May 2013")
        public String transactionDate;
        @ApiModelProperty(example = "500")
        public Double transactionAmount;
        @ApiModelProperty(example = "14")
        public Integer paymentTypeId;
        @ApiModelProperty(example = "acc123")
        public String accountNumber;
        @ApiModelProperty(example = "che123")
        public String checkNumber;
        @ApiModelProperty(example = "rou123")
        public String routingCode;
        @ApiModelProperty(example = "rec123")
        public String receiptNumber;
        @ApiModelProperty(example = "ban123")
        public String bankNumber;
    }

    @ApiModel(value = "PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse")
    public final static class PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse {
        private PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse() {
        }

        final class PostRecurringChanges {
            private PostRecurringChanges() {
            }

            @ApiModelProperty(example = "acc123")
            public String accountNumber;
            @ApiModelProperty(example = "che123")
            public String checkNumber;
            @ApiModelProperty(example = "rou123")
            public String routingCode;
            @ApiModelProperty(example = "rec123")
            public String receiptNumber;
            @ApiModelProperty(example = "ban123")
            public String bankNumber;
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "2")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "47")
        public Integer resourceId;
        public PostRecurringChanges changes;
    }

    @ApiModel(value = "PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse")
    public final static class PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse {
        private PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "2")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "48")
        public Integer resourceId;
        public PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse.PostRecurringChanges changes;
    }
}
