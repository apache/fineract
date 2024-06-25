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

import java.time.LocalDate;
import java.util.Arrays;

/**
 *
 * People typically use either of the following settings when calculating there interest using the daily method:
 * <ul>
 * <li>Actual or</li>
 * <li>360 or</li>
 * <li>364 or</li>
 * <li>365</li>
 * </ul>
 */
public enum DaysInYearType {

    INVALID(0, "DaysInYearType.invalid"), //
    ACTUAL(1, "DaysInYearType.actual"), //
    DAYS_360(360, "DaysInYearType.days360"), //
    DAYS_364(364, "DaysInYearType.days364"), //
    DAYS_365(365, "DaysInYearType.days365");

    private final Integer value;
    private final String code;

    DaysInYearType(final Integer value, final String code) {
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

    public static DaysInYearType fromInt(final Integer type) {
        DaysInYearType repaymentFrequencyType = DaysInYearType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    repaymentFrequencyType = DaysInYearType.ACTUAL;
                break;
                case 360:
                    repaymentFrequencyType = DaysInYearType.DAYS_360;
                break;
                case 364:
                    repaymentFrequencyType = DaysInYearType.DAYS_364;
                break;
                case 365:
                    repaymentFrequencyType = DaysInYearType.DAYS_365;
                break;
            }
        }
        return repaymentFrequencyType;
    }

    public boolean isActual() {
        return DaysInYearType.ACTUAL.getValue().equals(this.value);
    }

    public Integer getNumberOfDays(final LocalDate referenceDate) {
        if (referenceDate == null) {
            return null;
        }
        return this == ACTUAL ? referenceDate.lengthOfYear() : this.getValue();
    }
}
