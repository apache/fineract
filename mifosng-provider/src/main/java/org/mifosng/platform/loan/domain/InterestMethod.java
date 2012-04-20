package org.mifosng.platform.loan.domain;

public enum InterestMethod {
	DECLINING_BALANCE(0), FLAT(1), INVALID(2);

    private final Integer value;

    private InterestMethod(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

    public static InterestMethod fromInt(final Integer selectedMethod) {

		InterestMethod repaymentMethod = null;
		switch (selectedMethod) {
		case 0:
			repaymentMethod = InterestMethod.DECLINING_BALANCE;
			break;
		case 1:
			repaymentMethod = InterestMethod.FLAT;
			break;
		default:
			repaymentMethod = InterestMethod.INVALID;
			break;
		}
		return repaymentMethod;
    }
}