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

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_loan_charge_paid_by")
public class LoanChargePaidBy extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "loan_charge_id", nullable = false)
    private LoanCharge loanCharge;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "installment_number", nullable = true)
    private Integer installmentNumber;

    protected LoanChargePaidBy() {

    }

    public LoanChargePaidBy(final LoanTransaction loanTransaction, final LoanCharge loanCharge, final BigDecimal amount,
            Integer installmentNumber) {
        this.loanTransaction = loanTransaction;
        this.loanCharge = loanCharge;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
    }

    public LoanTransaction getLoanTransaction() {
        return this.loanTransaction;
    }

    public void setLoanTransaction(final LoanTransaction loanTransaction) {
        this.loanTransaction = loanTransaction;
    }

    public LoanCharge getLoanCharge() {
        return this.loanCharge;
    }

    public void setLoanCharge(final LoanCharge loanCharge) {
        this.loanCharge = loanCharge;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    
    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }
}
