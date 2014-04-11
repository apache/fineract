package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

public class LoanScheduleAccrualData {

    private final Long loanId;
    private final Long officeId;
    private final LocalDate dueDate;
    private final Long repaymentScheduleId;
    private final Long loanProductId;
    private final BigDecimal interestIncome;
    private final BigDecimal feeIncome;
    private final BigDecimal penaltyIncome;
    private final BigDecimal accruedInterestIncome;
    private final BigDecimal accruedFeeIncome;
    private final BigDecimal accruedPenaltyIncome;
    private final CurrencyData currencyData;

    public LoanScheduleAccrualData(final Long loanId, final Long officeId, final LocalDate dueDate, final Long repaymentScheduleId,
            final Long loanProductId, final BigDecimal interestIncome, final BigDecimal feeIncome, final BigDecimal penaltyIncome,
            final BigDecimal accruedInterestIncome, final BigDecimal accruedFeeIncome, final BigDecimal accruedPenaltyIncome,
            final CurrencyData currencyData) {
        this.loanId = loanId;
        this.officeId = officeId;
        this.dueDate = dueDate;
        this.repaymentScheduleId = repaymentScheduleId;
        this.loanProductId = loanProductId;
        this.interestIncome = interestIncome;
        this.feeIncome = feeIncome;
        this.penaltyIncome = penaltyIncome;
        this.accruedFeeIncome = accruedFeeIncome;
        this.accruedInterestIncome = accruedInterestIncome;
        this.accruedPenaltyIncome = accruedPenaltyIncome;
        this.currencyData = currencyData;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public Date getDueDate() {
        return this.dueDate.toDate();
    }

    public LocalDate getDueDateAsLocaldate() {
        return this.dueDate;
    }

    public Long getRepaymentScheduleId() {
        return this.repaymentScheduleId;
    }

    public Long getLoanProductId() {
        return this.loanProductId;
    }

    public BigDecimal getInterestIncome() {
        return this.interestIncome;
    }

    public BigDecimal getFeeIncome() {
        return this.feeIncome;
    }

    public BigDecimal getPenaltyIncome() {
        return this.penaltyIncome;
    }

    public BigDecimal getAccruedInterestIncome() {
        return this.accruedInterestIncome;
    }

    public BigDecimal getAccruedFeeIncome() {
        return this.accruedFeeIncome;
    }

    public BigDecimal getAccruedPenaltyIncome() {
        return this.accruedPenaltyIncome;
    }

    public CurrencyData getCurrencyData() {
        return this.currencyData;
    }

}
