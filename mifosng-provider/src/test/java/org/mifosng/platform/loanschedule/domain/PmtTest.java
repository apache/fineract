package org.mifosng.platform.loanschedule.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PmtTest {

	@Test
	public void shouldCalculatePmt() {
		
		double interestRateFraction = Double.valueOf("0.0175");
		
		double numberOfPayments = Double.valueOf("12");
		double principal = Double.valueOf("-144300");
		double futureValue = Double.valueOf("0");
		boolean type = false;
		
		double payment = DecliningBalanceMethodLoanScheduleGenerator.pmt(interestRateFraction, numberOfPayments, principal, futureValue, type);
		
		assertThat(payment, is(Double.valueOf("13436.317553939027")));
	}
}
