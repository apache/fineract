/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

public enum LoanTermVariationType {

    INVALID(0, "loanTermType.invalid"), //
    EMI_AMOUNT(1, "loanTermType.emiAmount"), //
    INTEREST_RATE(2, "loanTermType.interestRate"), //
    PRINCIPAL_AMOUNT(3, "loanTermType.principalAmount"), //
    DUE_DATE(4, "loanTermType.dueDate"), //
    INSERT_INSTALLMENT(5, "loanTermType.insertInstallment"), //
    DELETE_INSTALLMENT(6, "loanTermType.deleteInstallment");

    private final Integer value;
    private final String code;

    private LoanTermVariationType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static LoanTermVariationType fromInt(final Integer value) {

        LoanTermVariationType enumeration = LoanTermVariationType.INVALID;
        switch (value) {
            case 1:
                enumeration = LoanTermVariationType.EMI_AMOUNT;
            break;
            case 2:
                enumeration = LoanTermVariationType.INTEREST_RATE;
            break;
            case 3:
                enumeration = LoanTermVariationType.PRINCIPAL_AMOUNT;
            break;
            case 4:
                enumeration = LoanTermVariationType.DUE_DATE;
            break;
            case 5:
                enumeration = LoanTermVariationType.INSERT_INSTALLMENT;
            break;
            case 6:
                enumeration = LoanTermVariationType.DELETE_INSTALLMENT;
            break;
        }
        return enumeration;
    }

    public boolean isEMIAmountVariation() {
        return this.value.equals(LoanTermVariationType.EMI_AMOUNT.getValue());
    }

    public boolean isInterestRateVariation() {
        return this.value.equals(LoanTermVariationType.INTEREST_RATE.getValue());
    }

    public boolean isPrincipalAmountVariation() {
        return this.value.equals(LoanTermVariationType.PRINCIPAL_AMOUNT.getValue());
    }

    public boolean isDueDateVariation() {
        return this.value.equals(LoanTermVariationType.DUE_DATE.getValue());
    }

    public boolean isInsertInstallment() {
        return this.value.equals(LoanTermVariationType.INSERT_INSTALLMENT.getValue());
    }

    public boolean isDeleteInstallment() {
        return this.value.equals(LoanTermVariationType.DELETE_INSTALLMENT.getValue());
    }
}
