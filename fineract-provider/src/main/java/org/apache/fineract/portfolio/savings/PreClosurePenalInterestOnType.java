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
public enum PreClosurePenalInterestOnType {
    INVALID(0, "preClosurePenalInterestOnType.invalid"), WHOLE_TERM(1, "preClosurePenalInterestOnType.wholeTerm"), //
    TILL_PREMATURE_WITHDRAWAL(2, "preClosurePenalInterestOnType.tillPrematureWithdrawal"); //

    private final Integer value;
    private final String code;

    private PreClosurePenalInterestOnType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static PreClosurePenalInterestOnType fromInt(final Integer type) {
        PreClosurePenalInterestOnType penalInterestType = PreClosurePenalInterestOnType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    penalInterestType = PreClosurePenalInterestOnType.WHOLE_TERM;
                break;
                case 2:
                    penalInterestType = PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL;
                break;
            }
        }
        return penalInterestType;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final PreClosurePenalInterestOnType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public boolean isInvalid() {
        return this.value.equals(PreClosurePenalInterestOnType.INVALID.value);
    }

    public boolean isWholeTerm() {
        return this.value.equals(PreClosurePenalInterestOnType.WHOLE_TERM.getValue());
    }

    public boolean isTillPrematureWithdrawal() {
        return this.value.equals(PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL.getValue());
    }
}
