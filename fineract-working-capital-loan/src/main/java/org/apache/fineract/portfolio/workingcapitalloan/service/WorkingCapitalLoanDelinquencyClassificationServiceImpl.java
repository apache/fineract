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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WorkingCapitalLoanDelinquencyClassificationServiceImpl implements WorkingCapitalLoanDelinquencyClassificationService {

    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository delinquencyRangeScheduleTagHistoryRepository;

    /**
     * Classifies the delinquency of a loan based on the delinquency bucket and the business date.
     *
     * @param loan
     *            the loan for which the delinquency range tag should be applied
     * @param businessDate
     *            the date on which the tagging operation is performed
     * @param delinquencyBucket
     *            the delinquency bucket to search within
     */
    @Override
    public void classifyDelinquency(WorkingCapitalLoan loan, LocalDate businessDate, DelinquencyBucket delinquencyBucket) {

        List<WorkingCapitalLoanDelinquencyRangeSchedule> delinquencyRangeScheduleList = delinquencyRangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(loan.getId());

        for (WorkingCapitalLoanDelinquencyRangeSchedule range : delinquencyRangeScheduleList) {
            if (range.getToDate().isBefore(businessDate)) {
                long rangeDelinquentDays = range.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0
                        ? DateUtils.getDifferenceInDays(range.getToDate(), businessDate)
                        : 0L;
                boolean isDelinquent = rangeDelinquentDays > 0;

                if (isDelinquent) {
                    range.setDelinquentAmount(range.getOutstandingAmount());
                    range.setDelinquentDays(rangeDelinquentDays);
                    Optional<DelinquencyRange> delinquencyRangeByDays = findDelinquencyRangeByDays(delinquencyBucket,
                            (int) rangeDelinquentDays);
                    applyDelinquencyTagForRange(loan, range, delinquencyRangeByDays.orElse(null), businessDate);
                } else {
                    range.setDelinquentAmount(BigDecimal.ZERO);
                    range.setDelinquentDays(0L);
                    applyDelinquencyTagForRange(loan, range, null, businessDate);
                }
            }
        }
    }

    /**
     * Finds the delinquency range for a given delinquency bucket and number of days.
     *
     * @param delinquencyBucket
     *            the delinquency bucket to search within
     * @param delinquentDays
     *            the number of days the loan is delinquent
     * @return an Optional containing the matching delinquency range, or empty if not found
     */
    public Optional<DelinquencyRange> findDelinquencyRangeByDays(final DelinquencyBucket delinquencyBucket, final Integer delinquentDays) {
        return delinquencyBucket.getRanges().stream().filter(dr -> dr.getMinimumAgeDays() <= delinquentDays)
                .filter(dr -> dr.getMaximumAgeDays() == null || dr.getMaximumAgeDays() >= delinquentDays).findAny();
    }

    /**
     * Applies a delinquency tag for a specific range to the given loan. This method either adds a new tag for the
     * current delinquency range or lifts all existing tags if the current range is null.
     *
     * @param loan
     *            the loan for which the delinquency range tag should be applied
     * @param range
     *            the delinquency range schedule associated with the tagging operation
     * @param currentRange
     *            the current delinquency range to be applied; can be null to lift all previous tags
     * @param businessDate
     *            the date on which the tagging operation is performed
     */
    public void applyDelinquencyTagForRange(final WorkingCapitalLoan loan, final WorkingCapitalLoanDelinquencyRangeSchedule range,
            final DelinquencyRange currentRange, final LocalDate businessDate) {
        List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> updatedList = new ArrayList<>();
        List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> rangeScheduleTagHistoryList = delinquencyRangeScheduleTagHistoryRepository
                .findByRangeScheduleAndLiftedOnDateOrderByAddedOnDateAsc(range, null);

        WorkingCapitalLoanDelinquencyRangeScheduleTagHistory last = rangeScheduleTagHistoryList.isEmpty() ? null
                : rangeScheduleTagHistoryList.getLast();

        // do nothing if currentRange is in rangeScheduleTagHistoryList or last and currentRange are null
        if ((last == null && currentRange == null) || (last != null && currentRange != null && rangeScheduleTagHistoryList.stream()
                .anyMatch(tag -> Objects.equals(tag.getDelinquencyRange().getId(), currentRange.getId())))) {
            return;
        }

        if (currentRange == null) {
            // lift all previous tags
            rangeScheduleTagHistoryList.forEach(tag -> tag.setLiftedOnDate(businessDate));
            updatedList.addAll(rangeScheduleTagHistoryList);
        } else {
            // add current range
            WorkingCapitalLoanDelinquencyRangeScheduleTagHistory newTag = new WorkingCapitalLoanDelinquencyRangeScheduleTagHistory();
            newTag.setLoan(loan);
            newTag.setDelinquencyRange(currentRange);
            newTag.setRangeSchedule(range);
            newTag.setAddedOnDate(businessDate);
            newTag.setLiftedOnDate(null);
            updatedList.add(newTag);
        }

        delinquencyRangeScheduleTagHistoryRepository.saveAll(updatedList);
    }

}
