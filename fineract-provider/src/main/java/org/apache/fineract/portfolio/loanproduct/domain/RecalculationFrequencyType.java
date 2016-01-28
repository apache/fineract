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

import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public enum RecalculationFrequencyType {
    INVALID(0, "interestRecalculationFrequencyType.invalid"), //
    SAME_AS_REPAYMENT_PERIOD(1, "interestRecalculationFrequencyType.same.as.repayment.period"), //
    DAILY(2, "interestRecalculationFrequencyType.daily"), //
    WEEKLY(3, "interestRecalculationFrequencyType.weekly"), //
    MONTHLY(4, "interestRecalculationFrequencyType.monthly");

    private final Integer value;
    private final String code;
    private static final Map<Integer, RecalculationFrequencyType> intToEnumMap = new HashMap<>();

    static {
        for (final RecalculationFrequencyType type : RecalculationFrequencyType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static RecalculationFrequencyType fromInt(final Integer ruleTypeValue) {
        if (ruleTypeValue == null) { return RecalculationFrequencyType.INVALID; }
        final RecalculationFrequencyType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private RecalculationFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isSameAsRepayment() {
        return this.value.equals(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.getValue());
    }

    public boolean isDaily() {
        return this.value.equals(RecalculationFrequencyType.DAILY.getValue());
    }

    public boolean isWeekly() {
        return this.value.equals(RecalculationFrequencyType.WEEKLY.getValue());
    }

    public boolean isMonthly() {
        return this.value.equals(RecalculationFrequencyType.MONTHLY.getValue());
    }

    public boolean isSameFrequency(final PeriodFrequencyType frequencyType) {
        boolean isSameFre = false;
        switch (this) {
            case DAILY:
                isSameFre = frequencyType.isDaily();
            break;
            case MONTHLY:
                isSameFre = frequencyType.isMonthly();
            break;
            case WEEKLY:
                isSameFre = frequencyType.isWeekly();
            break;
            default:
            break;
        }

        return isSameFre;
    }
}
