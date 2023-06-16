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
package org.apache.fineract.portfolio.savingsaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SavingsInterestPostingPeriodsTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private SavingsPostingInterestPeriodType savingsPostingInterestPeriodType;
    private SavingsHelper savingsHelper;

    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    @BeforeEach
    public void setUp() {
        savingsHelper = new SavingsHelper(accountTransfersReadPlatformService);
    }

    @Test
    public void testDailyPostingPeriodRetrieval() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 12, 1);
        final LocalDate endingDate = LocalDate.of(2021, 12, 10);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.DAILY;
        List<LocalDateInterval> result = savingsHelper.determineInterestPostingPeriods(startingDate, endingDate,
                savingsPostingInterestPeriodType1, financialYearBeginningMonth, localDateList);

        List<LocalDateInterval> expected = new ArrayList<>();
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 1), LocalDate.of(2021, 12, 1)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 2), LocalDate.of(2021, 12, 2)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 3), LocalDate.of(2021, 12, 3)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 4), LocalDate.of(2021, 12, 4)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 5), LocalDate.of(2021, 12, 5)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 6), LocalDate.of(2021, 12, 6)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 7), LocalDate.of(2021, 12, 7)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 8), LocalDate.of(2021, 12, 8)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 9), LocalDate.of(2021, 12, 9)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 10), LocalDate.of(2021, 12, 10)));

        assertEquals(result.size(), expected.size());

        verifyInterestPostingDates(result, expected);
    }

    @Test
    public void testMonthlyPostingPeriodRetrievalStartDateFirstOfMonth() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 11, 1);
        final LocalDate endingDate = LocalDate.of(2022, 1, 1);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.MONTHLY;
        List<LocalDateInterval> result = savingsHelper.determineInterestPostingPeriods(startingDate, endingDate,
                savingsPostingInterestPeriodType1, financialYearBeginningMonth, localDateList);

        List<LocalDateInterval> expected = new ArrayList<>();
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 11, 1), LocalDate.of(2021, 11, 30)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 01), LocalDate.of(2021, 12, 31)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 01, 01), LocalDate.of(2022, 01, 31)));

        assertEquals(result.size(), expected.size());

        verifyInterestPostingDates(result, expected);
    }

    @Test
    public void testMonthlyPostingPeriodRetrievalStartDateMiddleOfMonth() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 11, 14);
        final LocalDate endingDate = LocalDate.of(2022, 3, 14);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.MONTHLY;
        List<LocalDateInterval> result = savingsHelper.determineInterestPostingPeriods(startingDate, endingDate,
                savingsPostingInterestPeriodType1, financialYearBeginningMonth, localDateList);

        List<LocalDateInterval> expected = new ArrayList<>();
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 11, 14), LocalDate.of(2021, 11, 30)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 01), LocalDate.of(2021, 12, 31)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 01, 01), LocalDate.of(2022, 01, 31)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 02, 01), LocalDate.of(2022, 02, 28)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 03, 01), LocalDate.of(2022, 03, 31)));
        assertEquals(result.size(), expected.size());

        verifyInterestPostingDates(result, expected);

    }

    @Test
    public void testActivationDateMonthlyPostingPeriodRetrievalStartDateMiddleOfMonth() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 11, 14);
        final LocalDate endingDate = LocalDate.of(2022, 5, 14);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.ACTIVATIONDATE;

        List<LocalDateInterval> result = savingsHelper.determineInterestPostingPeriods(startingDate, endingDate,
                savingsPostingInterestPeriodType1, financialYearBeginningMonth, localDateList);

        // expected result here
        List<LocalDateInterval> expected = new ArrayList<>();
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 11, 14), LocalDate.of(2021, 12, 13)));
        expected.add(LocalDateInterval.create(LocalDate.of(2021, 12, 14), LocalDate.of(2022, 1, 13)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 1, 14), LocalDate.of(2022, 2, 13)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 2, 14), LocalDate.of(2022, 3, 13)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 3, 14), LocalDate.of(2022, 4, 13)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 4, 14), LocalDate.of(2022, 5, 13)));
        expected.add(LocalDateInterval.create(LocalDate.of(2022, 5, 14), LocalDate.of(2022, 6, 13)));

        // Assert that the result matches the expected result
        assertEquals(expected.size(), result.size());

        verifyInterestPostingDates(result, expected);
    }

    private static void verifyInterestPostingDates(List<LocalDateInterval> result, List<LocalDateInterval> expected) {
        for (int i = 0; i < expected.size(); i++) {
            LocalDateInterval expectedInterval = expected.get(i);
            LocalDateInterval resultInterval = result.get(i);

            assertEquals(expectedInterval.startDate(), resultInterval.startDate());
            assertEquals(expectedInterval.endDate(), resultInterval.endDate());
        }
    }
}
