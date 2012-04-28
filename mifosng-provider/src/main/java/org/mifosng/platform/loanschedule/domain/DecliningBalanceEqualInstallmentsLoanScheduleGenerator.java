package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.MoneyData;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public class DecliningBalanceEqualInstallmentsLoanScheduleGenerator implements
		LoanScheduleGenerator {

	private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

	@Override
	public LoanSchedule generate(
			final LoanProductRelatedDetail loanScheduleInfo,
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate, 
			final LocalDate interestCalculatedFrom, CurrencyData currencyData) {

		List<ScheduledLoanInstallment> scheduledLoanInstallments = new ArrayList<ScheduledLoanInstallment>();

		List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(
				loanScheduleInfo, disbursementDate, firstRepaymentDate);
		
		LocalDate idealDisbursementDateBasedOnFirstRepaymentDate = this.scheduledDateGenerator.idealDisbursementDateBasedOnFirstRepaymentDate(loanScheduleInfo, scheduledDates);
		
		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);

		BigDecimal paymentPeriodsInYear = BigDecimal
				.valueOf(this.paymentPeriodsInOneYearCalculator.calculate(loanScheduleInfo.getRepaymentPeriodFrequencyType()));
		
		BigDecimal periodicInterestRate = loanScheduleInfo
				.getAnnualNominalInterestRate()
				.divide(paymentPeriodsInYear, mc)
				.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
				.multiply(BigDecimal.valueOf(loanScheduleInfo.getRepayEvery()));
		
		BigDecimal dailyInterestRate = loanScheduleInfo.getAnnualNominalInterestRate()
										.divide(BigDecimal.valueOf(Long.valueOf(365)), mc)
										.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
										.multiply(BigDecimal.valueOf(loanScheduleInfo.getRepayEvery()));
		
		double interestRateFraction = periodicInterestRate.doubleValue();
	
		double futureValue = 0;
		double numberOfPeriods = loanScheduleInfo.getNumberOfRepayments().doubleValue();
		double principal = loanScheduleInfo.getPrincipal().getAmount().multiply(BigDecimal.valueOf(-1)).doubleValue();
		
		double paymentPerInstallment = pmt(interestRateFraction, numberOfPeriods, principal, futureValue, false);
		double totalRepayment = paymentPerInstallment * numberOfPeriods;
		
		final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
		
		Money totalDuePerInstallment = Money.of(monetaryCurrency, BigDecimal.valueOf(paymentPerInstallment));
		Money totalDue = Money.of(monetaryCurrency, BigDecimal.valueOf(totalRepayment));
		
		Money totalInterestDue = totalDue.minus(loanScheduleInfo.getPrincipal());
		Money outstandingBalance = loanScheduleInfo.getPrincipal();
		Money totalPrincipal = Money.zero(monetaryCurrency);
		Money totalInterest = Money.zero(monetaryCurrency);
		
		double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator.calculateRepaymentPeriodAsAFractionOfDays(loanScheduleInfo.getRepaymentPeriodFrequencyType(), 
												loanScheduleInfo.getRepayEvery(), interestCalculatedFrom, scheduledDates, idealDisbursementDateBasedOnFirstRepaymentDate);

		LocalDate startDate = disbursementDate;
		int installmentNumber = 1;
		for (LocalDate scheduledDueDate : scheduledDates) {

			// number of days from startDate to this scheduledDate
			int daysInPeriod = Days.daysBetween(startDate.toDateMidnight().toDateTime(), scheduledDueDate.toDateMidnight().toDateTime()).getDays();
			
			BigDecimal interestRateToApply = dailyInterestRate.multiply(BigDecimal.valueOf(Long.valueOf(daysInPeriod)));
			
			Money interestDueBasedOnDays = outstandingBalance.multiplyRetainScale(interestRateToApply, RoundingMode.HALF_EVEN);
			
			Money interestPerInstallment = interestDueBasedOnDays; //outstandingBalance.multiplyRetainScale(periodicInterestRate, RoundingMode.HALF_EVEN);
			Money principalPerInstallment = totalDuePerInstallment.minus(interestPerInstallment);
			
			if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
				Money graceOnInterestForRepaymentPeriod = interestPerInstallment;
				interestPerInstallment = interestPerInstallment.minus(graceOnInterestForRepaymentPeriod);
				totalInterestDue = totalInterestDue.minus(graceOnInterestForRepaymentPeriod);
				interestCalculationGraceOnRepaymentPeriodFraction = interestCalculationGraceOnRepaymentPeriodFraction - Integer.valueOf(1).doubleValue();
			} else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25") && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {
				Money graceOnInterestForRepaymentPeriod = interestPerInstallment.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
				interestPerInstallment = interestPerInstallment.minus(graceOnInterestForRepaymentPeriod);
				totalInterestDue = totalInterestDue.minus(graceOnInterestForRepaymentPeriod);
				interestCalculationGraceOnRepaymentPeriodFraction = Double.valueOf("0");
			}
			
			totalPrincipal = totalPrincipal.plus(principalPerInstallment);
			totalInterest = totalInterest.plus(interestPerInstallment);

			if (installmentNumber == loanScheduleInfo.getNumberOfRepayments()) {
				Money principalDifference = totalPrincipal.minus(loanScheduleInfo
						.getPrincipal());
				if (principalDifference.isLessThanZero()) {
					principalPerInstallment = principalPerInstallment
							.plus(principalDifference.abs());
				} else if (principalDifference.isGreaterThanZero()) {
					principalPerInstallment = principalPerInstallment
							.minus(principalDifference.abs());
				}
				
				Money interestDifference = totalInterest.minus(totalInterestDue);
				if (interestDifference.isLessThanZero()) {
					interestPerInstallment = interestPerInstallment
							.plus(interestDifference.abs());
				} else if (interestDifference.isGreaterThanZero()) {
					interestPerInstallment = interestPerInstallment
							.minus(interestDifference.abs());
				}
			}

			Money totalInstallmentDue = principalPerInstallment.plus(interestPerInstallment);

			outstandingBalance = outstandingBalance.minus(principalPerInstallment);
			
			MoneyData principalPerInstallmentValue = MoneyData.of(currencyData, principalPerInstallment.getAmount());
			MoneyData interestPerInstallmentValue = MoneyData.of(currencyData, interestPerInstallment.getAmount());
			MoneyData totalInstallmentDueValue = MoneyData.of(currencyData, totalInstallmentDue.getAmount());
			MoneyData outstandingBalanceValue = MoneyData.of(currencyData, outstandingBalance.getAmount());
					
			ScheduledLoanInstallment installment = new ScheduledLoanInstallment(
					Integer.valueOf(installmentNumber), startDate,
					scheduledDueDate, principalPerInstallmentValue,
					interestPerInstallmentValue, 
					totalInstallmentDueValue,
					outstandingBalanceValue);

			scheduledLoanInstallments.add(installment);

			startDate = scheduledDueDate;

			installmentNumber++;
		}

		return new LoanSchedule(scheduledLoanInstallments);
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