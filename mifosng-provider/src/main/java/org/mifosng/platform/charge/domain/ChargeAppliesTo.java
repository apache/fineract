package org.mifosng.platform.charge.domain;

public enum ChargeAppliesTo {
    LOAN(0, "chargeAppliesTo.loan"),
    INVALID(1, "chargeAppliesTo.invalid");

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

    public static ChargeAppliesTo fromInt(final Integer chargeAppliesTo){
        ChargeAppliesTo chargeAppliesToType = null;
        switch(chargeAppliesTo){
            case 0:
                chargeAppliesToType = LOAN;
                break;
            default:
                chargeAppliesToType = INVALID;
                break;
        }
        return chargeAppliesToType;
    }
}
