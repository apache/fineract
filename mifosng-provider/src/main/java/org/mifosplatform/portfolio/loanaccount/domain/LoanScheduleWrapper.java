package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by
 * loan.
 * 
 * This wrapper should be side-effect free. It should not attempt to change
 * state of loan parts passed through to it.
 */
public class LoanScheduleWrapper {

    public Money calculateTotalOutstanding(final MonetaryCurrency currency, final List<LoanRepaymentScheduleInstallment> installments) {

        Money cumulativeValue = Money.zero(currency);

        for (LoanRepaymentScheduleInstallment scheduledRepayment : installments) {
            cumulativeValue = cumulativeValue.plus(scheduledRepayment.getTotalOutstanding(currency));
        }

        return cumulativeValue;
    }

    /**
     * FIXME - this is not a side effect free approach so move this into a
     * specific wrapper than makes this obvious from the read-only wrapper.
     */
    public void reprocess(final MonetaryCurrency currency, final LocalDate disbursementDate,
            final List<LoanRepaymentScheduleInstallment> repaymentPeriods, final Set<LoanCharge> loanCharges) {

        LocalDate startDate = disbursementDate;
        for (LoanRepaymentScheduleInstallment period : repaymentPeriods) {

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

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionBetween(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.amount());
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionBetween(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionBetween(periodStart, periodEnd) && loanCharge.isFeeCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionBetween(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.amount());
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionBetween(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency) {

        Money cumulative = Money.zero(currency);

        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isDueForCollectionBetween(periodStart, periodEnd) && loanCharge.isPenaltyCharge()) {
                cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
            }
        }

        return cumulative;
    }
}