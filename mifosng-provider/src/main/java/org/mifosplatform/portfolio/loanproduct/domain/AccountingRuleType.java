package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

public enum AccountingRuleType {

    NONE(1), CASH_BASED(2), ACCRUAL_BASED(3);

    private final Integer value;

    private AccountingRuleType(final Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    private static final Map<Integer, AccountingRuleType> intToEnumMap = new HashMap<Integer, AccountingRuleType>();
    static {
        for (AccountingRuleType type : AccountingRuleType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static AccountingRuleType fromInt(int i) {
        AccountingRuleType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

}
