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
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * People typically use either of the following settings when calculating there
 * interest using the daily method:
 * <ul>
 * <li>Actual or</li>
 * <li>30</li>
 * </ul>
 */
public enum DaysInMonthType {

    INVALID(0, "DaysInMonthType.invalid"), //
    ACTUAL(1, "DaysInMonthType.actual"), //
    DAYS_30(30, "DaysInMonthType.days360");

    private final Integer value;
    private final String code;

    private DaysInMonthType(final Integer value, final String code) {
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
        for (final DaysInMonthType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static DaysInMonthType fromInt(final Integer type) {
        DaysInMonthType repaymentFrequencyType = DaysInMonthType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    repaymentFrequencyType = DaysInMonthType.ACTUAL;
                break;
                case 30:
                    repaymentFrequencyType = DaysInMonthType.DAYS_30;
                break;

            }
        }
        return repaymentFrequencyType;
    }

    public boolean isDaysInMonth_30() {
        return DaysInMonthType.DAYS_30.getValue().equals(this.value);
    }
}