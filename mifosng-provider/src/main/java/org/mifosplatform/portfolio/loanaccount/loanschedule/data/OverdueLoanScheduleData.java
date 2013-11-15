package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;

public class OverdueLoanScheduleData {

    private final Long loanId;
    private final Long chargeId;
    private final String locale;
    private final BigDecimal amount;
    private final String dateFormat;
    private final String dueDate;

    public OverdueLoanScheduleData(final Long loanId, final Long chargeId, final String dueDate, final BigDecimal amount,
            final String dateFormat, final String locale) {
        this.loanId = loanId;
        this.chargeId = chargeId;
        this.dueDate = dueDate;
        this.amount = amount;
        this.dateFormat = dateFormat;
        this.locale = locale;
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

    @Override
    public String toString() {
        return "{" + "chargeId:" + this.chargeId + ", locale:'" + this.locale + '\'' + ", amount:" + this.amount + ", dateFormat:'"
                + this.dateFormat + '\'' + ", dueDate:'" + this.dueDate + '\'' + '}';
    }
}
