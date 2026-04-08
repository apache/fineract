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
package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Swagger documentation classes for Working Capital Loan Transactions API (GET list / GET one).
 */
public final class WorkingCapitalLoanTransactionsApiResourceSwagger {

    private WorkingCapitalLoanTransactionsApiResourceSwagger() {}

    @Schema(description = "GetWorkingCapitalLoanTransactionsResponse (Spring Data Page: content, totalElements, totalPages, number, size, first, last)")
    public static final class GetWorkingCapitalLoanTransactionsResponse {

        private GetWorkingCapitalLoanTransactionsResponse() {}

        public List<GetWorkingCapitalLoanTransactionIdResponse> content;
        @Schema(example = "5")
        public Long totalElements;
        @Schema(example = "1")
        public Integer totalPages;
        @Schema(example = "0")
        public Integer number;
        @Schema(example = "20")
        public Integer size;
        public Boolean first;
        public Boolean last;
    }

    @Schema(description = "Working Capital Loan transaction (e.g. disbursement) in GET transaction response.")
    public static final class GetWorkingCapitalLoanTransactionIdResponse {

        private GetWorkingCapitalLoanTransactionIdResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(description = "Transaction type")
        public LoanTransactionEnumData type;
        @Schema(example = "[2024, 2, 1]")
        public LocalDate transactionDate;
        @Schema(example = "[2024, 2, 1]")
        public LocalDate submittedOnDate;
        @Schema(example = "10000.00")
        public BigDecimal transactionAmount;
        @Schema(description = "Payment detail")
        public WorkingCapitalLoanTransactionPaymentDetailData paymentDetailData;
        @Schema(example = "txn-ext-001")
        public String externalId;
        @Schema(example = "false")
        public Boolean reversed;
        @Schema(example = "reversal-ext-001")
        public String reversalExternalId;
        @Schema(example = "[2024, 2, 5]")
        public LocalDate reversedOnDate;
        @Schema(example = "10000.00", description = "Principal portion from allocation")
        public BigDecimal principalPortion;
        @Schema(example = "0.00", description = "Fee charges portion from allocation")
        public BigDecimal feeChargesPortion;
        @Schema(example = "0.00", description = "Penalty charges portion from allocation")
        public BigDecimal penaltyChargesPortion;
    }

    @Schema(description = "Loan transaction type enum data (same as basic loan)")
    public static final class LoanTransactionEnumData {

        private LoanTransactionEnumData() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "loanTransactionType.disbursement")
        public String code;
        @Schema(example = "Disbursement")
        public String value;
    }

    @Schema(description = "Payment detail data")
    public static final class WorkingCapitalLoanTransactionPaymentDetailData {

        private WorkingCapitalLoanTransactionPaymentDetailData() {}

        @Schema(example = "62")
        public Long id;
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
}
