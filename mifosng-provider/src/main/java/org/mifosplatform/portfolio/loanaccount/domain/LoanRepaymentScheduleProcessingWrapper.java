/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by
 * loan.
 */
public class LoanRepaymentScheduleProcessingWrapper {

    public void reprocess(final MonetaryCurrency currency, final LocalDate disbursementDate,
            final List<LoanRepaymentScheduleInstallment> repaymentPeriods, final Set<LoanCharge> loanCharges) {

        LocalDate startDate = disbursementDate;
        for (final LoanRepaymentScheduleInstallment period : repaymentPeriods) {

            final Money feeChargesDueForRepaymentPeriod = cumulativeFeeChargesDueWithin(startDate, period.getDueDate(), loanCharges,
                    currency);
            final Money feeChargesWaivedForRepaymentPeriod = cumulativeFeeChargesWaivedWithin(startDate, period.getDueDate(), loanCharges,
                    currency);
            final Money feeChargesWrittenOffForRepaymentPeriod = cumulativeFeeChargesWrittenOffWithin(startDate, period.getDueDate(),
                    loanCharges, currency);

            final Money penaltyChargesDueForRepaymentPeriod = cumulativePenaltyChargesDueWithin(startDate, period.getDueDate(),
                    loanCharges, currency);
            final Money penaltyChargesWaivedForRepaymentPeriod = cumulativePenaltyChargesWaivedWithin(startDate, period.getDueDate(),
                    loanCharges, currency);
            final Money penaltyChargesWrittenOffForRepaymentPeriod = cumulativePenaltyChargesWrittenOffWithin(startDate,
                    period.getDueDate(), loanCharges, currency);

            period.updateChargePortion(feeChargesDueForRepaymentPeriod, feeChargesWaivedForRepaymentPeriod,
                    feeChargesWrittenOffForRepaymentPeriod, penaltyChargesDueForRepaymentPeriod, penaltyChargesWaivedForRepaymentPeriod,
                    penaltyChargesWrittenOffForRepaymentPeriod);

            startDate = period.getDueDate();
        }
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.amount());
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.amount());
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
            }
        }

        return cumulative;
    }
}