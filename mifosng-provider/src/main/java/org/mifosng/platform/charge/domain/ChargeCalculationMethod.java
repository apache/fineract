package org.mifosng.platform.charge.domain;

public enum ChargeCalculationMethod {
	
	INVALID(0, "chargeCalculationType.invalid"),
    FLAT(1, "chargeCalculationType.flat"),
    PERCENT_OF_AMOUNT(2, "chargeCalculationType.percent.of.amount"),
    PERCENT_OF_AMOUNT_AND_INTEREST(3, "chargeCalculationType.percent.of.amount.and.interest"),
    PERCENT_OF_INTEREST(4, "chargeCalculationType.percent.of.interest");
    
    private final Integer value;
    private final String code;

    private ChargeCalculationMethod(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static ChargeCalculationMethod fromInt(final Integer chargeCalculation){
        ChargeCalculationMethod chargeCalculationMethod = ChargeCalculationMethod.INVALID;
        switch(chargeCalculation){
            case 1:
                chargeCalculationMethod = FLAT;
                break;
            case 2:
                chargeCalculationMethod = PERCENT_OF_AMOUNT;
                break;
            case 3:
                chargeCalculationMethod = PERCENT_OF_AMOUNT_AND_INTEREST;
                break;
            case 4:
                chargeCalculationMethod = PERCENT_OF_INTEREST;
                break;
            default:
                chargeCalculationMethod = INVALID;
                break;
        }
        return chargeCalculationMethod;
    }
}