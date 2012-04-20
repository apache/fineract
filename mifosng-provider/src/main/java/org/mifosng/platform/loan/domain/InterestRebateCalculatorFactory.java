package org.mifosng.platform.loan.domain;

public interface InterestRebateCalculatorFactory {

	InterestRebateCalculator createCalcualtor(
			InterestMethod loanRepaymentScheduleMethod,
			AmortizationMethod amortizationMethod);

}
