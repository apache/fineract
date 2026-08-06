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

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;

/**
 * Reprocesses transaction allocations for a Working Capital loan after a backdated transaction changes the
 * chronological order.
 *
 * <p>
 * Transactions themselves are never reversed or replayed — only the allocation split (principal/fee/penalty portions)
 * of affected transactions is recalculated. From those recomputed portions this service also rebuilds the loan balance
 * (paid amounts) and the amortization schedule.
 *
 * <p>
 * Neither the delinquency nor the breach schedule is rebuilt by this service, and the two differ in why. Delinquency
 * does need rebuilding, but on a trigger of its own rather than as a consequence of reprocessing: a payment there
 * cascades, settling older open periods oldest-first before any remainder lands in the period holding its own date, so
 * inserting or removing a payment anywhere redistributes amounts across periods. Callers rebuild it whenever a
 * transaction is backdated — including on paths where no allocation replay happens at all. Breach needs nothing: each
 * payment is attributed solely to the period containing its own (immutable) transaction date, by its full transaction
 * amount rather than its allocated split, and never cascades across a boundary. Recomputing allocations therefore
 * cannot move a single breach figure, and the incremental apply/undo on the regular flow keeps it correct on its own.
 *
 * <p>
 * Allocation order only matters when payments compete for charge buckets, or when the loan overpays (the chronological
 * split then differs from booking-time order). Deciding whether a replay is worth running is the caller's job, not this
 * service's: the entry points here always do the work they promise. {@link #reprocessChargeFreeSuffix} is the bounded
 * alternative for the charge-free case, recomputing only the affected tail instead of the whole history.
 */
public interface WorkingCapitalLoanTransactionReprocessingService {

    void reprocessTransactions(WorkingCapitalLoan loan);

    /**
     * Reprocesses using the provided pre-loaded transaction list (avoids a redundant DB query when the caller has
     * already fetched them).
     */
    void reprocessTransactions(WorkingCapitalLoan loan, List<WorkingCapitalLoanTransaction> allTransactions);

    /**
     * Charge-free suffix reprocessing: recomputes only the allocations, balance and schedule of the transactions dated
     * on or after {@code boundaryDate}, leaving the (potentially large) prefix of older transactions unread. The
     * pre-suffix balance is recovered by rewinding the suffix off the current balance rather than replaying from zero,
     * which is exact for the charge-free case because a transaction's whole effect is its stored principal portion (the
     * overpaid remainder is {@code amount - principalPortion}).
     *
     * <p>
     * {@code reversedTransaction} is the transaction just reversed by an undo (or {@code null} for a backdated insert):
     * its stored allocation is still counted in the live balance at this point, so it is rewound but not replayed.
     *
     * <p>
     * Falls back to the full {@link #reprocessTransactions(WorkingCapitalLoan)} reset+replay when the loan is not
     * charge-free or the suffix contains a credit balance refund (whose overpayment interaction is re-derived against
     * the running overpayment during a full replay, and so is not reconstructable from the stored allocation alone).
     */
    void reprocessChargeFreeSuffix(WorkingCapitalLoan loan, LocalDate boundaryDate, WorkingCapitalLoanTransaction reversedTransaction);
}
