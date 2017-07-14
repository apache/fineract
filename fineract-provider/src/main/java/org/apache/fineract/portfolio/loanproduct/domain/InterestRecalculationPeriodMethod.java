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
 * <li>SAME_AS_REPAYMENT_PERIOD</li>
 * <li>DAILY</li>
 * <li>WEEKLY</li>
 * <li>FORTNIGHTLY</li>
 * <li>MONTHLY</li>
 * </ul>
 */

public enum InterestRecalculationPeriodMethod {
    INVALID(0, "interestRecalculationPeriodMethod.invalid"), //
    DAILY(1, "interestRecalculationPeriodMethod.daily"), //
    WEEKLY(2, "interestRecalculationPeriodMethod.weekly"), //
    FORTNIGHTLY(3, "interestRecalculationPeriodMethod.fortnightly"), //
    MONTHLY(4, "interestRecalculationPeriodMethod.monthly"), //
    SAME_AS_REPAYMENT_PERIOD(5, "interestRecalculationPeriodMethod.same.as.repayment.period");

    private final Integer value;
    private final String code;
    private static final Map<Integer, InterestRecalculationPeriodMethod> intToEnumMap = new HashMap<>();

    static {
        for (final InterestRecalculationPeriodMethod type : InterestRecalculationPeriodMethod.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestRecalculationPeriodMethod fromInt(final Integer ruleTypeValue) {
        final InterestRecalculationPeriodMethod type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestRecalculationPeriodMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }
}
