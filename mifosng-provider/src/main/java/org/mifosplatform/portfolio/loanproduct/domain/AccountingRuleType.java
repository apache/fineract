package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

public enum AccountingRuleType {

    NONE(1, "accountingRuleType.none"), CASH_BASED(2, "accountingRuleType.cash"), ACCRUAL_BASED(3, "accountingRuleType.accrual");

    private final Integer value;
    private final String code;

    private AccountingRuleType(final Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
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
