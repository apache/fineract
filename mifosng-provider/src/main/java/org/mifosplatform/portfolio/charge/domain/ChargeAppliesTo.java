/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

public enum ChargeAppliesTo {

    INVALID(0, "chargeAppliesTo.invalid"), //
    LOAN(1, "chargeAppliesTo.loan"), //
    SAVINGS(2, "chargeAppliesTo.savings");

    private final Integer value;
    private final String code;

    private ChargeAppliesTo(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static ChargeAppliesTo fromInt(final Integer chargeAppliesTo) {
        ChargeAppliesTo chargeAppliesToType = ChargeAppliesTo.INVALID;

        if (chargeAppliesTo != null) {
            switch (chargeAppliesTo) {
                case 1:
                    chargeAppliesToType = LOAN;
                break;
                case 2:
                    chargeAppliesToType = SAVINGS;
                break;
                default:
                    chargeAppliesToType = INVALID;
                break;
            }
        }

        return chargeAppliesToType;
    }

    public boolean isLoanCharge() {
        return this.value.equals(ChargeAppliesTo.LOAN.getValue());
    }

    public boolean isSavingsCharge() {
        return this.value.equals(ChargeAppliesTo.SAVINGS.getValue());
    }

    public static Object[] validValues() {
        return new Object[] { ChargeAppliesTo.LOAN.getValue(), ChargeAppliesTo.SAVINGS.getValue() };
    }
}