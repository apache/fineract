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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.AmortizationType;

/**
 * Data transfer object for loan amortization allocation information
 */
@Getter
@Builder
@AllArgsConstructor
public class LoanAmortizationAllocationData {

    private Long loanId;
    private ExternalId loanExternalId;
    private Long baseLoanTransactionId;
    private LocalDate baseLoanTransactionDate;
    private BigDecimal baseLoanTransactionAmount;
    private BigDecimal unrecognizedAmount;
    private BigDecimal chargedOffAmount;
    private BigDecimal adjustmentAmount;
    private List<AmortizationMappingData> amortizationMappings;

    /**
     * Data transfer object for amortization mapping details
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AmortizationMappingData {

        private Long amortizationLoanTransactionId;
        private ExternalId amortizationLoanTransactionExternalId;
        private LocalDate date;
        private AmortizationType type; // AM or AM_ADJ
        private BigDecimal amount;
    }
}
