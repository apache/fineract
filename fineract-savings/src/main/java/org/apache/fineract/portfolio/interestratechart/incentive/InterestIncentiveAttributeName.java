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

import java.util.Arrays;

public enum InterestIncentiveAttributeName {

    INVALID(1, "InterestIncentiveAttributeName.invalid"), //
    GENDER(2, "InterestIncentiveAttributeName.gender"), //
    AGE(3, "InterestIncentiveAttributeName.age"), //
    CLIENT_TYPE(4, "InterestIncentiveAttributeName.clientType"), //
    CLIENT_CLASSIFICATION(5, "InterestIncentiveAttributeName.clientClassification");

    private final Integer value;
    private final String code;

    InterestIncentiveAttributeName(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static InterestIncentiveAttributeName fromInt(final Integer value) {
        if (value == null) {
            return INVALID;
        }
        return switch (value) {
            case 2 -> GENDER;
            case 3 -> AGE;
            case 4 -> CLIENT_TYPE;
            case 5 -> CLIENT_CLASSIFICATION;
            default -> INVALID;
        };
    }

    @Override
    public String toString() {
        return name().replace("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static boolean isCodeValueAttribute(InterestIncentiveAttributeName attributeName) {
        return switch (attributeName) {
            case GENDER, CLIENT_TYPE, CLIENT_CLASSIFICATION -> true;
            default -> false;
        };
    }

    public static Integer[] integerValues() {
        return Arrays.stream(values()).filter(v -> v != INVALID).map(v -> v.value).toArray(Integer[]::new);
    }
}
