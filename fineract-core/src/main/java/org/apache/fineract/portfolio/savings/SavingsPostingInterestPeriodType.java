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
package org.apache.fineract.portfolio.savings;

import java.util.Arrays;

/**
 * The interest posting period is the span of time at the end of which savings earned but not yet credited/posted in a
 * client's account is credited/posted.
 */
public enum SavingsPostingInterestPeriodType {

    INVALID(0, "savingsPostingInterestPeriodType.invalid"), //
    DAILY(1, "savingsPostingInterestPeriodType.daily"), //
    MONTHLY(4, "savingsPostingInterestPeriodType.monthly"), //
    QUATERLY(5, "savingsPostingInterestPeriodType.quarterly"), //
    BIANNUAL(6, "savingsPostingInterestPeriodType.biannual"), ANNUAL(7, "savingsPostingInterestPeriodType.annual");

    private final Integer value;
    private final String code;

    SavingsPostingInterestPeriodType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    // TODO: do we really need this?!?
    public static Object[] integerValues() {
        return Arrays.stream(values()).filter(value -> !INVALID.equals(value)).map(value -> value.value).toList().toArray();
    }

    public static SavingsPostingInterestPeriodType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 1:
                return DAILY;
            case 4:
                return MONTHLY;
            case 5:
                return QUATERLY;
            case 6:
                return BIANNUAL;
            case 7:
                return ANNUAL;
            default:
                return INVALID;
        }
    }
}
