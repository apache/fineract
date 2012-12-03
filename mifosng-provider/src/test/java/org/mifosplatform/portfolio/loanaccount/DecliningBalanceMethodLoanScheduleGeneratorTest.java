package org.mifosplatform.portfolio.loanaccount;

//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.DecliningBalanceMethodLoanScheduleGenerator;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class DecliningBalanceMethodLoanScheduleGeneratorTest {

	// class under test
	private DecliningBalanceMethodLoanScheduleGenerator decliningBalanceGenerator;
	
	// test doubles
	private CurrencyData currencyData = null;
	private LoanProductRelatedDetail loanScheduleInfo = null;
	
	@Before
	public void setupForEachTest() {
		currencyData = new CurrencyData("USD", "US Dollar", 2, "$", "currency.USD");
		
		decliningBalanceGenerator = new DecliningBalanceMethodLoanScheduleGenerator();
	}
	
	@Test
	public void testVoid() {
		
	}
	
//	@Test
//	public void givenEqualInstallmentAmortisationShouldCalculatePaymentPerInstallmentUsingPmtFunctionAndDerivePrincipalAndInterestComponents() {
//		
//		// setup
//		Integer loanTermFrequency = 12;
//		PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.MONTHS;
//		loanScheduleInfo = LoanProductRelatedDetailTestHelper.createSettingsForEqualInstallmentAmortizationQuarterly();
//		
//		LocalDate disbursementDate = new LocalDate(2011, 1, 11);
//		LocalDate firstRepaymentDate = new LocalDate(2011, 5, 1);
//		LocalDate interestCalculatedFrom = new LocalDate(2011, 2, 1);
//		
//		// pre assertions
//		assertThat(loanTermFrequency, is(loanScheduleInfo.getNumberOfRepayments() * loanScheduleInfo.getRepayEvery()));
//		assertThat(loanTermFrequencyType, is(loanScheduleInfo.getRepaymentPeriodFrequencyType()));
//		
//		// exercise test
//		LoanSchedule schedule = decliningBalanceGenerator.generate(loanScheduleInfo, loanTermFrequency, loanTermFrequencyType, 
//				disbursementDate, firstRepaymentDate, interestCalculatedFrom, currencyData);
//		
//		// verification (post assertions)
//		given(schedule).assertHasInstallmentSizeOf(4);
//		given(schedule).assertTotalPaymentDueOf(1, is("57718.30")).assertInterestDueComponentOf(1, is("12000.00")).assertPrincipalDueComponentOf(1, is("45718.30"));
//		given(schedule).assertTotalPaymentDueOf(2, is("57718.30")).assertInterestDueComponentOf(2, is("9256.90")).assertPrincipalDueComponentOf(2, is("48461.40"));
//		given(schedule).assertTotalPaymentDueOf(3, is("57718.30")).assertInterestDueComponentOf(3, is("6349.22")).assertPrincipalDueComponentOf(3, is("51369.08"));
//		given(schedule).assertTotalPaymentDueOf(4, is("57718.29")).assertInterestDueComponentOf(4, is("3267.07")).assertPrincipalDueComponentOf(4, is("54451.22"));
//	}
//	
//	@Test
//	public void givenEqualPrincipalAmortisationShouldCalculateInterestComponentFromOutstandingPrincipalBlanceResultingInDecliningInterestAndTotalRepayments() {
//		
//		// setup
//		Integer loanTermFrequency = 12;
//		PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.MONTHS;
//		loanScheduleInfo = LoanProductRelatedDetailTestHelper.createSettingsForEqualPrincipalAmortizationQuarterly();
//		
//		LocalDate disbursementDate = new LocalDate(2011, 1, 11);
//		LocalDate firstRepaymentDate = new LocalDate(2011, 5, 1);
//		LocalDate interestCalculatedFrom = new LocalDate(2011, 2, 1);
//		
//		// pre assertions
//		assertThat(loanTermFrequency, is(loanScheduleInfo.getNumberOfRepayments() * loanScheduleInfo.getRepayEvery()));
//		assertThat(loanTermFrequencyType, is(loanScheduleInfo.getRepaymentPeriodFrequencyType()));
//		
//		// exercise test
//		LoanSchedule schedule = decliningBalanceGenerator.generate(loanScheduleInfo, loanTermFrequency, loanTermFrequencyType, 
//				disbursementDate, firstRepaymentDate, interestCalculatedFrom, currencyData);
//		
//		// verification (post assertions)
//		given(schedule).assertHasInstallmentSizeOf(4);
//		given(schedule).assertPrincipalDueComponentOf(1, is("50000.00")).assertInterestDueComponentOf(1, is("12000.00")).assertTotalPaymentDueOf(1, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(2, is("50000.00")).assertInterestDueComponentOf(2, is("9000.00")).assertTotalPaymentDueOf(2, is("59000.00"));
//		given(schedule).assertPrincipalDueComponentOf(3, is("50000.00")).assertInterestDueComponentOf(3, is("6000.00")).assertTotalPaymentDueOf(3, is("56000.00"));
//		given(schedule).assertPrincipalDueComponentOf(4, is("50000.00")).assertInterestDueComponentOf(4, is("3000.00")).assertTotalPaymentDueOf(4, is("53000.00"));
//	}
//	
//	private LoanScheduleAssertionBuilder given(final LoanSchedule schedule) {
//		return new LoanScheduleAssertionBuilder(schedule);
//	}
//	
//	private class LoanScheduleAssertionBuilder {
//		
//		private final LoanSchedule schedule;
//
//		public LoanScheduleAssertionBuilder(final LoanSchedule schedule) {
//			this.schedule = schedule;
//		}
//		
//		public LoanScheduleAssertionBuilder assertPrincipalDueComponentOf(final int installmentNumber, Matcher<String> matchesExpectedAmount) {
//			final Integer installmentIndex = installmentNumber -1;
//			List<ScheduledLoanInstallment> repaymentInstallments = schedule.getScheduledLoanInstallments();
//			ScheduledLoanInstallment repaymentInstallment = repaymentInstallments.get(installmentIndex);
//			
//			assertThat("Expected principal due component for installment " + installmentNumber + " is wrong: ", 
//					repaymentInstallment.getPrincipalDue().toString(), matchesExpectedAmount);
//			return this;
//		}
//
//		public LoanScheduleAssertionBuilder assertInterestDueComponentOf(final int installmentNumber, Matcher<String> matchesExpectedAmount) {
//			final Integer installmentIndex = installmentNumber -1;
//			List<ScheduledLoanInstallment> repaymentInstallments = schedule.getScheduledLoanInstallments();
//			ScheduledLoanInstallment repaymentInstallment = repaymentInstallments.get(installmentIndex);
//			
//			assertThat("Expected interest due component for installment " + installmentNumber + " is wrong: ", 
//					repaymentInstallment.getInterestDue().toString(), matchesExpectedAmount);
//			return this;
//		}
//
//		public LoanScheduleAssertionBuilder assertTotalPaymentDueOf(final int installmentNumber, Matcher<String> matchesExpectedAmount) {
//			final Integer installmentIndex = installmentNumber -1;
//			List<ScheduledLoanInstallment> repaymentInstallments = schedule.getScheduledLoanInstallments();
//			ScheduledLoanInstallment repaymentInstallment = repaymentInstallments.get(installmentIndex);
//			
//			assertThat("Expected total payment for installment " + installmentNumber + " is wrong: ", 
//					repaymentInstallment.getTotalInstallmentDue().toString(), matchesExpectedAmount);
//			return this;
//		}
//
//		public LoanScheduleAssertionBuilder assertHasInstallmentSizeOf(int expectedInstallmentSize) {
//			assertThat("Expected number of installments is wrong: ", schedule.getScheduledLoanInstallments().size(), is(expectedInstallmentSize));
//			return this;
//		}
//	}
}