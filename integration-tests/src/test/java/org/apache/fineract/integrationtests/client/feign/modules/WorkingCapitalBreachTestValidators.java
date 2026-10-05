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
package org.apache.fineract.integrationtests.client.feign.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;

public final class WorkingCapitalBreachTestValidators {

    private WorkingCapitalBreachTestValidators() {}

    public record ExpectedBreachPeriod(int periodNumber, String fromDate, String toDate, int numberOfDays, String minPaymentAmount,
            String outstandingAmount, Boolean breach, boolean reset) {

        /** {@code breach} is {@code null} for a period never evaluated as expired, and the distinction is asserted. */
        public static ExpectedBreachPeriod period(int periodNumber, String fromDate, String toDate, int numberOfDays,
                String minPaymentAmount, String outstandingAmount, Boolean breach, boolean reset) {
            return new ExpectedBreachPeriod(periodNumber, fromDate, toDate, numberOfDays, minPaymentAmount, outstandingAmount, breach,
                    reset);
        }
    }

    public record ExpectedBreachAction(String action, String startDate) {

        public static ExpectedBreachAction action(String action, String startDate) {
            return new ExpectedBreachAction(action, startDate);
        }
    }

    public static void validateBreachSchedule(final List<WorkingCapitalLoanBreachScheduleData> actualSchedule,
            final ExpectedBreachPeriod... expectedPeriods) {
        assertNotNull(actualSchedule, "Breach schedule must be present");
        final List<WorkingCapitalLoanBreachScheduleData> sorted = actualSchedule.stream()
                .sorted(Comparator.comparing(WorkingCapitalLoanBreachScheduleData::getPeriodNumber)).toList();
        assertEquals(expectedPeriods.length, sorted.size(), () -> "Breach schedule row count mismatch — expected " + expectedPeriods.length
                + " periods but found " + sorted.size() + ": " + describe(sorted));

        for (int i = 0; i < expectedPeriods.length; i++) {
            final ExpectedBreachPeriod expected = expectedPeriods[i];
            final WorkingCapitalLoanBreachScheduleData actual = sorted.get(i);
            final String row = "breach period " + expected.periodNumber();

            assertEquals(Integer.valueOf(expected.periodNumber()), actual.getPeriodNumber(), row + " — periodNumber");
            assertEquals(LocalDate.parse(expected.fromDate()), actual.getFromDate(), row + " — fromDate");
            assertEquals(LocalDate.parse(expected.toDate()), actual.getToDate(), row + " — toDate");
            assertEquals(Integer.valueOf(expected.numberOfDays()), actual.getNumberOfDays(), row + " — numberOfDays");
            assertBigDecimalEquals(new BigDecimal(expected.minPaymentAmount()), actual.getMinPaymentAmount(), row + " — minPaymentAmount");
            assertBigDecimalEquals(new BigDecimal(expected.outstandingAmount()), actual.getOutstandingAmount(),
                    row + " — outstandingAmount");
            if (expected.breach() == null) {
                assertNull(actual.getBreach(), row + " — breach flag expected null (never evaluated) but was " + actual.getBreach());
            } else {
                assertEquals(expected.breach(), actual.getBreach(), row + " — breach flag");
            }
            assertEquals(Boolean.valueOf(expected.reset()), actual.getReset(), row + " — reset flag");
        }
    }

    public static void validateBreachActions(final List<WorkingCapitalLoanBreachActionData> actualActions,
            final ExpectedBreachAction... expectedActions) {
        assertNotNull(actualActions, "Breach actions must be present");
        final List<WorkingCapitalLoanBreachActionData> sorted = actualActions.stream()
                .sorted(Comparator.comparing(WorkingCapitalLoanBreachActionData::getId)).toList();
        assertEquals(expectedActions.length, sorted.size(),
                () -> "Breach action count mismatch — expected " + expectedActions.length + " but found " + sorted.size());

        for (int i = 0; i < expectedActions.length; i++) {
            final ExpectedBreachAction expected = expectedActions[i];
            final WorkingCapitalLoanBreachActionData actual = sorted.get(i);
            final String row = "breach action #" + (i + 1);

            assertNotNull(actual.getAction(), row + " — action type must be present");
            assertEquals(expected.action(), actual.getAction().getValue(), row + " — action type");
            assertEquals(LocalDate.parse(expected.startDate()), actual.getStartDate(), row + " — startDate");
        }
    }

    public static void validateBreachPastDueAmount(final BigDecimal actual, final String expected) {
        assertNotNull(actual, "Breach past due amount must be present — expected " + expected);
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Breach past due amount — expected: " + expected + " but was: " + actual);
    }

    private static void assertBigDecimalEquals(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message + " — value was null, expected " + expected);
        assertEquals(0, expected.compareTo(actual), message + " — expected: " + expected + " but was: " + actual);
    }

    private static String describe(final List<WorkingCapitalLoanBreachScheduleData> schedule) {
        return schedule.stream()
                .map(period -> "P" + period.getPeriodNumber() + "[" + period.getFromDate() + ".." + period.getToDate() + " days="
                        + period.getNumberOfDays() + " outstanding=" + period.getOutstandingAmount() + " breach=" + period.getBreach()
                        + " reset=" + period.getReset() + "]")
                .reduce((a, b) -> a + ", " + b).orElse("<empty>");
    }
}
