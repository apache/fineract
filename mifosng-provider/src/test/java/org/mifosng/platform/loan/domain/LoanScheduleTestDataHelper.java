package org.mifosng.platform.loan.domain;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;

/**
 * Helper class for creating loan schedule data suitable for testing.
 */
public class LoanScheduleTestDataHelper {

	public static List<LoanRepaymentScheduleInstallment> createSimpleLoanSchedule(final LocalDate firstDueDate, final MonetaryCurrency currency) {
		LoanRepaymentScheduleInstallment firstInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
		.withDueDate(firstDueDate)
		.withPrincipal("1000.00")
		.withInterest("200.00")
		.build();

		LoanRepaymentScheduleInstallment secondInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
				.withDueDate(firstDueDate.plusMonths(1))
				.withPrincipal("1000.00")
				.withInterest("200.00")
				.build();
		
		LoanRepaymentScheduleInstallment thirdInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
		.withDueDate(firstDueDate.plusMonths(2))
		.withPrincipal("1000.00")
		.withInterest("200.00")
		.build();
		
		return Arrays.asList(firstInstallment, secondInstallment, thirdInstallment);
	}

	public static List<LoanRepaymentScheduleInstallment> createSimpleLoanScheduleWithFirstInstallmentFullyPaid(final LocalDate firstDueDate, final MonetaryCurrency currency) {
		
		LoanRepaymentScheduleInstallment firstInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
															.withDueDate(firstDueDate)
															.withPrincipal("1000.00")
															.withInterest("200.00")
															.completed()
															.build();

		LoanRepaymentScheduleInstallment secondInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
															.withDueDate(firstDueDate.plusMonths(1))
															.withPrincipal("1000.00")
															.withInterest("200.00")
															.build();
		
		LoanRepaymentScheduleInstallment thirdInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
															.withDueDate(firstDueDate.plusMonths(2))
															.withPrincipal("1000.00")
															.withInterest("200.00")
															.build();
		
		return Arrays.asList(firstInstallment, secondInstallment, thirdInstallment);
	}

}
