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
 * An enumeration of supported calendar periods used in savings.
 */
public enum SavingsPeriodFrequencyType {
    DAYS(0, "savingsPeriodFrequencyType.days"), //
    WEEKS(1, "savingsPeriodFrequencyType.weeks"), //
    MONTHS(2, "savingsPeriodFrequencyType.months"), //
    YEARS(3, "savingsPeriodFrequencyType.years"), //
    INVALID(4, "savingsPeriodFrequencyType.invalid");

    private final Integer value;
    private final String code;

    private SavingsPeriodFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static SavingsPeriodFrequencyType fromInt(final Integer type) {
        SavingsPeriodFrequencyType repaymentFrequencyType = SavingsPeriodFrequencyType.INVALID;
        if (type != null) {
            switch (type) {
                case 0:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.DAYS;
                break;
                case 1:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.WEEKS;
                break;
                case 2:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.MONTHS;
                break;
                case 3:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.YEARS;
                break;
            }
        }
        return repaymentFrequencyType;
    }

    public boolean isInvalid() {
        return this.value.equals(SavingsPeriodFrequencyType.INVALID.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final SavingsPeriodFrequencyType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}
