package org.mifosplatform.portfolio.loanaccount.domain;

public enum LoanTermVariationType {

    INVALID(0, "loanTermType.invalid"), //
    EMI_AMOUNT(1, "loanTermType.emiAmount"); //

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
        }
        return enumeration;
    }

    public boolean isEMIAmountVariation() {
        return this.value.equals(LoanTermVariationType.EMI_AMOUNT.getValue());
    }
}
