/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public class EqualPrincipalAmortizationLoanScheduleGenerator implements AmortizationLoanScheduleGenerator {

    private final PeriodicInterestRateCalculator periodicInterestRateCalculator = new PeriodicInterestRateCalculator();
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

    @Override
    public LoanScheduleData generate(final ApplicationCurrency currency, final LoanProductRelatedDetail loanScheduleInfo,
            final LocalDate disbursementDate, final LocalDate interestCalculatedFrom,
            final BigDecimal periodInterestRateForRepaymentPeriod, final LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
            final List<LocalDate> scheduledDates, final Set<LoanCharge> loanCharges) {

        final Collection<LoanSchedulePeriodData> periods = new ArrayList<LoanSchedulePeriodData>();

        // 1. we know 'principal' must be equal over repayments so use this to
        // calculate interest and thus total repayment due.
        final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
        Money totalDuePerInstallment = Money.zero(monetaryCurrency);

        Money outstandingBalance = loanScheduleInfo.getPrincipal();
        Money principalDisbursed = loanScheduleInfo.getPrincipal();
        Money totalPrincipal = Money.zero(monetaryCurrency);
        Money totalInterest = Money.zero(monetaryCurrency);

        double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator
                .calculateRepaymentPeriodAsAFractionOfDays(loanScheduleInfo.getRepaymentPeriodFrequencyType(),
                        loanScheduleInfo.getRepayEvery(), interestCalculatedFrom, scheduledDates,
                        idealDisbursementDateBasedOnFirstRepaymentDate);

        BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement()) {
                chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
            }
        }

        // create entries of disbursement period on loan schedule
        final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(disbursementDate,
                principalDisbursed.getAmount(), chargesDueAtTimeOfDisbursement, false);
        periods.add(disbursementPeriod);

        int loanTermInDays = Integer.valueOf(0);
        BigDecimal totalPrincipalDisbursed = principalDisbursed.getAmount();
        BigDecimal totalPrincipalExpected = BigDecimal.ZERO;
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestCharged = BigDecimal.ZERO;
        BigDecimal totalFeeChargesCharged = chargesDueAtTimeOfDisbursement;
        BigDecimal totalPenaltyChargesCharged = BigDecimal.ZERO;
        BigDecimal totalWaived = BigDecimal.ZERO;
        BigDecimal totalWrittenOff = BigDecimal.ZERO;
        BigDecimal totalRepaymentExpected = chargesDueAtTimeOfDisbursement;
        BigDecimal totalRepayment = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        LocalDate startDate = disbursementDate;
        int periodNumber = 1;
        for (LocalDate scheduledDueDate : scheduledDates) {

            // number of days from startDate to this scheduledDate
            int daysInPeriod = Days.daysBetween(startDate.toDateMidnight().toDateTime(), scheduledDueDate.toDateMidnight().toDateTime())
                    .getDays();

            Money interestForInstallment = this.periodicInterestRateCalculator.calculateInterestOn(outstandingBalance,
                    periodInterestRateForRepaymentPeriod, daysInPeriod, loanScheduleInfo);
            Money principalForInstallment = this.periodicInterestRateCalculator.calculatePrincipalOn(totalDuePerInstallment,
                    interestForInstallment, loanScheduleInfo);

            if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
                Money graceOnInterestForRepaymentPeriod = interestForInstallment;
                interestForInstallment = interestForInstallment.minus(graceOnInterestForRepaymentPeriod);
                interestCalculationGraceOnRepaymentPeriodFraction = interestCalculationGraceOnRepaymentPeriodFraction
                        - Integer.valueOf(1).doubleValue();
            } else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25")
                    && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {
                Money graceOnInterestForRepaymentPeriod = interestForInstallment
                        .multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
                interestForInstallment = interestForInstallment.minus(graceOnInterestForRepaymentPeriod);
                interestCalculationGraceOnRepaymentPeriodFraction = Double.valueOf("0");
            }

            totalPrincipal = totalPrincipal.plus(principalForInstallment);
            totalInterest = totalInterest.plus(interestForInstallment);

            if (periodNumber == loanScheduleInfo.getNumberOfRepayments()) {
                Money principalDifference = totalPrincipal.minus(loanScheduleInfo.getPrincipal());
                if (principalDifference.isLessThanZero()) {
                    principalForInstallment = principalForInstallment.plus(principalDifference.abs());
                } else if (principalDifference.isGreaterThanZero()) {
                    principalForInstallment = principalForInstallment.minus(principalDifference.abs());
                }
            }

            outstandingBalance = outstandingBalance.minus(principalForInstallment);

            final Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(startDate, scheduledDueDate, loanCharges, monetaryCurrency);
            final Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(startDate, scheduledDueDate, loanCharges,
                    monetaryCurrency);

            final Money totalInstallmentDue = principalForInstallment.plus(interestForInstallment).plus(feeChargesForInstallment)
                    .plus(penaltyChargesForInstallment);

            LoanSchedulePeriodData installment = LoanSchedulePeriodData.repaymentOnlyPeriod(periodNumber, startDate, scheduledDueDate,
                    principalForInstallment.getAmount(), outstandingBalance.getAmount(), interestForInstallment.getAmount(),
                    feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount(), totalInstallmentDue.getAmount());

            periods.add(installment);

            // handle cumulative fields
            loanTermInDays += daysInPeriod;
            totalPrincipalExpected = totalPrincipalExpected.add(principalForInstallment.getAmount());
            totalInterestCharged = totalInterestCharged.add(interestForInstallment.getAmount());
            totalFeeChargesCharged = totalFeeChargesCharged.add(feeChargesForInstallment.getAmount());
            totalPenaltyChargesCharged = totalPenaltyChargesCharged.add(penaltyChargesForInstallment.getAmount());
            totalRepaymentExpected = totalRepaymentExpected.add(totalInstallmentDue.getAmount());
            startDate = scheduledDueDate;

            periodNumber++;
        }

        final CurrencyData currencyData = new CurrencyData(currency.getCode(), currency.getName(),
                monetaryCurrency.getDigitsAfterDecimal(), currency.getDisplaySymbol(), currency.getNameCode());

        return new LoanScheduleData(currencyData, periods, loanTermInDays, totalPrincipalDisbursed, totalPrincipalExpected,
                totalPrincipalPaid, totalInterestCharged, totalFeeChargesCharged, totalPenaltyChargesCharged, totalWaived, totalWrittenOff,
                totalRepaymentExpected, totalRepayment, totalOutstanding);
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.amount());
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency monetaryCurrency) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.amount());
            }
        }

        return cumulative;
    }
}