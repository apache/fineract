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
package org.apache.fineract.test.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class WorkingCapitalScheduleMatcher {

    private WorkingCapitalScheduleMatcher() {}

    public static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    public static boolean matchesInteger(final Integer actual, final String expected) {
        if (isBlank(expected)) {
            return actual == null;
        }
        return actual != null && actual.equals(Integer.valueOf(expected));
    }

    public static boolean matchesLong(final Long actual, final String expected) {
        if (isBlank(expected)) {
            return actual == null;
        }
        return actual != null && actual.equals(Long.valueOf(expected));
    }

    public static boolean matchesDate(final LocalDate actual, final String expected) {
        if (isBlank(expected)) {
            return actual == null;
        }
        return actual != null && actual.equals(LocalDate.parse(expected));
    }

    public static boolean matchesFormattedDate(final String actualFormatted, final String expected) {
        if (isBlank(expected)) {
            return actualFormatted == null;
        }
        return expected.equals(actualFormatted);
    }

    public static boolean matchesDecimal(final BigDecimal actual, final String expected) {
        if (isBlank(expected)) {
            return actual == null;
        }
        if (actual == null) {
            return false;
        }
        return actual.compareTo(new BigDecimal(expected)) == 0;
    }

    public static boolean matchesDecimalWithScale(final BigDecimal actual, final String expected, final int scale) {
        if (isBlank(expected)) {
            return actual == null;
        }
        if (actual == null) {
            return false;
        }
        return actual.setScale(scale, RoundingMode.HALF_UP).compareTo(new BigDecimal(expected).setScale(scale, RoundingMode.HALF_UP)) == 0;
    }
}
