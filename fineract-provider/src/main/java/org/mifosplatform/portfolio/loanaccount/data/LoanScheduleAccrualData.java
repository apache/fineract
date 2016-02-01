/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

public class LoanScheduleAccrualData {

    private final Long loanId;
    private final Long officeId;
    private final LocalDate accruedTill;
    private final PeriodFrequencyType repaymentFrequency;
    private final Integer repayEvery;
    private final Integer installmentNumber;
    private final LocalDate dueDate;
    private final LocalDate fromDate;
    private final Long repaymentScheduleId;
    private final Long loanProductId;
    private final BigDecimal interestIncome;
    private final BigDecimal feeIncome;
    private final BigDecimal penaltyIncome;
    private final BigDecimal waivedInterestIncome;
    private final BigDecimal accruedInterestIncome;
    private final BigDecimal accruedFeeIncome;
    private final BigDecimal accruedPenaltyIncome;
    private final CurrencyData currencyData;
    private final LocalDate interestCalculatedFrom;

    private Map<LoanChargeData, BigDecimal> applicableCharges;
    private BigDecimal dueDateFeeIncome;
    private BigDecimal dueDatePenaltyIncome;
    private BigDecimal accruableIncome;

    public LoanScheduleAccrualData(final Long loanId, final Long officeId, final Integer installmentNumber, final LocalDate accruedTill,
            final PeriodFrequencyType repaymentFrequency, final Integer repayEvery, final LocalDate dueDate, final LocalDate fromDate,
            final Long repaymentScheduleId, final Long loanProductId, final BigDecimal interestIncome, final BigDecimal feeIncome,
            final BigDecimal penaltyIncome, final BigDecimal accruedInterestIncome, final BigDecimal accruedFeeIncome,
            final BigDecimal accruedPenaltyIncome, final CurrencyData currencyData, final LocalDate interestCalculatedFrom,
            final BigDecimal waivedInterestIncome) {
        this.loanId = loanId;
        this.installmentNumber = installmentNumber;
        this.officeId = officeId;
        this.accruedTill = accruedTill;
        this.dueDate = dueDate;
        this.fromDate = fromDate;
        this.repaymentScheduleId = repaymentScheduleId;
        this.loanProductId = loanProductId;
        this.interestIncome = interestIncome;
        this.feeIncome = feeIncome;
        this.penaltyIncome = penaltyIncome;
        this.accruedFeeIncome = accruedFeeIncome;
        this.accruedInterestIncome = accruedInterestIncome;
        this.accruedPenaltyIncome = accruedPenaltyIncome;
        this.currencyData = currencyData;
        this.repaymentFrequency = repaymentFrequency;
        this.repayEvery = repayEvery;
        this.interestCalculatedFrom = interestCalculatedFrom;
        this.waivedInterestIncome = waivedInterestIncome;
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

    public LocalDate getAccruedTill() {
        return this.accruedTill;
    }

    public LocalDate getFromDateAsLocaldate() {
        return this.fromDate;
    }

    public PeriodFrequencyType getRepaymentFrequency() {
        return this.repaymentFrequency;
    }

    public Integer getRepayEvery() {
        return this.repayEvery;
    }

    public LocalDate getInterestCalculatedFrom() {
        return this.interestCalculatedFrom;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public Map<LoanChargeData, BigDecimal> getApplicableCharges() {
        return this.applicableCharges;
    }

    public BigDecimal getDueDateFeeIncome() {
        return this.dueDateFeeIncome;
    }

    public BigDecimal getDueDatePenaltyIncome() {
        return this.dueDatePenaltyIncome;
    }

    public void updateChargeDetails(final Map<LoanChargeData, BigDecimal> applicableCharges, final BigDecimal dueDateFeeIncome,
            final BigDecimal dueDatePenaltyIncome) {
        this.applicableCharges = applicableCharges;
        this.dueDateFeeIncome = dueDateFeeIncome;
        this.dueDatePenaltyIncome = dueDatePenaltyIncome;
    }

    
    public BigDecimal getWaivedInterestIncome() {
        return this.waivedInterestIncome;
    }

    
    public BigDecimal getAccruableIncome () {
        return this.accruableIncome;
    }

    
    public void updateAccruableIncome (BigDecimal accruableIncome ) {
        this.accruableIncome = accruableIncome ;
    }


}
