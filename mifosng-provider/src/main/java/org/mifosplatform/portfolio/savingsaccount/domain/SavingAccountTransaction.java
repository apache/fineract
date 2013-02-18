/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.domain;

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
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountTransactionType;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_saving_account_transaction")
public class SavingAccountTransaction extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
    @ManyToOne(optional = false)
    @JoinColumn(name = "saving_account_id", nullable = false)
    private SavingAccount savingAccount;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer typeOf;

    @SuppressWarnings("unused")
    @OneToOne(optional = true, cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "contra_id")
    private SavingAccountTransaction contra;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private final Date transactionDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private final BigDecimal amount;

    public SavingAccountTransaction() {
        this.transactionDate = null;
        this.amount = BigDecimal.ZERO;
    }

    public SavingAccountTransaction(final DepositAccountTransactionType transactionType, final BigDecimal amount,
            final LocalDate paymentDate) {
        this.typeOf = transactionType.getValue();
        this.amount = amount;
        this.transactionDate = paymentDate.toDate();
    }

    public static SavingAccountTransaction deposit(final BigDecimal amount, final LocalDate paymentDate) {
        return new SavingAccountTransaction(DepositAccountTransactionType.DEPOSIT, amount == null ? new BigDecimal(0) : amount, paymentDate);
    }

    public static SavingAccountTransaction withdraw(final BigDecimal amount, final LocalDate paymentDate) {
        return new SavingAccountTransaction(DepositAccountTransactionType.WITHDRAW, amount == null ? new BigDecimal(0) : amount,
                paymentDate);
    }

    public static SavingAccountTransaction postInterest(final BigDecimal amount, final LocalDate paymentDate) {
        return new SavingAccountTransaction(DepositAccountTransactionType.INTEREST_POSTING, amount == null ? new BigDecimal(0) : amount,
                paymentDate);
    }

    public void updateAccount(final SavingAccount savingAccount) {
        this.savingAccount = savingAccount;
    }

    public DepositAccountTransactionType getTypeOf() {
        return DepositAccountTransactionType.fromInt(this.typeOf);
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public LocalDate getTransactionDate() {
        LocalDate transactionDate = null;
        if (this.transactionDate != null) {
            transactionDate = new LocalDate(this.transactionDate);
        }
        return transactionDate;
    }

}
