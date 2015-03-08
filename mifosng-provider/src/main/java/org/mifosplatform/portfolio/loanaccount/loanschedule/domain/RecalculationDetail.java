/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;

public class RecalculationDetail {

    private LocalDate transactionDate;
    private boolean isProcessed;
    private LoanTransaction transaction;

    public RecalculationDetail(final LocalDate transactionDate, final LoanTransaction transaction) {
        this.transactionDate = transactionDate;
        this.transaction = transaction;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public LoanTransaction getTransaction() {
        return this.transaction;
    }

    public boolean isProcessed() {
        return this.isProcessed;
    }

    public void setProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}
