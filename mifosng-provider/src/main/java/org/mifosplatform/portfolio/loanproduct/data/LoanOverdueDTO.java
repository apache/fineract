/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;

public class LoanOverdueDTO {

    private final Loan loan;
    private final boolean runInterestRecalculation;
    private final LocalDate recalculateFrom;

    public LoanOverdueDTO(final Loan loan, final boolean runInterestRecalculation, final LocalDate recalculateFrom) {
        this.loan = loan;
        this.runInterestRecalculation = runInterestRecalculation;
        this.recalculateFrom = recalculateFrom;
    }

    public boolean isRunInterestRecalculation() {
        return this.runInterestRecalculation;
    }

    public Loan getLoan() {
        return this.loan;
    }

    
    public LocalDate getRecalculateFrom() {
        return this.recalculateFrom;
    }
}
