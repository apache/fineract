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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * Down payment fees belong to the down payment period of their tranche ({@link LoanCharge#getDisbursementDetail()}).
 * Every disbursement has its own down payment period on its date, so when several tranches share a date the n-th
 * tranche of that date owns the n-th period. A fee without a tranche (single disbursement loan) belongs to the only
 * period on its date.
 */
public final class DownPaymentFees {

    private static final Comparator<LoanCharge> BY_ID = Comparator.comparing(LoanCharge::getId, Comparator.nullsLast(Long::compareTo));

    private DownPaymentFees() {}

    public static List<LoanCharge> dueOn(final Collection<LoanCharge> charges, final LocalDate date) {
        if (charges == null || date == null) {
            return List.of();
        }
        return charges.stream()
                .filter(charge -> charge.isActive() && charge.isDueAtDownPayment() && DateUtils.isEqual(date, charge.getDueLocalDate()))
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
}
