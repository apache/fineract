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

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;

public final class Utils {

    private static final SecureRandom random = new SecureRandom();

    private Utils() {}

    public static String randomStringGenerator(final String prefix, final int len, final String sourceSetString) {
        final int lengthOfSource = sourceSetString.length();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(sourceSetString.charAt(random.nextInt(lengthOfSource)));
        }
        return prefix + sb;
    }

    public static String randomStringGenerator(final String prefix, final int len) {
        return randomStringGenerator(prefix, len, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return randomStringGenerator(prefix, lenOfRandomSuffix);
    }

    public static LocalDate now() {
        return LocalDate.now(Clock.systemUTC());
    }

    /**
     * A record that formats a double value based on whether it's a whole number or not.
     * <p>
     * If the value is a whole number, the output will have one decimal place (e.g., 16.0). Otherwise, it will have two
     * decimal places (e.g., 16.90), but if the second decimal place is zero, it will be removed (so 16.90 becomes
     * 16.9).
     */
    public record DoubleFormatter(double value) {

        public String format() {
            boolean isWholeNumber = (value % 1.0 == 0);

            String result = isWholeNumber ? String.format("%.1f", value) : String.format("%.2f", value);

            // For non-whole numbers, remove trailing '0' if it exists
            if (!isWholeNumber && result.endsWith("0")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
    }
}
