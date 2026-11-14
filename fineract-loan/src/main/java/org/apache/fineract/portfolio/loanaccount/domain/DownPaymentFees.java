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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;

/**
 * Down payment fees belong to the down payment period of their tranche ({@link LoanCharge#getDisbursementDetail()}).
 * Every disbursement has its own down payment period on its date, so when several tranches share a date the n-th
 * tranche of that date owns the n-th period. A fee without a tranche (single disbursement loan) belongs to the only
 * period on its date. The fee of a tranche that is not disbursed yet belongs to no period
 * ({@link LoanCharge#isDownPaymentFeeAwaitingDisbursement()}).
 */
public final class DownPaymentFees {

    private static final Comparator<LoanCharge> BY_ID = Comparator.comparing(LoanCharge::getId, Comparator.nullsLast(Long::compareTo));

    private DownPaymentFees() {}

    public static List<LoanCharge> dueOn(final Collection<LoanCharge> charges, final LocalDate date) {
        if (charges == null || date == null) {
            return List.of();
        }
        return charges
                .stream().filter(charge -> charge.isActive() && charge.isDueAtDownPayment()
                        && !charge.isDownPaymentFeeAwaitingDisbursement() && DateUtils.isEqual(date, charge.getDueLocalDate()))
                .sorted(BY_ID).toList();
    }

    public static List<LoanRepaymentScheduleInstallment> downPaymentPeriodsOn(
            final Collection<LoanRepaymentScheduleInstallment> installments, final LocalDate date) {
        return installments
                .stream().filter(i -> i.isDownPayment() && DateUtils.isEqual(date, i.getDueDate())).sorted(Comparator
                        .comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    // 0-based index of the down payment period on the fee's due date the fee belongs to
    public static int periodIndexOf(final LoanCharge fee, final int periodsOnDate) {
        if (periodsOnDate <= 1 || fee.getDisbursementDetail() == null || fee.getLoan() == null) {
            return 0;
        }
        final List<LoanDisbursementDetails> tranchesOnDate = fee.getLoan().getDisbursementDetails().stream()
                .filter(tranche -> DateUtils.isEqual(fee.getDueLocalDate(), tranche.actualOrExpectedDisbursementDate())).toList();
        for (int index = 0; index < tranchesOnDate.size(); index++) {
            if (fee.isDownPaymentFeeOf(tranchesOnDate.get(index))) {
                return Math.min(index, periodsOnDate - 1);
            }
        }
        return 0;
    }

    public static List<LoanCharge> ofPeriod(final Collection<LoanCharge> charges, final LocalDate date, final int periodIndex,
            final int periodsOnDate) {
        return dueOn(charges, date).stream().filter(fee -> periodIndexOf(fee, periodsOnDate) == periodIndex).toList();
    }

    public static List<LoanCharge> ofPeriod(final Collection<LoanCharge> charges, final LoanRepaymentScheduleInstallment downPaymentPeriod,
            final Collection<LoanRepaymentScheduleInstallment> installments) {
        final List<LoanRepaymentScheduleInstallment> periodsOnDate = downPaymentPeriodsOn(installments, downPaymentPeriod.getDueDate());
        return ofPeriod(charges, downPaymentPeriod.getDueDate(), periodsOnDate.indexOf(downPaymentPeriod), periodsOnDate.size());
    }

    // Amount of the fees of the given down payment period an automatic down payment has to collect
    public static Money amountToCollect(final Collection<LoanCharge> charges, final MonetaryCurrency currency, final LocalDate date,
            final int periodIndex, final int periodsOnDate) {
        return ofPeriod(charges, date, periodIndex, periodsOnDate).stream().filter(fee -> !fee.isWaived())
                .map(fee -> amountToCollect(fee, currency)).reduce(Money.zero(currency), Money::plus);
    }

    // Prefers outstanding; a fee that still looks fully paid (reverse-replay) is collected in full
    private static Money amountToCollect(final LoanCharge fee, final MonetaryCurrency currency) {
        final Money outstanding = fee.getAmountOutstanding(currency);
        return outstanding.isGreaterThanZero() ? outstanding : fee.getAmount(currency);
    }

    /**
     * Fee refund of a refund, in date order: the Fee refunds of the earlier refunds are calculated first, the same way,
     * and use up the cap. Every refund of a supported type counts, whether or not its Fee refund exists yet, because a
     * missing one is created on the next replay. The amount depends only on the refunds, disbursements and fees of the
     * loan, not on the order the refunds were entered or on the state of a replay.
     */
    public static BigDecimal refundAmount(final Loan loan, final LoanTransaction refund) {
        final List<LoanTransaction> earlierRefunds = supportedRefunds(loan).stream()
                .filter(earlier -> LoanTransactionComparator.INSTANCE.compare(earlier, refund) < 0).toList();
        BigDecimal alreadyRefunded = BigDecimal.ZERO;
        for (final LoanTransaction earlier : earlierRefunds) {
            alreadyRefunded = alreadyRefunded.add(refundAmount(loan, earlier.getAmount(), earlier.getTransactionDate(), alreadyRefunded));
        }
        return refundAmount(loan, refund.getAmount(), refund.getTransactionDate(), alreadyRefunded);
    }

    /**
     * Refunds of a supported type without a non-reversed Fee refund: the Fee refund was not created or was reversed at
     * zero because the cap was used up.
     */
    public static List<LoanTransaction> refundsWithoutFeeRefund(final Loan loan) {
        final List<LoanTransaction> refundsWithFeeRefund = loan.getLoanTransactions().stream()
                .filter(transaction -> transaction.isNotReversed() && transaction.isFeeRefund()).map(DownPaymentFees::relatedRefund)
                .toList();
        return supportedRefunds(loan).stream().filter(refund -> !refundsWithFeeRefund.contains(refund)).toList();
    }

    private static List<LoanTransaction> supportedRefunds(final Loan loan) {
        final List<LoanTransactionType> supportedTypes = loan.getSupportedFeeRefundTransactionTypes();
        return loan.getLoanTransactions().stream()
                .filter(transaction -> transaction.isNotReversed() && supportedTypes.contains(transaction.getTypeOf()))
                .sorted(LoanTransactionComparator.INSTANCE).toList();
    }

    private static LoanTransaction relatedRefund(final LoanTransaction feeRefund) {
        return feeRefund.getLoanTransactionRelations(relation -> LoanTransactionRelationTypeEnum.RELATED.equals(relation.getRelationType()))
                .stream().map(LoanTransactionRelation::getToTransaction).findFirst().orElse(null);
    }

    /**
     * Fee refund of a refund, on the refund date: refundAmount / disbursed * (original amount of the fees charged by
     * then), capped by what has not been refunded yet.
     */
    static BigDecimal refundAmount(final Loan loan, final BigDecimal refundAmount, final LocalDate refundDate,
            final BigDecimal alreadyRefunded) {
        final BigDecimal disbursed = loan.getLoanTransactions().stream()
                .filter(transaction -> transaction.isNotReversed() && transaction.isDisbursement()
                        && !DateUtils.isAfter(transaction.getTransactionDate(), refundDate))
                .map(LoanTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        // Original charged amounts: waivers / adjustments / payments do not reduce the base
        final BigDecimal base = loan.getLoanCharges().stream()
                .filter(charge -> charge.isActive() && charge.isDueAtDownPayment() && !charge.isDownPaymentFeeAwaitingDisbursement()
                        && !DateUtils.isAfter(charge.getDueLocalDate(), refundDate))
                .map(LoanCharge::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!MathUtil.isGreaterThanZero(disbursed) || !MathUtil.isGreaterThanZero(base) || !MathUtil.isGreaterThanZero(refundAmount)) {
            return BigDecimal.ZERO;
        }
        final MonetaryCurrency currency = loan.getCurrency();
        final BigDecimal remaining = base.subtract(alreadyRefunded).max(BigDecimal.ZERO);
        final Money feeRefund = Money.of(currency, refundAmount.multiply(base).divide(disbursed, MoneyHelper.getMathContext()));
        return feeRefund.getAmount().min(Money.of(currency, remaining).getAmount());
    }
}
