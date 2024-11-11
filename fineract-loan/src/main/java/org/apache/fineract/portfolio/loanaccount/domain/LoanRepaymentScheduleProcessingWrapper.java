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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by loan.
 */
public class LoanRepaymentScheduleProcessingWrapper {

    public void reprocess(final MonetaryCurrency currency, final LocalDate disbursementDate,
            final List<LoanRepaymentScheduleInstallment> repaymentPeriods, final Set<LoanCharge> loanCharges) {

        Money totalInterest = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentPeriods) {
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
        }
        LocalDate startDate = disbursementDate;
        LoanRepaymentScheduleInstallment firstNormalPeriod = repaymentPeriods.stream()
                .sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber))
                .filter(repaymentPeriod -> !repaymentPeriod.isDownPayment()).findFirst().orElseThrow();
        for (final LoanRepaymentScheduleInstallment period : repaymentPeriods) {

            if (!period.isDownPayment()) {

                boolean isFirstNonDownPaymentPeriod = period.equals(firstNormalPeriod);

                final Money feeChargesDueForRepaymentPeriod = cumulativeFeeChargesDueWithin(startDate, period.getDueDate(), loanCharges,
                        currency, period, totalPrincipal, totalInterest, !period.isRecalculatedInterestComponent(),
                        isFirstNonDownPaymentPeriod);
                final Money feeChargesWaivedForRepaymentPeriod = cumulativeChargesWaivedWithin(startDate, period.getDueDate(), loanCharges,
                        currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod, feeCharge());
                final Money feeChargesWrittenOffForRepaymentPeriod = cumulativeChargesWrittenOffWithin(startDate, period.getDueDate(),
                        loanCharges, currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod, feeCharge());

                final Money penaltyChargesDueForRepaymentPeriod = cumulativePenaltyChargesDueWithin(startDate, period.getDueDate(),
                        loanCharges, currency, period, totalPrincipal, totalInterest, !period.isRecalculatedInterestComponent(),
                        isFirstNonDownPaymentPeriod);
                final Money penaltyChargesWaivedForRepaymentPeriod = cumulativeChargesWaivedWithin(startDate, period.getDueDate(),
                        loanCharges, currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod,
                        LoanCharge::isPenaltyCharge);
                final Money penaltyChargesWrittenOffForRepaymentPeriod = cumulativeChargesWrittenOffWithin(startDate, period.getDueDate(),
                        loanCharges, currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod,
                        LoanCharge::isPenaltyCharge);

                period.updateChargePortion(feeChargesDueForRepaymentPeriod, feeChargesWaivedForRepaymentPeriod,
                        feeChargesWrittenOffForRepaymentPeriod, penaltyChargesDueForRepaymentPeriod, penaltyChargesWaivedForRepaymentPeriod,
                        penaltyChargesWrittenOffForRepaymentPeriod);

                startDate = period.getDueDate();
            }
        }
    }

    private Money cumulativeFeeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency monetaryCurrency, LoanRepaymentScheduleInstallment period, final Money totalPrincipal,
            final Money totalInterest, boolean isInstallmentChargeApplicable, boolean isFirstPeriod) {

        Money cumulative = Money.zero(monetaryCurrency);
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement()) {
                boolean isDue = loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod);
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    cumulative = cumulative.plus(getInstallmentFee(monetaryCurrency, period, loanCharge));
                } else if (loanCharge.isOverdueInstallmentCharge() && isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
                        amount = amount.add(totalPrincipal.getAmount()).add(totalInterest.getAmount());
                    } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
                        amount = amount.add(totalInterest.getAmount());
                    } else {
                        // If charge type is specified due date and loan is
                        // multi disburment loan.
                        // Then we need to get as of this loan charge due date
                        // how much amount disbursed.
                        if (loanCharge.getLoan() != null && loanCharge.isSpecifiedDueDate()
                                && loanCharge.getLoan().isMultiDisburmentLoan()) {
                            for (final LoanDisbursementDetails loanDisbursementDetails : loanCharge.getLoan().getDisbursementDetails()) {
                                if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDate(), loanCharge.getDueDate())) {
                                    amount = amount.add(loanDisbursementDetails.principal());
                                }
                            }
                        } else {
                            amount = amount.add(totalPrincipal.getAmount());
                        }
                    }
                    BigDecimal loanChargeAmt = amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
                    cumulative = cumulative.plus(loanChargeAmt);
                } else if (isDue) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private Money cumulativeChargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd, final Set<LoanCharge> loanCharges,
            final MonetaryCurrency currency, boolean isInstallmentChargeApplicable, boolean isFirstPeriod,
            Predicate<LoanCharge> predicate) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (predicate.test(loanCharge)) {
                boolean isDue = loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod);
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWaived(currency));
                    }
                } else if (isDue) {
                    cumulative = cumulative.plus(loanCharge.getAmountWaived(currency));
                }
            }
        }

        return cumulative;
    }

    private Money cumulativeChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, boolean isInstallmentChargeApplicable,
            boolean isFirstPeriod, Predicate<LoanCharge> chargePredicate) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (chargePredicate.test(loanCharge)) {
                boolean isDue = loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod);
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                    if (loanChargePerInstallment != null) {
                        cumulative = cumulative.plus(loanChargePerInstallment.getAmountWrittenOff(currency));
                    }
                } else if (isDue) {
                    cumulative = cumulative.plus(loanCharge.getAmountWrittenOff(currency));
                }
            }
        }

        return cumulative;
    }

    private Predicate<LoanCharge> feeCharge() {
        return loanCharge -> loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement();
    }

    private Money cumulativePenaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd,
            final Set<LoanCharge> loanCharges, final MonetaryCurrency currency, LoanRepaymentScheduleInstallment period,
            final Money totalPrincipal, final Money totalInterest, boolean isInstallmentChargeApplicable, boolean isFirstPeriod) {

        Money cumulative = Money.zero(currency);

        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                boolean isDue = loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod);
                if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                    cumulative = cumulative.plus(getInstallmentFee(currency, period, loanCharge));
                } else if (loanCharge.isOverdueInstallmentCharge() && isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
                    cumulative = cumulative.plus(loanCharge.chargeAmount());
                } else if (isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
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
                } else if (isDue) {
                    cumulative = cumulative.plus(loanCharge.amount());
                }
            }
        }

        return cumulative;
    }

    private BigDecimal getInstallmentFee(MonetaryCurrency currency, LoanRepaymentScheduleInstallment period, LoanCharge loanCharge) {
        if (loanCharge.getChargeCalculation().isPercentageBased()) {
            BigDecimal amount = BigDecimal.ZERO;
            amount = getBaseAmount(currency, period, loanCharge, amount);
            return amount.multiply(loanCharge.getPercentage()).divide(BigDecimal.valueOf(100));
        } else {
            return loanCharge.amountOrPercentage();
        }
    }

    @NotNull
    private BigDecimal getBaseAmount(MonetaryCurrency monetaryCurrency, LoanRepaymentScheduleInstallment period, LoanCharge loanCharge,
            BigDecimal amount) {
        if (loanCharge.getChargeCalculation().isPercentageOfAmountAndInterest()) {
            amount = amount.add(period.getPrincipal(monetaryCurrency).getAmount())
                    .add(period.getInterestCharged(monetaryCurrency).getAmount());
        } else if (loanCharge.getChargeCalculation().isPercentageOfInterest()) {
            amount = amount.add(period.getInterestCharged(monetaryCurrency).getAmount());
        } else {
            amount = amount.add(period.getPrincipal(monetaryCurrency).getAmount());
        }
        return amount;
    }

    public static int fetchFirstNormalInstallmentNumber(List<LoanRepaymentScheduleInstallment> installments) {
        return installments.stream().sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber))
                .filter(repaymentPeriod -> !repaymentPeriod.isDownPayment()).findFirst().orElseThrow().getInstallmentNumber();
    }

    public static boolean isInPeriod(LocalDate targetDate, LoanRepaymentScheduleInstallment targetInstallment,
            List<LoanRepaymentScheduleInstallment> installments) {
        int firstPeriod = fetchFirstNormalInstallmentNumber(installments);
        return isInPeriod(targetDate, targetInstallment, targetInstallment.getInstallmentNumber().equals(firstPeriod));
    }

    public static boolean isInPeriod(LocalDate targetDate, LoanRepaymentScheduleInstallment installment, boolean isFirstPeriod) {
        return isInPeriod(targetDate, installment.getFromDate(), installment.getDueDate(), isFirstPeriod);
    }

    public static boolean isInPeriod(LocalDate targetDate, LocalDate fromDate, LocalDate toDate, boolean isFirstPeriod) {
        return isFirstPeriod ? DateUtils.isDateInRangeInclusive(targetDate, fromDate, toDate)
                : DateUtils.isDateInRangeFromExclusiveToInclusive(targetDate, fromDate, toDate);
    }

    public static boolean isBeforePeriod(LocalDate targetDate, LoanRepaymentScheduleInstallment installment, boolean isFirstPeriod) {
        LocalDate fromDate = installment.getFromDate();
        return isFirstPeriod ? DateUtils.isBefore(targetDate, fromDate) : !DateUtils.isAfter(targetDate, fromDate);
    }

    public static boolean isAfterPeriod(LocalDate targetDate, LoanRepaymentScheduleInstallment installment) {
        return DateUtils.isAfter(targetDate, installment.getDueDate());
    }

    public static Optional<LoanRepaymentScheduleInstallment> findInPeriod(LocalDate targetDate,
            List<LoanRepaymentScheduleInstallment> installments) {
        int firstNumber = fetchFirstNormalInstallmentNumber(installments);
        return installments.stream().filter(e -> isInPeriod(targetDate, e, e.getInstallmentNumber().equals(firstNumber))).findFirst();
    }
}
