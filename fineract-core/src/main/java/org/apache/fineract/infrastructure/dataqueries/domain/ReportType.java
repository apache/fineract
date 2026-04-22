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
package org.apache.fineract.infrastructure.dataqueries.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeration of valid report types for whitelist validation.
 *
 * This enum provides a secure whitelist of allowed report types to prevent SQL injection attacks in the reporting
 * system. Only these predefined types are allowed in report queries.
 */
public enum ReportType {

    /**
     * Standard report type for retrieving report data
     */
    REPORT("report"),

    /**
     * Parameter type for retrieving parameter definitions and possible values
     */
    PARAMETER("parameter");

    private final String value;

    /**
     * Cached set of all valid values for efficient validation
     */
    private static final Set<String> VALID_VALUES = Arrays.stream(values()).map(ReportType::getValue).collect(Collectors.toSet());

    ReportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Validates if a given report type is in the whitelist.
     *
     * @param type
     *            the report type to validate
     * @return true if the type is valid, false otherwise
     */
    public static boolean isValidType(String type) {
        return type != null && !type.trim().isEmpty() && VALID_VALUES.contains(type.toLowerCase(Locale.ROOT));
    }

    /**
     * Gets the ReportType enum value for a given string.
     *
     * @param type
     *            the report type string
     * @return the corresponding ReportType enum value
     * @throws IllegalArgumentException
     *             if the type is not valid
     */
    public static ReportType fromValue(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Report type cannot be null");
        }

        String lowerType = type.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(rt -> rt.getValue().equals(lowerType)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid report type: " + type));
    }
}
