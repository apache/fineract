/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanproduct.domain;

import java.math.BigDecimal;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

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
	
	Integer recurringMoratoriumOnPrincipalPeriods();
	
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
