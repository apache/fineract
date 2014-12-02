/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.domain;

import java.util.HashMap;
import java.util.Map;

public enum EntityAccountType {
    CLIENT(1, "accountType.client"), LOAN(2, "accountType.loan"), SAVINGS(3, "accountType.savings");

    private final Integer value;
    private final String code;

    private EntityAccountType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, EntityAccountType> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final EntityAccountType type : EntityAccountType.values()) {
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

    public static EntityAccountType fromInt(final int i) {
        final EntityAccountType type = intToEnumMap.get(Integer.valueOf(i));
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

    public boolean isClientAccount() {
        return this.value.equals(EntityAccountType.CLIENT.getValue());
    }

    public boolean isLoanAccount() {
        return this.value.equals(EntityAccountType.LOAN.getValue());
    }

    public boolean isSavingsAccount() {
        return this.value.equals(EntityAccountType.SAVINGS.getValue());
    }

}
