/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

/**
 * Represents the bare minimum repayment details needed for activities related
 * to generating repayment schedules.
 */
public interface LoanProductMinimumRepaymentScheduleRelatedDetail {

	MonetaryCurrency getCurrency();
	
	Money getPrincipal();
	
	Integer graceOnInterestCharged();
	
	Integer graceOnInterestPayment();
	
	Integer graceOnPrincipalPayment();
	
	Money getInArrearsTolerance();
	
	BigDecimal getNominalInterestRatePerPeriod();
	
	PeriodFrequencyType getInterestPeriodFrequencyType();
	
	BigDecimal getAnnualNominalInterestRate();
	
	InterestMethod getInterestMethod();
	
	InterestCalculationPeriodMethod getInterestCalculationPeriodMethod();
	
    Integer getRepayEvery();

    PeriodFrequencyType getRepaymentPeriodFrequencyType();

    Integer getNumberOfRepayments();
    
    AmortizationMethod getAmortizationMethod();
    
    Integer getGraceOnDueDate();
}
