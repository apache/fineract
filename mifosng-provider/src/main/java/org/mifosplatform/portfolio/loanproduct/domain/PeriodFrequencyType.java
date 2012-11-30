package org.mifosplatform.portfolio.loanproduct.domain;

public enum PeriodFrequencyType {
    DAYS(0, "periodFrequencyType.days"), //
    WEEKS(1, "periodFrequencyType.weeks"), //
    MONTHS(2, "periodFrequencyType.months"), //
    YEARS(3, "periodFrequencyType.years"), //
    INVALID(4, "periodFrequencyType.invalid");

    private final Integer value;
    private final String code;

    private PeriodFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public static PeriodFrequencyType fromInt(final Integer frequency) {

        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.INVALID;
        switch (frequency) {
            case 0:
                repaymentFrequencyType = PeriodFrequencyType.DAYS;
            break;
            case 1:
                repaymentFrequencyType = PeriodFrequencyType.WEEKS;
            break;
            case 2:
                repaymentFrequencyType = PeriodFrequencyType.MONTHS;
            break;
            case 3:
                repaymentFrequencyType = PeriodFrequencyType.YEARS;
            break;
            default:
                repaymentFrequencyType = PeriodFrequencyType.INVALID;
            break;
        }
        return repaymentFrequencyType;
    }
}
