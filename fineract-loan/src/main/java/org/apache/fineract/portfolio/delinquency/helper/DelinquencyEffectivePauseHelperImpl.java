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
package org.apache.fineract.portfolio.delinquency.helper;

import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.springframework.stereotype.Service;

@Service
public class DelinquencyEffectivePauseHelperImpl implements DelinquencyEffectivePauseHelper {

    @Override
    public List<LoanDelinquencyActionData> calculateEffectiveDelinquencyList(List<LoanDelinquencyAction> savedDelinquencyActions) {
        // partition them based on type
        Map<DelinquencyAction, List<LoanDelinquencyAction>> partitioned = savedDelinquencyActions.stream()
                .collect(Collectors.groupingBy(LoanDelinquencyAction::getAction));
        List<LoanDelinquencyActionData> effective = new ArrayList<>();
        List<LoanDelinquencyAction> pauses = partitioned.get(DelinquencyAction.PAUSE);
        if (pauses != null && pauses.size() > 0) {
            for (LoanDelinquencyAction loanDelinquencyAction : pauses) {
                Optional<LoanDelinquencyAction> resume = findMatchingResume(loanDelinquencyAction, partitioned.get(RESUME));
                LoanDelinquencyActionData loanDelinquencyActionData = new LoanDelinquencyActionData(loanDelinquencyAction);
                resume.ifPresent(r -> loanDelinquencyActionData.setEndDate(r.getStartDate()));
                effective.add(loanDelinquencyActionData);
            }
        }
        return effective;
    }

    @Override
    public Long getPausedDaysBeforeDate(List<LoanDelinquencyActionData> effectiveDelinquencyList, LocalDate date) {
        Long pausedDaysClosedPausePeriods = effectiveDelinquencyList.stream() //
                .filter(pausePeriod -> pausePeriod.getStartDate().isBefore(date) && pausePeriod.getEndDate().isBefore(date))
                .map(pausePeriod -> DateUtils.getDifferenceInDays(pausePeriod.getStartDate(), pausePeriod.getEndDate())) //
                .reduce(0L, Long::sum);
        Long pausedDaysRunningPausePeriods = effectiveDelinquencyList.stream() //
                .filter(pausePeriod -> pausePeriod.getStartDate().isBefore(date) && !pausePeriod.getEndDate().isBefore(date))
                .map(pausePeriod -> DateUtils.getDifferenceInDays(pausePeriod.getStartDate(), date)) //
                .reduce(0L, Long::sum);
        return Long.sum(pausedDaysClosedPausePeriods, pausedDaysRunningPausePeriods);
    }

    @Override
    public Long getPausedDaysWithinRange(List<LoanDelinquencyActionData> effectiveDelinquencyList, LocalDate startInclusive,
            LocalDate endExclusive) {
        if (startInclusive == null || endExclusive == null || !startInclusive.isBefore(endExclusive)) {
            return 0L;
        }
        return effectiveDelinquencyList.stream().map(pausePeriod -> {
            LocalDate pauseStart = pausePeriod.getStartDate();
            LocalDate pauseEnd = Optional.ofNullable(pausePeriod.getEndDate()).orElse(endExclusive);
            if (pauseStart == null || !pauseStart.isBefore(endExclusive)) {
                return 0L;
            }
            if (!pauseEnd.isAfter(startInclusive)) {
                return 0L;
            }
            LocalDate overlapStart = pauseStart.isAfter(startInclusive) ? pauseStart : startInclusive;
            LocalDate overlapEnd = pauseEnd.isBefore(endExclusive) ? pauseEnd : endExclusive;
            if (!overlapStart.isBefore(overlapEnd)) {
                return 0L;
            }
            return DateUtils.getDifferenceInDays(overlapStart, overlapEnd);
        }).reduce(0L, Long::sum);
    }

    private Optional<LoanDelinquencyAction> findMatchingResume(LoanDelinquencyAction pause, List<LoanDelinquencyAction> resumes) {
        if (resumes != null && resumes.size() > 0) {
            for (LoanDelinquencyAction resume : resumes) {
                if (!pause.getStartDate().isAfter(resume.getStartDate()) && !resume.getStartDate().isAfter(pause.getEndDate())) {
                    return Optional.of(resume);
                }
            }
        }
        return Optional.empty();
    }
}
