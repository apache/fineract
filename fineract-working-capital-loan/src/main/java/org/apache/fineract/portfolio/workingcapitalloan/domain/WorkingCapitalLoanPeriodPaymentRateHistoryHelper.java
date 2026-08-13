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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a loan's period payment rate out of its rate-change history.
 *
 * <p>
 * Changes are effective-dated and may be entered out of order — a backdated change carries a higher id than the changes
 * it precedes — so every question about "which rate applies" is answered by <em>effective date</em> order, never by
 * insertion order.
 */
public final class WorkingCapitalLoanPeriodPaymentRateHistoryHelper {

    /** Effective-date order, with the id breaking ties between changes sharing a date. */
    private static final Comparator<WorkingCapitalLoanPeriodPaymentRateChange> CHRONOLOGICAL = Comparator
            .comparing(WorkingCapitalLoanPeriodPaymentRateChange::getEffectiveDate)
            .thenComparing(WorkingCapitalLoanPeriodPaymentRateChange::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private WorkingCapitalLoanPeriodPaymentRateHistoryHelper() {}

    /**
     * The rate in force on {@code asOf}: the latest active change effective on or before that date, falling back to
     * {@code originalRate} when none has taken effect yet.
     *
     * <p>
     * Reversed changes are filtered here rather than assumed away, because a caller may reverse changes in memory
     * before asking — the same-date overwrite does exactly that, so that the predecessor it resolves is the rate in
     * force before the change being corrected rather than the correction's own mistake.
     *
     * @param changes
     *            the loan's rate changes, in any order, including any just reversed in memory
     * @param originalRate
     *            the rate the loan was created with, held on its product-related details. Deliberately taken from the
     *            caller rather than read back off the earliest change's {@code previousRate}: that field is only as
     *            good as the last {@link #restatePreviousRates}, so a history written before restating existed — or one
     *            whose earliest link has since been reversed — would answer with a superseded rate.
     */
    public static BigDecimal rateInEffectAt(final List<WorkingCapitalLoanPeriodPaymentRateChange> changes, final BigDecimal originalRate,
            final LocalDate asOf) {
        return changes.stream().filter(change -> !change.isReversed()).filter(change -> !change.getEffectiveDate().isAfter(asOf))
                .max(CHRONOLOGICAL).map(WorkingCapitalLoanPeriodPaymentRateChange::getNewRate).orElse(originalRate);
    }

    /**
     * Rewrites every non-reversed change's {@code previousRate} so the history reads as one chain in effective-date
     * order: the earliest is preceded by {@code originalRate}, and each later one by its predecessor's new rate.
     *
     * <p>
     * Needed because a change may be booked before a backdated one slots in behind it, which invalidates the
     * predecessor it recorded at the time. Without this the same timeline would report different predecessors depending
     * on the order the changes happened to be entered.
     *
     * <p>
     * Reversed changes keep the value they were written with — they record an action that was undone, not a link in the
     * chain that is in force.
     *
     * @param changes
     *            the loan's rate changes, the new one included, and any just reversed in memory — which is why this
     *            filters rather than trusting the caller to have excluded them
     * @param originalRate
     *            the rate the loan was created with, held on its product-related details. Restating from the loan's own
     *            field rather than from the chain it is about to rewrite means this is self-correcting: a history that
     *            predates restating is brought back onto the true original the first time a change is booked
     * @return the changes whose {@code previousRate} actually moved, for the caller to persist
     */
    public static List<WorkingCapitalLoanPeriodPaymentRateChange> restatePreviousRates(
            final List<WorkingCapitalLoanPeriodPaymentRateChange> changes, final BigDecimal originalRate) {
        final List<WorkingCapitalLoanPeriodPaymentRateChange> restated = new ArrayList<>();
        BigDecimal predecessor = originalRate;
        for (final WorkingCapitalLoanPeriodPaymentRateChange change : changes.stream().filter(c -> !c.isReversed()).sorted(CHRONOLOGICAL)
                .toList()) {
            final BigDecimal current = change.getPreviousRate();
            final boolean alreadyCorrect = current == null ? predecessor == null
                    : predecessor != null && current.compareTo(predecessor) == 0;
            if (!alreadyCorrect) {
                change.setPreviousRate(predecessor);
                restated.add(change);
            }
            predecessor = change.getNewRate();
        }
        return restated;
    }
}
