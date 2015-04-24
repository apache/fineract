/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.service.DateUtils;
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
            final Money outstandingBalance, final LoanApplicationTerms loanApplicationTerms, final int periodNumber, final MathContext mc,
            final TreeMap<LocalDate, Money> principalVariation, final Map<LocalDate, Money> compoundingMap,
            final LocalDate periodStartDate, final LocalDate periodEndDate, final int daysForInterestInFullPeriod) {

        LocalDate interestStartDate = periodStartDate;
        Money interestForThisInstallment = totalCumulativePrincipal.zero();
        Money compoundedMoney = totalCumulativePrincipal.zero();
        Money compoundedInterest = totalCumulativePrincipal.zero();
        Money balanceForInterestCalculation = outstandingBalance;
        Money cumulatingInterestDueToGrace = cumulatingInterestPaymentDueToGrace;
        final int daysInPeriodApplicableForInterest = Days.daysBetween(periodStartDate, periodEndDate).getDays();
        if (principalVariation != null) {
            // identifies rest date after current date for reducing all
            // compounding
            // values
            LocalDate compoundingEndDate = principalVariation.ceilingKey(DateUtils.getLocalDateOfTenant());
            if (compoundingEndDate == null) {
                compoundingEndDate = DateUtils.getLocalDateOfTenant();
            }

            for (Map.Entry<LocalDate, Money> principal : principalVariation.entrySet()) {

                if (!principal.getKey().isAfter(periodEndDate)) {
                    int interestForDays = Days.daysBetween(interestStartDate, principal.getKey()).getDays();
                    if (interestForDays > 0) {
                        final PrincipalInterest result = loanApplicationTerms.calculateTotalInterestForPeriod(calculator,
                                interestCalculationGraceOnRepaymentPeriodFraction, periodNumber, mc, cumulatingInterestDueToGrace,
                                interestForDays, balanceForInterestCalculation);
                        if (loanApplicationTerms.getInterestCalculationPeriodMethod().isDaily()) {
                            interestForThisInstallment = interestForThisInstallment.plus(result.interest());
                            cumulatingInterestDueToGrace = result.interestPaymentDueToGrace();
                        } else {
                            interestForThisInstallment = interestForThisInstallment.plus(calculateInterestForDays(
                                    daysForInterestInFullPeriod, result.interest().getAmount(), interestForDays));
                            cumulatingInterestDueToGrace = cumulatingInterestDueToGrace.plus(calculateInterestForDays(
                                    daysForInterestInFullPeriod, result.interestPaymentDueToGrace().minus(cumulatingInterestDueToGrace)
                                            .getAmount(), interestForDays));
                        }

                        interestStartDate = principal.getKey();
                    }
                    Money compoundFee = totalCumulativePrincipal.zero();
                    if (compoundingMap.containsKey(principal.getKey())) {
                        Money interestToBeCompounded = totalCumulativePrincipal.zero();
                        // for interest compounding
                        if (loanApplicationTerms.getInterestRecalculationCompoundingMethod().isInterestCompoundingEnabled()) {
                            interestToBeCompounded = interestForThisInstallment.minus(compoundedInterest);
                            balanceForInterestCalculation = balanceForInterestCalculation.plus(interestToBeCompounded);
                            compoundedInterest = interestForThisInstallment;
                        }
                        // fee compounding will be done after calculation
                        compoundFee = compoundingMap.get(principal.getKey());
                        compoundedMoney = compoundedMoney.plus(interestToBeCompounded).plus(compoundFee);
                    }
                    balanceForInterestCalculation = balanceForInterestCalculation.plus(principal.getValue()).plus(compoundFee);
                }

            }
            if (!periodEndDate.isBefore(compoundingEndDate)) {
                balanceForInterestCalculation = balanceForInterestCalculation.minus(compoundedMoney);
                compoundingMap.clear();
            } else if (compoundedMoney.isGreaterThanZero()) {
                compoundingMap.put(periodEndDate, compoundedMoney);
                compoundingMap.put(compoundingEndDate, compoundedMoney.negated());
                clearMapDetails(periodEndDate, compoundingMap);
            }
        }
        int interestForDays = Days.daysBetween(interestStartDate, periodEndDate).getDays();

        final PrincipalInterest result = loanApplicationTerms.calculateTotalInterestForPeriod(calculator,
                interestCalculationGraceOnRepaymentPeriodFraction, periodNumber, mc, cumulatingInterestDueToGrace, interestForDays,
                balanceForInterestCalculation);
        if (loanApplicationTerms.getInterestCalculationPeriodMethod().isDaily()) {
            interestForThisInstallment = interestForThisInstallment.plus(result.interest());
            cumulatingInterestDueToGrace = result.interestPaymentDueToGrace();
        } else {
            interestForThisInstallment = interestForThisInstallment.plus(calculateInterestForDays(daysForInterestInFullPeriod, result
                    .interest().getAmount(), interestForDays));
            cumulatingInterestDueToGrace = cumulatingInterestDueToGrace.plus(calculateInterestForDays(
                    daysForInterestInFullPeriod, result.interestPaymentDueToGrace().minus(cumulatingInterestDueToGrace)
                            .getAmount(), interestForDays));
        }

        Money interestForPeriod = interestForThisInstallment;
        if (interestForPeriod.isGreaterThanZero()) {
            interestForPeriod = interestForPeriod.minus(cumulatingInterestPaymentDueToGrace);
        } else {
            interestForPeriod = cumulatingInterestDueToGrace.minus(cumulatingInterestPaymentDueToGrace);
        }
        Money principalForThisInstallment = loanApplicationTerms.calculateTotalPrincipalForPeriod(calculator,
                daysInPeriodApplicableForInterest, outstandingBalance, periodNumber, mc, interestForPeriod);

        // update cumulative fields for principal & interest
        final Money interestBroughtFowardDueToGrace = cumulatingInterestDueToGrace;
        final Money totalCumulativePrincipalToDate = totalCumulativePrincipal.plus(principalForThisInstallment);

        // adjust if needed
        principalForThisInstallment = loanApplicationTerms.adjustPrincipalIfLastRepaymentPeriod(principalForThisInstallment,
                totalCumulativePrincipalToDate, periodNumber);

        return new PrincipalInterest(principalForThisInstallment, interestForThisInstallment, interestBroughtFowardDueToGrace);
    }
}