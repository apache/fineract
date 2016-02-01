/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;

public class OverdueLoanScheduleData {

    private final Long loanId;
    private final Long chargeId;
    private final String locale;
    private final BigDecimal amount;
    private final String dateFormat;
    private final String dueDate;
    private final BigDecimal principalOverdue;
    private final BigDecimal interestOverdue;
    private final Integer periodNumber;

    public OverdueLoanScheduleData(final Long loanId, final Long chargeId, final String dueDate, final BigDecimal amount,
            final String dateFormat, final String locale, final BigDecimal principalOverdue, final BigDecimal interestOverdue,
            final Integer periodNumber) {
        this.loanId = loanId;
        this.chargeId = chargeId;
        this.dueDate = dueDate;
        this.amount = amount;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.principalOverdue = principalOverdue;
        this.interestOverdue = interestOverdue;
        this.periodNumber = periodNumber;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public String getDueDate() {
        return this.dueDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public String getLocale() {
        return this.locale;
    }


    public Integer getPeriodNumber() {
        return this.periodNumber;
    }
    
    @Override
    public String toString() {
        return "{" + "chargeId:" + this.chargeId + ", locale:'" + this.locale + '\'' + ", amount:" + this.amount + ", dateFormat:'"
                + this.dateFormat + '\'' + ", dueDate:'" + this.dueDate + '\'' + ", principal:'" + this.principalOverdue + '\''
                + ", interest:'" + this.interestOverdue + '\'' + '}';
    }

}
