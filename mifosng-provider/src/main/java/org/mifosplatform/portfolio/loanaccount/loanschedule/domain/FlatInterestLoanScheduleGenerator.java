/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;
import java.util.Map;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;

public class FlatInterestLoanScheduleGenerator extends AbstractLoanScheduleGenerator {

    @Override
    public PrincipalInterest calculatePrincipalInterestComponentsForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final Money totalCumulativePrincipal,
            final Money totalCumulativeInterest, final Money totalInterestDueForLoan, final Money cumulatingInterestPaymentDueToGrace,
            final Money outstandingBalance, final LoanApplicationTerms loanApplicationTerms, final int periodNumber, final MathContext mc,
            @SuppressWarnings("unused") Map<LocalDate, Money> principalVariation, LocalDate periodStartDate, LocalDate periodEndDate,
            @SuppressWarnings("unused") int daysForInterestInFullPeriod) {
        final int daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDate, periodEndDate).getDays();
        Money principalForThisInstallment = loanApplicationTerms.calculateTotalPrincipalForPeriod(calculator,
                daysInPeriodApplicableForInterest, outstandingBalance, periodNumber, mc, null);

        final PrincipalInterest result = loanApplicationTerms.calculateTotalInterestForPeriod(calculator,
                interestCalculationGraceOnRepaymentPeriodFraction, periodNumber, mc, cumulatingInterestPaymentDueToGrace,
                daysInPeriodApplicableForInterest, outstandingBalance);
        Money interestForThisInstallment = result.interest();

        // update cumulative fields for principal & interest
        final Money interestBroughtForwardDueToGrace = result.interestPaymentDueToGrace();
        final Money totalCumulativePrincipalToDate = totalCumulativePrincipal.plus(principalForThisInstallment);
        final Money totalCumulativeInterestToDate = totalCumulativeInterest.plus(interestForThisInstallment);

        // adjust if needed
        principalForThisInstallment = loanApplicationTerms.adjustPrincipalIfLastRepaymentPeriod(principalForThisInstallment,
                totalCumulativePrincipalToDate, periodNumber);

        interestForThisInstallment = loanApplicationTerms.adjustInterestIfLastRepaymentPeriod(interestForThisInstallment,
                totalCumulativeInterestToDate, totalInterestDueForLoan, periodNumber);

        return new PrincipalInterest(principalForThisInstallment, interestForThisInstallment, interestBroughtForwardDueToGrace);
    }
}