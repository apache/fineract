package org.mifosng.platform.loan.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.DerivedLoanData;
import org.mifosng.data.LoanAccountSummaryData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanRepaymentDataComparator;
import org.mifosng.data.LoanRepaymentPeriodData;
import org.mifosng.data.LoanRepaymentScheduleData;
import org.mifosng.data.MoneyData;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

// FIXME - The derived details of the loan schedule are now updated on the fly when someone does a transaction against a loan. So this is not fully needed.
//       - we still use this to figure out how much of a given transactions amount was principal versus interest but might not need this and we should probably
//       - derive and update that on the database on the fly
public class DerivedLoanDataProcessor {

	public DerivedLoanData process(List<LoanRepaymentScheduleInstallment> repaymentSchedulePeriods, List<LoanTransaction> loanTransactions, 
			CurrencyData currencyDetail, Money arrearsTolerance) {
		
		// 1. derive repayment schedule
		LoanRepaymentScheduleData repaymentScheduleDetails = generateRepaymentScheduleData(repaymentSchedulePeriods, currencyDetail);
		List<LoanRepaymentPeriodData> loanRepaymentPeriods = repaymentScheduleDetails.getPeriods();
		 
		// break up payments based on principal and interest
		List<LoanRepaymentData> loanRepayments = new ArrayList<LoanRepaymentData>();
		int repaymentScheduleIndex = 0;
		for (LoanTransaction transaction : loanTransactions) {
			if (transaction.isRepayment() || transaction.isWaiver()) {

				MonetaryCurrency currency = new MonetaryCurrency(currencyDetail.getCode(), currencyDetail.getDecimalPlaces());
				
				if (transaction.getAmount(currency).isGreaterThanZero()) {
					LoanRepaymentPeriodData scheduledRepaymentPeriod = loanRepaymentPeriods
							.get(repaymentScheduleIndex);

					if (transaction.isRepayment()) {
						LoanRepaymentData repaymentData = processLoanRepayment(
								transaction, scheduledRepaymentPeriod,
								repaymentScheduleIndex, loanRepaymentPeriods,
								arrearsTolerance, currencyDetail);
						loanRepayments.add(repaymentData);
					}

					if (transaction.isWaiver()) {
						LoanRepaymentData repaymentData = processWaiver(
								transaction, scheduledRepaymentPeriod,
								repaymentScheduleIndex, loanRepaymentPeriods,
								arrearsTolerance, currencyDetail);
						loanRepayments.add(repaymentData);
					}

					if (scheduledRepaymentPeriod.isFullyPaid()) {
						repaymentScheduleIndex++;
					}
				}
			}
		}
		
		loanRepaymentPeriods = calculateArrears(loanRepaymentPeriods, arrearsTolerance, currencyDetail);
		
		LoanRepaymentScheduleData processRepaymentScheduleData = new LoanRepaymentScheduleData(loanRepaymentPeriods);
		LoanAccountSummaryData summaryData = calculateTotalsFromRepaymentScheduleData(loanRepaymentPeriods, currencyDetail);
		
		Collections.sort(loanRepayments, new LoanRepaymentDataComparator());
		
		if (loanRepayments.isEmpty()) {
			loanRepayments = null;
		}
		
		return new DerivedLoanData(processRepaymentScheduleData, summaryData, loanRepayments);
	}
	
	private LoanRepaymentData processWaiver(LoanTransaction transaction,
			LoanRepaymentPeriodData scheduledRepaymentPeriod,
			int repaymentScheduleIndex,
			List<LoanRepaymentPeriodData> loanRepaymentPeriods,
			Money arrearsTolerance, CurrencyData currencyData) {
		
		MonetaryCurrency currency = new MonetaryCurrency(currencyData.getCode(), currencyData.getDecimalPlaces());
		Money totalToWaive = transaction.getAmount(currency);
		
		Money interestToWaive = moneyFrom(scheduledRepaymentPeriod.getInterestOutstanding());
		Money principalToWaive = moneyFrom(scheduledRepaymentPeriod.getPrincipalOutstanding());
		
		Money interestWaived = Money.zero(currency);
		
		if (totalToWaive.isGreaterThanOrEqualTo(interestToWaive)) {
			totalToWaive = totalToWaive.minus(interestToWaive);
			interestWaived = interestToWaive;
		} else {
			interestWaived = totalToWaive;
			totalToWaive = Money.zero(currency);
		}
		
		Money principalWaived = Money.zero(currency);
		if (totalToWaive.isGreaterThanOrEqualTo(principalToWaive)) {
			totalToWaive = totalToWaive.minus(principalToWaive);
			principalWaived = principalToWaive;
		} else {
			principalWaived = totalToWaive;
			totalToWaive = Money.zero(currency);
		}
		
		MoneyData zero = moneyDataFrom(currencyData, Money.zero(currency));

		Money totalWaivedInPeriodToDate = Money.zero(currency);
		if (scheduledRepaymentPeriod.getTotalWaived() != null) {
			totalWaivedInPeriodToDate = moneyFrom(scheduledRepaymentPeriod.getTotalWaived());
		}
		Money totalWaivedInTransaction = interestWaived.plus(principalWaived);
		totalWaivedInPeriodToDate = totalWaivedInPeriodToDate.plus(totalWaivedInTransaction);
		scheduledRepaymentPeriod.setTotalWaived(moneyDataFrom(currencyData, totalWaivedInPeriodToDate));
		
		MoneyData totalWaived = moneyDataFrom(currencyData, totalWaivedInTransaction);
		
		Money principalOutstanding = moneyFrom(scheduledRepaymentPeriod.getPrincipalOutstanding());
		Money remainingPrincipalOutstanding = principalOutstanding.minus(principalWaived);
		scheduledRepaymentPeriod.setPrincipalOutstanding(moneyDataFrom(currencyData, remainingPrincipalOutstanding));
		
		Money interestOutstanding = moneyFrom(scheduledRepaymentPeriod.getInterestOutstanding());
		Money remainingInterestOutstanding = interestOutstanding.minus(interestWaived);
		scheduledRepaymentPeriod.setInterestOutstanding(moneyDataFrom(currencyData, remainingInterestOutstanding));
		
		Money totalOutstanding = moneyFrom(scheduledRepaymentPeriod.getTotalOutstanding());
		Money remainingOutstanding = totalOutstanding.minus(moneyFrom(totalWaived));
		scheduledRepaymentPeriod.setTotalOutstanding(moneyDataFrom(currencyData, remainingOutstanding));
		
		LoanRepaymentData loanRepaymentData = new LoanRepaymentData(transaction.getId(), transaction.getTransactionDate(), moneyDataFrom(currencyData, principalWaived),
				moneyDataFrom(currencyData, interestWaived), totalWaived, zero);
		loanRepaymentData.setTotalWaived(totalWaived);
		return loanRepaymentData;
	}

	private List<LoanRepaymentPeriodData> calculateArrears(
			List<LoanRepaymentPeriodData> loanRepaymentPeriods,
			Money arrearsTolerance, CurrencyData currencyData) {

		LocalDate today = new LocalDate();
		int repaymentIndex = 0;
		for (LoanRepaymentPeriodData currentRepaymentSchedule : loanRepaymentPeriods) {
			if (currentRepaymentSchedule.isInArrearsWithToleranceOf(
					moneyDataFrom(currencyData, arrearsTolerance), today)) {
				currentRepaymentSchedule
						.setTotalArrears(currentRepaymentSchedule
								.getTotalOutstanding());
				
				LocalDate arrearsFrom = determineInArrearsFrom(currentRepaymentSchedule, loanRepaymentPeriods, repaymentIndex);
				
				LocalDate arrearsTo = determineInArrearsTo(currentRepaymentSchedule, loanRepaymentPeriods, repaymentIndex);
				
				currentRepaymentSchedule.setArrearsFrom(arrearsFrom);
				currentRepaymentSchedule.setArrearsTo(arrearsTo);
			}
			
			repaymentIndex++;
		}

		return loanRepaymentPeriods;
	}

	private LocalDate determineInArrearsTo(
			LoanRepaymentPeriodData currentRepaymentSchedule,
			List<LoanRepaymentPeriodData> loanRepaymentPeriods,
			int repaymentIndex) {
		
		LocalDate today = new LocalDate();
		LocalDate arrearsDate = null;
		
		if (loanRepaymentPeriods.size() - 1 > repaymentIndex) {
			LoanRepaymentPeriodData nextRepaymentSchedule = loanRepaymentPeriods
					.get(repaymentIndex);
			
			arrearsDate = nextRepaymentSchedule.getDate().minusDays(1);
		}

		if (loanRepaymentPeriods.size() - 1 == repaymentIndex) {
			arrearsDate = today;
		}
		
		return arrearsDate;
	}

	private LocalDate determineInArrearsFrom(
			LoanRepaymentPeriodData currentRepaymentSchedule,
			List<LoanRepaymentPeriodData> loanRepaymentPeriods,
			int repaymentIndex) {

		
		int previousScheduleIndex = repaymentIndex - 1;
		LocalDate arrearsDate = null;
		if (currentRepaymentSchedule.getLastAffectingPaymentOn() != null) {

			if (currentRepaymentSchedule.getLastAffectingPaymentOn().isAfter(
					currentRepaymentSchedule.getDate())) {
				
				if (loanRepaymentPeriods.size() - 1 > previousScheduleIndex && previousScheduleIndex > -1) {
					LoanRepaymentPeriodData lastRepaymentSchedule = loanRepaymentPeriods
							.get(previousScheduleIndex);
					arrearsDate = lastRepaymentSchedule.getDate().plusDays(1);
				} else {
					arrearsDate = currentRepaymentSchedule.getDate().minusMonths(1);
				}
				
			} else {
				arrearsDate = currentRepaymentSchedule.getLastAffectingPaymentOn();
			}
		} else {
			if (loanRepaymentPeriods.size() - 1 > previousScheduleIndex && previousScheduleIndex > -1) {
				LoanRepaymentPeriodData lastRepaymentSchedule = loanRepaymentPeriods
						.get(previousScheduleIndex);
				arrearsDate = lastRepaymentSchedule.getDate().plusDays(1);
			} else {
				arrearsDate = currentRepaymentSchedule.getDate().minusMonths(1);
			}
		}

		return arrearsDate;
	}

	private LoanAccountSummaryData calculateTotalsFromRepaymentScheduleData(
			List<LoanRepaymentPeriodData> loanRepaymentPeriods, CurrencyData currencyData) {
		
		MonetaryCurrency monetaryCurrency = new MonetaryCurrency(currencyData.getCode(), currencyData.getDecimalPlaces());
		
		Money principalExpected = Money.zero(monetaryCurrency);
		Money principalPaid = Money.zero(monetaryCurrency);
		Money principalOutstanding = Money.zero(monetaryCurrency);
		
		Money interestExpected = Money.zero(monetaryCurrency);
		Money interestPaid = Money.zero(monetaryCurrency);
		Money interestOutstanding = Money.zero(monetaryCurrency);
		
		Money totalExpected = Money.zero(monetaryCurrency);
		Money totalPaid = Money.zero(monetaryCurrency);
		Money totalOutstanding = Money.zero(monetaryCurrency);
		
		Money totalInArrears = Money.zero(monetaryCurrency);
		Money totalWaived = Money.zero(monetaryCurrency);
		
		
		for (LoanRepaymentPeriodData period : loanRepaymentPeriods) {
			
			principalExpected = principalExpected.plus(moneyFrom(period.getPrincipal()));
			principalPaid = principalPaid.plus(moneyFrom(period.getPrincipalPaid()));
			principalOutstanding = principalOutstanding.plus(moneyFrom(period.getPrincipalOutstanding()));
			
			interestExpected = interestExpected.plus(moneyFrom(period.getInterest()));
			interestPaid = interestPaid.plus(moneyFrom(period.getInterestPaid()));
			interestOutstanding = interestOutstanding.plus(moneyFrom(period.getInterestOutstanding()));
			
			totalExpected = totalExpected.plus(moneyFrom(period.getTotal()));
			totalPaid = totalPaid.plus(moneyFrom(period.getTotalPaid()));
			totalOutstanding = totalOutstanding.plus(moneyFrom(period.getTotalOutstanding()));
			
			if (period.getTotalWaived() != null) {
				totalWaived = totalWaived.plus(moneyFrom(period.getTotalWaived()));
			}
			
			if (period.getTotalArrears() != null) {
				totalInArrears = totalInArrears.plus(moneyFrom(period.getTotalArrears()));
			}
		}
		
		return new LoanAccountSummaryData(moneyDataFrom(currencyData, principalExpected), moneyDataFrom(currencyData, principalPaid), 
				moneyDataFrom(currencyData, principalOutstanding), moneyDataFrom(currencyData, interestExpected), 
				moneyDataFrom(currencyData, interestPaid), moneyDataFrom(currencyData, interestOutstanding), 
				moneyDataFrom(currencyData, totalExpected), moneyDataFrom(currencyData, totalPaid), moneyDataFrom(currencyData, totalOutstanding), 
				moneyDataFrom(currencyData, totalInArrears), moneyDataFrom(currencyData, totalWaived));
	}

	/**
	 * TODO - pays off interest first then principal so should make this configurable when generating 'derived view of loan account'
	 */
	private LoanRepaymentData processLoanRepayment(LoanTransaction repayment,
			LoanRepaymentPeriodData scheduledRepayment, 
			int repaymentScheduleIndex, 
			List<LoanRepaymentPeriodData> repaymentSchedulePeriods, 
			Money arrearsTolerance, CurrencyData currencyDetail) {
		
		MoneyData zero = MoneyData.zero(currencyDetail);
		MoneyData cumulativeInterestPaid = MoneyData.zero(currencyDetail);
		MoneyData cumulativePrincipalPaid = MoneyData.zero(currencyDetail);
		
		LoanRepaymentPeriodData currentRepaymentSchedule = scheduledRepayment;
		int currentRepaymentScheduleIndex = repaymentScheduleIndex;
		MoneyData overpaid = null;
		
		MonetaryCurrency currency = new MonetaryCurrency(currencyDetail.getCode(), currencyDetail.getDecimalPlaces());
		
		Money remaining = repayment.getAmount(currency);
		while (remaining.isGreaterThanZero()) {
			
			Money interestOutstanding = moneyFrom(currentRepaymentSchedule.getInterestOutstanding());
			Money principalOutstanding = moneyFrom(currentRepaymentSchedule.getPrincipalOutstanding());
			
			// pay off interest component
			if (remaining.isGreaterThanOrEqualTo(interestOutstanding)) {
				Money previousInterestPaid = moneyFrom(cumulativeInterestPaid);
				cumulativeInterestPaid = moneyDataFrom(currencyDetail, previousInterestPaid.plus(interestOutstanding));
				
				MoneyData interestPaid = moneyDataFrom(currencyDetail, interestOutstanding);
				
				Money paidToDate = moneyFrom(currentRepaymentSchedule.getInterestPaid());
				Money amountPaid = moneyFrom(interestPaid);
	
				paidToDate = paidToDate.plus(amountPaid);
	
				MoneyData totalInterest = moneyDataFrom(currencyDetail, paidToDate);
				currentRepaymentSchedule.setInterestPaid(totalInterest);
				
				currentRepaymentSchedule.setInterestOutstanding(zero);
				
				Money totalPaidToDate = moneyFrom(currentRepaymentSchedule.getTotalPaid());
				totalPaidToDate = totalPaidToDate.plus(interestOutstanding);
				
				currentRepaymentSchedule.setTotalPaid(moneyDataFrom(currencyDetail, totalPaidToDate));
				
				Money outstanding = moneyFrom(currentRepaymentSchedule.getTotal()).minus(moneyFrom(currentRepaymentSchedule.getTotalPaid()));
				currentRepaymentSchedule.setTotalOutstanding(moneyDataFrom(currencyDetail, outstanding));
				
				if (interestOutstanding.isGreaterThanZero()) {
					currentRepaymentSchedule.setLastAffectingPaymentOn(repayment.getTransactionDate());
				}
				
				remaining = remaining.minus(interestOutstanding);
			} else {
				// partial payment of principal
				Money previousInterestPaid = moneyFrom(cumulativeInterestPaid);
				cumulativeInterestPaid = moneyDataFrom(currencyDetail, previousInterestPaid.plus(remaining));
				
				MoneyData interestPaid = moneyDataFrom(currencyDetail, remaining);
	
				Money paidToDate = moneyFrom(currentRepaymentSchedule.getInterestPaid());
				Money amountPaid = moneyFrom(interestPaid);
	
				paidToDate = paidToDate.plus(amountPaid);
	
				MoneyData totalInterest = moneyDataFrom(currencyDetail, paidToDate);
				currentRepaymentSchedule.setInterestPaid(totalInterest);
	
				interestOutstanding = interestOutstanding.minus(amountPaid);
	
				currentRepaymentSchedule.setInterestOutstanding(moneyDataFrom(currencyDetail, interestOutstanding));
				
				Money totalPaidToDate = moneyFrom(currentRepaymentSchedule.getTotalPaid());
				totalPaidToDate = totalPaidToDate.plus(amountPaid);
				
				currentRepaymentSchedule.setTotalPaid(moneyDataFrom(currencyDetail, totalPaidToDate));
				
				Money outstanding = moneyFrom(currentRepaymentSchedule.getTotal()).minus(moneyFrom(currentRepaymentSchedule.getTotalPaid()));
				currentRepaymentSchedule.setTotalOutstanding(moneyDataFrom(currencyDetail, outstanding));
				
				if (interestPaid.isGreaterThanZero()) {
					currentRepaymentSchedule.setLastAffectingPaymentOn(repayment.getTransactionDate());
				}
				
				remaining = remaining.minus(amountPaid);
			}
						
			// pay off principal component
			if (remaining.isGreaterThanOrEqualTo(principalOutstanding)) {
				Money previousPrincipalPaid = moneyFrom(cumulativePrincipalPaid);
				cumulativePrincipalPaid = moneyDataFrom(currencyDetail, previousPrincipalPaid.plus(principalOutstanding));
				
				MoneyData principalPaid = moneyDataFrom(currencyDetail, principalOutstanding);
				
				Money paidToDate = moneyFrom(currentRepaymentSchedule.getPrincipalPaid());
				Money amountPaid = moneyFrom(principalPaid);
				
				paidToDate = paidToDate.plus(amountPaid);
				
				MoneyData totalPrincipal = moneyDataFrom(currencyDetail, paidToDate);
				currentRepaymentSchedule.setPrincipalPaid(totalPrincipal);
				currentRepaymentSchedule.setPrincipalOutstanding(zero);
				
				Money totalPaidToDate = moneyFrom(currentRepaymentSchedule.getTotalPaid());
				totalPaidToDate = totalPaidToDate.plus(principalOutstanding);
				
				currentRepaymentSchedule.setTotalPaid(moneyDataFrom(currencyDetail, totalPaidToDate));
				
				Money outstanding = moneyFrom(currentRepaymentSchedule.getTotal()).minus(moneyFrom(currentRepaymentSchedule.getTotalPaid()));
				currentRepaymentSchedule.setTotalOutstanding(moneyDataFrom(currencyDetail, outstanding));
				
				remaining = remaining.minus(principalOutstanding);
			} else {
				// partial payment of principal
				Money previousPrincipalPaid = moneyFrom(cumulativePrincipalPaid);
				cumulativePrincipalPaid = moneyDataFrom(currencyDetail, previousPrincipalPaid.plus(remaining));
				
				MoneyData principalPaid = moneyDataFrom(currencyDetail, remaining);
				
				Money paidToDate = moneyFrom(currentRepaymentSchedule.getPrincipalPaid());
				Money amountPaid = moneyFrom(principalPaid);
				
				paidToDate = paidToDate.plus(amountPaid);
				
				MoneyData totalPrincipal = moneyDataFrom(currencyDetail, paidToDate);
				currentRepaymentSchedule.setPrincipalPaid(totalPrincipal);
				
				principalOutstanding = principalOutstanding.minus(amountPaid);
				currentRepaymentSchedule.setPrincipalOutstanding(moneyDataFrom(currencyDetail, principalOutstanding));
				
				Money totalPaidToDate = moneyFrom(currentRepaymentSchedule.getTotalPaid());
				totalPaidToDate = totalPaidToDate.plus(amountPaid);
				
				currentRepaymentSchedule.setTotalPaid(moneyDataFrom(currencyDetail, totalPaidToDate));
				Money outstanding = moneyFrom(currentRepaymentSchedule.getTotal()).minus(moneyFrom(currentRepaymentSchedule.getTotalPaid()));
				currentRepaymentSchedule.setTotalOutstanding(moneyDataFrom(currencyDetail, outstanding));
				
				if (principalPaid.isGreaterThanZero()) {
					currentRepaymentSchedule.setLastAffectingPaymentOn(repayment.getTransactionDate());
				}
				
				remaining = remaining.minus(amountPaid);
			}
			
			// mark schedule as paid in full
			if (currentRepaymentSchedule.isFullyPaid()) {
				currentRepaymentSchedule.setPaidInFullOn(repayment.getTransactionDate());
			}
			
			// check if this installment has
			if (currentRepaymentSchedule.isInArrearsWithToleranceOf(moneyDataFrom(currencyDetail, arrearsTolerance), repayment.getTransactionDate())) {
				currentRepaymentSchedule.setTotalArrears(currentRepaymentSchedule.getTotalOutstanding());
			} else {
				currentRepaymentSchedule.setTotalArrears(MoneyData.zero(currencyDetail));
			}
			
			if (remaining.isGreaterThanZero()) {
				currentRepaymentScheduleIndex++;
				if (repaymentSchedulePeriods.size() > currentRepaymentScheduleIndex) {
					currentRepaymentSchedule = repaymentSchedulePeriods.get(currentRepaymentScheduleIndex);
				} else {
					// loan has been overpaid by an amount so
					overpaid = moneyDataFrom(currencyDetail, remaining);
					// note - maybe should set the overpayment 'amount' against repayment period.
					remaining = Money.zero(remaining.getCurrency());
				}
			}
		}
		
		Money totalInterestComponent = moneyFrom(cumulativeInterestPaid);
		Money totalPrincipalComponent=  moneyFrom(cumulativePrincipalPaid);
		
		MoneyData totalPaid = moneyDataFrom(currencyDetail, totalPrincipalComponent.plus(totalInterestComponent));
		
		LoanRepaymentData loanRepaymentData = new LoanRepaymentData(repayment.getId(), repayment.getTransactionDate(), cumulativePrincipalPaid, cumulativeInterestPaid, totalPaid, overpaid);
		return loanRepaymentData;
	}
	
	private LoanRepaymentScheduleData generateRepaymentScheduleData(
			List<LoanRepaymentScheduleInstallment> repaymentSchedulePeriods,
			CurrencyData currencyData) {

		MoneyData zero = MoneyData.zero(currencyData);
		
		MonetaryCurrency currency = new MonetaryCurrency(currencyData.getCode(), currencyData.getDecimalPlaces());

		List<LoanRepaymentPeriodData> loanScheduleDetails = new ArrayList<LoanRepaymentPeriodData>();

		for (LoanRepaymentScheduleInstallment scheduledRepayment : repaymentSchedulePeriods) {
			
			MoneyData principal = MoneyData.of(currencyData, scheduledRepayment.getPrincipal(currency).getAmount());
			MoneyData interest = MoneyData.of(currencyData, scheduledRepayment.getInterest(currency).getAmount());
			MoneyData total = MoneyData.of(currencyData, scheduledRepayment.getTotal(currency).getAmount());

			LoanRepaymentPeriodData periodData = new LoanRepaymentPeriodData(
					scheduledRepayment.getInstallmentNumber(),
					scheduledRepayment.getDueDate(), zero, principal, interest,
					total, total);

			loanScheduleDetails.add(periodData);
		}
		
		return new LoanRepaymentScheduleData(loanScheduleDetails);
	}
	
	private Money moneyFrom(MoneyData money) {
		MonetaryCurrency monetaryCurrency = new MonetaryCurrency(money.getCurrencyCode(), money.getDigitsAfterDecimal());
		return Money.of(monetaryCurrency, money.getAmount());
	}
	
	private MoneyData moneyDataFrom(CurrencyData currencyData, Money money) {
		return MoneyData.of(currencyData, money.getAmount());
	}
}