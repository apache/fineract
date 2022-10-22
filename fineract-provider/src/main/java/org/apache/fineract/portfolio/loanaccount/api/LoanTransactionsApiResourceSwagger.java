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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/30/17.
 */
final class LoanTransactionsApiResourceSwagger {

    private LoanTransactionsApiResourceSwagger() {}

    @Schema(description = "GetLoansLoanIdTransactionsTemplateResponse")
    public static final class GetLoansLoanIdTransactionsTemplateResponse {

        private GetLoansLoanIdTransactionsTemplateResponse() {}

        static final class GetLoansTransactionType {

            private GetLoansTransactionType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "loanTransactionType.repayment")
            public String code;
            @Schema(example = "Repayment")
            public String description;
        }

        static final class GetLoansTotal {

            private GetLoansTotal() {}

            @Schema(example = "XOF")
            public String currencyCode;
            @Schema(example = "0")
            public Integer digitsAfterDecimal;
            @Schema(example = "0")
            public Integer inMultiplesOf;
            @Schema(example = "471")
            public Float amount;
            @Schema(example = "CFA Franc BCEAO")
            public String defaultName;
            @Schema(example = "currency.XOF")
            public String nameCode;
            @Schema(example = "CFA")
            public String displaySymbol;
            @Schema(example = "false")
            public Boolean zero;
            @Schema(example = "true")
            public Boolean greaterThanZero;
            @Schema(example = "471 CFA")
            public String displaySymbolValue;
        }

        public GetLoansTransactionType transactionType;
        @Schema(example = "[2009, 8, 1]")
        public LocalDate date;
        public GetLoansTotal total;
    }

    @Schema(description = "GetLoansLoanIdTransactionsTransactionIdResponse")
    public static final class GetLoansLoanIdTransactionsTransactionIdResponse {

        private GetLoansLoanIdTransactionsTransactionIdResponse() {}

        static final class GetLoansType {

            private GetLoansType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "loanTransactionType.repayment")
            public String code;
            @Schema(example = "Repayment")
            public String description;
            @Schema(example = "false")
            public Boolean disbursement;
            @Schema(example = "false")
            public Boolean repaymentAtDisbursement;
            @Schema(example = "true")
            public Boolean repayment;
            @Schema(example = "false")
            public Boolean contra;
            @Schema(example = "false")
            public Boolean waiveInterest;
            @Schema(example = "false")
            public Boolean waiveCharges;
            @Schema(example = "false")
            public Boolean writeOff;
            @Schema(example = "false")
            public Boolean recoveryRepayment;
        }

        static final class GetLoansCurrency {

            private GetLoansCurrency() {}

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

        static final class GetLoanTransactionRelation {

            private GetLoanTransactionRelation() {}

            @Schema(example = "1")
            public Long fromLoanTransaction;
            @Schema(example = "10")
            public Long toLoanTransaction;
            @Schema(example = "CHARGEBACK")
            private String relationType;
            @Schema(example = "100.00")
            private Double amount;
            @Schema(example = "Repayment Adjustment Chargeback")
            private String paymentType;

        }

        @Schema(example = "3")
        public Integer id;
        public GetLoansType type;
        @Schema(example = "[2012, 5, 14]")
        public LocalDate date;
        @Schema(example = "[2012, 5, 14]")
        public LocalDate submittedOnDate;
        @Schema(example = "false")
        public Boolean manuallyReversed;
        public GetLoansCurrency currency;
        @Schema(example = "559.88")
        public Double amount;
        @Schema(example = "559.88")
        public Double interestPortion;
        @Schema(example = "20120514")
        public String reversalExternalId;
        @Schema(example = "[2012, 5, 18]")
        public LocalDate reversedOnDate;
        @Schema(example = "1000.00")
        public Double netDisbursalAmount;
        @Schema(example = "240.00")
        public Double principalPortion;
        @Schema(example = "23.90")
        public Double feeChargesPortion;
        @Schema(example = "12.80")
        public Double penaltyChargesPortion;
        @Schema(example = "33.00")
        public Double overpaymentPortion;
        @Schema(example = "55.50")
        public Double unrecognizedIncomePortion;
        @Schema(example = "100.00")
        public Double outstandingLoanBalance;
        @Schema(example = "[2012, 5, 18]")
        public LocalDate possibleNextRepaymentDate;
        public Set<GetLoanTransactionRelation> transactionRelations;
    }

    @Schema(description = "PostLoansLoanIdTransactionsRequest")
    public static final class PostLoansLoanIdTransactionsRequest {

        private PostLoansLoanIdTransactionsRequest() {}

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "28 June 2022")
        public String transactionDate;
        @Schema(example = "50000.00")
        public Double transactionAmount;
        @Schema(example = "An optional note about why your adjusting or changing the transaction.")
        public String note;
        @Schema(example = "3e7791ce-aa10-11ec-b909-0242ac120002")
        public String externalId;
        @Schema(example = "3")
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

    @Schema(description = "PostLoansLoanIdTransactionsResponse")
    public static final class PostLoansLoanIdTransactionsResponse {

        private PostLoansLoanIdTransactionsResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "22")
        public Integer resourceId;
    }

    @Schema(description = "PostLoansLoanIdTransactionsTransactionIdRequest")
    public static final class PostLoansLoanIdTransactionsTransactionIdRequest {

        private PostLoansLoanIdTransactionsTransactionIdRequest() {}

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "28 June 2022")
        public String transactionDate;
        @Schema(example = "50,000.00")
        public Double transactionAmount;
        @Schema(example = "An optional note about why your adjusting or changing the transaction.")
        public String note;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String reversalExternalId;
        @Schema(example = "1")
        public Integer paymentTypeId;
        @Schema(example = "4ff9b1cb988b7")
        public String externalId;
    }

    @Schema(description = "PostLoansLoanIdTransactionsTransactionIdResponse")
    public static final class PostLoansLoanIdTransactionsTransactionIdResponse {

        private PostLoansLoanIdTransactionsTransactionIdResponse() {}

        @Schema(example = "16")
        public Integer resourceId;
    }

    @Schema(description = "PutChargeTransactionChangesResponse")
    public static final class PutChargeTransactionChangesResponse {

        private PutChargeTransactionChangesResponse() {}

        static final class Changes {

            private Changes() {}

            @Schema(example = "amount")
            public String amount;
        }

        @Schema(example = "1")
        public Integer resourceId;
        @Schema(example = "48")
        public Integer loanId;
        public PutChargeTransactionChangesResponse.Changes changes;

    }

    @Schema(description = "PutChargeTransactionChangesRequest")
    public static final class PutChargeTransactionChangesRequest {

        private PutChargeTransactionChangesRequest() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "2")
        public Integer loanId;
    }
}
