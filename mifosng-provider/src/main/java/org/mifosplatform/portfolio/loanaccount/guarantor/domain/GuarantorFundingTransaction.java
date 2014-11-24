package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @ManyToOne
    @JoinColumn(name = "deposit_on_hold_transaction_id", nullable = false)
    private DepositAccountOnHoldTransaction depositAccountOnHoldTransaction;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

}
