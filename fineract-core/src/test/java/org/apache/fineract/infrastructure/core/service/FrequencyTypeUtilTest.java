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
package org.apache.fineract.infrastructure.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FrequencyTypeUtilTest {

    @ParameterizedTest
    @CsvSource({ "1, DAYS, 2, -1", "2, WEEKS, 2, 0", "3, MONTHS, 2, 1" })
    void comparesFrequenciesOfTheSameType(int frequency1, String frequencyType, int frequency2, int expectedSign) {
        int result = FrequencyTypeUtil.compareFrequencies(frequency1, frequencyType, frequency2, frequencyType);

        assertEquals(expectedSign, Integer.signum(result));
    }

    @ParameterizedTest
    @CsvSource({ "7, DAYS, 1, WEEKS, 0", "2, WEEKS, 15, DAYS, -1", "2, MONTHS, 8, WEEKS, 1", "1, YEARS, 12, MONTHS, 1" })
    void comparesFrequenciesOfDifferentTypes(int frequency1, String frequencyType1, int frequency2, String frequencyType2,
            int expectedSign) {
        int result = FrequencyTypeUtil.compareFrequencies(frequency1, frequencyType1, frequency2, frequencyType2);

        assertEquals(expectedSign, Integer.signum(result));
    }
}
