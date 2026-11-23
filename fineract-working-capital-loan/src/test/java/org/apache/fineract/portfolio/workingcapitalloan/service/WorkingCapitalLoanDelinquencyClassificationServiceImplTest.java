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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Classification of one overdue period (toDate 2026-02-06) against delinquency buckets with gaps, driven the way COB
 * and instant classification call it. The tag history is kept in memory.
 */
class WorkingCapitalLoanDelinquencyClassificationServiceImplTest {

    private static final LocalDate TO_DATE = LocalDate.of(2026, 2, 6);

    private final List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> tagHistory = new ArrayList<>();
    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository tagHistoryRepository = mock(
            WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository.class);
    private final WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class, RETURNS_DEEP_STUBS);
    private WorkingCapitalLoanDelinquencyClassificationServiceImpl service;

    // Identity on purpose: the saveAll stub stores each tag instance once
    @BeforeEach
    @SuppressWarnings({ "unchecked", "ReferenceEquality" })
    void setUp() {
        when(tagHistoryRepository.findByRangeScheduleAndLiftedOnDateOrderByAddedOnDateAsc(any(), isNull()))
                .thenAnswer(invocation -> tagHistory.stream().filter(tag -> tag.getLiftedOnDate() == null)
                        .sorted(Comparator.comparing(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory::getAddedOnDate)).toList());
        doAnswer(invocation -> {
            for (WorkingCapitalLoanDelinquencyRangeScheduleTagHistory tag : (Iterable<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory>) invocation
                    .getArgument(0)) {
                if (tagHistory.stream().noneMatch(stored -> stored == tag)) {
                    tagHistory.add(tag);
                }
            }
            return null;
        }).when(tagHistoryRepository).saveAll(any());
        when(tagHistoryRepository.findLatestTagChangeDate(any())).thenAnswer(invocation -> tagHistory.stream()
                .map(tag -> tag.getLiftedOnDate() != null ? tag.getLiftedOnDate() : tag.getAddedOnDate()).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null));

        final WorkingCapitalLoanDelinquencyRangeSchedule period = mock(WorkingCapitalLoanDelinquencyRangeSchedule.class);
        when(period.getToDate()).thenReturn(TO_DATE);
        when(period.getOutstandingAmount()).thenReturn(BigDecimal.valueOf(270));
        when(period.getReset()).thenReturn(false);
        final WorkingCapitalLoanDelinquencyRangeScheduleRepository periodRepository = mock(
                WorkingCapitalLoanDelinquencyRangeScheduleRepository.class);
        when(periodRepository.findByLoanIdOrderByPeriodNumberAsc(any())).thenReturn(List.of(period));
        when(loan.getId()).thenReturn(1L);

        service = new WorkingCapitalLoanDelinquencyClassificationServiceImpl(periodRepository, tagHistoryRepository,
                mock(WorkingCapitalLoanDelinquencyActionRepository.class), mock(GlobalConfigurationRepositoryWrapper.class),
                mock(BusinessEventNotifierService.class));
    }

    @Test
    void dailyCobPastTheBoundedLastRangeOfAGappedBucketLiftsEachRangeOnceOnItsExit() {
        useBucket(range(1L, "GD00", 1, 1), range(2L, "GD05", 5, 30));

        runDailyCob(TO_DATE.minusDays(1), TO_DATE.plusDays(35));

        assertEquals(List.of("GD00 2026-02-07..2026-02-08", "GD05 2026-02-11..2026-03-09"), history());
    }

    @Test
    void dailyCobDoesNotQueryTheLatestTagChange() {
        useBucket(range(1L, "GD00", 1, 1), range(2L, "GD05", 5, 30));

        runDailyCob(TO_DATE.minusDays(1), TO_DATE.plusDays(35));

        verify(tagHistoryRepository, never()).findLatestTagChangeDate(any());
    }

    @Test
    void firstCobOnAGapDayKeepsTheActiveTagOfALaterRangeAndLiftsItOnItsExit() {
        useBucket(range(1L, "GD00", 1, 1), range(2L, "GD03", 3, 4), range(3L, "GD07", 7, null));

        // Instant classification on day 3 (no COB yet) tags GD03; the first COB then lands on day 5, in the gap
        when(loan.getLastClosedBusinessDate()).thenReturn(null);
        service.classifyDelinquency(loan, TO_DATE.plusDays(3));
        service.classifyDelinquency(loan, TO_DATE.plusDays(5));

        assertEquals(List.of("GD00 2026-02-07..2026-02-08", "GD03 2026-02-09..2026-02-11"), history());
    }

    @Test
    void lateEvaluationsLeaveTheSameTagHistoryAsDailyCob() {
        useBucket(range(1L, "GD00", 1, 1), range(2L, "GD03", 3, 4), range(3L, "GD07", 7, 20), range(4L, "GD30", 30, null));

        runDailyCob(TO_DATE.minusDays(1), TO_DATE.plusDays(40));
        final List<String> dailyHistory = history();

        tagHistory.clear();
        when(loan.getLastClosedBusinessDate()).thenReturn(null);
        for (int day : new int[] { 3, 5, 9, 25, 41 }) {
            service.classifyDelinquency(loan, TO_DATE.plusDays(day));
        }

        assertEquals(List.of("GD00 2026-02-07..2026-02-08", "GD03 2026-02-09..2026-02-11", "GD07 2026-02-13..2026-02-27",
                "GD30 2026-03-08..open"), dailyHistory);
        assertEquals(dailyHistory, history());
    }

    private void runDailyCob(final LocalDate firstCobDate, final LocalDate lastCobDate) {
        // The step for COB date X classifies as of X + 1; lastClosedBusinessDate is set to X only after the steps
        for (LocalDate cobDate = firstCobDate; !cobDate.isAfter(lastCobDate); cobDate = cobDate.plusDays(1)) {
            when(loan.getLastClosedBusinessDate()).thenReturn(cobDate.minusDays(1));
            service.classifyDelinquency(loan, cobDate.plusDays(1));
        }
    }

    private void useBucket(final DelinquencyRange... ranges) {
        final DelinquencyBucket bucket = mock(DelinquencyBucket.class);
        when(bucket.getRanges()).thenReturn(List.of(ranges));
        when(loan.getLoanProductRelatedDetails().getDelinquencyBucket()).thenReturn(bucket);
    }

    private static DelinquencyRange range(final Long id, final String classification, final Integer minimumAgeDays,
            final Integer maximumAgeDays) {
        final DelinquencyRange range = DelinquencyRange.instance(classification, minimumAgeDays, maximumAgeDays);
        range.setId(id);
        return range;
    }

    private List<String> history() {
        return tagHistory.stream().sorted(Comparator.comparing(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory::getAddedOnDate))
                .map(tag -> tag.getDelinquencyRange().getClassification() + " " + tag.getAddedOnDate() + ".."
                        + (tag.getLiftedOnDate() == null ? "open" : tag.getLiftedOnDate()))
                .toList();
    }
}
