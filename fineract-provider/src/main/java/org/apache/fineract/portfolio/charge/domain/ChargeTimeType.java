/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.charge.domain;

public enum ChargeTimeType {
    INVALID(0, "chargeTimeType.invalid"), //
    DISBURSEMENT(1, "chargeTimeType.disbursement"), // only for loan charges
    SPECIFIED_DUE_DATE(2, "chargeTimeType.specifiedDueDate"), // for loan and
    SAVINGS_ACTIVATION(3, "chargeTimeType.savingsActivation"), // only for
    SAVINGS_CLOSURE(4, "chargeTimeType.savingsClosure"), // only for savings
    WITHDRAWAL_FEE(5, "chargeTimeType.withdrawalFee"), // only for savings
    ANNUAL_FEE(6, "chargeTimeType.annualFee"), // only for savings
    MONTHLY_FEE(7, "chargeTimeType.monthlyFee"), // only for savings
    INSTALMENT_FEE(8, "chargeTimeType.instalmentFee"), // only for loan charges
    OVERDUE_INSTALLMENT(9, "chargeTimeType.overdueInstallment"), // only for
    OVERDRAFT_FEE(10, "chargeTimeType.overdraftFee"), // only for savings
    WEEKLY_FEE(11, "chargeTimeType.weeklyFee"), // only for savings
    TRANCHE_DISBURSEMENT(12, "chargeTimeType.tranchedisbursement"), // only for
                                                                    // loan
    SHAREACCOUNT_ACTIVATION(13, "chargeTimeType.activation"), // only for loan
    SHARE_PURCHASE(14, "chargeTimeType.sharespurchase"), 
    SHARE_REDEEM(15, "chargeTimeType.sharesredeem"),
    
    SAVINGS_NOACTIVITY_FEE(16,"chargeTimeType.savingsNoActivityFee");

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
                ChargeTimeType.INSTALMENT_FEE.getValue(), ChargeTimeType.OVERDUE_INSTALLMENT.getValue(),
                ChargeTimeType.TRANCHE_DISBURSEMENT.getValue() };
    }

    public static Object[] validLoanChargeValues() {
        return new Integer[] { ChargeTimeType.DISBURSEMENT.getValue(), ChargeTimeType.SPECIFIED_DUE_DATE.getValue(),
                ChargeTimeType.INSTALMENT_FEE.getValue() };
    }

    public static Object[] validSavingsValues() {
        return new Integer[] { ChargeTimeType.SPECIFIED_DUE_DATE.getValue(), ChargeTimeType.SAVINGS_ACTIVATION.getValue(),
                ChargeTimeType.SAVINGS_CLOSURE.getValue(), ChargeTimeType.WITHDRAWAL_FEE.getValue(), ChargeTimeType.ANNUAL_FEE.getValue(),
                ChargeTimeType.MONTHLY_FEE.getValue(), ChargeTimeType.OVERDRAFT_FEE.getValue(), ChargeTimeType.WEEKLY_FEE.getValue(),
                ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getValue()};
    }

    public static Object[] validClientValues() {
        return new Integer[] { ChargeTimeType.SPECIFIED_DUE_DATE.getValue() };
    }

    public static Object[] validShareValues() {
        return new Integer[] { ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue(), ChargeTimeType.SHARE_PURCHASE.getValue(), ChargeTimeType.SHARE_REDEEM.getValue() };
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
                case 12:
                    chargeTimeType = TRANCHE_DISBURSEMENT;
                break;
                case 13:
                    chargeTimeType = SHAREACCOUNT_ACTIVATION;
                break;
                case 14:
                    chargeTimeType = SHARE_PURCHASE;
                break;
                case 15:
                    chargeTimeType = SHARE_REDEEM;
                break;
                case 16:
                	chargeTimeType = SAVINGS_NOACTIVITY_FEE;
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
    
    public boolean isSavingsNoActivityFee() {
    	return this.value.equals(ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getValue());
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
        return isTimeOfDisbursement() || isOnSpecifiedDueDate() || isInstalmentFee() || isOverdueInstallment() || isTrancheDisbursement();
    }

    public boolean isAllowedClientChargeTime() {
        return isOnSpecifiedDueDate();
    }

    public boolean isAllowedSavingsChargeTime() {
        return isOnSpecifiedDueDate() || isSavingsActivation() || isSavingsClosure() || isWithdrawalFee() || isAnnualFee()
                || isMonthlyFee() || isWeeklyFee() || isOverdraftFee() || isSavingsNoActivityFee();
    }

    public boolean isOverdraftFee() {
        return this.value.equals(ChargeTimeType.OVERDRAFT_FEE.getValue());
    }

    public boolean isTrancheDisbursement() {
        return this.value.equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
    }

    public boolean isShareAccountActivation() {
        return this.value.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue());
    }

    public boolean isSharesPurchase() {
        return this.value.equals(ChargeTimeType.SHARE_PURCHASE.getValue());
    }

    public boolean isSharesRedeem() {
        return this.value.equals(ChargeTimeType.SHARE_REDEEM.getValue());
    }
}