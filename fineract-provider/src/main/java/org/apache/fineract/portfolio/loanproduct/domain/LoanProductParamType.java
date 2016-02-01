/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

public enum LoanProductParamType {

    INVALID(0, "LoanProductParamType.invalid"), //
    PRINCIPAL(1, "LoanProductParamType.principal"), //
    INTERESTRATE(2, "LoanProductParamType.interestrate"), //
    REPAYMENT(3, "LoanProductParamType.repayment"); //

    private final Integer value;
    private final String code;

    private LoanProductParamType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static LoanProductParamType fromInt(final Integer chargeTime) {
        LoanProductParamType loanProductParamType = LoanProductParamType.INVALID;
        if (chargeTime != null) {
            switch (chargeTime) {
                case 1:
                    loanProductParamType = PRINCIPAL;
                break;
                case 2:
                    loanProductParamType = INTERESTRATE;
                break;
                case 3:
                    loanProductParamType = REPAYMENT;
                break;
                default:
                    loanProductParamType = INVALID;
                break;
            }
        }
        return loanProductParamType;
    }

    public boolean isParamTypePrincipal() {
        return LoanProductParamType.PRINCIPAL.getValue().equals(this.value);
    }

    public boolean isParamTypeInterestTate() {
        return LoanProductParamType.INTERESTRATE.getValue().equals(this.value);
    }

    public boolean isParamTypeRepayment() {
        return LoanProductParamType.REPAYMENT.getValue().equals(this.value);
    }

}