package org.mifosplatform.portfolio.account.domain;

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

    protected AccountAssociations() {}

    private AccountAssociations(final Loan loanAccount, final SavingsAccount savingsAccount, final Loan linkedLoanAccount,
            final SavingsAccount linkedSavingsAccount) {
        this.loanAccount = loanAccount;
        this.savingsAccount = savingsAccount;
        this.linkedLoanAccount = linkedLoanAccount;
        this.linkedSavingsAccount = linkedSavingsAccount;
    }

    public static AccountAssociations associateSavingsAccount(Loan loan, SavingsAccount savingsAccount) {
        return new AccountAssociations(loan, null, null, savingsAccount);
    }

    public SavingsAccount linkedSavingsAccount() {
        return this.linkedSavingsAccount;
    }

    public void updateLinkedSavingsAccount(final SavingsAccount savingsAccount) {
        this.linkedSavingsAccount = savingsAccount;
    }
}
