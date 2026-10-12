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
import java.util.List;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;

public final class WorkingCapitalLoanDelinquencyRangeScheduleEvaluationUtils {

    private WorkingCapitalLoanDelinquencyRangeScheduleEvaluationUtils() {}

    public static LocalDate calculateToDate(final LocalDate fromDate, final Integer frequency,
            final DelinquencyFrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAYS -> fromDate.plusDays(frequency - 1);
            case WEEKS -> fromDate.plusWeeks(frequency).minusDays(1);
            case MONTHS -> fromDate.plusMonths(frequency).minusDays(1);
            case YEARS -> fromDate.plusYears(frequency).minusDays(1);
        };
    }

    /**
     * End date a period gets when a reschedule re-dates it: the new frequency applied from the period start, extended
     * by the recorded pauses that overlap it. Shared by the reschedule validator and the schedule service so the check
     * cannot drift from the mutation it guards.
     */
    public static LocalDate calculateRescheduledToDate(final LocalDate fromDate, final Integer frequency,
            final DelinquencyFrequencyType frequencyType, final List<WorkingCapitalLoanDelinquencyAction> actions) {
        return WorkingCapitalLoanDelinquencyPauseUtils.extendToDateByRecordedPauses(fromDate,
                calculateToDate(fromDate, frequency, frequencyType), actions);
    }

}
