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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Swagger documentation for Loan Amortization Allocation API
 */
final class LoanAmortizationAllocationApiResourceSwagger {

    private LoanAmortizationAllocationApiResourceSwagger() {}

    /**
     * Common response class for all loan amortization allocation APIs Used for both Capitalized Income and Buydown Fee
     * endpoints
     */
    @Schema(description = "LoanAmortizationAllocationResponse")
    public static final class LoanAmortizationAllocationResponse {

        private LoanAmortizationAllocationResponse() {}

        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "loan-ext-123")
        public String loanExternalId;
        @Schema(example = "123")
        public Long baseLoanTransactionId;
        @Schema(example = "2024-01-15")
        public LocalDate baseLoanTransactionDate;
        @Schema(example = "1000.00")
        public BigDecimal baseLoanTransactionAmount;
        @Schema(example = "50.00")
        public BigDecimal unrecognizedAmount;
        @Schema(example = "0.00")
        public BigDecimal chargedOffAmount;
        @Schema(example = "25.00")
        public BigDecimal adjustmentAmount;
        public List<AmortizationMappingData> amortizationMappings;

        /**
         * Data transfer object for amortization mapping details
         */
        static final class AmortizationMappingData {

            private AmortizationMappingData() {}

            @Schema(example = "789")
            public Long amortizationLoanTransactionId;
            @Schema(example = "amort-txn-ext-789")
            public String amortizationLoanTransactionExternalId;
            @Schema(example = "2024-01-15")
            public LocalDate date;
            @Schema(example = "AM", description = "AM for amortization, AM_ADJ for amortization adjustment")
            public String type;
            @Schema(example = "10.00")
            public BigDecimal amount;
        }
    }
}
