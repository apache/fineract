package org.mifosng.platform.loan.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.DerivedLoanData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanRepaymentPeriodData;
import org.mifosng.data.LoanRepaymentScheduleData;
import org.mifosng.data.MoneyData;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DerivedLoanDataProcessorTest {

	private DerivedLoanDataProcessor loanDataProcessor = new DerivedLoanDataProcessor();
	private MonetaryCurrency cfaCurrency = new MonetaryCurrencyBuilder().withCode("XOF").withDigitsAfterDecimal(0).build();
	private Money arrearsTolerance = new MoneyBuilder().with(cfaCurrency).with("0.0").build();
	
	@Test
	public void shouldDeriveLoanRepaymentScheduleWithSingleRepaymentPeriodAndRepaymentInFull() {
		
		
		
		List<LoanRepaymentScheduleInstallment> repaymentScheduleWithSinglePeriod = new ArrayList<LoanRepaymentScheduleInstallment>();
		
		LoanRepaymentScheduleInstallment firstRepaymentPeriod = new LoanRepaymentScheduleInstallmentBuilder(cfaCurrency)
																					.withPrincipal("1000")
																					.withInterest("10")
																					.withDueDate(LocalDate.now())
																					.build();
		repaymentScheduleWithSinglePeriod.add(firstRepaymentPeriod);
		
		List<LoanTransaction> singleRepaymentInFull = new ArrayList<LoanTransaction>();
		
		Money fullAmount = new MoneyBuilder().with(cfaCurrency).with("1010").build();
		
		LoanTransaction fullRepayment = new LoanTransactionBuilder().with(fullAmount).build();
		singleRepaymentInFull.add(fullRepayment);
		
		// exercise test
		DerivedLoanData derivedData = loanDataProcessor.process(repaymentScheduleWithSinglePeriod, singleRepaymentInFull, asCurrencyData(cfaCurrency), arrearsTolerance);

		// verification
		assertThat(derivedData, is(notNullValue()));
		
		LoanRepaymentScheduleData repaymentSchedule = derivedData.getRepaymentSchedule();
		assertThat(repaymentSchedule, is(notNullValue()));
		
		List<LoanRepaymentPeriodData> periods = repaymentSchedule.getPeriods();
		assertThat(periods, is(notNullValue()));
		assertThat(periods.size(), is(1));
		
		LoanRepaymentPeriodData periodData = periods.get(0);
		
		MoneyData expectedPrincipal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		MoneyData expectedInterest = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		
		MoneyData expectedTotal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1010").build());
		
		assertThat(periodData.getPrincipal(), is(expectedPrincipal));
		assertThat(periodData.getPrincipalPaid(), is(expectedPrincipalPaid));
		assertThat(periodData.getPrincipalOutstanding(), is(expectedPrincipalOutstanding));
		
		assertThat(periodData.getInterest(), is(expectedInterest));
		assertThat(periodData.getInterestPaid(), is(expectedInterestPaid));
		assertThat(periodData.getInterestOutstanding(), is(expectedInterestOutstanding));
		
		// assert repayments are ok
		List<LoanRepaymentData> loanRepayments = derivedData.getLoanRepayments();
		assertThat(loanRepayments, is(notNullValue()));
		assertThat(loanRepayments.size(), is(1));
		
		LoanRepaymentData firstRepayment = loanRepayments.get(0);
		assertThat(firstRepayment.getPrincipal(), is(expectedPrincipalPaid));
		assertThat(firstRepayment.getInterest(), is(expectedInterestPaid));
		assertThat(firstRepayment.getTotal(), is(expectedTotal));
	}
	
	private CurrencyData asCurrencyData(MonetaryCurrency currency) {
		return new CurrencyData(currency.getCode(), "", currency.getDigitsAfterDecimal(), "CFA", "");
	}

	@Test
	public void shouldDeriveLoanRepaymentScheduleWithSingleRepaymentPeriodAndPartialRepayment() {
		
		List<LoanRepaymentScheduleInstallment> repaymentScheduleWithSinglePeriod = new ArrayList<LoanRepaymentScheduleInstallment>();
		
		LoanRepaymentScheduleInstallment firstRepaymentPeriod = new LoanRepaymentScheduleInstallmentBuilder(cfaCurrency)
																					.withPrincipal("1000")
																					.withInterest("10")
																					.withDueDate(LocalDate.now())
																					.build();
		repaymentScheduleWithSinglePeriod.add(firstRepaymentPeriod);
		
		List<LoanTransaction> singlePartialRepayment = new ArrayList<LoanTransaction>();
		
		Money partialAmount = new MoneyBuilder().with(cfaCurrency).with("510").build();
		
		LoanTransaction partialRepayment = new LoanTransactionBuilder().with(partialAmount).build();
		singlePartialRepayment.add(partialRepayment);
		
		// exercise test
		DerivedLoanData derivedData = loanDataProcessor.process(repaymentScheduleWithSinglePeriod, singlePartialRepayment, asCurrencyData(cfaCurrency), arrearsTolerance);

		// verification
		assertThat(derivedData, is(notNullValue()));
		
		LoanRepaymentScheduleData repaymentSchedule = derivedData.getRepaymentSchedule();
		assertThat(repaymentSchedule, is(notNullValue()));
		
		List<LoanRepaymentPeriodData> periods = repaymentSchedule.getPeriods();
		assertThat(periods, is(notNullValue()));
		assertThat(periods.size(), is(1));
		
		LoanRepaymentPeriodData periodData = periods.get(0);
		
		MoneyData expectedPrincipal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("500").build());
		MoneyData expectedPrincipalOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("500").build());
		MoneyData expectedInterest = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		
		MoneyData expectedTotal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("510").build());
		
		assertThat(periodData.getPrincipal(), is(expectedPrincipal));
		assertThat(periodData.getPrincipalPaid(), is(expectedPrincipalPaid));
		assertThat(periodData.getPrincipalOutstanding(), is(expectedPrincipalOutstanding));
		
		assertThat(periodData.getInterest(), is(expectedInterest));
		assertThat(periodData.getInterestPaid(), is(expectedInterestPaid));
		assertThat(periodData.getInterestOutstanding(), is(expectedInterestOutstanding));
		
		// assert repayments are ok
		List<LoanRepaymentData> loanRepayments = derivedData.getLoanRepayments();
		assertThat(loanRepayments, is(notNullValue()));
		assertThat(loanRepayments.size(), is(1));
		
		LoanRepaymentData firstRepayment = loanRepayments.get(0);
		assertThat(firstRepayment.getPrincipal(), is(expectedPrincipalPaid));
		assertThat(firstRepayment.getInterest(), is(expectedInterestPaid));
		assertThat(firstRepayment.getTotal(), is(expectedTotal));
	}
	
	
	@Test
	public void shouldDeriveLoanRepaymentScheduleWithSingleRepaymentPeriodAndPartialRepaymentLessThanInterestComponent() {
		
		List<LoanRepaymentScheduleInstallment> repaymentScheduleWithSinglePeriod = new ArrayList<LoanRepaymentScheduleInstallment>();
		
		LoanRepaymentScheduleInstallment firstRepaymentPeriod = new LoanRepaymentScheduleInstallmentBuilder(cfaCurrency)
																					.withPrincipal("1000")
																					.withInterest("10")
																					.withDueDate(LocalDate.now())
																					.build();
		repaymentScheduleWithSinglePeriod.add(firstRepaymentPeriod);
		
		List<LoanTransaction> singlePartialRepayment = new ArrayList<LoanTransaction>();
		
		Money partialAmount = new MoneyBuilder().with(cfaCurrency).with("5").build();
		
		LoanTransaction partialRepayment = new LoanTransactionBuilder().with(partialAmount).build();
		singlePartialRepayment.add(partialRepayment);
		
		// exercise test
		DerivedLoanData derivedData = loanDataProcessor.process(repaymentScheduleWithSinglePeriod, singlePartialRepayment, asCurrencyData(cfaCurrency), arrearsTolerance);

		// verification
		assertThat(derivedData, is(notNullValue()));
		
		LoanRepaymentScheduleData repaymentSchedule = derivedData.getRepaymentSchedule();
		assertThat(repaymentSchedule, is(notNullValue()));
		
		List<LoanRepaymentPeriodData> periods = repaymentSchedule.getPeriods();
		assertThat(periods, is(notNullValue()));
		assertThat(periods.size(), is(1));
		
		LoanRepaymentPeriodData periodData = periods.get(0);
		
		MoneyData expectedPrincipal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		MoneyData expectedPrincipalOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedInterest = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("5").build());
		MoneyData expectedInterestOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("5").build());
		
		MoneyData expectedTotal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("5").build());
		
		assertThat(periodData.getPrincipal(), is(expectedPrincipal));
		assertThat(periodData.getPrincipalPaid(), is(expectedPrincipalPaid));
		assertThat(periodData.getPrincipalOutstanding(), is(expectedPrincipalOutstanding));
		
		assertThat(periodData.getInterest(), is(expectedInterest));
		assertThat(periodData.getInterestPaid(), is(expectedInterestPaid));
		assertThat(periodData.getInterestOutstanding(), is(expectedInterestOutstanding));
		
		// assert repayments are ok
		List<LoanRepaymentData> loanRepayments = derivedData.getLoanRepayments();
		assertThat(loanRepayments, is(notNullValue()));
		assertThat(loanRepayments.size(), is(1));
		
		LoanRepaymentData firstRepayment = loanRepayments.get(0);
		assertThat(firstRepayment.getPrincipal(), is(expectedPrincipalPaid));
		assertThat(firstRepayment.getInterest(), is(expectedInterestPaid));
		assertThat(firstRepayment.getTotal(), is(expectedTotal));
	}
	
	@Test
	public void shouldDeriveLoanRepaymentScheduleWithSingleRepaymentPeriodAndOverPayment() {
		
		List<LoanRepaymentScheduleInstallment> repaymentScheduleWithSinglePeriod = new ArrayList<LoanRepaymentScheduleInstallment>();
		
		LoanRepaymentScheduleInstallment firstRepaymentPeriod = new LoanRepaymentScheduleInstallmentBuilder(cfaCurrency)
																					.withPrincipal("1000")
																					.withInterest("10")
																					.withDueDate(LocalDate.now())
																					.build();
		repaymentScheduleWithSinglePeriod.add(firstRepaymentPeriod);
		
		List<LoanTransaction> singleOverRepayment = new ArrayList<LoanTransaction>();
		
		Money partialAmount = new MoneyBuilder().with(cfaCurrency).with("1200").build();
		
		LoanTransaction overRepayment = new LoanTransactionBuilder().with(partialAmount).build();
		singleOverRepayment.add(overRepayment);
		
		// exercise test
		DerivedLoanData derivedData = loanDataProcessor.process(repaymentScheduleWithSinglePeriod, singleOverRepayment, asCurrencyData(cfaCurrency), arrearsTolerance);

		// verification
		assertThat(derivedData, is(notNullValue()));
		
		LoanRepaymentScheduleData repaymentSchedule = derivedData.getRepaymentSchedule();
		assertThat(repaymentSchedule, is(notNullValue()));
		
		List<LoanRepaymentPeriodData> periods = repaymentSchedule.getPeriods();
		assertThat(periods, is(notNullValue()));
		assertThat(periods.size(), is(1));
		
		LoanRepaymentPeriodData periodData = periods.get(0);
		
		MoneyData expectedPrincipal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		MoneyData expectedInterest = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		
		MoneyData expectedTotal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1010").build());
		MoneyData expectedOverpayment = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("190").build());
		
		assertThat(periodData.getPrincipal(), is(expectedPrincipal));
		assertThat(periodData.getPrincipalPaid(), is(expectedPrincipalPaid));
		assertThat(periodData.getPrincipalOutstanding(), is(expectedPrincipalOutstanding));
		
		assertThat(periodData.getInterest(), is(expectedInterest));
		assertThat(periodData.getInterestPaid(), is(expectedInterestPaid));
		assertThat(periodData.getInterestOutstanding(), is(expectedInterestOutstanding));
		
		// assert repayments are ok
		List<LoanRepaymentData> loanRepayments = derivedData.getLoanRepayments();
		assertThat(loanRepayments, is(notNullValue()));
		assertThat(loanRepayments.size(), is(1));
		
		LoanRepaymentData firstRepayment = loanRepayments.get(0);
		assertThat(firstRepayment.getPrincipal(), is(expectedPrincipalPaid));
		assertThat(firstRepayment.getInterest(), is(expectedInterestPaid));
		assertThat(firstRepayment.getTotal(), is(expectedTotal));
		assertThat(firstRepayment.getOverpaid(), is(expectedOverpayment));
	}
	
	@Test
	public void shouldDeriveLoanRepaymentScheduleWithTwoRepaymentPeriodAndOneSinglePayment() {
		
		List<LoanRepaymentScheduleInstallment> repaymentScheduleWithSinglePeriod = new ArrayList<LoanRepaymentScheduleInstallment>();
		
		LoanRepaymentScheduleInstallment firstRepaymentPeriod = new LoanRepaymentScheduleInstallmentBuilder(cfaCurrency)
																					.withPrincipal("1000")
																					.withInterest("10")
																					.withDueDate(LocalDate.now())
																					.build();
		
		LoanRepaymentScheduleInstallment secondRepaymentPeriod = new LoanRepaymentScheduleInstallmentBuilder(cfaCurrency)
																					.withPrincipal("1000")
																					.withInterest("10")
																					.withDueDate(LocalDate.now().plusWeeks(1))
																					.build();
		
		repaymentScheduleWithSinglePeriod.add(firstRepaymentPeriod);
		repaymentScheduleWithSinglePeriod.add(secondRepaymentPeriod);
		
		List<LoanTransaction> singleRepayment = new ArrayList<LoanTransaction>();
		
		Money partialAmount = new MoneyBuilder().with(cfaCurrency).with("1200").build();
		
		LoanTransaction overRepaymentForFirstPeriod = new LoanTransactionBuilder().with(partialAmount).build();
		singleRepayment.add(overRepaymentForFirstPeriod);
		
		// exercise test
		DerivedLoanData derivedData = loanDataProcessor.process(repaymentScheduleWithSinglePeriod, singleRepayment, asCurrencyData(cfaCurrency), arrearsTolerance);

		// verification
		assertThat(derivedData, is(notNullValue()));
		
		LoanRepaymentScheduleData repaymentSchedule = derivedData.getRepaymentSchedule();
		assertThat(repaymentSchedule, is(notNullValue()));
		
		List<LoanRepaymentPeriodData> periods = repaymentSchedule.getPeriods();
		assertThat(periods, is(notNullValue()));
		assertThat(periods.size(), is(2));
		
		LoanRepaymentPeriodData firstPeriod = periods.get(0);
		
		MoneyData expectedPrincipal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		MoneyData expectedPrincipalOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		MoneyData expectedInterest = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		MoneyData expectedInterestOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		
		assertThat(firstPeriod.getPrincipal(), is(expectedPrincipal));
		assertThat(firstPeriod.getPrincipalPaid(), is(expectedPrincipalPaid));
		assertThat(firstPeriod.getPrincipalOutstanding(), is(expectedPrincipalOutstanding));
		
		assertThat(firstPeriod.getInterest(), is(expectedInterest));
		assertThat(firstPeriod.getInterestPaid(), is(expectedInterestPaid));
		assertThat(firstPeriod.getInterestOutstanding(), is(expectedInterestOutstanding));
		
		LoanRepaymentPeriodData secondPeriod = periods.get(1);
		
		expectedPrincipal = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1000").build());
		expectedPrincipalPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("180").build());
		expectedPrincipalOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("820").build());
		expectedInterest = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		expectedInterestPaid = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("10").build());
		expectedInterestOutstanding = moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("0").build());
		
		assertThat(secondPeriod.getPrincipal(), is(expectedPrincipal));
		assertThat(secondPeriod.getPrincipalPaid(), is(expectedPrincipalPaid));
		assertThat(secondPeriod.getPrincipalOutstanding(), is(expectedPrincipalOutstanding));
		
		assertThat(secondPeriod.getInterest(), is(expectedInterest));
		assertThat(secondPeriod.getInterestPaid(), is(expectedInterestPaid));
		assertThat(secondPeriod.getInterestOutstanding(), is(expectedInterestOutstanding));
		
		// assert repayments are ok
		List<LoanRepaymentData> loanRepayments = derivedData.getLoanRepayments();
		assertThat(loanRepayments, is(notNullValue()));
		assertThat(loanRepayments.size(), is(1));
		
		LoanRepaymentData firstRepayment = loanRepayments.get(0);
		assertThat(firstRepayment.getPrincipal(), is(moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1180").build())));
		assertThat(firstRepayment.getInterest(), is(moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("20").build())));
		assertThat(firstRepayment.getTotal(), is(moneyDataFrom(new MoneyBuilder().with(cfaCurrency).with("1200").build())));
	}
	
	private MoneyData moneyDataFrom(Money money) {
		CurrencyData currency = new CurrencyData(money.getCurrencyCode(), "", money.getCurrencyDigitsAfterDecimal(), "CFA", "");
		return MoneyData.of(currency, money.getAmount());
	}
}