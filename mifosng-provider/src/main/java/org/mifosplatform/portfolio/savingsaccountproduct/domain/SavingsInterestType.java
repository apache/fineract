package org.mifosplatform.portfolio.savingsaccountproduct.domain;

public enum SavingsInterestType {

    INVALID(0, "savingsInterestType.invalid"), SIMPLE(1, "savingsInterestType.simple"), COMPOUNDING(2, "savingsInterestType.compounding");

    private final Integer value;
    private final String code;

    private SavingsInterestType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static SavingsInterestType fromInt(final Integer frequency) {

        SavingsInterestType savingsInterestType = SavingsInterestType.INVALID;
        switch (frequency) {
            case 1:
                savingsInterestType = SavingsInterestType.SIMPLE;
            break;
            case 2:
                savingsInterestType = SavingsInterestType.COMPOUNDING;
            break;
            default:
                savingsInterestType = SavingsInterestType.INVALID;
            break;
        }
        return savingsInterestType;
    }

}
