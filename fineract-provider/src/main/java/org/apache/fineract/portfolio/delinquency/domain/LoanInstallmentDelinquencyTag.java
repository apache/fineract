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
package org.apache.fineract.portfolio.delinquency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "m_loan_installment_delinquency_tag")
public class LoanInstallmentDelinquencyTag extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "delinquency_range_id", nullable = false)
    private DelinquencyRange delinquencyRange;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "installment_id", nullable = false)
    private LoanRepaymentScheduleInstallment installment;

    @Column(name = "addedon_date", nullable = false)
    private LocalDate addedOnDate;

    @Column(name = "liftedon_date", nullable = true)
    private LocalDate liftedOnDate;

    @Column(name = "first_overdue_date", nullable = false)
    private LocalDate firstOverdueDate;

    @Column(name = "outstanding_amount", scale = 6, precision = 19)
    private BigDecimal outstandingAmount;

    @Version
    private Long version;

    public LoanInstallmentDelinquencyTag(DelinquencyRange delinquencyRange, Loan loan, LoanRepaymentScheduleInstallment installment,
            LocalDate addedOnDate, LocalDate liftedOnDate, LocalDate firstOverdueDate, BigDecimal outstandingAmount) {
        this.delinquencyRange = delinquencyRange;
        this.loan = loan;
        this.installment = installment;
        this.addedOnDate = addedOnDate;
        this.liftedOnDate = liftedOnDate;
        this.firstOverdueDate = firstOverdueDate;
        this.outstandingAmount = outstandingAmount;
    }
}
