/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.common;

import java.util.HashMap;
import java.util.Map;

public enum AccountingRuleType {

    NONE(1, "accountingRuleType.none"), //
    CASH_BASED(2, "accountingRuleType.cash"), //
    ACCRUAL_PERIODIC(3, "accountingRuleType.accrual.periodic"), //
    ACCRUAL_UPFRONT(4, "accountingRuleType.accrual.upfront"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, AccountingRuleType> intToEnumMap = new HashMap<>();
    static {
        for (final AccountingRuleType type : AccountingRuleType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static AccountingRuleType fromInt(final Integer ruleTypeValue) {
        final AccountingRuleType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private AccountingRuleType(final Integer value, final String code) {
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
}