/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_overdue_installment_charge")
public class LoanOverdueInstallmentCharge extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_charge_id", referencedColumnName = "id", nullable = false)
    private LoanCharge loancharge;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_schedule_id", referencedColumnName = "id", nullable = false)
    private LoanRepaymentScheduleInstallment installment;

    @Column(name = "frequency_number")
    private Integer frequencyNumber;

    public LoanOverdueInstallmentCharge() {

    }

    public LoanOverdueInstallmentCharge(final LoanCharge loanCharge, final LoanRepaymentScheduleInstallment installment,
            final Integer frequencyNumber) {
        this.loancharge = loanCharge;
        this.installment = installment;
        this.frequencyNumber = frequencyNumber;
    }

    public void updateLoanRepaymentScheduleInstallment(LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment) {
        this.installment = loanRepaymentScheduleInstallment;
    }

    public LoanRepaymentScheduleInstallment getInstallment() {
        return this.installment;
    }

}