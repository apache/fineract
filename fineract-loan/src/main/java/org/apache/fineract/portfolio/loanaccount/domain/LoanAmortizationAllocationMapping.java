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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

/**
 * Entity to store the mapping between base loan transactions (Capitalized Income or Buy Down Fee) and their
 * amortization allocation schedule. This mapping table stores which base loan transaction amortization occurs, on which
 * date and what was the calculated amount.
 */
@Entity
@Table(name = "m_loan_amortization_allocation_mapping")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanAmortizationAllocationMapping extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "base_loan_transaction_id", nullable = false)
    private Long baseLoanTransactionId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "amortization_loan_transaction_id", nullable = false)
    private Long amortizationLoanTransactionId;

    /**
     * Type of the amortization transaction (AM - amortization, AM_ADJ - amortization adjustment)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "amortization_type", nullable = false)
    private AmortizationType amortizationType;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

}
