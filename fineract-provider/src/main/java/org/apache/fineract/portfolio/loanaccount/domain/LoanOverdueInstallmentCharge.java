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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_loan_overdue_installment_charge")
public class LoanOverdueInstallmentCharge extends AbstractPersistableCustom<Long> {

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