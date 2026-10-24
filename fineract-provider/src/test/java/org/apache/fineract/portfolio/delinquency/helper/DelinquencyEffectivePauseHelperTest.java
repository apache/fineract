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

import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelinquencyEffectivePauseHelperTest {

    @InjectMocks
    private DelinquencyEffectivePauseHelperImpl underTest;

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseFirstDay() {
        // given
        List<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        List<LoanDelinquencyActionData> effectivePeriods = underTest.calculateEffectiveDelinquencyList(delinquencyActions);

        // then
        assertEquals(effectivePeriods.get(0).getStartDate(), LocalDate.of(2023, 1, 10));
        assertEquals(effectivePeriods.get(0).getEndDate(), LocalDate.of(2023, 1, 11));
        assertEquals(effectivePeriods.get(1).getStartDate(), LocalDate.of(2023, 1, 12));
        assertEquals(effectivePeriods.get(1).getEndDate(), LocalDate.of(2023, 1, 13));
        assertEquals(effectivePeriods.get(2).getStartDate(), LocalDate.of(2023, 1, 15));
        assertEquals(effectivePeriods.get(2).getEndDate(), LocalDate.of(2023, 1, 20));
    }

    @Test
    public void testResumeIsAppliedToOneOfThePauseNotActive() {
        // given
        List<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 20)),
                new LoanDelinquencyAction(null, RESUME, LocalDate.of(2023, JANUARY, 11), null),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 13), LocalDate.of(2023, JANUARY, 14)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        List<LoanDelinquencyActionData> effectivePeriods = underTest.calculateEffectiveDelinquencyList(delinquencyActions);

        // then
        assertEquals(effectivePeriods.get(0).getStartDate(), LocalDate.of(2023, 1, 10));
        assertEquals(effectivePeriods.get(0).getEndDate(), LocalDate.of(2023, 1, 11));
        assertEquals(effectivePeriods.get(1).getStartDate(), LocalDate.of(2023, 1, 13));
        assertEquals(effectivePeriods.get(1).getEndDate(), LocalDate.of(2023, 1, 14));
        assertEquals(effectivePeriods.get(2).getStartDate(), LocalDate.of(2023, 1, 15));
        assertEquals(effectivePeriods.get(2).getEndDate(), LocalDate.of(2023, 1, 20));
    }

    @Test
    public void testResumeDoesNotEndPauseCreatedAfterIt() {
        List<LoanDelinquencyAction> delinquencyActions = List.of(
                savedAction(1L, PAUSE, LocalDate.of(2023, OCTOBER, 16), LocalDate.of(2023, OCTOBER, 30)),
                savedAction(2L, RESUME, LocalDate.of(2023, OCTOBER, 20), null),
                savedAction(3L, PAUSE, LocalDate.of(2023, OCTOBER, 20), LocalDate.of(2023, OCTOBER, 25)));

        List<LoanDelinquencyActionData> effectivePeriods = underTest.calculateEffectiveDelinquencyList(delinquencyActions);

        assertEquals(2, effectivePeriods.size());
        assertEquals(LocalDate.of(2023, OCTOBER, 16), effectivePeriods.get(0).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 20), effectivePeriods.get(0).getEndDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 20), effectivePeriods.get(1).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 25), effectivePeriods.get(1).getEndDate());
        assertEquals(9L,
                underTest.getPausedDaysWithinRange(effectivePeriods, LocalDate.of(2023, OCTOBER, 16), LocalDate.of(2023, OCTOBER, 31)));
    }

    @Test
    public void testResumeEndsEveryPauseCreatedBeforeItThatContainsItsDate() {
        List<LoanDelinquencyAction> delinquencyActions = List.of(
                savedAction(1L, PAUSE, LocalDate.of(2023, OCTOBER, 25), LocalDate.of(2023, NOVEMBER, 5)),
                savedAction(2L, PAUSE, LocalDate.of(2023, OCTOBER, 30), LocalDate.of(2023, NOVEMBER, 15)),
                savedAction(3L, RESUME, LocalDate.of(2023, OCTOBER, 31), null));

        List<LoanDelinquencyActionData> effectivePeriods = underTest.calculateEffectiveDelinquencyList(delinquencyActions);

        assertEquals(2, effectivePeriods.size());
        assertEquals(LocalDate.of(2023, OCTOBER, 25), effectivePeriods.get(0).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 31), effectivePeriods.get(0).getEndDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 30), effectivePeriods.get(1).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 31), effectivePeriods.get(1).getEndDate());
    }

    @Test
    public void testResumeEndsPauseCreatedBeforeItStartingOnItsDate() {
        List<LoanDelinquencyAction> delinquencyActions = List.of(
                savedAction(1L, PAUSE, LocalDate.of(2023, OCTOBER, 25), LocalDate.of(2023, OCTOBER, 30)),
                savedAction(2L, PAUSE, LocalDate.of(2023, OCTOBER, 30), LocalDate.of(2023, NOVEMBER, 15)),
                savedAction(3L, RESUME, LocalDate.of(2023, OCTOBER, 30), null));

        List<LoanDelinquencyActionData> effectivePeriods = underTest.calculateEffectiveDelinquencyList(delinquencyActions);

        assertEquals(2, effectivePeriods.size());
        assertEquals(LocalDate.of(2023, OCTOBER, 25), effectivePeriods.get(0).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 30), effectivePeriods.get(0).getEndDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 30), effectivePeriods.get(1).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 30), effectivePeriods.get(1).getEndDate());
    }

    @Test
    public void testResumeEndsContainingPauseWhenCreationOrderIsUnknown() {
        List<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, RESUME, LocalDate.of(2023, OCTOBER, 20), null),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, OCTOBER, 16), LocalDate.of(2023, OCTOBER, 30)));

        List<LoanDelinquencyActionData> effectivePeriods = underTest.calculateEffectiveDelinquencyList(delinquencyActions);

        assertEquals(1, effectivePeriods.size());
        assertEquals(LocalDate.of(2023, OCTOBER, 16), effectivePeriods.get(0).getStartDate());
        assertEquals(LocalDate.of(2023, OCTOBER, 20), effectivePeriods.get(0).getEndDate());
    }

    private LoanDelinquencyAction savedAction(Long id, DelinquencyAction action, LocalDate startDate, LocalDate endDate) {
        LoanDelinquencyAction loanDelinquencyAction = new LoanDelinquencyAction(null, action, startDate, endDate);
        loanDelinquencyAction.setId(id);
        return loanDelinquencyAction;
    }
}
