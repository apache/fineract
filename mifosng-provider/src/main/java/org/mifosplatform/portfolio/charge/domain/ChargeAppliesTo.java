/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

public enum ChargeAppliesTo {

    INVALID(0, "chargeAppliesTo.invalid"), //
    LOAN(1, "chargeAppliesTo.loan");

    private final Integer value;
    private final String code;

    private ChargeAppliesTo(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static ChargeAppliesTo fromInt(final Integer chargeAppliesTo) {
        ChargeAppliesTo chargeAppliesToType = ChargeAppliesTo.INVALID;
        switch (chargeAppliesTo) {
            case 1:
                chargeAppliesToType = LOAN;
            break;
            default:
                chargeAppliesToType = INVALID;
            break;
        }
        return chargeAppliesToType;
    }
}