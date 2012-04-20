package org.mifosng.platform.loan.domain;

public enum PeriodFrequencyType {
	DAYS(0), WEEKS(1), MONTHS(2), YEARS(3), INVALID(4);

    private final Integer value;

    private PeriodFrequencyType(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
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
