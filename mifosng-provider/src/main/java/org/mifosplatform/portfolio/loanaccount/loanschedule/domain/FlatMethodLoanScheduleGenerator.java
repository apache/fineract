/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public class FlatMethodLoanScheduleGenerator implements LoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

    @Override
    public LoanScheduleData generate(final ApplicationCurrency currency, final LoanProductRelatedDetail loanScheduleInfo,
            final Integer loanTermFrequency, final PeriodFrequencyType loanTermFrequencyType, final LocalDate disbursementDate,
            final LocalDate firstRepaymentDate, final LocalDate interestCalculatedFrom, final Set<LoanCharge> loanCharges) {

        final Collection<LoanSchedulePeriodData> periods = new ArrayList<LoanSchedulePeriodData>();

        final List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);

        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);

        BigDecimal loanTermPeriodsInYear = BigDecimal.valueOf(this.paymentPeriodsInOneYearCalculator.calculate(loanTermFrequencyType));
        BigDecimal interestRateForLoanTerm = loanScheduleInfo.getAnnualNominalInterestRate().divide(loanTermPeriodsInYear, mc)
                .divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc).multiply(BigDecimal.valueOf(loanTermFrequency));

        final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
        Money totalInterestForLoanTerm = loanScheduleInfo.getPrincipal().multiplyRetainScale(interestRateForLoanTerm,
                RoundingMode.HALF_EVEN);

        Money interestPerInstallment = totalInterestForLoanTerm.dividedBy(Long.valueOf(loanScheduleInfo.getNumberOfRepayments()),
                RoundingMode.HALF_EVEN);

        Money principalPerInstallment = loanScheduleInfo.getPrincipal().dividedBy(loanScheduleInfo.getNumberOfRepayments(),
                RoundingMode.HALF_EVEN);

        Money outstandingBalance = loanScheduleInfo.getPrincipal();
        Money principalDisbursed = loanScheduleInfo.getPrincipal();
        Money totalPrincipal = Money.zero(outstandingBalance.getCurrency());
        Money totalInterest = Money.zero(outstandingBalance.getCurrency());

        BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueAtDisbursement()) {
                chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
            }
        }

        BigDecimal cumulativeChargesToDate = chargesDueAtTimeOfDisbursement;

        // create entries of disbursement period on loan schedule
        final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(disbursementDate,
                principalDisbursed.getAmount(), chargesDueAtTimeOfDisbursement, false);
        periods.add(disbursementPeriod);

        int loanTermInDays = Integer.valueOf(0);
        BigDecimal totalPrincipalDisbursed = principalDisbursed.getAmount();
        BigDecimal totalPrincipalExpected = BigDecimal.ZERO;
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestCharged = BigDecimal.ZERO;
        BigDecimal totalFeeChargesCharged = BigDecimal.ZERO;
        BigDecimal totalPenaltyChargesCharged = BigDecimal.ZERO;
        BigDecimal totalWaived = BigDecimal.ZERO;
        BigDecimal totalWrittenOff = BigDecimal.ZERO;
        BigDecimal totalRepaymentExpected = chargesDueAtTimeOfDisbursement;
        BigDecimal totalRepayment = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        LocalDate startDate = disbursementDate;
        int periodNumber = 1;
        for (LocalDate scheduledDueDate : scheduledDates) {
            totalPrincipal = totalPrincipal.plus(principalPerInstallment);
            totalInterest = totalInterest.plus(interestPerInstallment);

            // number of days from startDate to this scheduledDate
            int daysInPeriod = Days.daysBetween(startDate.toDateMidnight().toDateTime(), scheduledDueDate.toDateMidnight().toDateTime())
                    .getDays();

            if (periodNumber == loanScheduleInfo.getNumberOfRepayments()) {
                final Money difference = totalPrincipal.minus(loanScheduleInfo.getPrincipal());
                if (difference.isLessThanZero()) {
                    principalPerInstallment = principalPerInstallment.plus(difference.abs());
                } else if (difference.isGreaterThanZero()) {
                    principalPerInstallment = principalPerInstallment.minus(difference.abs());
                }

                final Money interestDifference = totalInterest.minus(totalInterestForLoanTerm);
                if (interestDifference.isLessThanZero()) {
                    interestPerInstallment = interestPerInstallment.plus(interestDifference.abs());
                } else if (interestDifference.isGreaterThanZero()) {
                    interestPerInstallment = interestPerInstallment.minus(interestDifference.abs());
                }
            }

            outstandingBalance = outstandingBalance.minus(principalPerInstallment);

            final Money feeChargesForInstallment = cumulativeFeeChargesDueWithin(startDate, scheduledDueDate, loanCharges, monetaryCurrency);
            final Money penaltyChargesForInstallment = cumulativePenaltyChargesDueWithin(startDate, scheduledDueDate, loanCharges,
                    monetaryCurrency);

            final Money totalInstallmentDue = principalPerInstallment //
                    .plus(interestPerInstallment) //
                    .plus(feeChargesForInstallment) //
                    .plus(penaltyChargesForInstallment);

            cumulativeChargesToDate = cumulativeChargesToDate.add(feeChargesForInstallment.getAmount()).add(
                    penaltyChargesForInstallment.getAmount());

            LoanSchedulePeriodData installment = LoanSchedulePeriodData.repaymentOnlyPeriod(periodNumber, startDate, scheduledDueDate,
                    principalPerInstallment.getAmount(), outstandingBalance.getAmount(), interestPerInstallment.getAmount(),
                    feeChargesForInstallment.getAmount(), penaltyChargesForInstallment.getAmount(), totalInstallmentDue.getAmount());

            periods.add(installment);

            // handle cumulative fields
            loanTermInDays += daysInPeriod;
            totalPrincipalExpected = totalPrincipalExpected.add(principalPerInstallment.getAmount());
            totalInterestCharged = totalInterestCharged.add(interestPerInstallment.getAmount());
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