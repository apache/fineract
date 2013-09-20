/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

public enum InterestMethod {
    DECLINING_BALANCE(0, "interestType.declining.balance"), FLAT(1, "interestType.flat"), INVALID(2, "interestType.invalid");

    private final Integer value;
    private final String code;

    private InterestMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static InterestMethod fromInt(final Integer selectedMethod) {

        InterestMethod repaymentMethod = null;
        switch (selectedMethod) {
            case 0:
                repaymentMethod = InterestMethod.DECLINING_BALANCE;
            break;
            case 1:
                repaymentMethod = InterestMethod.FLAT;
            break;
            default:
                repaymentMethod = InterestMethod.INVALID;
            break;
        }
        return repaymentMethod;
    }
}