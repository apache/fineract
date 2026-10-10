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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;

/**
 * The near breach relevant state of a breach schedule taken before it is rebuilt, against which the rebuilt periods are
 * judged stale and the near breach change is detected.
 */
record WorkingCapitalLoanNearBreachBaseline(Map<Integer, Snapshot> periods, Optional<Snapshot> openPeriod) {

    static WorkingCapitalLoanNearBreachBaseline of(final List<WorkingCapitalLoanBreachSchedule> periods, final LocalDate businessDate) {
        final Map<Integer, Snapshot> snapshots = periods.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanBreachSchedule::getPeriodNumber, Snapshot::of));
        return new WorkingCapitalLoanNearBreachBaseline(snapshots, openPeriod(periods, businessDate).map(Snapshot::of));
    }

    private static Optional<WorkingCapitalLoanBreachSchedule> openPeriod(final List<WorkingCapitalLoanBreachSchedule> periods,
            final LocalDate businessDate) {
        return periods.stream().filter(
                period -> !DateUtils.isAfter(period.getFromDate(), businessDate) && !DateUtils.isBefore(period.getToDate(), businessDate))
                .findFirst();
    }

    /** A period deleted and regenerated with the same bounds takes over the value the deleted one carried. */
    void carryOverToRegenerated(final WorkingCapitalLoanBreachSchedule period) {
        final Snapshot before = periods.get(period.getPeriodNumber());
        if (before != null && !before.isSamePeriod(period) && before.hasSameBounds(period) && period.getNearBreach() == null) {
            period.setNearBreach(before.nearBreach());
        }
    }

    /**
     * The open period is always stale. A closed period that was never evaluated stays {@code null} unless it was open
     * in the baseline and is being closed out now; one carrying a value is stale when it moved since the baseline, or
     * always when {@code rederiveClosedValues}, so a value that no longer matches the replayed paid amount does not
     * survive.
     */
    boolean isStale(final WorkingCapitalLoanBreachSchedule period, final LocalDate businessDate, final boolean rederiveClosedValues) {
        if (!DateUtils.isBefore(period.getToDate(), businessDate)) {
            return true;
        }
        final Snapshot before = periods.get(period.getPeriodNumber());
        if (period.getNearBreach() == null) {
            return before != null && before.isSamePeriod(period) && before.isOpenOn(businessDate);
        }
        return rederiveClosedValues || before == null || before.hasMoved(period);
    }

    /**
     * Whether a period holds a different value than in the baseline. When the period open in the baseline was deleted,
     * as by the undo of a restart reset, its value is compared with the period open now instead.
     */
    boolean hasChanged(final List<WorkingCapitalLoanBreachSchedule> current, final LocalDate businessDate) {
        final boolean periodChanged = current.stream().anyMatch(period -> {
            final Snapshot before = periods.get(period.getPeriodNumber());
            return !Objects.equals(before == null ? null : before.nearBreach(), period.getNearBreach());
        });
        return periodChanged
                || openPeriod.filter(open -> current.stream().noneMatch(open::isSamePeriod))
                        .filter(deleted -> !Objects.equals(deleted.nearBreach(),
                                openPeriod(current, businessDate).map(WorkingCapitalLoanBreachSchedule::getNearBreach).orElse(null)))
                        .isPresent();
    }

    record Snapshot(Long id, LocalDate fromDate, LocalDate toDate, BigDecimal minPaymentAmount, BigDecimal paidAmount, Boolean nearBreach) {

        static Snapshot of(final WorkingCapitalLoanBreachSchedule period) {
            return new Snapshot(period.getId(), period.getFromDate(), period.getToDate(), period.getMinPaymentAmount(),
                    period.getPaidAmount(), period.getNearBreach());
        }

        boolean isSamePeriod(final WorkingCapitalLoanBreachSchedule period) {
            return id != null && id.equals(period.getId());
        }

        boolean hasSameBounds(final WorkingCapitalLoanBreachSchedule period) {
            return DateUtils.isEqual(fromDate, period.getFromDate()) && DateUtils.isEqual(toDate, period.getToDate());
        }

        boolean hasMoved(final WorkingCapitalLoanBreachSchedule period) {
            return !isSamePeriod(period) || !hasSameBounds(period) || !MathUtil.isEqualTo(minPaymentAmount, period.getMinPaymentAmount())
                    || !MathUtil.isEqualTo(paidAmount, period.getPaidAmount());
        }

        boolean isOpenOn(final LocalDate businessDate) {
            return !DateUtils.isBefore(toDate, businessDate);
        }
    }
}
