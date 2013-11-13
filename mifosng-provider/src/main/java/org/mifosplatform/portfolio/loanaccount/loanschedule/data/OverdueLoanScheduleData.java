package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 13-11-13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class OverdueLoanScheduleData {
    private final Long loanId;
    private final Long chargeId;
    private final String locale;
    private final BigDecimal amount;
    private final String dateFormat;
    private final String dueDate;



    public OverdueLoanScheduleData(Long loanId, Long chargeId, String dueDate, BigDecimal amount, String dateFormat, String locale) {
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
        return "{" +
                "chargeId:" + chargeId +
                ", locale:'" + locale + '\'' +
                ", amount:" + amount +
                ", dateFormat:'" + dateFormat + '\'' +
                ", dueDate:'" + dueDate + '\'' +
                '}';
    }
}
