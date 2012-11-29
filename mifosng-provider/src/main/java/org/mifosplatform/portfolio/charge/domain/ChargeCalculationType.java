package org.mifosplatform.portfolio.charge.domain;

public enum ChargeCalculationType {

    INVALID(0, "chargeCalculationType.invalid"), //
    FLAT(1, "chargeCalculationType.flat"), //
    PERCENT_OF_AMOUNT(2, "chargeCalculationType.percent.of.amount"), //
    PERCENT_OF_AMOUNT_AND_INTEREST(3, "chargeCalculationType.percent.of.amount.and.interest"), //
    PERCENT_OF_INTEREST(4, "chargeCalculationType.percent.of.interest");

    private final Integer value;
    private final String code;

    private ChargeCalculationType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static ChargeCalculationType fromInt(final Integer chargeCalculation) {
        ChargeCalculationType chargeCalculationType = ChargeCalculationType.INVALID;
        switch (chargeCalculation) {
            case 1:
                chargeCalculationType = FLAT;
            break;
            case 2:
                chargeCalculationType = PERCENT_OF_AMOUNT;
            break;
            case 3:
                chargeCalculationType = PERCENT_OF_AMOUNT_AND_INTEREST;
            break;
            case 4:
                chargeCalculationType = PERCENT_OF_INTEREST;
            break;
            default:
                chargeCalculationType = INVALID;
            break;
        }
        return chargeCalculationType;
    }
}