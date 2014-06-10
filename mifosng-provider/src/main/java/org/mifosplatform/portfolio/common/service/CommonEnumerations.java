package org.mifosplatform.portfolio.common.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.ConditionType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

public class CommonEnumerations {

    public static EnumOptionData termFrequencyType(final int id, final String codePrefix) {
        return termFrequencyType(PeriodFrequencyType.fromInt(id), codePrefix);
    }

    public static EnumOptionData termFrequencyType(final PeriodFrequencyType type, final String codePrefix) {
        EnumOptionData optionData = null;
        switch (type) {
            case DAYS:
                optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.YEARS.getCode(), "Years");
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
                optionData = new EnumOptionData(ConditionType.NOT_EQUAL.getValue().longValue(), codePrefix
                        + ConditionType.NOT_EQUAL.getCode(), "notEqual");
            break;
            case GRETERTHAN:
                optionData = new EnumOptionData(ConditionType.GRETERTHAN.getValue().longValue(), codePrefix
                        + ConditionType.GRETERTHAN.getCode(), "greterthan");
            break;
            case LESSTHAN:
                optionData = new EnumOptionData(ConditionType.LESSTHAN.getValue().longValue(), codePrefix
                        + ConditionType.LESSTHAN.getCode(), "lessthan");
            break;
            default:
                optionData = new EnumOptionData(ConditionType.INVALID.getValue().longValue(), ConditionType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static List<EnumOptionData> conditionType(final ConditionType[] conditionTypes, final String codePrefix) {
        final List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (final ConditionType conditionType : conditionTypes) {
            if (!conditionType.isInvalid()) {
                optionDatas.add(conditionType(conditionType, codePrefix));
            }
        }
        return optionDatas;
    }

}
