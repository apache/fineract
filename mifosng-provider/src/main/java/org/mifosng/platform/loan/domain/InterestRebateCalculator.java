package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.Money;

@Deprecated
public interface InterestRebateCalculator {

	Money calculate(
			LocalDate actualDisbursementDate,
			LocalDate paidInFullDate,
			Money loanPrincipal,
			BigDecimal interestRatePerAnnum,
			List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
			List<LoanTransaction> loanRepayments);

}
