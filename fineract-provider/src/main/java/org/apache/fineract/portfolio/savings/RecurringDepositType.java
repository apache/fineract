/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration of supported calendar periods used in savings.
 */
public enum RecurringDepositType {
    INVALID(0, "recurringDepositType.invalid"), VOLUNTARY(1, "recurringDepositType.voluntary"), //
    MANDATORY(2, "recurringDepositType.mandatory"); //

    private final Integer value;
    private final String code;

    private RecurringDepositType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static RecurringDepositType fromInt(final Integer type) {
        RecurringDepositType rdType = RecurringDepositType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    rdType = RecurringDepositType.VOLUNTARY;
                break;
                case 2:
                    rdType = RecurringDepositType.MANDATORY;
                break;
            }
        }
        return rdType;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final RecurringDepositType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public boolean isInvalid() {
        return this.value.equals(RecurringDepositType.INVALID.value);
    }
}
