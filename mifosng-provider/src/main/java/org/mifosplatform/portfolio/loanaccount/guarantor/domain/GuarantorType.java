package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

import java.util.HashMap;
import java.util.Map;

public enum GuarantorType {
    CUSTOMER(1, "guarantor.existing.customer"), STAFF(2, "guarantor.staff"), EXTERNAL(3, "guarantor.external");

    private final Integer value;
    private final String code;

    private GuarantorType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    private static final Map<Integer, GuarantorType> intToEnumMap = new HashMap<Integer, GuarantorType>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (GuarantorType type : GuarantorType.values()) {
            if (i == 0) {
                minValue = type.value;
            }
            intToEnumMap.put(type.value, type);
            if (minValue >= type.value) {
                minValue = type.value;
            }
            if (maxValue < type.value) {
                maxValue = type.value;
            }
            i = i + 1;
        }
    }

    public static GuarantorType fromInt(int i) {
        GuarantorType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public static int getMinValue() {
        return minValue;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }

}
