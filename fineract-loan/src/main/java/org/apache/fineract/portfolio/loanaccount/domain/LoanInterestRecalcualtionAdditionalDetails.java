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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_loan_interest_recalculation_additional_details")
public class LoanInterestRecalcualtionAdditionalDetails extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_repayment_schedule_id", nullable = false)
    private LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected LoanInterestRecalcualtionAdditionalDetails() {

    }

    public LoanInterestRecalcualtionAdditionalDetails(final LocalDate effectiveDate, final BigDecimal amount) {
        if (effectiveDate != null) {
            this.effectiveDate = effectiveDate;
        }
        this.amount = amount;
    }

    public LocalDate getEffectiveDate() {
        return this.effectiveDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public LoanRepaymentScheduleInstallment getLoanRepaymentScheduleInstallment() {
        return loanRepaymentScheduleInstallment;
    }

    public void setLoanRepaymentScheduleInstallment(LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment) {
        this.loanRepaymentScheduleInstallment = loanRepaymentScheduleInstallment;
    }
}
