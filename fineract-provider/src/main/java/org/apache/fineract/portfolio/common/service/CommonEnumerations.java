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
package org.apache.fineract.portfolio.common.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.common.domain.ConditionType;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public final class CommonEnumerations {

    private CommonEnumerations() {

    }

    public static final String DAYS_IN_MONTH_TYPE = "daysInMonthType";
    public static final String DAYS_IN_YEAR_TYPE = "daysInYearType";

    public static EnumOptionData commonEnumueration(final String typeName, final int id) {
        EnumOptionData enumData = null;
        if (typeName.equals(DAYS_IN_MONTH_TYPE)) {
            enumData = daysInMonthType(id);
        } else if (typeName.equals(DAYS_IN_YEAR_TYPE)) {
            enumData = daysInYearType(id);
        }
        return enumData;
    }

    public static EnumOptionData termFrequencyType(final int id, final String codePrefix) {
        return termFrequencyType(PeriodFrequencyType.fromInt(id), codePrefix);
    }

    public static EnumOptionData termFrequencyType(final PeriodFrequencyType type, final String codePrefix) {
        EnumOptionData optionData = null;
        switch (type) {
            case DAYS:
                optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                        codePrefix + PeriodFrequencyType.YEARS.getCode(), "Years");
            break;
            default:
                optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData conditionType(final int id, final String codePrefix) {
        return conditionType(ConditionType.fromInt(id), codePrefix);
    }

    public static EnumOptionData conditionType(final ConditionType type, final String codePrefix) {
        EnumOptionData optionData = null;
        switch (type) {
            case EQUAL:
                optionData = new EnumOptionData(ConditionType.EQUAL.getValue().longValue(), codePrefix + ConditionType.EQUAL.getCode(),
                        "equal");
            break;
            case NOT_EQUAL:
                optionData = new EnumOptionData(ConditionType.NOT_EQUAL.getValue().longValue(),
                        codePrefix + ConditionType.NOT_EQUAL.getCode(), "notEqual");
            break;
            case GRETERTHAN:
                optionData = new EnumOptionData(ConditionType.GRETERTHAN.getValue().longValue(),
                        codePrefix + ConditionType.GRETERTHAN.getCode(), "greterthan");
            break;
            case LESSTHAN:
                optionData = new EnumOptionData(ConditionType.LESSTHAN.getValue().longValue(),
                        codePrefix + ConditionType.LESSTHAN.getCode(), "lessthan");
            break;
            default:
                optionData = new EnumOptionData(ConditionType.INVALID.getValue().longValue(), ConditionType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> conditionType(final ConditionType[] conditionTypes, final String codePrefix) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final ConditionType conditionType : conditionTypes) {
            if (!conditionType.isInvalid()) {
                optionDatas.add(conditionType(conditionType, codePrefix));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData daysInMonthType(final int id) {
        return daysInMonthType(DaysInMonthType.fromInt(id));
    }

    public static EnumOptionData daysInMonthType(final DaysInMonthType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case ACTUAL:
                optionData = new EnumOptionData(DaysInMonthType.ACTUAL.getValue().longValue(), DaysInMonthType.ACTUAL.getCode(), "Actual");
            break;
            case DAYS_30:
                optionData = new EnumOptionData(DaysInMonthType.DAYS_30.getValue().longValue(), DaysInMonthType.DAYS_30.getCode(),
                        "30 Days");
            break;
            default:
                optionData = new EnumOptionData(DaysInMonthType.INVALID.getValue().longValue(), DaysInMonthType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData daysInYearType(final int id) {
        return daysInYearType(DaysInYearType.fromInt(id));
    }

    public static EnumOptionData daysInYearType(final DaysInYearType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case ACTUAL:
                optionData = new EnumOptionData(DaysInYearType.ACTUAL.getValue().longValue(), DaysInYearType.ACTUAL.getCode(), "Actual");
            break;
            case DAYS_360:
                optionData = new EnumOptionData(DaysInYearType.DAYS_360.getValue().longValue(), DaysInYearType.DAYS_360.getCode(),
                        "360 Days");
            break;
            case DAYS_364:
                optionData = new EnumOptionData(DaysInYearType.DAYS_364.getValue().longValue(), DaysInYearType.DAYS_364.getCode(),
                        "364 Days");
            break;
            case DAYS_365:
                optionData = new EnumOptionData(DaysInYearType.DAYS_365.getValue().longValue(), DaysInYearType.DAYS_365.getCode(),
                        "365 Days");
            break;
            default:
                optionData = new EnumOptionData(DaysInYearType.INVALID.getValue().longValue(), DaysInYearType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

}
