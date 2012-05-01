package org.mifosng.data.command;

import java.math.BigDecimal;

public interface LoanProductCommandData {

	String getName();

	String getDescription();

	String getCurrencyCode();

	Integer getDigitsAfterDecimal();

	Integer getRepaymentEvery();

	Integer getRepaymentFrequency();

	Boolean isFlexibleRepaymentSchedule();

	Boolean isInterestRebateAllowed();

	BigDecimal getPrincipal();

	Boolean getFlexibleRepaymentSchedule();

	Boolean getInterestRebateAllowed();

	Integer getInterestRateFrequencyMethod();

	Integer getInterestMethod();

	Integer getAmortizationMethod();

	Integer getNumberOfRepayments();

	BigDecimal getInterestRatePerPeriod();

	CommonLoanProperties getCommonLoanProperties();

	BigDecimal getInArrearsToleranceAmount();
	
	Integer getInterestCalculationPeriodMethod();
}