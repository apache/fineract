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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.junit.jupiter.api.Test;

class SavingsHelperNPExceptionTest {

    private final SavingsHelper savingsHelper = new SavingsHelper(null);

    @Test
    void shouldNotThrowNullPointerExceptionWhenPostingPeriodTypeIsNull() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 4, 1);
        SavingsPostingInterestPeriodType nullPostingPeriodType = null;

        Integer financialYearBeginningMonth = 1;
        List<LocalDate> postInterestAsOn = new ArrayList<>();
        List<LocalDateInterval> result = savingsHelper.determineInterestPostingPeriods(start, end, nullPostingPeriodType,
                financialYearBeginningMonth, postInterestAsOn);
        assertTrue(result.isEmpty(), "Result should be empty when postingPeriodType is null");
    }
}
