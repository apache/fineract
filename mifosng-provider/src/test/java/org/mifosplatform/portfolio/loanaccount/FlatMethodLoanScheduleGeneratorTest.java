package org.mifosplatform.portfolio.loanaccount;

//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.FlatMethodLoanScheduleGenerator;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class FlatMethodLoanScheduleGeneratorTest {

	// class under test
	private FlatMethodLoanScheduleGenerator flatLoanScheduleGenerator;
	
	// test doubles
	private CurrencyData currencyData = null;
	private LoanProductRelatedDetail loanScheduleInfo = null;
	
	@Before
	public void setupForEachTest() {
		currencyData = new CurrencyData("USD", "US Dollar", 2, "$", "currency.USD");
		
		flatLoanScheduleGenerator = new FlatMethodLoanScheduleGenerator();
	}

	@Test
	public void rewriteAllTests() {
		
	}
	
//	@Test
//	public void givenFlatMethodShouldCalculateFlatInterestComponentToDerivePrincipalAndTotalRepaymentRegardlessOfEqualInstallmentAmortisation() {
//		
//		// setup
//		Integer loanTermFrequency = 12;
//		PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.MONTHS;
//		loanScheduleInfo = LoanProductRelatedDetailTestHelper.createSettingsForFlatQuarterly(AmortizationMethod.EQUAL_INSTALLMENTS);
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
//		LoanSchedule schedule = flatLoanScheduleGenerator.generate(loanScheduleInfo, loanTermFrequency, loanTermFrequencyType, 
//				disbursementDate, firstRepaymentDate, interestCalculatedFrom, currencyData);
//		
//		// verification (post assertions)
//		given(schedule).assertHasInstallmentSizeOf(4);
//		given(schedule).assertPrincipalDueComponentOf(1, is("50000.00")).assertInterestDueComponentOf(1, is("12000.00")).assertTotalPaymentDueOf(1, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(2, is("50000.00")).assertInterestDueComponentOf(2, is("12000.00")).assertTotalPaymentDueOf(2, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(3, is("50000.00")).assertInterestDueComponentOf(3, is("12000.00")).assertTotalPaymentDueOf(3, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(4, is("50000.00")).assertInterestDueComponentOf(4, is("12000.00")).assertTotalPaymentDueOf(4, is("62000.00"));
//	}
//	
//	@Test
//	public void givenFlatMethodShouldCalculateFlatInterestComponentToDerivePrincipalAndTotalRepaymentRegardlessOfEqualPrincipalAmortisation() {
//		
//		// setup
//		Integer loanTermFrequency = 12;
//		PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.MONTHS;
//		loanScheduleInfo = LoanProductRelatedDetailTestHelper.createSettingsForFlatQuarterly(AmortizationMethod.EQUAL_PRINCIPAL);
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
//		LoanSchedule schedule = flatLoanScheduleGenerator.generate(loanScheduleInfo, loanTermFrequency, loanTermFrequencyType, 
//				disbursementDate, firstRepaymentDate, interestCalculatedFrom, currencyData);
//		
//		// verification (post assertions)
//		given(schedule).assertHasInstallmentSizeOf(4);
//		given(schedule).assertPrincipalDueComponentOf(1, is("50000.00")).assertInterestDueComponentOf(1, is("12000.00")).assertTotalPaymentDueOf(1, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(2, is("50000.00")).assertInterestDueComponentOf(2, is("12000.00")).assertTotalPaymentDueOf(2, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(3, is("50000.00")).assertInterestDueComponentOf(3, is("12000.00")).assertTotalPaymentDueOf(3, is("62000.00"));
//		given(schedule).assertPrincipalDueComponentOf(4, is("50000.00")).assertInterestDueComponentOf(4, is("12000.00")).assertTotalPaymentDueOf(4, is("62000.00"));
//	}
//	
//	@Test
//	public void givenFlatMethodAndIrregularFirstRepaymentShouldCalculateFlatInterestComponent() {
//		
//		// setup
//		currencyData = new CurrencyData("KSH", "Keyan Shilling", 0, "KSh", "currency.KSH");
//		Integer loanTermFrequency = 9;
//		PeriodFrequencyType loanTermFrequencyType = PeriodFrequencyType.MONTHS;
//		loanScheduleInfo = LoanProductRelatedDetailTestHelper.createSettingsForIrregularFlatEveryFourMonths();
//		
//		LocalDate disbursementDate = new LocalDate(2012, 3, 27);
//		LocalDate firstRepaymentDate = new LocalDate(2011, 7, 27);
//		LocalDate interestCalculatedFrom = disbursementDate;
//		
//		// exercise test
//		LoanSchedule schedule = flatLoanScheduleGenerator.generate(loanScheduleInfo, loanTermFrequency, loanTermFrequencyType, 
//				disbursementDate, firstRepaymentDate, interestCalculatedFrom, currencyData);
//		
//		// verification (post assertions)
//		given(schedule).assertHasInstallmentSizeOf(2);
//		given(schedule).assertPrincipalDueComponentOf(1, is("7500")).assertInterestDueComponentOf(1, is("1350")).assertTotalPaymentDueOf(1, is("8850"));
//		given(schedule).assertPrincipalDueComponentOf(2, is("7500")).assertInterestDueComponentOf(2, is("1350")).assertTotalPaymentDueOf(2, is("8850"));
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