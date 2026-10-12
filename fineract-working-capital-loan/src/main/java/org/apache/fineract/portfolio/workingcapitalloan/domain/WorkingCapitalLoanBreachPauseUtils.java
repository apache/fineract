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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WorkingCapitalLoanBreachPauseUtils {

    private WorkingCapitalLoanBreachPauseUtils() {}

    /**
     * A resumed pause effectively ends on the (inclusive) resume date, so a later pause may start the next day.
     */
    public static LocalDate resolveEffectivePauseEnd(final WorkingCapitalLoanBreachAction pause,
            final List<WorkingCapitalLoanBreachAction> actions) {
        final LocalDate pauseStart = pause.getStartDate();
        final LocalDate pauseEnd = pause.getEndDate();
        if (pauseStart == null || pauseEnd == null) {
            return pauseEnd;
        }
        return actions.stream().filter(Objects::nonNull)
                .filter(action -> WorkingCapitalLoanBreachActionType.RESUME.equals(action.getAction()) && action.getStartDate() != null
                        && !action.getStartDate().isBefore(pauseStart) && !action.getStartDate().isAfter(pauseEnd))
                .map(WorkingCapitalLoanBreachAction::getStartDate).min(Comparator.naturalOrder()).orElse(pauseEnd);
    }

    /**
     * The recorded pauses as plain date ranges, with resumes already applied to their end dates.
     */
    public static List<WorkingCapitalLoanPausePeriod> toEffectivePauses(final List<WorkingCapitalLoanBreachAction> actions) {
        if (actions == null) {
            return List.of();
        }
        return actions.stream().filter(Objects::nonNull)
                .filter(action -> WorkingCapitalLoanBreachActionType.PAUSE.equals(action.getAction()))
                .map(pause -> new WorkingCapitalLoanPausePeriod(pause.getStartDate(), resolveEffectivePauseEnd(pause, actions))).toList();
    }

    /**
     * Extends an inclusive period end date by the recorded pauses. See
     * {@link WorkingCapitalLoanPausePeriodUtils#applyPauses} for the rule.
     */
    public static LocalDate extendToDateByRecordedPauses(final LocalDate fromDate, final LocalDate baseToDate,
            final List<WorkingCapitalLoanBreachAction> actions) {
        return WorkingCapitalLoanPausePeriodUtils.extendToDate(fromDate, baseToDate, toEffectivePauses(actions));
    }

}
