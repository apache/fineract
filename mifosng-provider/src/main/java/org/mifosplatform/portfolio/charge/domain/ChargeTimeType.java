/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

public enum ChargeTimeType {

    INVALID(0, "chargeTimeType.invalid"), //
    DISBURSEMENT(1, "chargeTimeType.disbursement"), //only for loan charges
    SPECIFIED_DUE_DATE(2, "chargeTimeType.specifiedDueDate"), // for loan and savings charges
    SAVINGS_ACTIVATION(3, "chargeTimeType.savingsActivation"), // only for savings
    SAVINGS_CLOSURE(4, "chargeTimeType.savingsClosure"), // only for savings
    WITHDRAWAL_FEE(5, "chargeTimeType.withdrawalFee"), // only for savings
    ANNUAL_FEE(6, "chargeTimeType.annualFee"), // only for savings
    MONTHLY_FEE(7, "chargeTimeType.monthlyFee"); // only for savings

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
        int max = 1;// Default it to one (1)
        for (ChargeTimeType chargeTimeType : ChargeTimeType.values()) {
            max = (chargeTimeType.getValue() > max) ? chargeTimeType.getValue() : max;
        }
        return max;
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
                chargeTimeType = SAVINGS_ACTIVATION;
            break;
            case 4:
                chargeTimeType = SAVINGS_CLOSURE;
            break;
            case 5:
                chargeTimeType = WITHDRAWAL_FEE;
            break;
            case 6:
                chargeTimeType = ANNUAL_FEE;
            break;
            case 7:
                chargeTimeType = MONTHLY_FEE;
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

    public boolean isSavingsActivation() {
        return this.value.equals(ChargeTimeType.SAVINGS_ACTIVATION.getValue());
    }

    public boolean isSavingsClosure() {
        return this.value.equals(ChargeTimeType.SAVINGS_CLOSURE.getValue());
    }
    
    public boolean isWithdrawalFee() {
        return this.value.equals(ChargeTimeType.WITHDRAWAL_FEE.getValue());
    }
    
    public boolean isAnnualFee() {
        return this.value.equals(ChargeTimeType.ANNUAL_FEE.getValue());
    }

    public boolean isMonthlyFee() {
        return this.value.equals(ChargeTimeType.MONTHLY_FEE.getValue());
    }
    
    public boolean isAllowedLoanChargeTime() {
        return isTimeOfDisbursement() || isOnSpecifiedDueDate();
    }

    public boolean isAllowedSavingsChargeTime() {
        return isOnSpecifiedDueDate() || isSavingsActivation() || isSavingsClosure() || isWithdrawalFee() || isAnnualFee()
                || isMonthlyFee();
    }
}