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
package org.apache.fineract.portfolio.group.domain;

import java.util.HashMap;
import java.util.Map;

public enum GroupTypes {

    INVALID(0l, "lendingStrategy.invalid", "invalid"), //
    CENTER(1l, "groupTypes.center", "center"), //
    GROUP(2l, "groupTypes.group", "group"); //

    private Long id;
    private String code;
    private String value;

    private GroupTypes(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    private static final Map<Long, GroupTypes> intToEnumMap = new HashMap<>();
    private static long minValue;
    private static long maxValue;
    static {
        int i = 0;
        for (final GroupTypes type : GroupTypes.values()) {
            if (i == 0) {
                minValue = type.id;
            }
            intToEnumMap.put(type.id, type);
            if (minValue >= type.id) {
                minValue = type.id;
            }
            if (maxValue < type.id) {
                maxValue = type.id;
            }
            i = i + 1;
        }
    }

    public static GroupTypes fromInt(final int i) {
        final GroupTypes type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public static long getMinValue() {
        return minValue;
    }

    public static long getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }
}