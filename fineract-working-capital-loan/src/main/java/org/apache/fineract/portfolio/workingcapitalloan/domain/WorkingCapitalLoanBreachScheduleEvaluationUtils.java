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
import java.util.Optional;

public final class WorkingCapitalLoanBreachScheduleEvaluationUtils {

    private WorkingCapitalLoanBreachScheduleEvaluationUtils() {}

    public static Optional<WorkingCapitalLoanBreachSchedule> resolveEvaluationPeriod(final List<WorkingCapitalLoanBreachSchedule> periods,
            final LocalDate actionDate) {
        if (periods == null || periods.isEmpty() || actionDate == null) {
            return Optional.empty();
        }
        final Optional<WorkingCapitalLoanBreachSchedule> containingPeriod = periods.stream().filter(p -> p.getFromDate() != null
                && p.getToDate() != null && !p.getFromDate().isAfter(actionDate) && !p.getToDate().isBefore(actionDate)).findFirst();
        if (containingPeriod.isPresent()) {
            return containingPeriod;
        }
        return periods.stream().filter(p -> p.getToDate() != null && !p.getToDate().isAfter(actionDate))
                .max(Comparator.comparingInt(period -> period.getPeriodNumber() != null ? period.getPeriodNumber() : Integer.MIN_VALUE));
    }

}
