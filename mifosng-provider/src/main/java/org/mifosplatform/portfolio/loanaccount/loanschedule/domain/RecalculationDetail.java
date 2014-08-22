/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;

public class RecalculationDetail {

    private boolean isLatePayment;
    private LocalDate startDate;
    private LocalDate toDate;
    private Money amount;

    public RecalculationDetail(final boolean isLatePayment, final LocalDate startDate, final LocalDate toDate, final Money amount) {
        this.isLatePayment = isLatePayment;
        this.startDate = startDate;
        this.toDate = toDate;
        this.amount = amount;
    }

    public boolean isLatePayment() {
        return this.isLatePayment;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getToDate() {
        return this.toDate;
    }

    public Money getAmount() {
        return this.amount;
    }

    public boolean isOverlapping(LocalDate startDate, LocalDate endDate) {
        return !this.startDate.isAfter(endDate) && !startDate.isAfter(this.toDate);
    }

    
    public void updateAmount(Money amount) {
        this.amount = amount;
    }
}
