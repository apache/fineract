package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.LoanScheduleNewData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

/**
 * <p>
 * Declining balance can amortized (see {@link AmortizationMethod}) in two ways at present:
 * <ol><li>Equal principal payments</li><li>Equal installment payments</li></ol>
 * </p>
 * 
 * <p>
 * When amortized using <i>equal principal payments</i>, the <b>principal component</b> of each installment is fixed and <b>interest due</b> 
 * is calculated from the <b>outstanding principal balance</b> resulting in a different <b>total payment due</b> for each installment.
 * </p>
 * 
 * <p>
 * When amortized using <i>equal installments</i>, the <b>total payment due</b> for each installment is fixed and 
 * is calculated using the excel like <code>pmt</code> function. The <b>interest due</b> is calculated from the <b>outstanding principal balance</b>
 * which results in a <b>principal component</b> that is <b>total payment due</b> minus <b>interest due</b>.
 * </p> 
 */
public class DecliningBalanceMethodLoanScheduleGenerator implements LoanScheduleGenerator {

	private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
	private final PeriodicInterestRateCalculator periodicInterestRateCalculator = new PeriodicInterestRateCalculator();
	private final AmortizationLoanScheduleGeneratorFactory amortizationLoanScheduleGeneratorFactory = new AmortizationLoanScheduleGeneratorFactory();
	
	@Override
	public LoanScheduleNewData generate(
			final ApplicationCurrency applicationCurrency,
			final LoanProductRelatedDetail loanScheduleInfo,
			final Integer loanTermFrequency, 
			final PeriodFrequencyType loanTermFrequencyType, 
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate,
			final LocalDate interestCalculatedFrom) {
		
		// 1. generate valid set of 'due dates' based on some of the 'loan attributes'
		final List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);
		
		final LocalDate idealDisbursementDateBasedOnFirstRepaymentDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(loanScheduleInfo, scheduledDates);
		
		// 2. determine the 'periodic' interest rate based on the 'repayment periods' so we can use 
		final BigDecimal periodInterestRateForRepaymentPeriod = this.periodicInterestRateCalculator.calculateFrom(loanScheduleInfo);
		
		// Determine with 'amortisation' approach to use
		final AmortizationLoanScheduleGenerator generator = this.amortizationLoanScheduleGeneratorFactory.createGenerator(loanScheduleInfo.getAmortizationMethod());
		
		return generator.generate(applicationCurrency,
				loanScheduleInfo, 
				disbursementDate, 
				firstRepaymentDate,
				interestCalculatedFrom,
				periodInterestRateForRepaymentPeriod, 
				idealDisbursementDateBasedOnFirstRepaymentDate, 
				scheduledDates);
	}
}