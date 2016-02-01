/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_portfolio_account_associations")
public class AccountAssociations extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_account_id", nullable = true)
    private Loan loanAccount;

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

    @ManyToOne
    @JoinColumn(name = "linked_loan_account_id", nullable = true)
    private Loan linkedLoanAccount;

    @ManyToOne
    @JoinColumn(name = "linked_savings_account_id", nullable = true)
    private SavingsAccount linkedSavingsAccount;

    @Column(name = "association_type_enum", nullable = false)
    private Integer associationType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected AccountAssociations() {}

    private AccountAssociations(final Loan loanAccount, final SavingsAccount savingsAccount, final Loan linkedLoanAccount,
            final SavingsAccount linkedSavingsAccount, final Integer associationType, boolean active) {
        this.loanAccount = loanAccount;
        this.savingsAccount = savingsAccount;
        this.linkedLoanAccount = linkedLoanAccount;
        this.linkedSavingsAccount = linkedSavingsAccount;
        this.associationType = associationType;
        this.active = active;
    }

    public static AccountAssociations associateSavingsAccount(final Loan loan, final SavingsAccount savingsAccount,
            final Integer associationType, boolean isActive) {
        return new AccountAssociations(loan, null, null, savingsAccount, associationType, isActive);
    }

    public static AccountAssociations associateSavingsAccount(final SavingsAccount savingsAccount,
            final SavingsAccount linkedSavingsAccount, final Integer associationType, boolean isActive) {
        return new AccountAssociations(null, savingsAccount, null, linkedSavingsAccount, associationType, isActive);
    }

    public SavingsAccount linkedSavingsAccount() {
        return this.linkedSavingsAccount;
    }

    public void updateLinkedSavingsAccount(final SavingsAccount savingsAccount) {
        this.linkedSavingsAccount = savingsAccount;
    }
}
