package org.mifosplatform.portfolio.savingsdepositproduct.domain;

public enum TenureTypeEnum {

    INVALID(0, "tenureTypeEnum.invalid"), //
    FIXED_PERIOD(1, "tenureTypeEnum.fixedPeriod"), // 
    PERPETUAL(2, "tenureTypeEnum.perpetual");

    private final Integer value;
    private final String code;

    private TenureTypeEnum(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static TenureTypeEnum fromInt(final Integer frequency) {

        TenureTypeEnum tenureTypeEnum = TenureTypeEnum.INVALID;
        switch (frequency) {
            case 1:
                tenureTypeEnum = TenureTypeEnum.FIXED_PERIOD;
            break;
            case 2:
                tenureTypeEnum = TenureTypeEnum.PERPETUAL;
            break;
            default:
                tenureTypeEnum = TenureTypeEnum.INVALID;
            break;
        }
        return tenureTypeEnum;
    }

	public boolean isFixedPeriod() {
		return this.value.equals(TenureTypeEnum.FIXED_PERIOD.value);
	}

	public boolean isPerpetual() {
		return this.value.equals(TenureTypeEnum.PERPETUAL.value);
	}
}