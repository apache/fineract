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

public enum InterestIncentiveAttributeName {

    INVALID(1, "InterestIncentiveAttributeName.invalid"), //
    GENDER(2, "InterestIncentiveAttributeName.gender"), //
    AGE(3, "InterestIncentiveAttributeName.age"), //
    CLIENT_TYPE(4, "InterestIncentiveAttributeName.clientType"), //
    CLIENT_CLASSIFICATION(5, "InterestIncentiveAttributeName.clientClassification"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestIncentiveAttributeName> intToEnumMap = new HashMap<>();
    static {
        for (final InterestIncentiveAttributeName type : InterestIncentiveAttributeName.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestIncentiveAttributeName fromInt(final Integer ruleTypeValue) {
        final InterestIncentiveAttributeName type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestIncentiveAttributeName(final Integer value, final String code) {
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

    public boolean isGender() {
        return InterestIncentiveAttributeName.GENDER.getValue().equals(this.value);
    }

    public boolean isAge() {
        return InterestIncentiveAttributeName.AGE.getValue().equals(this.value);
    }

    public boolean isClientType() {
        return InterestIncentiveAttributeName.CLIENT_TYPE.getValue().equals(this.value);
    }

    public boolean isClientClassification() {
        return InterestIncentiveAttributeName.CLIENT_CLASSIFICATION.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return InterestIncentiveAttributeName.INVALID.getValue().equals(this.value);
    }

    public static boolean isCodeValueAttribute(InterestIncentiveAttributeName attributeName) {
        boolean isCodeValue = false;
        switch (attributeName) {
            case GENDER:
            case CLIENT_TYPE:
            case CLIENT_CLASSIFICATION:
                isCodeValue = true;
            break;
            default:
            break;
        }
        return isCodeValue;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final InterestIncentiveAttributeName enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

}