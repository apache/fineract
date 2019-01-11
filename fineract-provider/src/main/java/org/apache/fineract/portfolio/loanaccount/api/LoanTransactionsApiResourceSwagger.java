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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

/**
 * Created by Chirag Gupta on 12/30/17.
 */
final class LoanTransactionsApiResourceSwagger {
    private LoanTransactionsApiResourceSwagger() {
    }

    @ApiModel(value = "GetLoansLoanIdTransactionsTemplateResponse")
    public final static class GetLoansLoanIdTransactionsTemplateResponse {
        private GetLoansLoanIdTransactionsTemplateResponse() {
        }

        final class GetLoansTransactionType {
            private GetLoansTransactionType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "loanTransactionType.repayment")
            public String code;
            @ApiModelProperty(example = "Repayment")
            public String value;
        }

        final class GetLoansTotal {
            private GetLoansTotal() {
            }

            @ApiModelProperty(example = "XOF")
            public String currencyCode;
            @ApiModelProperty(example = "0")
            public Integer digitsAfterDecimal;
            @ApiModelProperty(example = "0")
            public Integer inMultiplesOf;
            @ApiModelProperty(example = "471")
            public Float amount;
            @ApiModelProperty(example = "CFA Franc BCEAO")
            public String defaultName;
            @ApiModelProperty(example = "currency.XOF")
            public String nameCode;
            @ApiModelProperty(example = "CFA")
            public String displaySymbol;
            @ApiModelProperty(example = "false")
            public Boolean zero;
            @ApiModelProperty(example = "true")
            public Boolean greaterThanZero;
            @ApiModelProperty(example = "471 CFA")
            public String displaySymbolValue;
        }

        public GetLoansTransactionType transactionType;
        @ApiModelProperty(example = "[2009, 8, 1]")
        public LocalDate date;
        public GetLoansTotal total;
    }

    @ApiModel(value = "GetLoansLoanIdTransactionsTransactionIdResponse")
    public final static class GetLoansLoanIdTransactionsTransactionIdResponse {
        private GetLoansLoanIdTransactionsTransactionIdResponse() {
        }

        final class GetLoansType {
            private GetLoansType() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "loanTransactionType.repayment")
            public String code;
            @ApiModelProperty(example = "Repayment")
            public String value;
            @ApiModelProperty(example = "false")
            public Boolean disbursement;
            @ApiModelProperty(example = "false")
            public Boolean repaymentAtDisbursement;
            @ApiModelProperty(example = "true")
            public Boolean repayment;
            @ApiModelProperty(example = "false")
            public Boolean contra;
            @ApiModelProperty(example = "false")
            public Boolean waiveInterest;
            @ApiModelProperty(example = "false")
            public Boolean waiveCharges;
            @ApiModelProperty(example = "false")
            public Boolean writeOff;
            @ApiModelProperty(example = "false")
            public Boolean recoveryRepayment;
        }

        final class GetLoansCurrency {
            private GetLoansCurrency() {
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

        @ApiModelProperty(example = "3")
        public Integer id;
        public GetLoansType type;
        @ApiModelProperty(example = "[2012, 5, 14]")
        public LocalDate date;
        @ApiModelProperty(example = "false")
        public Boolean manuallyReversed;
        public GetLoansCurrency currency;
        @ApiModelProperty(example = "559.88")
        public Double amount;
        @ApiModelProperty(example = "559.88")
        public Double interestPortion;
    }

    @ApiModel(value = "PostLoansLoanIdTransactionsRequest")
    public final static class PostLoansLoanIdTransactionsRequest {
        private PostLoansLoanIdTransactionsRequest() {
        }
    }

    @ApiModel(value = "PostLoansLoanIdTransactionsResponse")
    public final static class PostLoansLoanIdTransactionsResponse {
        private PostLoansLoanIdTransactionsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "22")
        public Integer resourceId;
    }

    @ApiModel(value = "PostLoansLoanIdTransactionsTransactionIdRequest")
    public final static class PostLoansLoanIdTransactionsTransactionIdRequest {
        private PostLoansLoanIdTransactionsTransactionIdRequest() {
        }

        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "25 May 2012")
        public String transactionDate;
        @ApiModelProperty(example = "50,000.00")
        public Double transactionAmount;
        @ApiModelProperty(example = "An optional note about why your adjusting or changing the transaction.")
        public String note;
    }

    @ApiModel(value = "PostLoansLoanIdTransactionsTransactionIdResponse")
    public final static class PostLoansLoanIdTransactionsTransactionIdResponse {
        private PostLoansLoanIdTransactionsTransactionIdResponse() {
        }

        @ApiModelProperty(example = "16")
        public Integer resourceId;
    }
}
