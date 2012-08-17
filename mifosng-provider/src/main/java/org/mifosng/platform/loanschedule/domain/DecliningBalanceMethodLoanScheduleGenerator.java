package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
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
	public LoanSchedule generate(
			final LoanProductRelatedDetail loanScheduleInfo,
			final Integer loanTermFrequency, 
			final PeriodFrequencyType loanTermFrequencyType, 
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate, 
			final LocalDate interestCalculatedFrom, 
			final CurrencyData currencyData) {

		// 1. generate valid set of 'due dates' based on some of the 'loan attributes'
		List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);
		
		LocalDate idealDisbursementDateBasedOnFirstRepaymentDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(loanScheduleInfo, scheduledDates);
		
		// 2. determine the 'periodic' interest rate based on the 'repayment periods' so we can use 
		final BigDecimal periodInterestRateForRepaymentPeriod = this.periodicInterestRateCalculator.calculateFrom(loanScheduleInfo);
		
		// Determine with 'amortisation' approach to use
		AmortizationLoanScheduleGenerator generator = this.amortizationLoanScheduleGeneratorFactory.createGenerator(loanScheduleInfo.getAmortizationMethod());
		
		return generator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate, interestCalculatedFrom, currencyData,
				periodInterestRateForRepaymentPeriod, idealDisbursementDateBasedOnFirstRepaymentDate, scheduledDates);
	}

	/**
	 * Future value of an amount given the number of payments, rate, amount of
	 * individual payment, present value and boolean value indicating whether
	 * payments are due at the beginning of period (false => payments are due at
	 * end of period)
	 * 
	 * @param r
	 *            rate
	 * @param n
	 *            num of periods
	 * @param y
	 *            pmt per period
	 * @param p
	 *            future value
	 * @param t
	 *            type (true=pmt at end of period, false=pmt at begining of
	 *            period)
	 */
	public static double fv(double r, double n, double y, double p, boolean t) {
		double retval = 0;
		if (r == 0) {
			retval = -1 * (p + (n * y));
		} else {
			double r1 = r + 1;
			retval = ((1 - Math.pow(r1, n)) * (t ? r1 : 1) * y) / r - p
					* Math.pow(r1, n);
		}
		return retval;
	}

	/**
	 * Present value of an amount given the number of future payments, rate,
	 * amount of individual payment, future value and boolean value indicating
	 * whether payments are due at the beginning of period (false => payments
	 * are due at end of period)
	 * 
	 * @param r
	 * @param n
	 * @param y
	 * @param f
	 * @param t
	 */
	public static double pv(double r, double n, double y, double f, boolean t) {
		double retval = 0;
		if (r == 0) {
			retval = -1 * ((n * y) + f);
		} else {
			double r1 = r + 1;
			retval = (((1 - Math.pow(r1, n)) / r) * (t ? r1 : 1) * y - f)
					/ Math.pow(r1, n);
		}
		return retval;
	}

	/**
	 * calculates the Net Present Value of a principal amount given the discount
	 * rate and a sequence of cash flows (supplied as an array). If the amounts
	 * are income the value should be positive, else if they are payments and
	 * not income, the value should be negative.
	 * 
	 * @param r
	 * @param cfs
	 *            cashflow amounts
	 */
	public static double npv(double r, double[] cfs) {
		double npv = 0;
		double r1 = r + 1;
		double trate = r1;
		for (int i = 0, iSize = cfs.length; i < iSize; i++) {
			npv += cfs[i] / trate;
			trate *= r1;
		}
		return npv;
	}

	/**
	 * PMT calculates a fixed monthly payment to be paid by borrower every 'period' to ensure loan is paid off in full (with interest).
	 * 
	 * This monthly payment c depends upon
	 * the monthly interest rate r (expressed as a fraction, not a percentage,
	 * i.e., divide the quoted yearly percentage rate by 100 and by 12 to obtain
	 * the monthly interest rate), the number of monthly payments N called the
	 * loan's term, and the amount borrowed P known as the loan's principal; c
	 * is given by the formula:
	 * 
	 * c = (r / (1 - (1 + r)^-N))P
	 * 
	 * @param interestRateFraction
	 * @param numberOfPayments
	 * @param principal
	 * @param futureValue
	 * @param type
	 */
	public static double pmt(double interestRateFraction, double numberOfPayments, double principal, double futureValue, boolean type) {
		double payment = 0;
		if (interestRateFraction == 0) {
			payment = -1 * (futureValue + principal) / numberOfPayments;
		} else {
			double r1 = interestRateFraction + 1;
			payment = (futureValue + principal * Math.pow(r1, numberOfPayments)) * interestRateFraction
					/ ((type ? r1 : 1) * (1 - Math.pow(r1, numberOfPayments)));
		}
		return payment;
	}

	/**
	 * 
	 * @param r
	 * @param y
	 * @param p
	 * @param f
	 * @param t
	 */
	public static double nper(double r, double y, double p, double f, boolean t) {
		double retval = 0;
		if (r == 0) {
			retval = -1 * (f + p) / y;
		} else {
			double r1 = r + 1;
			double ryr = (t ? r1 : 1) * y / r;
			double a1 = ((ryr - f) < 0) ? Math.log(f - ryr) : Math.log(ryr - f);
			double a2 = ((ryr - f) < 0) ? Math.log(-p - ryr) : Math
					.log(p + ryr);
			double a3 = Math.log(r1);
			retval = (a1 - a2) / a3;
		}
		return retval;
	}
}