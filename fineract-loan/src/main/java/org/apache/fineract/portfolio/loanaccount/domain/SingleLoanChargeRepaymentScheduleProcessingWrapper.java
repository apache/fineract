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
import java.util.List;
import java.util.function.Predicate;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by loan.
 */
public class SingleLoanChargeRepaymentScheduleProcessingWrapper {

    public void reprocess(final MonetaryCurrency currency, final LocalDate disbursementDate,
            final List<LoanRepaymentScheduleInstallment> repaymentPeriods, LoanCharge loanCharge) {

        Money totalInterest = Money.zero(currency);
        Money totalPrincipal = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentPeriods) {
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
        }
        LocalDate startDate = disbursementDate;
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(repaymentPeriods);
        for (final LoanRepaymentScheduleInstallment period : repaymentPeriods) {

            if (!period.isDownPayment()) {
                boolean isFirstNonDownPaymentPeriod = period.getInstallmentNumber().equals(firstNormalInstallmentNumber);

                final Money feeChargesDueForRepaymentPeriod = feeChargesDueWithin(startDate, period.getDueDate(), loanCharge, currency,
                        period, totalPrincipal, totalInterest, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod);
                final Money feeChargesWaivedForRepaymentPeriod = chargesWaivedWithin(startDate, period.getDueDate(), loanCharge, currency,
                        !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod, feeCharge());
                final Money feeChargesWrittenOffForRepaymentPeriod = loanChargesWrittenOffWithin(startDate, period.getDueDate(), loanCharge,
                        currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod, feeCharge());

                final Money penaltyChargesDueForRepaymentPeriod = penaltyChargesDueWithin(startDate, period.getDueDate(), loanCharge,
                        currency, period, totalPrincipal, totalInterest, !period.isRecalculatedInterestComponent(),
                        isFirstNonDownPaymentPeriod);
                final Money penaltyChargesWaivedForRepaymentPeriod = chargesWaivedWithin(startDate, period.getDueDate(), loanCharge,
                        currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod, LoanCharge::isPenaltyCharge);
                final Money penaltyChargesWrittenOffForRepaymentPeriod = loanChargesWrittenOffWithin(startDate, period.getDueDate(),
                        loanCharge, currency, !period.isRecalculatedInterestComponent(), isFirstNonDownPaymentPeriod,
                        LoanCharge::isPenaltyCharge);

                period.addToChargePortion(feeChargesDueForRepaymentPeriod, feeChargesWaivedForRepaymentPeriod,
                        feeChargesWrittenOffForRepaymentPeriod, penaltyChargesDueForRepaymentPeriod, penaltyChargesWaivedForRepaymentPeriod,
                        penaltyChargesWrittenOffForRepaymentPeriod);

                startDate = period.getDueDate();
            }
        }
    }

    private Money feeChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency monetaryCurrency, LoanRepaymentScheduleInstallment period, final Money totalPrincipal,
            final Money totalInterest, boolean isInstallmentChargeApplicable, boolean isFirstPeriod) {

        if (loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement()) {
            boolean isDue = loanChargeIsDue(periodStart, periodEnd, isFirstPeriod, loanCharge);
            if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                return Money.of(monetaryCurrency, getInstallmentFee(monetaryCurrency, period, loanCharge));
            } else if (loanCharge.isOverdueInstallmentCharge() && isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
                return Money.of(monetaryCurrency, loanCharge.chargeAmount());
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
                    if (loanCharge.getLoan() != null && loanCharge.isSpecifiedDueDate() && loanCharge.getLoan().isMultiDisburmentLoan()) {
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
                return Money.of(monetaryCurrency, loanChargeAmt);
            } else if (isDue) {
                return Money.of(monetaryCurrency, loanCharge.amount());
            }
        }
        return Money.zero(monetaryCurrency);
    }

    private Money chargesWaivedWithin(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency currency, boolean isInstallmentChargeApplicable, boolean isFirstPeriod,
            Predicate<LoanCharge> predicate) {

        if (predicate.test(loanCharge)) {
            boolean isDue = loanChargeIsDue(periodStart, periodEnd, isFirstPeriod, loanCharge);
            if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                if (loanChargePerInstallment != null) {
                    return loanChargePerInstallment.getAmountWaived(currency);
                }
            } else if (isDue) {
                return loanCharge.getAmountWaived(currency);
            }
        }

        return Money.zero(currency);
    }

    private Money loanChargesWrittenOffWithin(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency currency, boolean isInstallmentChargeApplicable, boolean isFirstPeriod,
            Predicate<LoanCharge> chargePredicate) {
        if (chargePredicate.test(loanCharge)) {
            boolean isDue = loanChargeIsDue(periodStart, periodEnd, isFirstPeriod, loanCharge);
            if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                LoanInstallmentCharge loanChargePerInstallment = loanCharge.getInstallmentLoanCharge(periodEnd);
                if (loanChargePerInstallment != null) {
                    return loanChargePerInstallment.getAmountWrittenOff(currency);
                }
            } else if (isDue) {
                return loanCharge.getAmountWrittenOff(currency);
            }
        }
        return Money.zero(currency);
    }

    private Predicate<LoanCharge> feeCharge() {
        return loanCharge -> loanCharge.isFeeCharge() && !loanCharge.isDueAtDisbursement();
    }

    private boolean loanChargeIsDue(LocalDate periodStart, LocalDate periodEnd, boolean isFirstPeriod, LoanCharge loanCharge) {
        return isFirstPeriod ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(periodStart, periodEnd)
                : loanCharge.isDueForCollectionFromAndUpToAndIncluding(periodStart, periodEnd);
    }

    private Money penaltyChargesDueWithin(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency currency, LoanRepaymentScheduleInstallment period, final Money totalPrincipal, final Money totalInterest,
            boolean isInstallmentChargeApplicable, boolean isFirstPeriod) {

        if (loanCharge.isPenaltyCharge()) {
            boolean isDue = loanChargeIsDue(periodStart, periodEnd, isFirstPeriod, loanCharge);
            if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
                return Money.of(currency, getInstallmentFee(currency, period, loanCharge));
            } else if (loanCharge.isOverdueInstallmentCharge() && isDue && loanCharge.getChargeCalculation().isPercentageBased()) {
                return Money.of(currency, loanCharge.chargeAmount());
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
                return Money.of(currency, loanChargeAmt);
            } else if (isDue) {
                return Money.of(currency, loanCharge.amount());
            }
        }

        return Money.zero(currency);
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

}
