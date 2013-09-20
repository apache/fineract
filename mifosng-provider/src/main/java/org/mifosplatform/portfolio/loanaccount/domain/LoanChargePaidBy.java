package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_charge_paid_by")
public class LoanChargePaidBy extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @ManyToOne
    @JoinColumn(name = "loan_charge_id", nullable = false)
    private LoanCharge loanCharge;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected LoanChargePaidBy() {

    }

    public LoanChargePaidBy(final LoanTransaction loanTransaction, final LoanCharge loanCharge, final BigDecimal amount) {
        this.loanTransaction = loanTransaction;
        this.loanCharge = loanCharge;
        this.amount = amount;
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
}
