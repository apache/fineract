/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.ArrayList;
import java.util.List;

public enum SavingsWithdrawalFeesType {

    INVALID(0, "savingsWithdrawalFeesType.invalid"), //
    FLAT(1, "savingsWithdrawalFeesType.flat"), //
    PERCENT_OF_AMOUNT(2, "savingsWithdrawalFeesType.percent.of.amount");

    private final Integer value;
    private final String code;

    private SavingsWithdrawalFeesType(final Integer value, final String code) {
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
        for (final SavingsWithdrawalFeesType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static SavingsWithdrawalFeesType fromInt(final Integer type) {

        SavingsWithdrawalFeesType withdrawalFeeType = SavingsWithdrawalFeesType.INVALID;
        switch (type) {
            case 1:
                withdrawalFeeType = FLAT;
            break;
            case 2:
                withdrawalFeeType = PERCENT_OF_AMOUNT;
            break;
        }
        return withdrawalFeeType;
    }
}