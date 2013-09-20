/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

public enum ChargeTimeType {

    INVALID(0, "chargeTimeType.invalid"), //
    DISBURSEMENT(1, "chargeTimeType.disbursement"), //
    SPECIFIED_DUE_DATE(2, "chargeTimeType.specifiedDueDate"), MONTHLY(3, "chargeTimeType.monthly"), YEARLY(4, "chargeTimeType.yearly");

    private final Integer value;
    private final String code;

    private ChargeTimeType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static int minValue() {
        return 1;
    }

    public static int maxValue() {
        return 4;
    }

    public static ChargeTimeType fromInt(final Integer chargeTime) {
        ChargeTimeType chargeTimeType = ChargeTimeType.INVALID;
        switch (chargeTime) {
            case 1:
                chargeTimeType = DISBURSEMENT;
            break;
            case 2:
                chargeTimeType = SPECIFIED_DUE_DATE;
            break;
            case 3:
                chargeTimeType = MONTHLY;
            break;
            case 4:
                chargeTimeType = YEARLY;
            break;
            default:
                chargeTimeType = INVALID;
            break;
        }
        return chargeTimeType;
    }

    public boolean isTimeOfDisbursement() {
        return ChargeTimeType.DISBURSEMENT.getValue().equals(this.value);
    }

    public boolean isOnSpecifiedDueDate() {
        return this.value.equals(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
    }

    public boolean isMonthly() {
        return this.value.equals(ChargeTimeType.MONTHLY.getValue());
    }

    public boolean isYearly() {
        return this.value.equals(ChargeTimeType.YEARLY.getValue());
    }

    public boolean isAllowedLoanChargeTime() {
        return isTimeOfDisbursement() || isOnSpecifiedDueDate();
    }

    public boolean isAllowedSavingsChargeTime() {
        return isOnSpecifiedDueDate() || isMonthly() || isYearly();
    }
}