package org.mifosng.platform.charge.domain;

public enum ChargeTimeType {
    DISBURSEMENT(0, "chargeTimeType.disbursement"),
    INVALID(1, "chargeTimeType.invalid");

    private final Integer value;
    private final String code;

    private ChargeTimeType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static ChargeTimeType fromInt(final Integer chargeTime) {
        ChargeTimeType chargeTimeType = null;
        switch(chargeTime){
            case 0:
                chargeTimeType = DISBURSEMENT;
                break;
            default:
                chargeTimeType = INVALID;
                break;
        }
        return chargeTimeType;
    }
}
