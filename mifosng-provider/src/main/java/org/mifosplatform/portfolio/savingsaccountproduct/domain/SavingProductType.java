package org.mifosplatform.portfolio.savingsaccountproduct.domain;

public enum SavingProductType {

    INVALID(0, "savingProductType.invalid"), //
    RECURRING(1, "savingProductType.reccuring"), //
    REGULAR(2, "savingProductType.regular");

    private final Integer value;
    private final String code;

    private SavingProductType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static SavingProductType fromInt(final Integer frequency) {

        SavingProductType savingProductType = SavingProductType.INVALID;
        switch (frequency) {
            case 1:
                savingProductType = SavingProductType.RECURRING;
            break;
            case 2:
                savingProductType = SavingProductType.REGULAR;
            break;
            default:
                savingProductType = SavingProductType.INVALID;
            break;
        }
        return savingProductType;
    }

    public boolean isReccuring() {
        return this.value.equals(SavingProductType.RECURRING.getValue());
    }

    public boolean isRegular() {
        return this.value.equals(SavingProductType.REGULAR.getValue());
    }

}
