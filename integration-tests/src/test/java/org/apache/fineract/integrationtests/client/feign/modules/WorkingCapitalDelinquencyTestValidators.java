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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse;
import org.apache.fineract.client.models.WorkingCapitalCollection;
import org.apache.fineract.client.models.WorkingCapitalCollectionRangeScheduleDelinquency;

public final class WorkingCapitalDelinquencyTestValidators {

    private WorkingCapitalDelinquencyTestValidators() {}

    /**
     * One row of {@code GET /working-capital-loans/{id}/delinquencyrangetags}; {@code liftedOnDate} null means active.
     */
    public record ExpectedTag(int periodNumber, String addedOnDate, String liftedOnDate, String classification) {

        public static ExpectedTag tag(int periodNumber, String addedOnDate, String liftedOnDate, String classification) {
            return new ExpectedTag(periodNumber, addedOnDate, liftedOnDate, classification);
        }

        String render() {
            return "P" + periodNumber + " added=" + addedOnDate + " lifted=" + liftedOnDate + " " + classification;
        }
    }

    /** One entry of {@code delinquent.installmentLevelDelinquency} on GET WC loan. */
    public record ExpectedRangeDelinquency(Long rangeId, String classification, int minimumAgeDays, Integer maximumAgeDays,
            String delinquentAmount) {

        public static ExpectedRangeDelinquency rangeDelinquency(Long rangeId, String classification, int minimumAgeDays,
                Integer maximumAgeDays, String delinquentAmount) {
            return new ExpectedRangeDelinquency(rangeId, classification, minimumAgeDays, maximumAgeDays, delinquentAmount);
        }
    }

    /**
     * Compares the full tag history as a multiset: the API orders by addedOnDate only, so rows added on the same day
     * have no guaranteed order. Both sides are rendered and sorted before comparison, which keeps the failure message
     * readable.
     */
    public static void validateTagHistory(final List<GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse> actual,
            final ExpectedTag... expected) {
        assertNotNull(actual, "Delinquency tag history must be present");
        final List<String> expectedRows = Arrays.stream(expected).map(ExpectedTag::render).sorted().toList();
        final List<String> actualRows = actual.stream().map(WorkingCapitalDelinquencyTestValidators::render).sorted().toList();
        assertEquals(String.join("\n", expectedRows), String.join("\n", actualRows), "Delinquency tag history mismatch");
    }

    /** Asserts which periods hold an active tag and that each of them holds exactly one. */
    public static void validateOneActiveTagPerPeriod(final List<GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse> actual,
            final Integer... expectedActivePeriods) {
        assertNotNull(actual, "Delinquency tag history must be present");
        final Map<Integer, Long> actualActiveCounts = actual.stream().filter(row -> row.getLiftedOnDate() == null)
                .collect(Collectors.groupingBy(GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse::getPeriodNumber,
                        TreeMap::new, Collectors.counting()));
        final Map<Integer, Long> expectedActiveCounts = Arrays.stream(expectedActivePeriods)
                .collect(Collectors.toMap(period -> period, period -> 1L, (a, b) -> a, TreeMap::new));
        assertEquals(expectedActiveCounts, actualActiveCounts,
                "Active (liftedOnDate == null) tag count per period — expected exactly one per delinquent period");
    }

    public static void validateInstallmentLevelDelinquency(final WorkingCapitalCollection delinquent,
            final ExpectedRangeDelinquency... expected) {
        assertNotNull(delinquent, "GET WC loan must return delinquent data");
        final List<WorkingCapitalCollectionRangeScheduleDelinquency> actual = delinquent.getInstallmentLevelDelinquency();
        assertNotNull(actual, "delinquent.installmentLevelDelinquency must be present");
        assertEquals(expected.length, actual.size(), () -> "installmentLevelDelinquency entry count — expected one entry per range "
                + Arrays.toString(expected) + " but was " + describe(actual));
        for (int i = 0; i < expected.length; i++) {
            final ExpectedRangeDelinquency exp = expected[i];
            final WorkingCapitalCollectionRangeScheduleDelinquency act = actual.get(i);
            final String row = "installmentLevelDelinquency[" + i + "] " + describe(actual);
            assertEquals(exp.rangeId(), act.getRangeId(), row + " — rangeId");
            assertEquals(exp.classification(), act.getClassification(), row + " — classification");
            assertEquals(Integer.valueOf(exp.minimumAgeDays()), act.getMinimumAgeDays(), row + " — minimumAgeDays");
            assertEquals(exp.maximumAgeDays(), act.getMaximumAgeDays(), row + " — maximumAgeDays");
            assertAmount(exp.delinquentAmount(), act.getDelinquentAmount(), row + " — delinquentAmount");
        }
    }

    public static void validateLoanLevelDelinquency(final WorkingCapitalCollection delinquent, final String delinquentAmount,
            final long delinquentDays, final String delinquentDate) {
        assertNotNull(delinquent, "GET WC loan must return delinquent data");
        assertAmount(delinquentAmount, delinquent.getDelinquentAmount(), "delinquent.delinquentAmount");
        assertEquals(Long.valueOf(delinquentDays), delinquent.getDelinquentDays(), "delinquent.delinquentDays");
        assertEquals(LocalDate.parse(delinquentDate), delinquent.getDelinquentDate(), "delinquent.delinquentDate");
    }

    public static void validatePastDueDays(final WorkingCapitalCollection delinquent, final long pastDueDays) {
        assertNotNull(delinquent, "GET WC loan must return delinquent data");
        assertEquals(Long.valueOf(pastDueDays), delinquent.getPastDueDays(), "delinquent.pastDueDays");
    }

    private static void assertAmount(final String expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message + " — value was null, expected " + expected);
        assertEquals(0, new BigDecimal(expected).compareTo(actual), message + " — expected: " + expected + " but was: " + actual);
    }

    private static String render(final GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse row) {
        final String classification = row.getDelinquencyRange() == null ? null : row.getDelinquencyRange().getClassification();
        return "P" + row.getPeriodNumber() + " added=" + row.getAddedOnDate() + " lifted=" + row.getLiftedOnDate() + " " + classification;
    }

    private static String describe(final List<WorkingCapitalCollectionRangeScheduleDelinquency> entries) {
        return entries.stream().map(e -> "{" + e.getClassification() + " rangeId=" + e.getRangeId() + " " + e.getMinimumAgeDays() + "-"
                + e.getMaximumAgeDays() + " amount=" + e.getDelinquentAmount() + "}").collect(Collectors.joining(", ", "[", "]"));
    }
}
