/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

/**
 * <p>
 * The interest period is the span of time at the end of which savings in a
 * client’s account earn interest.
 * </p>
 * 
 * <p>
 * Interest periods may differ for different types of savings products. In all
 * cases, savings accounts must have established and published interest periods.
 * </p>
 */
public enum SavingsInterestPeriodType {

    INVALID(0, "savingsCalculationPeriodType.invalid"), //
    DAILY(1, "savingsCalculationPeriodType.daily"), //
    WEEKLY(2, "savingsCalculationPeriodType.weekly"), //
    BIWEEKLY(3, "savingsCalculationPeriodType.biweekly"), //
    MONTHLY(4, "savingsCalculationPeriodType.monthly"), //
    QUATERLY(5, "savingsCalculationPeriodType.quarterly"), //
    SEMIANNUAL(6, "savingsCalculationPeriodType.semiannual"), //
    ANNUAL(7, "savingsCalculationPeriodType.annual");

    private final Integer value;
    private final String code;

    private SavingsInterestPeriodType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public static SavingsInterestPeriodType fromInt(final Integer type) {
        SavingsInterestPeriodType repaymentFrequencyType = SavingsInterestPeriodType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    repaymentFrequencyType = SavingsInterestPeriodType.DAILY;
                break;
                case 2:
                    repaymentFrequencyType = SavingsInterestPeriodType.WEEKLY;
                break;
                case 3:
                    repaymentFrequencyType = SavingsInterestPeriodType.BIWEEKLY;
                break;
                case 4:
                    repaymentFrequencyType = SavingsInterestPeriodType.MONTHLY;
                break;
                case 5:
                    repaymentFrequencyType = SavingsInterestPeriodType.QUATERLY;
                break;
                case 6:
                    repaymentFrequencyType = SavingsInterestPeriodType.SEMIANNUAL;
                break;
                case 7:
                    repaymentFrequencyType = SavingsInterestPeriodType.ANNUAL;
                break;
            }
        }
        return repaymentFrequencyType;
    }
}