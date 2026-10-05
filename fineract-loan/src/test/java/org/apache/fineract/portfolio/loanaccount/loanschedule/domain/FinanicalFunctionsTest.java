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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FinanicalFunctionsTest {

    @Test
    void nopShouldInvertPmtAtZeroInterest() {
        double payment = FinanicalFunctions.pmt(0, 12, 1000, 0, false);

        assertEquals(12, FinanicalFunctions.nop(0, payment, 1000, 0, false));
    }

    @Test
    void nopShouldInvertPmtWithInterest() {
        double[] rates = { 0.001, 0.01, 0.05 };
        int[] periods = { 6, 12, 24, 36 };
        boolean[] paymentTypes = { false, true };

        for (double rate : rates) {
            for (int period : periods) {
                for (boolean paymentType : paymentTypes) {
                    double payment = FinanicalFunctions.pmt(rate, period, 1000, 0, paymentType);

                    assertEquals(period, FinanicalFunctions.nop(rate, payment, 1000, 0, paymentType));
                }
            }
        }
    }

    @Test
    void nopShouldInvertPmtForAnnuitiesDueWithFutureValue() {
        double payment = FinanicalFunctions.pmt(0.01, 12, 1000, 100, true);

        assertEquals(12, FinanicalFunctions.nop(0.01, payment, 1000, 100, true));
    }
}
