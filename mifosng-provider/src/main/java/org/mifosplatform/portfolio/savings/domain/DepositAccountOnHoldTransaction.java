/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorFundingTransaction;
import org.mifosplatform.portfolio.savings.DepositAccountOnHoldTransactionType;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_account_on_hold_transaction")
public class DepositAccountOnHoldTransaction extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer transactionType;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private Date transactionDate;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "depositAccountOnHoldTransaction", optional = true, orphanRemoval = true)
    private GuarantorFundingTransaction guarantorFundingTransaction;

    protected DepositAccountOnHoldTransaction() {}

    private DepositAccountOnHoldTransaction(final SavingsAccount savingsAccount, final BigDecimal amount,
            final DepositAccountOnHoldTransactionType transactionType, final LocalDate transactionDate, final boolean reversed) {
        this.savingsAccount = savingsAccount;
        this.amount = amount;
        this.transactionType = transactionType.getValue();
        this.transactionDate = transactionDate.toDate();
        this.createdDate = new Date();
        this.reversed = reversed;
    }

    public static DepositAccountOnHoldTransaction hold(final SavingsAccount savingsAccount, final BigDecimal amount,
            final LocalDate transactionDate) {
        boolean reversed = false;
        return new DepositAccountOnHoldTransaction(savingsAccount, amount, DepositAccountOnHoldTransactionType.HOLD, transactionDate,
                reversed);
    }

    public static DepositAccountOnHoldTransaction release(final SavingsAccount savingsAccount, final BigDecimal amount,
            final LocalDate transactionDate) {
        boolean reversed = false;
        return new DepositAccountOnHoldTransaction(savingsAccount, amount, DepositAccountOnHoldTransactionType.RELEASE, transactionDate,
                reversed);
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void reverseTransaction() {
        this.reversed = true;
        if (this.getTransactionType().isHold()) {
            this.savingsAccount.releaseFunds(this.amount);
        } else {
            this.savingsAccount.holdFunds(this.amount);
        }
    }

    public DepositAccountOnHoldTransactionType getTransactionType() {
        return DepositAccountOnHoldTransactionType.fromInt(this.transactionType);

    }

}
