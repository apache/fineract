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

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by loan.
 */
public class SingleLoanChargeRepaymentScheduleProcessingWrapper {

    public void reprocess(final MonetaryCurrency currency, final LocalDate disbursementDate,
            final List<LoanRepaymentScheduleInstallment> installments, LoanCharge loanCharge) {
        Loan loan = loanCharge.getLoan();
        Money zero = Money.zero(currency);
        Money totalInterest = zero;
        Money totalPrincipal = zero;
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalPrincipal = totalPrincipal.plus(installment.getPrincipal(currency));
        }
        List<LoanChargePaidBy> accruals = null;
        if (loanCharge.isSpecifiedDueDate()) {
            LoanRepaymentScheduleInstallment addedPeriod = addChargeOnlyRepaymentInstallmentIfRequired(loanCharge, installments);
            if (addedPeriod != null) {
                addedPeriod.updateObligationsMet(currency, disbursementDate);
            }
            accruals = loanCharge.getLoanChargePaidBySet().stream().filter(e -> !e.getLoanTransaction().isReversed()
                    && (e.getLoanTransaction().isAccrual() || e.getLoanTransaction().isAccrualAdjustment())).toList();
        }
        LocalDate startDate = disbursementDate;
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.isDownPayment()) {
                continue;
            }
            boolean installmentChargeApplicable = !installment.isRecalculatedInterestComponent();
            Integer installmentNumber = installment.getInstallmentNumber();
            boolean isFirstPeriod = installmentNumber.equals(firstNormalInstallmentNumber);
            Predicate<LoanCharge> feePredicate = e -> e.isFeeCharge() && !e.isDueAtDisbursement();
            LocalDate dueDate = installment.getDueDate();
            final Money feeChargesDue = calcChargeDue(startDate, dueDate, loanCharge, currency, installment, totalPrincipal, totalInterest,
                    installmentChargeApplicable, isFirstPeriod, feePredicate);
            final Money feeChargesWaived = calcChargeWaived(startDate, dueDate, loanCharge, currency, installmentChargeApplicable,
                    isFirstPeriod, feePredicate);
            final Money feeChargesWrittenOff = calcChargeWrittenOff(startDate, dueDate, loanCharge, currency, installmentChargeApplicable,
                    isFirstPeriod, feePredicate);

            Predicate<LoanCharge> penaltyPredicate = LoanCharge::isPenaltyCharge;
            final Money penaltyChargesDue = calcChargeDue(startDate, dueDate, loanCharge, currency, installment, totalPrincipal,
                    totalInterest, installmentChargeApplicable, isFirstPeriod, penaltyPredicate);
            final Money penaltyChargesWaived = calcChargeWaived(startDate, dueDate, loanCharge, currency, installmentChargeApplicable,
                    isFirstPeriod, penaltyPredicate);
            final Money penaltyChargesWrittenOff = calcChargeWrittenOff(startDate, dueDate, loanCharge, currency,
                    installmentChargeApplicable, isFirstPeriod, penaltyPredicate);

            installment.addToChargePortion(feeChargesDue, feeChargesWaived, feeChargesWrittenOff, penaltyChargesDue, penaltyChargesWaived,
                    penaltyChargesWrittenOff);

            if (accruals != null && !accruals.isEmpty() && installment.isAdditional()
                    && loanCharge.isDueInPeriod(startDate, dueDate, isFirstPeriod)) {
                BigDecimal amount = null;
                for (LoanChargePaidBy accrual : accruals) {
                    accrual.setInstallmentNumber(installmentNumber);
                    amount = accrual.getLoanTransaction().isAccrual() ? MathUtil.add(amount, accrual.getAmount())
                            : MathUtil.subtract(amount, accrual.getAmount());
                }
                Money accruedAmount = Money.of(currency, MathUtil.negativeToZero(amount));
                boolean isFee = loanCharge.isFeeCharge();
                installment.updateAccrualPortion(installment.getInterestAccrued(currency),
                        MathUtil.plus(installment.getFeeAccrued(currency), (isFee ? accruedAmount : null)),
                        MathUtil.plus(installment.getPenaltyAccrued(currency), (isFee ? null : accruedAmount)));
            }
            startDate = dueDate;
        }
    }

    @NotNull
    private Money calcChargeDue(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency currency, LoanRepaymentScheduleInstallment period, final Money totalPrincipal, final Money totalInterest,
            boolean isInstallmentChargeApplicable, boolean isFirstPeriod, Predicate<LoanCharge> predicate) {
        Money zero = Money.zero(currency);
        if (!predicate.test(loanCharge)) {
            return zero;
        }
        if (loanCharge.isFeeCharge() && loanCharge.isDueAtDisbursement()) {
            return zero;
        }
        if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
            return Money.of(currency, getInstallmentFee(currency, period, loanCharge));
        }
        if (!loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod)) {
            return zero;
        }
        ChargeCalculationType calculationType = loanCharge.getChargeCalculation();
        if (loanCharge.isOverdueInstallmentCharge() && calculationType.isPercentageBased()) {
            return Money.of(currency, loanCharge.chargeAmount());
        }
        if (calculationType.isFlat()) {
            return loanCharge.getAmount(currency);
        }
        BigDecimal baseAmount = BigDecimal.ZERO;
        Loan loan = loanCharge.getLoan();
        if (loan != null && loanCharge.isFeeCharge() && !calculationType.hasInterest() && loanCharge.isSpecifiedDueDate()
                && loan.isMultiDisburmentLoan()) {
            // If charge type is specified due date and loan is multi disburment loan.
            // Then we need to get as of this loan charge due date how much amount disbursed.
            for (final LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
                if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDate(), loanCharge.getDueDate())) {
                    baseAmount = MathUtil.add(baseAmount, loanDisbursementDetails.principal());
                }
            }
        } else {
            baseAmount = getBaseAmount(loanCharge, totalPrincipal.getAmount(), totalInterest.getAmount());
        }
        return Money.of(currency, MathUtil.percentageOf(baseAmount, loanCharge.getPercentage(), MoneyHelper.getMathContext()));
    }

    private Money calcChargeWaived(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency currency, boolean isInstallmentChargeApplicable, boolean isFirstPeriod,
            Predicate<LoanCharge> predicate) {
        Money zero = Money.zero(currency);
        if (!predicate.test(loanCharge)) {
            return zero;
        }
        if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
            LoanInstallmentCharge installmentCharge = loanCharge.getInstallmentLoanCharge(periodEnd);
            return installmentCharge == null ? zero : installmentCharge.getAmountWaived(currency);
        }
        if (loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod)) {
            return loanCharge.getAmountWaived(currency);
        }
        return zero;
    }

    private Money calcChargeWrittenOff(final LocalDate periodStart, final LocalDate periodEnd, final LoanCharge loanCharge,
            final MonetaryCurrency currency, boolean isInstallmentChargeApplicable, boolean isFirstPeriod,
            Predicate<LoanCharge> predicate) {
        Money zero = Money.zero(currency);
        if (!predicate.test(loanCharge)) {
            return zero;
        }
        if (loanCharge.isInstalmentFee() && isInstallmentChargeApplicable) {
            LoanInstallmentCharge installmentCharge = loanCharge.getInstallmentLoanCharge(periodEnd);
            return installmentCharge == null ? zero : installmentCharge.getAmountWrittenOff(currency);
        }
        if (loanCharge.isDueInPeriod(periodStart, periodEnd, isFirstPeriod)) {
            return loanCharge.getAmountWrittenOff(currency);
        }
        return zero;
    }

    private BigDecimal getInstallmentFee(MonetaryCurrency currency, LoanRepaymentScheduleInstallment period, LoanCharge loanCharge) {
        if (loanCharge.getChargeCalculation().isFlat()) {
            return loanCharge.amountOrPercentage();
        }
        return MathUtil.percentageOf(getBaseAmount(currency, period, loanCharge, null), loanCharge.getPercentage(),
                MoneyHelper.getMathContext());
    }

    @NotNull
    private BigDecimal getBaseAmount(MonetaryCurrency currency, LoanRepaymentScheduleInstallment period, LoanCharge loanCharge,
            BigDecimal amount) {
        BigDecimal baseAmount = getBaseAmount(loanCharge, period.getPrincipal(currency).getAmount(),
                period.getInterestCharged(currency).getAmount());
        return MathUtil.add(amount, baseAmount);
    }

    @NotNull
    private BigDecimal getBaseAmount(LoanCharge loanCharge, BigDecimal principal, BigDecimal interest) {
        ChargeCalculationType calcType = loanCharge.getChargeCalculation();
        if (calcType.isPercentageOfAmountAndInterest()) {
            return MathUtil.add(principal, interest);
        }
        if (calcType.isPercentageOfInterest()) {
            return interest;
        }
        return principal;
    }

    /**
     * @return newly added period if there is any
     */
    public LoanRepaymentScheduleInstallment addChargeOnlyRepaymentInstallmentIfRequired(@NotNull LoanCharge loanCharge,
            List<LoanRepaymentScheduleInstallment> installments) {
        if (installments == null) {
            return null;
        }
        if (!loanCharge.isSpecifiedDueDate()) {
            return null;
        }
        LocalDate chargeDueDate = loanCharge.getEffectiveDueDate();
        LoanRepaymentScheduleInstallment latestInstallment = installments.stream().filter(i -> !i.isDownPayment())
                .reduce((first, second) -> second).orElseThrow();
        if (!DateUtils.isAfter(chargeDueDate, latestInstallment.getDueDate())) {
            return null;
        }
        if (latestInstallment.isAdditional()) {
            latestInstallment.updateDueDate(chargeDueDate);
            return null;
        } else {
            Loan loan = loanCharge.getLoan();
            final LoanRepaymentScheduleInstallment additionalInstallment = new LoanRepaymentScheduleInstallment(loan,
                    (loan.getLoanRepaymentScheduleInstallmentsSize() + 1), latestInstallment.getDueDate(), chargeDueDate, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null);
            additionalInstallment.markAsAdditional();
            loan.addLoanRepaymentScheduleInstallment(additionalInstallment);
            return additionalInstallment;
        }
    }
}
