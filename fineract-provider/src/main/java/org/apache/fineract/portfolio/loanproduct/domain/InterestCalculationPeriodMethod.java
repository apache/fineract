/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

public enum InterestCalculationPeriodMethod {
    DAILY(0, "interestCalculationPeriodType.daily"), //
    SAME_AS_REPAYMENT_PERIOD(1, "interestCalculationPeriodType.same.as.repayment.period"), //
    INVALID(2, "interestCalculationPeriodType.invalid");

    private final Integer value;
    private final String code;

    private InterestCalculationPeriodMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static InterestCalculationPeriodMethod fromInt(final Integer selectedMethod) {

        InterestCalculationPeriodMethod repaymentMethod = null;
        switch (selectedMethod) {
            case 0:
                repaymentMethod = InterestCalculationPeriodMethod.DAILY;
            break;
            case 1:
                repaymentMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
            break;
            default:
                repaymentMethod = InterestCalculationPeriodMethod.INVALID;
            break;
        }
        return repaymentMethod;
    }

    public boolean isDaily() {
        return this.value.equals(InterestCalculationPeriodMethod.DAILY.getValue());
    }

}