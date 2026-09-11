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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import org.apache.fineract.client.models.GetInterestRateChartsChartSlabs;

/**
 * Projects the interest a deposit account should earn, so a test can state an expectation independently of the server.
 * The arithmetic is that of the RestAssured {@code FixedDepositAccountHelper}: it accumulates in {@code float}, so a
 * projection is only good to about seven significant digits and must be compared to the server's value with a
 * tolerance, never for equality.
 */
public final class DepositInterestCalculator {

    private DepositInterestCalculator() {}

    /** The annual rate the chart applies to a deposit of this many periods, or zero when no slab covers it. */
    public static BigDecimal interestRateFor(Collection<GetInterestRateChartsChartSlabs> chartSlabs, int depositPeriod) {
        for (GetInterestRateChartsChartSlabs slab : chartSlabs) {
            Integer fromPeriod = slab.getFromPeriod();
            Integer toPeriod = slab.getToPeriod();
            if (fromPeriod != null && toPeriod != null && depositPeriod >= fromPeriod && depositPeriod <= toPeriod) {
                return slab.getAnnualInterestRate();
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * The principal of a recurring deposit after {@code depositPeriod} months, where {@code depositAmount} is paid in
     * at the start of each month before that month's interest accrues.
     */
    public static float principalAfterCompoundingInterest(Calendar startDate, float principal, float depositAmount, int depositPeriod,
            double interestPerDay, int compoundingInterval, int postingInterval) {
        Calendar currentDate = (Calendar) startDate.clone();
        float totalInterest = 0.0f;

        for (int month = 1; month <= depositPeriod; month++) {
            int daysInMonth = currentDate.getActualMaximum(Calendar.DATE);
            principal += depositAmount;
            for (int day = 0; day < daysInMonth; day++) {
                float interestEarned = (float) (principal * interestPerDay);
                totalInterest += interestEarned;
                if (compoundingInterval == 0) {
                    principal += interestEarned;
                }
            }
            if (month % postingInterval == 0 || month == depositPeriod) {
                if (compoundingInterval != 0) {
                    principal += totalInterest;
                }
                totalInterest = 0.0f;
            }
            currentDate.add(Calendar.MONTH, 1);
        }
        return principal;
    }

    /**
     * The principal after {@code depositPeriod} months of daily interest, compounded every {@code compoundingInterval}
     * months (0 meaning daily) and posted every {@code postingInterval} months.
     */
    public static float principalAfterCompoundingInterest(Calendar startDate, float principal, int depositPeriod, double interestPerDay,
            int compoundingInterval, int postingInterval) {
        Calendar currentDate = (Calendar) startDate.clone();
        float totalInterest = 0.0f;

        for (int month = 1; month <= depositPeriod; month++) {
            int daysInMonth = currentDate.getActualMaximum(Calendar.DATE);
            for (int day = 0; day < daysInMonth; day++) {
                float interestEarned = (float) (principal * interestPerDay);
                totalInterest += interestEarned;
                if (compoundingInterval == 0) {
                    principal += interestEarned;
                }
            }
            if (month % postingInterval == 0 || month == depositPeriod) {
                if (compoundingInterval != 0) {
                    principal += totalInterest;
                }
                totalInterest = 0.0f;
            }
            currentDate.add(Calendar.MONTH, 1);
        }
        return principal;
    }
}
