/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

/**
 * The interest posting period is the span of time at the end of which savings
 * earned but not yet credited/posted in a client's account is credited/posted.
 */
public enum SavingsInterestPostingPeriodType {

    INVALID(0, "savingsInterestPostingPeriodType.invalid"), //
    MONTHLY(4, "savingsInterestPostingPeriodType.monthly"), //
    QUATERLY(5, "savingsInterestPostingPeriodType.quarterly"), //
    BI_ANNUAL(6, "savingsInterestPostingPeriodType.biannual"), //
    ANNUAL(7, "savingsInterestPostingPeriodType.annual");

    private final Integer value;
    private final String code;

    private SavingsInterestPostingPeriodType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public static SavingsInterestPostingPeriodType fromInt(final Integer type) {
        SavingsInterestPostingPeriodType repaymentFrequencyType = SavingsInterestPostingPeriodType.INVALID;
        if (type != null) {
            switch (type) {
                case 4:
                    repaymentFrequencyType = SavingsInterestPostingPeriodType.MONTHLY;
                break;
                case 5:
                    repaymentFrequencyType = SavingsInterestPostingPeriodType.QUATERLY;
                break;
                case 6:
                    repaymentFrequencyType = SavingsInterestPostingPeriodType.BI_ANNUAL;
                break;
                case 7:
                    repaymentFrequencyType = SavingsInterestPostingPeriodType.ANNUAL;
                break;
            }
        }
        return repaymentFrequencyType;
    }
}
