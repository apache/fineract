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
package org.apache.fineract.adhocquery.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ReportRunFrequency {
    DAILY(1, "reportRunFrequency.daily"),
    WEEKLY(2, "reportRunFrequency.weekly"),
    MONTHLY(3, "reportRunFrequency.monthly"),
    YEARLY(4, "reportRunFrequency.yearly"),
    CUSTOM(5, "reportRunFrequency.custom");

    private static final Map<Long, ReportRunFrequency> MAP = Arrays.stream(ReportRunFrequency.values()).collect(Collectors.toMap(
        ReportRunFrequency::getValue, e -> e
    ));

    private final long value;
    private final String code;

    private ReportRunFrequency(final long value, final String code) {
        this.value = value;
        this.code = code;
    }

    public long getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static ReportRunFrequency fromId(final long id) {
        return ReportRunFrequency.MAP.get(id);
    }
}
