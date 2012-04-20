package org.mifosng.data.command;

import java.math.BigDecimal;

public interface LoanProductCommandData {

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getCurrencyCode();

	public abstract Integer getDigitsAfterDecimal();

	public abstract Integer getRepaymentEvery();

	public abstract Integer getRepaymentFrequency();

	public abstract Boolean isFlexibleRepaymentSchedule();

	public abstract Boolean isInterestRebateAllowed();

	public abstract BigDecimal getPrincipal();

	public abstract Boolean getFlexibleRepaymentSchedule();

	public abstract Boolean getInterestRebateAllowed();

	public abstract Integer getInterestRateFrequencyMethod();

	public abstract Integer getInterestMethod();

	public abstract Integer getAmortizationMethod();

	public abstract Integer getNumberOfRepayments();

	public abstract BigDecimal getInterestRatePerPeriod();

	public abstract CommonLoanProperties getCommonLoanProperties();

	public abstract BigDecimal getInArrearsToleranceAmount();

}