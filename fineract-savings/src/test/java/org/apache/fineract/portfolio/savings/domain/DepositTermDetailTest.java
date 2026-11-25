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
package org.apache.fineract.portfolio.savings.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.junit.jupiter.api.Test;

class DepositTermDetailTest {

    @Test
    void monthlyMultipleAfterMinimumInYears() {
        DepositTermDetail term = DepositTermDetail.createFrom(1, 5, SavingsPeriodFrequencyType.YEARS, SavingsPeriodFrequencyType.YEARS, 1,
                SavingsPeriodFrequencyType.MONTHS);

        assertTrue(term.isValidInMultiplesOfPeriod(18, SavingsPeriodFrequencyType.MONTHS));
        assertTrue(term.isValidInMultiplesOfPeriod(2, SavingsPeriodFrequencyType.YEARS));
    }

    @Test
    void yearlyMultipleAfterMinimumInMonths() {
        DepositTermDetail term = DepositTermDetail.createFrom(12, 60, SavingsPeriodFrequencyType.MONTHS, SavingsPeriodFrequencyType.MONTHS,
                1, SavingsPeriodFrequencyType.YEARS);

        assertTrue(term.isValidInMultiplesOfPeriod(2, SavingsPeriodFrequencyType.YEARS));
        assertTrue(term.isValidInMultiplesOfPeriod(36, SavingsPeriodFrequencyType.MONTHS));
        assertFalse(term.isValidInMultiplesOfPeriod(18, SavingsPeriodFrequencyType.MONTHS));
    }

    @Test
    void minimumInYearsEqualToMaximumInMonths() {
        assertFalse(DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.YEARS, SavingsPeriodFrequencyType.MONTHS, null, null)
                .isMinDepositTermGreaterThanMaxDepositTerm());
        assertFalse(DepositTermDetail.createFrom(2, 24, SavingsPeriodFrequencyType.YEARS, SavingsPeriodFrequencyType.MONTHS, null, null)
                .isMinDepositTermGreaterThanMaxDepositTerm());
        assertTrue(DepositTermDetail.createFrom(2, 18, SavingsPeriodFrequencyType.YEARS, SavingsPeriodFrequencyType.MONTHS, null, null)
                .isMinDepositTermGreaterThanMaxDepositTerm());
    }

    @Test
    void weeklyMultipleAfterMinimumInDays() {
        DepositTermDetail term = DepositTermDetail.createFrom(7, 70, SavingsPeriodFrequencyType.DAYS, SavingsPeriodFrequencyType.DAYS, 1,
                SavingsPeriodFrequencyType.WEEKS);

        assertTrue(term.isValidInMultiplesOfPeriod(3, SavingsPeriodFrequencyType.WEEKS));
        assertFalse(term.isValidInMultiplesOfPeriod(10, SavingsPeriodFrequencyType.DAYS));
    }
}
