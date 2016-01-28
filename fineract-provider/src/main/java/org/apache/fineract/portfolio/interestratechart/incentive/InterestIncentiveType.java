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
package org.apache.fineract.portfolio.interestratechart.incentive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum InterestIncentiveType {

    INVALID(1, "InterestIncentiveType.invalid"), //
    FIXED(2, "InterestIncentiveType.fixed"), //
    INCENTIVE(3, "InterestIncentiveType.incentive"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestIncentiveType> intToEnumMap = new HashMap<>();
    static {
        for (final InterestIncentiveType type : InterestIncentiveType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestIncentiveType fromInt(final Integer ruleTypeValue) {
        final InterestIncentiveType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestIncentiveType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isIncentive() {
        return InterestIncentiveType.INCENTIVE.getValue().equals(this.value);
    }

    public boolean isFixed() {
        return InterestIncentiveType.FIXED.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return InterestIncentiveType.INVALID.getValue().equals(this.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final InterestIncentiveType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

}