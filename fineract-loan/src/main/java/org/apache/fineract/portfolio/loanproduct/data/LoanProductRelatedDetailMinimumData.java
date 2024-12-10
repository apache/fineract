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
package org.apache.fineract.portfolio.loanproduct.data;

import java.math.BigDecimal;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

public class LoanProductRelatedDetailMinimumData implements LoanProductMinimumRepaymentScheduleRelatedDetail {

    private final CurrencyData currency;

    private final Money principal;
    private final Money inArrearsTolerance;

    private final BigDecimal interestRatePerPeriod;
    private final BigDecimal annualNominalInterestRate;

    private final Integer interestChargingGrace;
    private final Integer interestPaymentGrace;
    private final Integer principalGrace;
    private final Integer recurringMoratoriumOnPrincipalPeriods;

    private final InterestMethod interestMethod;
    private final InterestCalculationPeriodMethod interestCalculationPeriodMethod;

    private final DaysInYearType daysInYearType;
    private final DaysInMonthType daysInMonthType;

    private final AmortizationMethod amortizationMethod;

    private final PeriodFrequencyType repaymentPeriodFrequencyType;
    private final Integer repaymentEvery;
    private final Integer numberOfRepayments;

    public LoanProductRelatedDetailMinimumData(CurrencyData currency, Money principal, Money inArrearsTolerance,
            BigDecimal interestRatePerPeriod, BigDecimal annualNominalInterestRate, Integer interestChargingGrace,
            Integer interestPaymentGrace, Integer principalGrace, Integer recurringMoratoriumOnPrincipalPeriods,
            InterestMethod interestMethod, InterestCalculationPeriodMethod interestCalculationPeriodMethod, DaysInYearType daysInYearType,
            DaysInMonthType daysInMonthType, AmortizationMethod amortizationMethod, PeriodFrequencyType repaymentPeriodFrequencyType,
            Integer repaymentEvery, Integer numberOfRepayments) {
        this.currency = currency;
        this.principal = principal;
        this.inArrearsTolerance = inArrearsTolerance;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.annualNominalInterestRate = annualNominalInterestRate;
        this.interestChargingGrace = defaultToNullIfZero(interestChargingGrace);
        this.interestPaymentGrace = defaultToNullIfZero(interestPaymentGrace);
        this.principalGrace = defaultToNullIfZero(principalGrace);
        this.recurringMoratoriumOnPrincipalPeriods = recurringMoratoriumOnPrincipalPeriods;
        this.interestMethod = interestMethod;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
        this.daysInYearType = daysInYearType;
        this.daysInMonthType = daysInMonthType;
        this.amortizationMethod = amortizationMethod;
        this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
        this.repaymentEvery = repaymentEvery;
        this.numberOfRepayments = numberOfRepayments;
    }

    private Integer defaultToNullIfZero(final Integer value) {
        Integer defaultTo = value;
        if (Integer.valueOf(0).equals(value)) {
            defaultTo = null;
        }
        return defaultTo;
    }

    @Override
    public CurrencyData getCurrencyData() {
        return currency;
    }

    @Override
    public Money getPrincipal() {
        return principal;
    }

    @Override
    public Integer getGraceOnInterestCharged() {
        return interestChargingGrace;
    }

    @Override
    public Integer getGraceOnInterestPayment() {
        return interestPaymentGrace;
    }

    @Override
    public Integer getGraceOnPrincipalPayment() {
        return principalGrace;
    }

    @Override
    public Integer getRecurringMoratoriumOnPrincipalPeriods() {
        return recurringMoratoriumOnPrincipalPeriods;
    }

    @Override
    public Money getInArrearsTolerance() {
        return inArrearsTolerance;
    }

    @Override
    public BigDecimal getNominalInterestRatePerPeriod() {
        return interestRatePerPeriod;
    }

    @Override
    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return repaymentPeriodFrequencyType;
    }

    @Override
    public BigDecimal getAnnualNominalInterestRate() {
        return annualNominalInterestRate;
    }

    @Override
    public InterestMethod getInterestMethod() {
        return interestMethod;
    }

    @Override
    public InterestCalculationPeriodMethod getInterestCalculationPeriodMethod() {
        return interestCalculationPeriodMethod;
    }

    @Override
    public Integer getRepayEvery() {
        return repaymentEvery;
    }

    @Override
    public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
        return repaymentPeriodFrequencyType;
    }

    @Override
    public Integer getNumberOfRepayments() {
        return numberOfRepayments;
    }

    @Override
    public AmortizationMethod getAmortizationMethod() {
        return amortizationMethod;
    }

    @Override
    public Integer getGraceOnArrearsAgeing() {
        return 0;
    }

    @Override
    public Integer getDaysInMonthType() {
        return daysInMonthType.getValue();
    }

    @Override
    public Integer getDaysInYearType() {
        return daysInYearType.getValue();
    }
}
