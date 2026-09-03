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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanOutstandingMath.bucketOutstanding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAsOfBalanceData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rebuilds the charged side of the balance from the events that wrote it, filtered to those dated on or before the
 * requested date, and pairs it with the stored paid and written-off side.
 *
 * <p>
 * The charged side is exactly three fields, and each has a dated event behind it:
 * <ul>
 * <li>{@code principal} - the single disbursement, plus any discount fee, less any discount fee adjustment. All three
 * are transaction rows carrying a transaction date.</li>
 * <li>{@code fee} / {@code penalty} - the sum of the loan's charges, each counting from the earlier of the date it was
 * added and the date it falls due.</li>
 * </ul>
 *
 * <p>
 * Everything else is left at its stored value on purpose. Payments are not date-filtered, so the quote remains the
 * amount that closes the loan rather than the amount owed on that date; this matches how the core loan module's payoff
 * quote behaves and is what keeps a prepayment backdated behind an existing payment from landing overpaid. The same
 * applies to {@code principalAdjustment}, which is produced only by an over-refunded credit balance refund during
 * reprocessing: it is path-dependent on later transactions and carries no date of its own, so there is nothing to scope
 * it by.
 *
 * <p>
 * Nothing here loads or mutates an entity - both queries are scalar sums - so no part of this can be flushed back. That
 * matters more than the annotation below suggests: this module runs on EclipseLink, where {@code readOnly = true}
 * defers the database transaction but does not stop a managed entity being written at commit.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanAsOfBalanceReadServiceImpl implements WorkingCapitalLoanAsOfBalanceReadService {

    /**
     * A working capital loan is disbursed exactly once, and the discount fee that rides on that disbursement is a
     * transaction in its own right, so summing these two reproduces the principal that {@code applyDisbursement} set.
     */
    private static final List<LoanTransactionType> PRINCIPAL_INCREASING_TYPES = List.of(LoanTransactionType.DISBURSEMENT,
            LoanTransactionType.DISCOUNT_FEE);
    private static final List<LoanTransactionType> PRINCIPAL_DECREASING_TYPES = List.of(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT);

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;

    @Override
    public Optional<WorkingCapitalLoanAsOfBalanceData> retrieveAsOf(final Long loanId, final WorkingCapitalLoanBalance balance,
            final LocalDate asOfDate) {
        if (balance == null) {
            return Optional.empty();
        }

        final BigDecimal principalCharged = MathUtil.subtract(
                transactionRepository.sumAmountsOfTypesUpTo(loanId, PRINCIPAL_INCREASING_TYPES, asOfDate),
                transactionRepository.sumAmountsOfTypesUpTo(loanId, PRINCIPAL_DECREASING_TYPES, asOfDate));
        final BigDecimal feeCharged = chargeRepository.sumChargedKnownBy(loanId, false, asOfDate);
        final BigDecimal penaltyCharged = chargeRepository.sumChargedKnownBy(loanId, true, asOfDate);

        final BigDecimal principalOutstanding = bucketOutstanding(MathUtil.add(principalCharged, balance.getPrincipalAdjustment()),
                balance.getPrincipalPaid(), balance.getPrincipalWrittenOff());
        final BigDecimal feeOutstanding = bucketOutstanding(feeCharged, balance.getFeePaid(), balance.getFeeWrittenOff());
        final BigDecimal penaltyOutstanding = bucketOutstanding(penaltyCharged, balance.getPenaltyPaid(), balance.getPenaltyWrittenOff());

        return Optional.of(new WorkingCapitalLoanAsOfBalanceData(asOfDate, principalOutstanding, feeOutstanding, penaltyOutstanding,
                MathUtil.add(principalOutstanding, feeOutstanding, penaltyOutstanding), balance.getOverpaymentAmount(),
                balance.getWrittenOffOutstanding()));
    }
}
