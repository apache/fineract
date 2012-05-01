package org.mifosng.platform.loan.domain;

public enum InterestCalculationPeriodMethod {
	DAILY(0), SAME_AS_REPAYMENT_PERIOD(1), INVALID(2);

    private final Integer value;

    private InterestCalculationPeriodMethod(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

    public static InterestCalculationPeriodMethod fromInt(final Integer selectedMethod) {

		InterestCalculationPeriodMethod repaymentMethod = null;
		switch (selectedMethod) {
		case 0:
			repaymentMethod = InterestCalculationPeriodMethod.DAILY;
			break;
		case 1:
			repaymentMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
			break;
		default:
			repaymentMethod = InterestCalculationPeriodMethod.INVALID;
			break;
		}
		return repaymentMethod;
    }
}