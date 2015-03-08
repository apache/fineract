/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
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
            final List<LoanRepaymentScheduleInstallment> repaymentPeriods, final Set<LoanCharge> loanCharges,
            final LocalDate lastTransactionDate) {

        Money totalInterest = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentPeriods) {
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
        }
        LocalDate startDate = disbursementDate;
        for (final LoanRepaymentScheduleInstallment period : repaymentPeriods) {

            final Money feeChargesDueForRepaymentPeriod = cumulativeFeeChargesDueWithin(startDate, period.getDueDate(), loanCharges,
                    currency, period, repaymentPeriods.size(), totalPrincipal, totalInterest, true,
                    lastTransactionDate);
            final Money feeChargesWaivedForRepaymentPeriod = cumulativeFeeChargesWaivedWithin(startDate, period.getDueDate(), loanCharges,
                    currency, true);
            final Money feeChargesWrittenOffForRepaymentPeriod = cumulativeFeeChargesWrittenOffWithin(startDate, period.getDueDate(),
                    loanCharges, currency, true);

            final Money penaltyChargesDueForRepaymentPeriod = cumulativePenaltyChargesDueWithin(startDate, period.getDueDate(),
                    loanCharges, currency, period, repaymentPeriods.size(), totalPrincipal, totalInterest,
                    true, lastTransactionDate);
            final Money penaltyChargesWaivedForRepaymentPeriod = cumulativePenaltyChargesWaivedWithin(startDate, period.getDueDate(),
                    loanCharges, currency, true);
            final Money penaltyChargesWrittenOffForRepaymentPeriod = cumulativePenaltyChargesWrittenOffWithin(startDate,
                    period.getDueDate(), loanCharges, currency, true);

            period.updateChargePortion(feeChargesDueForRepaymentPeriod, feeChargesWaivedForRepaymentPeriod,
                    feeChargesWrittenOffForRepaymentPeriod, penaltyChargesDueForRepaymentPeriod, penaltyChargesWaivedForRepaymentPeriod,
                    penaltyChargesWrittenOffForRepaymentPeriod);

            startDate = period.getDueDate();
        }
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency, LoanRepaymentScheduleInstallment period, int numberOfRepayments,
            final Money totalPrincipal, final Money totalInterest, boolean isInstallmentChargeApplicable,
            final LocalDate lastTransactionDate) {

        Money cumulative = Money.zero(monetaryCurrency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    if (loanCharge.getChargeCalculation().isPercentageBased()) {
                        BigDecimal amount = BigDecimal.ZERO;
                        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                            amount = amount.add(period.getPrincipal(monetaryCurrency).getAmount()).add(
                                    period.getInterestCharged(monetaryCurrency).getAmount());
                        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                            amount = amount.add(period.getInterestCharged(monetaryCurrency).getAmount());
                        } else {
                            amount = amount.add(period.getPrincipal(monetaryCurrency).getAmount());
                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = cumulative.plus(loanChargeAmt);
                    } else {
                        cumulative = cumulative.plus(loanCharge.amount().divide(BigDecimal.valueOf(numberOfRepayments)));
                    }
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                        amount = amount.add(totalPrincipal.getAmount()).add(totalInterest.getAmount());
                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                        amount = amount.add(totalInterest.getAmount());
                    } else {
                        amount = amount.add(totalPrincipal.getAmount());
                    }
                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                    cumulative = cumulative.plus(loanChargeAmt);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWaived(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
                }
            }
        }

        return cumulative;
    }

    private Money cumulativeFeeChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWrittenOff(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
                }
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, LoanRepaymentScheduleInstallment period,
            int numberOfRepayments, final Money totalPrincipal, final Money totalInterest, boolean isInstallmentChargeApplicable,
            final LocalDate lastTransactionDate) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    if (loanCharge.getChargeCalculation().isPercentageBased()) {
                        BigDecimal amount = BigDecimal.ZERO;
                        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                            amount = amount.add(period.getPrincipal(currency).getAmount()).add(
                                    period.getInterestCharged(currency).getAmount());
                        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                            amount = amount.add(period.getInterestCharged(currency).getAmount());
                        } else {
                            amount = amount.add(period.getPrincipal(currency).getAmount());
                        }
                        BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                        cumulative = cumulative.plus(loanChargeAmt);
                    } else {
                        cumulative = cumulative.plus(loanCharge.amount().divide(BigDecimal.valueOf(numberOfRepayments)));
                    }
                } else if (loanCharge.isOverdueInstallmentCharge()
                        && loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)
                        && loanCharge.getChargeCalculation().isPercentageBased()
                        && !lastTransactionDate.isAfter(loanCharge.getDueLocalDate())) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                        amount = amount.add(totalPrincipal.getAmount()).add(totalInterest.getAmount());
                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                        amount = amount.add(totalInterest.getAmount());
                    } else {
                        amount = amount.add(totalPrincipal.getAmount());
                    }
                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                    cumulative = cumulative.plus(loanChargeAmt);
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWaived(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
                }
            }
        }

        return cumulative;
    }

    private Money cumulativePenaltyChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWrittenOff(currency));
                    }
                } else if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd)) {
                    cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
                }
            }
        }

        return cumulative;
    }
}