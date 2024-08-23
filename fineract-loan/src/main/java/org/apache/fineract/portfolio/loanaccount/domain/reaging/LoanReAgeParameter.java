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
package org.apache.fineract.portfolio.loanaccount.domain.reaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

// TODO FINERACT-1932-Fineract modularization: Move to fineract-progressive-loan module after refactor of Loan and LoanTransaction classes
@Entity
@Table(name = "m_loan_reage_parameter")
@AllArgsConstructor
@Getter
public class LoanReAgeParameter extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private PeriodFrequencyType frequencyType;

    @Column(name = "frequency_number", nullable = false)
    private Integer frequencyNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "number_of_installments", nullable = false)
    private Integer numberOfInstallments;

    // for JPA, don't use
    protected LoanReAgeParameter() {}

    public LoanReAgeParameter getCopy(LoanTransaction loanTransaction) {
        return new LoanReAgeParameter(loanTransaction, frequencyType, frequencyNumber, startDate, numberOfInstallments);
    }
}
