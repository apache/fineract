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
import java.util.List;
import org.apache.fineract.client.models.WorkingCapitalLoanPeriodPaymentRateChangeData;

public final class WorkingCapitalLoanRateHistoryValidators {

    private WorkingCapitalLoanRateHistoryValidators() {}

    /** Rates are unique per scenario, so newRate stays a stable key when two changes share an effective date. */
    public static WorkingCapitalLoanPeriodPaymentRateChangeData entryByNewRate(
            final List<WorkingCapitalLoanPeriodPaymentRateChangeData> history, final BigDecimal newRate) {
        final List<WorkingCapitalLoanPeriodPaymentRateChangeData> matches = history.stream()
                .filter(entry -> entry.getNewRate() != null && newRate.compareTo(entry.getNewRate()) == 0).toList();
        assertEquals(1, matches.size(),
                "Expected exactly one rate-change entry with newRate=" + newRate + " but found " + matches.size() + " in: " + history);
        return matches.getFirst();
    }

    public static void validateSnapshot(final String label, final WorkingCapitalLoanPeriodPaymentRateChangeData entry,
            final String expectedAnnualEir, final String expectedDailyPayment, final int expectedSegmentTerm) {
        validateDecimal(label, "calculatedAnnualEir", entry.getCalculatedAnnualEir(), expectedAnnualEir);
        validateDecimal(label, "dailyPaymentAmount", entry.getDailyPaymentAmount(), expectedDailyPayment);
        assertEquals(expectedSegmentTerm, entry.getSegmentTerm(), label + ": 'segmentTerm'");
    }

    /**
     * compareTo, not equals: 6-dp columns return trailing zeros, but rescaling the actual first would let 47.224 pass.
     */
    public static void validateDecimal(final String label, final String field, final BigDecimal actual, final String expected) {
        assertNotNull(actual, label + ": expected '" + field + "' = " + expected + " but it was null");
        final BigDecimal expectedValue = new BigDecimal(expected);
        assertEquals(0, expectedValue.compareTo(actual), label + ": '" + field + "' expected " + expected + " but was " + actual);
    }
}
