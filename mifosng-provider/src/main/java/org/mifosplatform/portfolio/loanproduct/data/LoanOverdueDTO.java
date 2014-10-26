package org.mifosplatform.portfolio.loanproduct.data;

import org.mifosplatform.portfolio.loanaccount.domain.Loan;

public class LoanOverdueDTO {

    private final Loan loan;
    private final boolean runInterestRecalculation;

    public LoanOverdueDTO(final Loan loan, final boolean runInterestRecalculation) {
        this.loan = loan;
        this.runInterestRecalculation = runInterestRecalculation;
    }

    public boolean isRunInterestRecalculation() {
        return this.runInterestRecalculation;
    }

    public Loan getLoan() {
        return this.loan;
    }
}
