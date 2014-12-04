/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_guarantor_transaction")
public class GuarantorFundingTransaction extends AbstractPersistable<Long> {

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
