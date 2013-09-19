/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

public enum ChargeAppliesTo {

    INVALID(0, "chargeAppliesTo.invalid"), //
    LOAN(1, "chargeAppliesTo.loan"),
    SAVINGS(2, "chargeAppliesTo.savings");

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

    public static int minValue(){
        return 1;
    }
    
    public static int maxValue(){
        return 2;//modify whenever a new value added
    }
    
    public static ChargeAppliesTo fromInt(final Integer chargeAppliesTo) {
        ChargeAppliesTo chargeAppliesToType = ChargeAppliesTo.INVALID;
        switch (chargeAppliesTo) {
            case 1:
                chargeAppliesToType = LOAN;
            break;
            case 2:
                chargeAppliesToType = SAVINGS;
            break;
            default:
                chargeAppliesToType = INVALID;
            break;
        }
        return chargeAppliesToType;
    }
    
    public boolean isLoanCharge(){
        return this.value.equals(ChargeAppliesTo.LOAN.getValue());
    }
    
    public boolean isSavingsCharge(){
        return this.value.equals(ChargeAppliesTo.SAVINGS.getValue());
    }
}