/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

public enum ChargeTimeType {

    INVALID(0, "chargeTimeType.invalid"), //
    DISBURSEMENT(1, "chargeTimeType.disbursement"), // only for loan charges
    SPECIFIED_DUE_DATE(2, "chargeTimeType.specifiedDueDate"), // for loan and
                                                              // savings charges
    SAVINGS_ACTIVATION(3, "chargeTimeType.savingsActivation"), // only for
                                                               // savings
    SAVINGS_CLOSURE(4, "chargeTimeType.savingsClosure"), // only for savings
    WITHDRAWAL_FEE(5, "chargeTimeType.withdrawalFee"), // only for savings
    ANNUAL_FEE(6, "chargeTimeType.annualFee"), // only for savings
    MONTHLY_FEE(7, "chargeTimeType.monthlyFee"), // only for savings
    INSTALMENT_FEE(8, "chargeTimeType.instalmentFee"), // only for loan charges
    OVERDUE_INSTALLMENT(9, "chargeTimeType.overdueInstallment"), // only for
                                                                 // loan charges
    OVERDRAFT_FEE(10, "chargeTimeType.overdraftFee"),// only for savings
    WEEKLY_FEE(11, "chargeTimeType.weeklyFee"); // only for savings

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

    public static Object[] validLoanValues() {
        return new Integer[] { ChargeTimeType.DISBURSEMENT.getValue(), ChargeTimeType.SPECIFIED_DUE_DATE.getValue(),
                ChargeTimeType.INSTALMENT_FEE.getValue(), ChargeTimeType.OVERDUE_INSTALLMENT.getValue() };
    }
    
    public static Object[] validLoanChargeValues() {
        return new Integer[] { ChargeTimeType.DISBURSEMENT.getValue(), ChargeTimeType.SPECIFIED_DUE_DATE.getValue(),
                ChargeTimeType.INSTALMENT_FEE.getValue()};
    }

    public static Object[] validSavingsValues() {
        return new Integer[] { ChargeTimeType.SPECIFIED_DUE_DATE.getValue(), ChargeTimeType.SAVINGS_ACTIVATION.getValue(),
                ChargeTimeType.SAVINGS_CLOSURE.getValue(), ChargeTimeType.WITHDRAWAL_FEE.getValue(), ChargeTimeType.ANNUAL_FEE.getValue(),
                ChargeTimeType.MONTHLY_FEE.getValue(), ChargeTimeType.OVERDRAFT_FEE.getValue(), ChargeTimeType.WEEKLY_FEE.getValue() };
    }

    public static ChargeTimeType fromInt(final Integer chargeTime) {
        ChargeTimeType chargeTimeType = ChargeTimeType.INVALID;
        if (chargeTime != null) {
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
                case 8:
                    chargeTimeType = INSTALMENT_FEE;
                break;
                case 9:
                    chargeTimeType = OVERDUE_INSTALLMENT;
                break;
                case 10:
                    chargeTimeType = OVERDRAFT_FEE;
                break;
                case 11:
                    chargeTimeType = WEEKLY_FEE;
                break;
                default:
                    chargeTimeType = INVALID;
                break;
            }
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
    
    public boolean isWeeklyFee() {
    	return this.value.equals(ChargeTimeType.WEEKLY_FEE.getValue());
    }
    
    public boolean isInstalmentFee() {
        return this.value.equals(ChargeTimeType.INSTALMENT_FEE.getValue());
    }

    public boolean isOverdueInstallment() {
        return this.value.equals(ChargeTimeType.OVERDUE_INSTALLMENT.getValue());
    }

    public boolean isAllowedLoanChargeTime() {
        return isTimeOfDisbursement() || isOnSpecifiedDueDate() || isInstalmentFee() || isOverdueInstallment();
    }

    public boolean isAllowedSavingsChargeTime() {
        return isOnSpecifiedDueDate() || isSavingsActivation() || isSavingsClosure() || isWithdrawalFee() || isAnnualFee()
                || isMonthlyFee() || isWeeklyFee() || isOverdraftFee();
    }

    public boolean isOverdraftFee() {
        return this.value.equals(ChargeTimeType.OVERDRAFT_FEE.getValue());
    }

}