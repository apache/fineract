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

public final class FrequencyTypeUtil {

    private FrequencyTypeUtil() {}

    public static int compareFrequencies(final Integer frequency1, final String frequencyType1, final Integer frequency2,
            final String frequencyType2) {
        if (frequencyType1.equals(frequencyType2)) {
            return frequency1.compareTo(frequency2);
        }
        return frequencyToDays(frequency1, frequencyType1).compareTo(frequencyToDays(frequency2, frequencyType2));
    }

    private static Integer frequencyToDays(final Integer frequency, final String frequencyType) {
        return switch (frequencyType) {
            case "DAYS" -> frequency;
            case "WEEKS" -> frequency * 7;
            case "MONTHS" -> frequency * 30;
            case "YEARS" -> frequency * 365;
            default -> 0;
        };
    }
}
