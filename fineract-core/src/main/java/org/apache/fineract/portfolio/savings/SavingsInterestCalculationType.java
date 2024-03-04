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
 *
 * There are two methods to calculate the interest on a savings account:
 * <ul>
 * <li>The daily balance method; and</li>
 * <li>The average daily balance method.</li>
 * </ul>
 *
 * <p>
 * The interest calculation must be based on a point in time for determining the balance in the account, such as:
 * </p>
 * <ul>
 * <li>beginning-of-day balance</li>
 * <li>end-of-day balance</li>
 * <li>close-of-business-day balance</li>
 * </ul>
 *
 * <p>
 * Any one of the three may be used, but must be applied consistently. End-of-day balance is used by default at present.
 * </p>
 */
public enum SavingsInterestCalculationType {

    INVALID(0, "savingsInterestCalculationType.invalid"), //
    DAILY_BALANCE(1, "savingsInterestCalculationType.dailybalance"), //
    AVERAGE_DAILY_BALANCE(2, "savingsInterestCalculationType.averagedailybalance");

    private final Integer value;
    private final String code;

    SavingsInterestCalculationType(final Integer value, final String code) {
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

    public static SavingsInterestCalculationType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 1:
                return DAILY_BALANCE;
            case 2:
                return AVERAGE_DAILY_BALANCE;
            default:
                return INVALID;
        }
    }
}
