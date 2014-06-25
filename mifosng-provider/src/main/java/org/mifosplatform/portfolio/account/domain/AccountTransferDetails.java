/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_account_transfer_details")
public class AccountTransferDetails extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "from_office_id", nullable = false)
    private Office fromOffice;

    @ManyToOne
    @JoinColumn(name = "from_client_id", nullable = false)
    private Client fromClient;

    @ManyToOne
    @JoinColumn(name = "from_savings_account_id", nullable = true)
    private SavingsAccount fromSavingsAccount;

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
    @JoinColumn(name = "to_loan_account_id", nullable = true)
    private Loan toLoanAccount;

    @ManyToOne
    @JoinColumn(name = "from_loan_account_id", nullable = true)
    private Loan fromLoanAccount;

    @Column(name = "transfer_type")
    private Integer transferType;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accountTransferDetails", orphanRemoval = true)
    private final List<AccountTransferTransaction> accountTransferTransactions = new ArrayList<>();

    @OneToOne(mappedBy = "accountTransferDetails", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private AccountTransferStandingInstruction accountTransferStandingInstruction;

    public static AccountTransferDetails savingsToSavingsTransfer(final Office fromOffice, final Client fromClient,
            final SavingsAccount fromSavingsAccount, final Office toOffice, final Client toClient, final SavingsAccount toSavingsAccount,
            Integer transferType) {

        return new AccountTransferDetails(fromOffice, fromClient, fromSavingsAccount, null, toOffice, toClient, toSavingsAccount, null,
                transferType, null);
    }

    public static AccountTransferDetails savingsToLoanTransfer(final Office fromOffice, final Client fromClient,
            final SavingsAccount fromSavingsAccount, final Office toOffice, final Client toClient, final Loan toLoanAccount,
            Integer transferType) {
        return new AccountTransferDetails(fromOffice, fromClient, fromSavingsAccount, null, toOffice, toClient, null, toLoanAccount,
                transferType, null);
    }

    public static AccountTransferDetails LoanTosavingsTransfer(final Office fromOffice, final Client fromClient,
            final Loan fromLoanAccount, final Office toOffice, final Client toClient, final SavingsAccount toSavingsAccount,
            Integer transferType) {
        return new AccountTransferDetails(fromOffice, fromClient, null, fromLoanAccount, toOffice, toClient, toSavingsAccount, null,
                transferType, null);
    }

    protected AccountTransferDetails() {
        //
    }

    private AccountTransferDetails(final Office fromOffice, final Client fromClient, final SavingsAccount fromSavingsAccount,
            final Loan fromLoanAccount, final Office toOffice, final Client toClient, final SavingsAccount toSavingsAccount,
            final Loan toLoanAccount, final Integer transferType,
            final AccountTransferStandingInstruction accountTransferStandingInstruction) {
        this.fromOffice = fromOffice;
        this.fromClient = fromClient;
        this.fromSavingsAccount = fromSavingsAccount;
        this.fromLoanAccount = fromLoanAccount;
        this.toOffice = toOffice;
        this.toClient = toClient;
        this.toSavingsAccount = toSavingsAccount;
        this.toLoanAccount = toLoanAccount;
        this.transferType = transferType;
        this.accountTransferStandingInstruction = accountTransferStandingInstruction;
    }

    public SavingsAccount toSavingsAccount() {
        return this.toSavingsAccount;
    }

    public SavingsAccount fromSavingsAccount() {
        return this.fromSavingsAccount;
    }

    public void addAccountTransferTransaction(AccountTransferTransaction accountTransferTransaction) {
        this.accountTransferTransactions.add(accountTransferTransaction);
    }

    public void updateAccountTransferStandingInstruction(final AccountTransferStandingInstruction accountTransferStandingInstruction) {
        this.accountTransferStandingInstruction = accountTransferStandingInstruction;
    }

    public Loan toLoanAccount() {
        return this.toLoanAccount;
    }

    public Loan fromLoanAccount() {
        return this.fromLoanAccount;
    }

    public AccountTransferStandingInstruction accountTransferStandingInstruction() {
        return this.accountTransferStandingInstruction;
    }

    public AccountTransferType transferType() {
        return AccountTransferType.fromInt(this.transferType);
    }

}