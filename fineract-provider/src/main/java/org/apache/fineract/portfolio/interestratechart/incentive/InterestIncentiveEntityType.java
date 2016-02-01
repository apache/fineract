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

public enum InterestIncentiveEntityType {

    INVALID(1, "InterestIncentiveEntityType.invalid"), //
    CUSTOMER(2, "InterestIncentiveEntityType.customer"), //
    ACCOUNT(3, "InterestIncentiveEntityType.account"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestIncentiveEntityType> intToEnumMap = new HashMap<>();
    static {
        for (final InterestIncentiveEntityType type : InterestIncentiveEntityType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestIncentiveEntityType fromInt(final Integer ruleTypeValue) {
        final InterestIncentiveEntityType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestIncentiveEntityType(final Integer value, final String code) {
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

    public boolean isCustomer() {
        return InterestIncentiveEntityType.CUSTOMER.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return InterestIncentiveEntityType.INVALID.getValue().equals(this.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final InterestIncentiveEntityType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}