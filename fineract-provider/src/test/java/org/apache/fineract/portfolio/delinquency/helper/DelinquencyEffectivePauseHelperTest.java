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
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
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

}
