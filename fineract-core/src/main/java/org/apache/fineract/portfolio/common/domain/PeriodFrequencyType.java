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
package org.apache.fineract.portfolio.common.domain;

import java.util.Arrays;

public enum PeriodFrequencyType {

    DAYS(0, "periodFrequencyType.days"), //
    WEEKS(1, "periodFrequencyType.weeks"), //
    MONTHS(2, "periodFrequencyType.months"), //
    YEARS(3, "periodFrequencyType.years"), //
    WHOLE_TERM(4, "periodFrequencyType.whole_term"), //
    INVALID(5, "periodFrequencyType.invalid");

    private final Integer value;
    private final String code;

    PeriodFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static PeriodFrequencyType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 0:
                return DAYS;
            case 1:
                return WEEKS;
            case 2:
                return MONTHS;
            case 3:
                return YEARS;
            case 4:
                return WHOLE_TERM;
            default:
                return INVALID;
        }
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isMonthly() {
        return this.equals(MONTHS);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isYearly() {
        return this.equals(YEARS);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isWeekly() {
        return this.equals(WEEKS);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isDaily() {
        return this.equals(DAYS);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isWholeTerm() {
        return this.equals(WHOLE_TERM);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isInvalid() {
        return this.equals(INVALID);
    }

    // TODO: do we really need this?!?
    public static Object[] integerValues() {
        return Arrays.stream(values()).filter(value -> !INVALID.equals(value)).map(value -> value.value).toList().toArray();
    }
}
