package org.mifosng.platform.loan.domain;

public enum AmortizationMethod {
	EQUAL_PRINCIPAL(0), EQUAL_INSTALLMENTS(1), INVALID(2);

    private final Integer value;

    private AmortizationMethod(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

    public static AmortizationMethod fromInt(final Integer selectedMethod) {

        if (selectedMethod == null) {
            return null;
        }

		AmortizationMethod repaymentMethod = null;
		switch (selectedMethod) {
		case 0:
			repaymentMethod = AmortizationMethod.EQUAL_PRINCIPAL;
			break;
		case 1:
			repaymentMethod = AmortizationMethod.EQUAL_INSTALLMENTS;
			break;
		default:
			repaymentMethod = AmortizationMethod.INVALID;
			break;
		}
		return repaymentMethod;
    }
}