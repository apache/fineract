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
package org.apache.fineract.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

/***
 * 
 * People typically use either of the following settings when defining interest
 * recalculation method:
 * <ul>
 * <li>NONE</li>
 * <li>INTEREST</li>
 * <li>FEE</li>
 * <li>INTEREST_AND_FEE</li>
 * </ul>
 */
public enum InterestRecalculationCompoundingMethod {

    NONE(0, "interestRecalculationCompoundingMethod.none"), //
    INTEREST(1, "interestRecalculationCompoundingMethod.interest"), //
    FEE(2, "interestRecalculationCompoundingMethod.fee"), //
    INTEREST_AND_FEE(3, "interestRecalculationCompoundingMethod.interest.and.fee");

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestRecalculationCompoundingMethod> intToEnumMap = new HashMap<>();
    static {
        for (final InterestRecalculationCompoundingMethod type : InterestRecalculationCompoundingMethod.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestRecalculationCompoundingMethod fromInt(final Integer ruleTypeValue) {
        final InterestRecalculationCompoundingMethod type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestRecalculationCompoundingMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isFeeCompoundingEnabled() {
        return this.getValue().equals(InterestRecalculationCompoundingMethod.FEE.getValue())
                || this.getValue().equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getValue());
    }

    public boolean isCompoundingEnabled() {
        return !this.getValue().equals(InterestRecalculationCompoundingMethod.NONE.getValue());
    }

    public boolean isInterestCompoundingEnabled() {
        return this.getValue().equals(InterestRecalculationCompoundingMethod.INTEREST.getValue())
                || this.getValue().equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getValue());
    }
}
