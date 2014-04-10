/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_account_transfer_transaction")
public class AccountTransferTransaction extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "account_transfer_details_id", nullable = true)
    private AccountTransferDetails accountTransferDetails;

    @ManyToOne
    @JoinColumn(name = "from_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction fromSavingsTransaction;

    @ManyToOne
    @JoinColumn(name = "to_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction toSavingsTransaction;

    @ManyToOne
    @JoinColumn(name = "to_loan_transaction_id", nullable = true)
    private LoanTransaction toLoanTransaction;

    @ManyToOne
    @JoinColumn(name = "from_loan_transaction_id", nullable = true)
    private LoanTransaction fromLoanTransaction;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date")
    private Date date;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 100)
    private String description;

    public static AccountTransferTransaction savingsToSavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {

        return new AccountTransferTransaction(accountTransferDetails, withdrawal, deposit, null, null, transactionDate, transactionAmount,
                description);
    }

    public static AccountTransferTransaction savingsToLoanTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction withdrawal, final LoanTransaction loanRepaymentTransaction, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        return new AccountTransferTransaction(accountTransferDetails, withdrawal, null, loanRepaymentTransaction, null, transactionDate,
                transactionAmount, description);
    }

    public static AccountTransferTransaction LoanTosavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final SavingsAccountTransaction deposit, final LoanTransaction loanRefundTransaction, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        return new AccountTransferTransaction(accountTransferDetails, null, deposit, null, loanRefundTransaction, transactionDate,
                transactionAmount, description);
    }

    protected AccountTransferTransaction() {
        //
    }

    private AccountTransferTransaction(final AccountTransferDetails accountTransferDetails, final SavingsAccountTransaction withdrawal,
            final SavingsAccountTransaction deposit, final LoanTransaction loanRepaymentTransaction,
            final LoanTransaction loanRefundTransaction, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {
        this.accountTransferDetails = accountTransferDetails;
        this.fromLoanTransaction = loanRefundTransaction;
        this.fromSavingsTransaction = withdrawal;
        this.toSavingsTransaction = deposit;
        this.toLoanTransaction = loanRepaymentTransaction;
        this.date = transactionDate.toDate();
        this.currency = transactionAmount.getCurrency();
        this.amount = transactionAmount.getAmountDefaultedToNullIfZero();
        this.description = description;
    }

    public LoanTransaction getFromLoanTransaction() {
        return this.fromLoanTransaction;
    }

    public SavingsAccountTransaction getFromTransaction() {
        return this.fromSavingsTransaction;
    }

    public LoanTransaction getToLoanTransaction() {
        return this.toLoanTransaction;
    }

    public SavingsAccountTransaction getToSavingsTransaction() {
        return this.toSavingsTransaction;
    }

    public void reverse() {
        this.reversed = true;
    }

    public void updateToLoanTransaction(LoanTransaction toLoanTransaction) {
        this.toLoanTransaction = toLoanTransaction;
    }

    public AccountTransferDetails accountTransferDetails() {
        return this.accountTransferDetails;
    }
}