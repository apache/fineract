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
package org.apache.fineract.portfolio.loanaccount;

import java.math.BigDecimal;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

/**
 * This class is used to keep in one place configurations for setting up
 * {@link LoanProductRelatedDetail} object used in {@link LoanScheduleGenerator}
 * 's
 */
public class LoanProductRelatedDetailTestHelper {

    public static LoanProductRelatedDetail createSettingsForEqualPrincipalAmortizationQuarterly() {

        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(4);
        final AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_PRINCIPAL;

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    public static LoanProductRelatedDetail createSettingsForEqualInstallmentAmortizationQuarterly() {
        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(4);
        final AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_INSTALLMENTS;

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    public static LoanProductRelatedDetail createSettingsForFlatQuarterly(final AmortizationMethod amortizationMethod) {

        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.FLAT;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(4);

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    public static LoanProductRelatedDetail createSettingsForIrregularFlatEveryFourMonths() {

        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("KSH").withDigitsAfterDecimal(0).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("15000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.FLAT;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(2);

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        final AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_PRINCIPAL;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    private static LoanProductRelatedDetail createLoanProductRelatedDetail(final MonetaryCurrency currency,
            final BigDecimal defaultPrincipal, final BigDecimal defaultNominalInterestRatePerPeriod,
            final PeriodFrequencyType interestPeriodFrequencyType, final BigDecimal defaultAnnualNominalInterestRate,
            final InterestMethod interestMethod, final InterestCalculationPeriodMethod interestCalculationPeriodMethod,
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfRepayments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance) {

        final Integer graceOnPrincipalPayment = Integer.valueOf(0);
        final Integer recurringMoratoriumOnPrincipalPeriods = Integer.valueOf(0);
        final Integer graceOnInterestPayment = Integer.valueOf(0);
        final Integer graceOnInterestCharged = Integer.valueOf(0);
        final Integer graceOnArrearsAgeing = Integer.valueOf(0);

        final Integer daysInMonthType = DaysInMonthType.ACTUAL.getValue();
        final Integer daysInYearType = DaysInYearType.ACTUAL.getValue();
        final boolean isInterestRecalculationEnabled = false;
        final boolean considerPartialPeriodInterest = false;
        final boolean isEqualAmortization = false;
        return new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, considerPartialPeriodInterest, repayEvery,
                repaymentFrequencyType, defaultNumberOfRepayments, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged,
                amortizationMethod, inArrearsTolerance, graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, isEqualAmortization);
    }
}