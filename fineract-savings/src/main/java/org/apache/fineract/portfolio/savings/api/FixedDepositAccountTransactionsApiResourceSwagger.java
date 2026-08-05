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

/**
 *
 * Created by Apache Fineract Swagger Annotations.
 *
 */

final class FixedDepositAccountTransactionsApiResourceSwagger {

    private FixedDepositAccountTransactionsApiResourceSwagger() {}

    @Schema(description = "PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest")

    public static final class PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest {

        private PostFixedDepositAccountsFixedDepositAccountIdTransactionsRequest() {}

        @Schema(example = "en")

        public String locale;

        @Schema(example = "dd MMMM yyyy")

        public String dateFormat;

        @Schema(example = "01 January 2014")

        public String transactionDate;

        @Schema(example = "100.00")

        public Double transactionAmount;

        @Schema(example = "payment type note")

        public String note;

    }

    @Schema(description = "PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse")

    public static final class PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse {

        private PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse() {}

        @Schema(example = "1")

        public Long officeId;

        @Schema(example = "2")

        public Long clientId;

        @Schema(example = "1")

        public Long savingsId;

        @Schema(example = "48")

        public Long resourceId;

        public PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdChanges changes;

    }

    @Schema(description = "GetFixedDepositAccountsAccountIdTransactionsResponse")

    public static final class GetFixedDepositAccountsAccountIdTransactionsResponse {

        private GetFixedDepositAccountsAccountIdTransactionsResponse() {}

        @Schema(description = "GetFixedDepositAccountsAccountIdTransactionsType")
        public static final class GetFixedDepositAccountsAccountIdTransactionsType {

            private GetFixedDepositAccountsAccountIdTransactionsType() {}

            @Schema(example = "1")
            public Long id;

            @Schema(example = "savingsAccountTransactionType.interestPosting")
            public String code;

            @Schema(example = "Interest Posting")
            public String description;

            @Schema(example = "true")
            public Boolean interestPosting;

            @Schema(example = "false")
            public Boolean deposit;

            @Schema(example = "false")
            public Boolean withdrawal;

        }

        @Schema(example = "1")
        public Long id;

        public GetFixedDepositAccountsAccountIdTransactionsType transactionType;

        @Schema(example = "1")
        public Long accountId;

        @Schema(example = "000000001")
        public Long accountNo;

        @Schema(example = "[2014, 6, 25]")
        public java.time.LocalDate date;

        @Schema(example = "5000")
        public Float amount;

        @Schema(example = "0")
        public Float runningBalance;

        @Schema(example = "false")
        public Boolean reversed;

    }

    @Schema(description = "PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdChanges")

    public static final class PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdChanges {

        private PostFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdChanges() {}

        @Schema(example = "en")

        public String locale;

        @Schema(example = "dd MMMM yyyy")

        public String dateFormat;

        @Schema(example = "01 January 2014")

        public String transactionDate;

        @Schema(example = "100.00")

        public Double transactionAmount;

    }

}
