package org.mifosng.platform.charge.domain;

public enum ChargeTimeType {
	
    INVALID(0, "chargeTimeType.invalid"),
    DISBURSEMENT(1, "chargeTimeType.disbursement");

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
        ChargeTimeType chargeTimeType = ChargeTimeType.INVALID;
        switch(chargeTime){
            case 1:
                chargeTimeType = DISBURSEMENT;
                break;
            default:
                chargeTimeType = INVALID;
                break;
        }
        return chargeTimeType;
    }
}