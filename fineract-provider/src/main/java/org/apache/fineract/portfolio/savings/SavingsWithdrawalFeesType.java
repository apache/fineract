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

import java.util.ArrayList;
import java.util.List;

public enum SavingsWithdrawalFeesType {

    INVALID(0, "savingsWithdrawalFeesType.invalid"), //
    FLAT(1, "savingsWithdrawalFeesType.flat"), //
    PERCENT_OF_AMOUNT(2, "savingsWithdrawalFeesType.percent.of.amount");

    private final Integer value;
    private final String code;

    private SavingsWithdrawalFeesType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final SavingsWithdrawalFeesType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static SavingsWithdrawalFeesType fromInt(final Integer type) {

        SavingsWithdrawalFeesType withdrawalFeeType = SavingsWithdrawalFeesType.INVALID;
        switch (type) {
            case 1:
                withdrawalFeeType = FLAT;
            break;
            case 2:
                withdrawalFeeType = PERCENT_OF_AMOUNT;
            break;
        }
        return withdrawalFeeType;
    }
}