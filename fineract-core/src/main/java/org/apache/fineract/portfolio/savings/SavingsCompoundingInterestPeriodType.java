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
 * <p>
 * The compounding interest period is the span of time at the end of which savings in a client's account earn interest.
 * </p>
 */
public enum SavingsCompoundingInterestPeriodType {

    INVALID(0, "savingsCompoundingInterestPeriodType.invalid"), DAILY(1, "savingsCompoundingInterestPeriodType.daily"),
    // WEEKLY(2, "savingsCompoundingInterestPeriodType.weekly"),
    // BIWEEKLY(3, "savingsCompoundingInterestPeriodType.biweekly"),
    MONTHLY(4, "savingsCompoundingInterestPeriodType.monthly"),

    QUATERLY(5, "savingsCompoundingInterestPeriodType.quarterly"), BI_ANNUAL(6, "savingsCompoundingInterestPeriodType.biannual"), ANNUAL(7,
            "savingsCompoundingInterestPeriodType.annual");

    // NO_COMPOUNDING_SIMPLE_INTEREST(8, "savingsCompoundingInterestPeriodType.nocompounding");

    private final Integer value;
    private final String code;

    SavingsCompoundingInterestPeriodType(final Integer value, final String code) {
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

    public static SavingsCompoundingInterestPeriodType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 1:
                return DAILY;
            // case 2:
            // return WEEKLY;
            // case 3:
            // return BIWEEKLY;
            case 4:
                return MONTHLY;
            case 5:
                return QUATERLY;
            case 6:
                return BI_ANNUAL;
            case 7:
                return ANNUAL;
            // case 8:
            // return NO_COMPOUNDING_SIMPLE_INTEREST;
            default:
                return INVALID;
        }
    }
}
