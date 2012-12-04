package org.mifosplatform.portfolio.savingsaccountproduct.domain;

public enum SavingsLockinPeriodEnum {

    DAYS(0, "lockin.period.type.days"), //
    WEEKS(1, "lockin.period.type.weeks"), //
    MONTHS(2, "lockin.period.type.months"), //
    YEARS(3, "lockin.period.type.years"), //
    INVALID(4, "lockin.period.type.invalid");

    private final Integer value;
    private final String code;

    private SavingsLockinPeriodEnum(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public static SavingsLockinPeriodEnum fromInt(final Integer frequency) {

        SavingsLockinPeriodEnum lockinPeriodType = SavingsLockinPeriodEnum.INVALID;
        switch (frequency) {
            case 0:
                lockinPeriodType = SavingsLockinPeriodEnum.DAYS;
            break;
            case 1:
                lockinPeriodType = SavingsLockinPeriodEnum.WEEKS;
            break;
            case 2:
                lockinPeriodType = SavingsLockinPeriodEnum.MONTHS;
            break;
            case 3:
                lockinPeriodType = SavingsLockinPeriodEnum.YEARS;
            break;
            default:
                lockinPeriodType = SavingsLockinPeriodEnum.INVALID;
            break;
        }
        return lockinPeriodType;
    }

}
