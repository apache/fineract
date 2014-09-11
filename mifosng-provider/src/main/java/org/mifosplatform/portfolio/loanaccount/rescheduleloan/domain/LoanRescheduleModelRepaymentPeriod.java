/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

public final class LoanRescheduleModelRepaymentPeriod implements LoanRescheduleModalPeriod {
	
	private int periodNumber;
	private int oldPeriodNumber;
    private LocalDate fromDate;
    private LocalDate dueDate;
    private Money principalDue;
    private Money outstandingLoanBalance;
    private Money interestDue;
    private Money feeChargesDue;
    private Money penaltyChargesDue;
    private Money totalDue;
    private boolean isNew;
    
    public LoanRescheduleModelRepaymentPeriod(final int periodNumber, final int oldPeriodNumber, LocalDate fromDate, 
    		final LocalDate dueDate, final Money principalDue, final Money outstandingLoanBalance, final Money interestDue, 
    		final Money feeChargesDue, final Money penaltyChargesDue, final Money totalDue, final boolean isNew) {
    	this.periodNumber = periodNumber;
    	this.oldPeriodNumber = oldPeriodNumber;
    	this.fromDate = fromDate;
    	this.dueDate = dueDate;
    	this.principalDue = principalDue;
    	this.outstandingLoanBalance = outstandingLoanBalance;
    	this.interestDue = interestDue;
    	this.feeChargesDue = feeChargesDue;
    	this.penaltyChargesDue = penaltyChargesDue;
    	this.totalDue = totalDue;
    	this.isNew = isNew;
    }
    
    public static LoanRescheduleModelRepaymentPeriod instance(final int periodNumber, final int oldPeriodNumber, LocalDate fromDate, 
    		final LocalDate dueDate, final Money principalDue, final Money outstandingLoanBalance, final Money interestDue, 
    		final Money feeChargesDue, final Money penaltyChargesDue, final Money totalDue, final boolean isNew) {
    	
    	return new LoanRescheduleModelRepaymentPeriod(periodNumber, oldPeriodNumber, fromDate, dueDate, principalDue, 
    			outstandingLoanBalance, interestDue, feeChargesDue, penaltyChargesDue, totalDue, isNew);
    }

	@Override
	public LoanSchedulePeriodData toData() {
		return LoanSchedulePeriodData.repaymentOnlyPeriod(this.periodNumber, this.fromDate, this.dueDate, this.principalDue.getAmount(),
                this.outstandingLoanBalance.getAmount(), this.interestDue.getAmount(), this.feeChargesDue.getAmount(),
                this.penaltyChargesDue.getAmount(), this.totalDue.getAmount());
	}

	@Override
	public Integer periodNumber() {
		return this.periodNumber;
	}
	
	@Override
	public Integer oldPeriodNumber() {
		return this.oldPeriodNumber;
	}

	@Override
	public LocalDate periodFromDate() {
		return this.fromDate;
	}

	@Override
	public LocalDate periodDueDate() {
		return this.dueDate;
	}

	@Override
	public BigDecimal principalDue() {
		BigDecimal value = null;
		
        if (this.principalDue != null) {
            value = this.principalDue.getAmount();
        }

        return value;
	}

	@Override
	public BigDecimal interestDue() {
		BigDecimal value = null;
		
        if (this.interestDue != null) {
            value = this.interestDue.getAmount();
        }

        return value;
	}

	@Override
	public BigDecimal feeChargesDue() {
		BigDecimal value = null;
		
        if (this.feeChargesDue != null) {
            value = this.feeChargesDue.getAmount();
        }

        return value;
	}

	@Override
	public BigDecimal penaltyChargesDue() {
		BigDecimal value = null;
		
        if (this.penaltyChargesDue != null) {
            value = this.penaltyChargesDue.getAmount();
        }

        return value;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}
	
	public void updatePeriodNumber(Integer periodNumber) {
		this.periodNumber = periodNumber;
	}
	
	public void updateOldPeriodNumber(Integer oldPeriodNumber) {
		this.oldPeriodNumber = oldPeriodNumber;
	}
    
    public void updatePeriodFromDate(LocalDate periodFromDate) {
    	this.fromDate = periodFromDate;
    }
    
    public void updatePeriodDueDate(LocalDate periodDueDate) {
    	this.dueDate = periodDueDate;
    }
    
    public void updatePrincipalDue(Money principalDue) {
    	this.principalDue = principalDue;
    }
    
    public void updateInterestDue(Money interestDue) {
    	this.interestDue = interestDue;
    }
    
    public void updateFeeChargesDue(Money feeChargesDue) {
    	this.feeChargesDue = feeChargesDue;
    }
    
    public void updatePenaltyChargesDue(Money penaltyChargesDue) {
    	this.penaltyChargesDue = penaltyChargesDue;
    }
    
    public void updateOutstandingLoanBalance(Money outstandingLoanBalance) {
    	this.outstandingLoanBalance = outstandingLoanBalance;
    }
    
    public void updateTotalDue(Money totalDue) {
    	this.totalDue = totalDue;
    }

	public void updateIsNew(boolean isNew) {
		this.isNew = isNew;
	}
}
