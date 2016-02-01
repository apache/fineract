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

/**
 * The interest posting period is the span of time at the end of which savings
 * earned but not yet credited/posted in a client's account is credited/posted.
 */
public enum SavingsPostingInterestPeriodType {

    INVALID(0, "savingsPostingInterestPeriodType.invalid"), //
    MONTHLY(4, "savingsPostingInterestPeriodType.monthly"), //
    QUATERLY(5, "savingsPostingInterestPeriodType.quarterly"), //
    BIANNUAL(6, "savingsPostingInterestPeriodType.biannual"), ANNUAL(7, "savingsPostingInterestPeriodType.annual");

    private final Integer value;
    private final String code;

    private SavingsPostingInterestPeriodType(final Integer value, final String code) {
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
        for (final SavingsPostingInterestPeriodType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static SavingsPostingInterestPeriodType fromInt(final Integer type) {
        SavingsPostingInterestPeriodType repaymentFrequencyType = SavingsPostingInterestPeriodType.INVALID;
        if (type != null) {
            switch (type) {
                case 4:
                    repaymentFrequencyType = SavingsPostingInterestPeriodType.MONTHLY;
                break;
                case 5:
                    repaymentFrequencyType = SavingsPostingInterestPeriodType.QUATERLY;
                break;
                case 6:
                    repaymentFrequencyType = SavingsPostingInterestPeriodType.BIANNUAL;
                break;
                case 7:
                    repaymentFrequencyType = SavingsPostingInterestPeriodType.ANNUAL;
                break;
            }
        }
        return repaymentFrequencyType;
    }
}