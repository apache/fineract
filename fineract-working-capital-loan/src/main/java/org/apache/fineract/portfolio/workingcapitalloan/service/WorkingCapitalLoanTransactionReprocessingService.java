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

import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;

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
 * The delinquency and breach schedules are intentionally <em>not</em> rebuilt here: they are maintained incrementally
 * by the regular transaction flow. This is a known, accepted limitation for the current simple-scenario scope — a
 * backdated re-allocation that shifts principal between transactions can leave the per-period paid amounts slightly
 * stale, but only when a period boundary falls between the affected dates (the totals are unchanged).
 *
 * <p>
 * Allocation order only matters when payments compete for charge buckets, or when the loan overpays (the chronological
 * split then differs from booking-time order). A charge-free, non-overpaid loan is skipped as a no-op.
 */
public interface WorkingCapitalLoanTransactionReprocessingService {

    void reprocessTransactions(WorkingCapitalLoan loan);
}
