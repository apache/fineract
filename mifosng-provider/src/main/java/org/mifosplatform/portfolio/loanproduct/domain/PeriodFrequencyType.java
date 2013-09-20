/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

public enum PeriodFrequencyType {
    DAYS(0, "periodFrequencyType.days"), //
    WEEKS(1, "periodFrequencyType.weeks"), //
    MONTHS(2, "periodFrequencyType.months"), //
    YEARS(3, "periodFrequencyType.years"), //
    INVALID(4, "periodFrequencyType.invalid");

    private final Integer value;
    private final String code;

    private PeriodFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static PeriodFrequencyType fromInt(final Integer frequency) {
        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.INVALID;
        if (frequency != null) {
            switch (frequency) {
                case 0:
                    repaymentFrequencyType = PeriodFrequencyType.DAYS;
                break;
                case 1:
                    repaymentFrequencyType = PeriodFrequencyType.WEEKS;
                break;
                case 2:
                    repaymentFrequencyType = PeriodFrequencyType.MONTHS;
                break;
                case 3:
                    repaymentFrequencyType = PeriodFrequencyType.YEARS;
                break;
            }
        }
        return repaymentFrequencyType;
    }
}
