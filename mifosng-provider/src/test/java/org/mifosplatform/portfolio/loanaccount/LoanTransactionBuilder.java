package org.mifosplatform.portfolio.loanaccount;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;

public class LoanTransactionBuilder {

    @SuppressWarnings("unused")
    private Money transactionAmount = new MoneyBuilder().build();
    @SuppressWarnings("unused")
    private LocalDate transactionDate = LocalDate.now();
    @SuppressWarnings("unused")
    private boolean repayment = false;

    /**
     * public LoanTransaction build() {
     * 
     * LoanTransaction transaction = null;
     * 
     * if (repayment) { transaction =
     * LoanTransaction.repayment(transactionAmount, transactionDate); }
     * 
     * return transaction; }
     **/

    public LoanTransactionBuilder with(final Money newAmount) {
        this.transactionAmount = newAmount;
        return this;
    }

    public LoanTransactionBuilder with(final LocalDate withTransactionDate) {
        this.transactionDate = withTransactionDate;
        return this;
    }

    public LoanTransactionBuilder repayment() {
        this.repayment = true;
        return this;
    }
}
