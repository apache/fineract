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
package org.apache.fineract.portfolio.loanaccount.guarantor.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_guarantor_transaction")
public class GuarantorFundingTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "guarantor_fund_detail_id", nullable = false)
    private GuarantorFundingDetails guarantorFundingDetails;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = true)
    private LoanTransaction loanTransaction;

    @OneToOne
    @JoinColumn(name = "deposit_on_hold_transaction_id", nullable = false)
    private DepositAccountOnHoldTransaction depositAccountOnHoldTransaction;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    protected GuarantorFundingTransaction() {}

    public GuarantorFundingTransaction(final GuarantorFundingDetails guarantorFundingDetails, final LoanTransaction loanTransaction,
            final DepositAccountOnHoldTransaction depositAccountOnHoldTransaction) {
        this.depositAccountOnHoldTransaction = depositAccountOnHoldTransaction;
        this.guarantorFundingDetails = guarantorFundingDetails;
        this.loanTransaction = loanTransaction;
        this.reversed = false;
    }

    public void reverseTransaction() {
        if (!this.reversed) {
            this.reversed = true;
            BigDecimal amountForReverse = this.depositAccountOnHoldTransaction.getAmount();
            this.depositAccountOnHoldTransaction.reverseTransaction();
            if (this.depositAccountOnHoldTransaction.getTransactionType().isRelease()) {
                this.guarantorFundingDetails.undoReleaseFunds(amountForReverse);
            }
        }
    }

}
