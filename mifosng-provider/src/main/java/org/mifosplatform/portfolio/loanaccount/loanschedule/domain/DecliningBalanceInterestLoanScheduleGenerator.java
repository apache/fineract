/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;

import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;

/**
 * <p>
 * Declining balance can be amortized (see {@link AmortizationMethod}) in two
 * ways at present:
 * <ol>
 * <li>Equal principal payments</li>
 * <li>Equal installment payments</li>
 * </ol>
 * </p>
 *
 * <p>
 * When amortized using <i>equal principal payments</i>, the <b>principal
 * component</b> of each installment is fixed and <b>interest due</b> is
 * calculated from the <b>outstanding principal balance</b> resulting in a
 * different <b>total payment due</b> for each installment.
 * </p>
 *
 * <p>
 * When amortized using <i>equal installments</i>, the <b>total payment due</b>
 * for each installment is fixed and is calculated using the excel like
 * <code>pmt</code> function. The <b>interest due</b> is calculated from the
 * <b>outstanding principal balance</b> which results in a <b>principal
 * component</b> that is <b>total payment due</b> minus <b>interest due</b>.
 * </p>
 */
public class DecliningBalanceInterestLoanScheduleGenerator extends AbstractLoanScheduleGenerator {

    @Override
    public PrincipalInterest calculatePrincipalInterestComponentsForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final Money totalCumulativePrincipal,
            @SuppressWarnings("unused") final Money totalCumulativeInterest,
            @SuppressWarnings("unused") final Money totalInterestDueForLoan, final Money cumulatingInterestPaymentDueToGrace,
            final int daysInPeriodApplicableForInterest, final Money outstandingBalance, final LoanApplicationTerms loanApplicationTerms,
            final int periodNumber, final MathContext mc) {

        final PrincipalInterest result = loanApplicationTerms.calculateTotalInterestForPeriod(calculator,
                interestCalculationGraceOnRepaymentPeriodFraction, periodNumber, mc, cumulatingInterestPaymentDueToGrace,
                daysInPeriodApplicableForInterest, outstandingBalance);

        final Money interestForThisInstallment = result.interest();

        Money principalForThisInstallment = loanApplicationTerms.calculateTotalPrincipalForPeriod(calculator,
                daysInPeriodApplicableForInterest, outstandingBalance, periodNumber, mc);

        // update cumulative fields for principal & interest
        final Money interestBroughtFowardDueToGrace = result.interestPaymentDueToGrace();
        final Money totalCumulativePrincipalToDate = totalCumulativePrincipal.plus(principalForThisInstallment);

        // adjust if needed
        principalForThisInstallment = loanApplicationTerms.adjustPrincipalIfLastRepaymentPeriod(principalForThisInstallment,
                totalCumulativePrincipalToDate, periodNumber);

        return new PrincipalInterest(principalForThisInstallment, interestForThisInstallment, interestBroughtFowardDueToGrace);
    }
}