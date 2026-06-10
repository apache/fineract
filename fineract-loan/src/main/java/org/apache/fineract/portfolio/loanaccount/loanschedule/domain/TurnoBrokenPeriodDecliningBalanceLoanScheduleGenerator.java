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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Turno customisation of the cumulative declining-balance schedule generator.
 *
 * <p>
 * Turno's lenders (e.g. Vivriti) keep a plain-annuity EMI (which the stock cumulative generator already produces under
 * {@code SAME_AS_REPAYMENT_PERIOD}) but charge the <b>first</b> installment's interest on the actual number of days between
 * disbursement and the first repayment ("broken period"), at a 30-day-month daily rate, rather than a full month. When the
 * broken period exceeds 30 days the excess interest is collected as a surcharge on top of the regular EMI (the first
 * installment's principal stays on a 30-day basis).
 * </p>
 *
 * <p>
 * Under {@code SAME_AS_REPAYMENT_PERIOD} the stock first-period interest equals {@code outstandingBalance * monthlyRate}
 * (a full 30-day month). Turno's first-period interest is {@code outstandingBalance * (monthlyRate / 30) * brokenDays}, i.e.
 * {@code stockInterest * brokenDays / 30}. So this generator overrides <b>only</b> the first period and leaves every other
 * period (and all non-{@code SAME_AS_REPAYMENT_PERIOD} loans) to the stock behaviour via {@code super}.
 * </p>
 *
 * <p>
 * Registered as {@link Primary} so it transparently replaces the stock cumulative declining-balance generator wherever it is
 * injected (e.g. {@code DefaultLoanScheduleGeneratorFactory}); the gate below makes it a no-op for everything except a
 * Vivriti-style first period, so other products are unaffected.
 * </p>
 */
@Component
@Primary
public class TurnoBrokenPeriodDecliningBalanceLoanScheduleGenerator extends CumulativeDecliningBalanceInterestLoanScheduleGenerator {

    private static final int DAYS_IN_MONTH = 30;
    // Turno daily rate = annualRate% / 12 (months) / 100 (percent) / 30 (days) = annualRate / 36000.
    private static final BigDecimal DAILY_RATE_DIVISOR = BigDecimal.valueOf(36000);
    // Full 30-day-month interest = OB * dailyRate * 30 = OB * annualRate / 1200.
    private static final BigDecimal THIRTY_DAY_DIVISOR = BigDecimal.valueOf(1200);

    public TurnoBrokenPeriodDecliningBalanceLoanScheduleGenerator(final ScheduledDateGenerator scheduledDateGenerator,
            final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator,
            final LoanTransactionRepository loanTransactionRepository, final CurrencyMapper currencyMapper) {
        super(scheduledDateGenerator, paymentPeriodsInOneYearCalculator, loanTransactionRepository, currencyMapper);
    }

    @Override
    public PrincipalInterest calculatePrincipalInterestComponentsForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final BigDecimal interestCalculationGraceOnRepaymentPeriodFraction, final Money totalCumulativePrincipal,
            final Money totalCumulativeInterest, final Money totalInterestDueForLoan, final Money cumulatingInterestPaymentDueToGrace,
            final Money outstandingBalance, final LoanApplicationTerms loanApplicationTerms, final int periodNumber, final MathContext mc,
            final TreeMap<LocalDate, Money> principalVariation, final Map<LocalDate, Money> compoundingMap, final LocalDate periodStartDate,
            final LocalDate periodEndDate, final Collection<LoanTermVariationsData> termVariations) {

        final PrincipalInterest stock = super.calculatePrincipalInterestComponentsForPeriod(calculator,
                interestCalculationGraceOnRepaymentPeriodFraction, totalCumulativePrincipal, totalCumulativeInterest,
                totalInterestDueForLoan, cumulatingInterestPaymentDueToGrace, outstandingBalance, loanApplicationTerms, periodNumber, mc,
                principalVariation, compoundingMap, periodStartDate, periodEndDate, termVariations);

        // Only the FIRST period of a Vivriti-style loan (plain-annuity EMI via SAME_AS_REPAYMENT_PERIOD) deviates.
        // TODO: replace this config-based gate with an explicit per-product flag when MAS/Shivalik are onboarded.
        if (periodNumber != 1 || !loanApplicationTerms.getInterestCalculationPeriodMethod().isSameAsRepaymentPeriod()) {
            return stock;
        }

        // Broken period = actual calendar days from DISBURSEMENT to the first repayment date.
        // (Under SAME_AS_REPAYMENT the period start passed in is the nominal month start, not the disbursal date.)
        final LocalDate disbursementDate = loanApplicationTerms.getExpectedDisbursementDate();
        final int brokenDays = DateUtils.getExactDifferenceInDays(disbursementDate, periodEndDate);
        if (brokenDays == DAYS_IN_MONTH) {
            return stock; // a full 30-day month: stock already matches Turno
        }

        // Turno first-period interest = round(OB * dailyRate * brokenDays), dailyRate = annualRate% / 12 / 100 / 30
        // = annualRate / 36000. Compute from the EXACT balance & rate (single rounding) to avoid the double-rounding
        // that scaling the already-rounded stock interest would introduce.
        final BigDecimal dailyRate = loanApplicationTerms.getAnnualNominalInterestRate().divide(DAILY_RATE_DIVISOR, mc);
        final BigDecimal exactInterest = outstandingBalance.getAmount().multiply(dailyRate, mc)
                .multiply(BigDecimal.valueOf(brokenDays), mc);
        final Money brokenInterest = Money.of(outstandingBalance.getCurrency(), exactInterest, mc);

        final Money emi = stock.principal().plus(stock.interest()); // plain-annuity installment (whole rupee)
        final Money newPrincipal;
        if (brokenDays > DAYS_IN_MONTH) {
            // Long first period: principal stays on a 30-day basis, so installment = EMI + surcharge.
            // Turno computes round(EMI_unrounded - OB*dailyRate*30) using the UNROUNDED annuity EMI; the
            // whole-rupee EMI would round 1 rupee differently here, so derive the unrounded EMI ourselves.
            final BigDecimal monthlyRate = loanApplicationTerms.getAnnualNominalInterestRate().divide(THIRTY_DAY_DIVISOR, mc);
            final int n = loanApplicationTerms.getNumberOfRepayments();
            final BigDecimal pow = BigDecimal.ONE.add(monthlyRate).pow(n, mc); // (1+r)^n
            final BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(pow, mc)); // 1 - (1+r)^-n
            final BigDecimal emiUnrounded = outstandingBalance.getAmount().multiply(monthlyRate, mc).divide(denominator, mc);
            final BigDecimal thirtyDayInterest = outstandingBalance.getAmount().multiply(monthlyRate, mc); // OB * r
            newPrincipal = Money.of(outstandingBalance.getCurrency(), emiUnrounded.subtract(thirtyDayInterest), mc);
        } else {
            // Short first period: installment stays = EMI; the interest saved moves to principal.
            newPrincipal = emi.minus(brokenInterest);
        }

        final PrincipalInterest result = new PrincipalInterest(newPrincipal, brokenInterest, stock.interestPaymentDueToGrace());
        result.setRescheduleInterestPortion(stock.getRescheduleInterestPortion());
        return result;
    }
}
