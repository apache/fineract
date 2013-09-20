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
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_account_transfer")
public class AccountTransfer extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "from_office_id", nullable = false)
    private Office fromOffice;

    @ManyToOne
    @JoinColumn(name = "from_client_id", nullable = false)
    private Client fromClient;

    @ManyToOne
    @JoinColumn(name = "from_savings_account_id", nullable = true)
    private SavingsAccount fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_office_id", nullable = false)
    private Office toOffice;

    @ManyToOne
    @JoinColumn(name = "to_client_id", nullable = false)
    private Client toClient;

    @ManyToOne
    @JoinColumn(name = "to_savings_account_id", nullable = true)
    private SavingsAccount toSavingsAccount;

    @ManyToOne
    @JoinColumn(name = "from_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction fromTransaction;

    @ManyToOne
    @JoinColumn(name = "to_savings_transaction_id", nullable = true)
    private SavingsAccountTransaction toSavingsTransaction;

    @ManyToOne
    @JoinColumn(name = "to_loan_account_id", nullable = true)
    private Loan toLoanAccount;

    @ManyToOne
    @JoinColumn(name = "from_loan_account_id", nullable = true)
    private Loan fromLoanAccount;

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

    public static AccountTransfer savingsToSavingsTransfer(final Office fromOffice, final Client fromClient,
            final SavingsAccount fromSavingsAccount, final Office toOffice, final Client toClient, final SavingsAccount toSavingsAccount,
            final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {

        return new AccountTransfer(fromOffice, fromClient, fromSavingsAccount, null, toOffice, toClient, toSavingsAccount, null,
                withdrawal, deposit, null, null, transactionDate, transactionAmount, description);
    }

    public static AccountTransfer savingsToLoanTransfer(final Office fromOffice, final Client fromClient,
            final SavingsAccount fromSavingsAccount, final Office toOffice, final Client toClient, final Loan toLoanAccount,
            final SavingsAccountTransaction withdrawal, final LoanTransaction loanRepaymentTransaction, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        return new AccountTransfer(fromOffice, fromClient, fromSavingsAccount, null, toOffice, toClient, null, toLoanAccount, withdrawal,
                null, loanRepaymentTransaction, null, transactionDate, transactionAmount, description);
    }

    public static AccountTransfer LoanTosavingsTransfer(final Office fromOffice, final Client fromClient, final Loan fromLoanAccount,
            final Office toOffice, final Client toClient, final SavingsAccount toSavingsAccount, final SavingsAccountTransaction deposit,
            final LoanTransaction loanRefundTransaction, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {
        return new AccountTransfer(fromOffice, fromClient, null, fromLoanAccount, toOffice, toClient, toSavingsAccount, null, null,
                deposit, null, loanRefundTransaction, transactionDate, transactionAmount, description);
    }

    protected AccountTransfer() {
        //
    }

    private AccountTransfer(final Office fromOffice, final Client fromClient, final SavingsAccount fromSavingsAccount,
            final Loan fromLoanAccount, final Office toOffice, final Client toClient, final SavingsAccount toSavingsAccount,
            final Loan toLoanAccount, final SavingsAccountTransaction withdrawal, final SavingsAccountTransaction deposit,
            final LoanTransaction loanRepaymentTransaction, final LoanTransaction loanRefundTransaction, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        this.fromOffice = fromOffice;
        this.fromClient = fromClient;
        this.fromAccount = fromSavingsAccount;
        this.fromLoanAccount = fromLoanAccount;
        this.fromLoanTransaction = loanRefundTransaction;
        this.toOffice = toOffice;
        this.toClient = toClient;
        this.toSavingsAccount = toSavingsAccount;
        this.fromTransaction = withdrawal;
        this.toSavingsTransaction = deposit;
        this.toLoanAccount = toLoanAccount;
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
        return this.fromTransaction;
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

    public SavingsAccount getToSavingsAccount() {
        return this.toSavingsAccount;
    }

    public SavingsAccount getFromAccount() {
        return this.fromAccount;
    }
}