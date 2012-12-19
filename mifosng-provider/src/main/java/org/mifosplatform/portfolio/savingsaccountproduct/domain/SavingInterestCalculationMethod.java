package org.mifosplatform.portfolio.savingsaccountproduct.domain;

public enum SavingInterestCalculationMethod {

    AVERAGEBAL(1, "savinginterest.calculation.method.averagebal"), MINBAL(2, "savinginterest.calculation.method.minbal"), MONTHLYCOLLECTION(
            3, "savinginterest.calculation.method.monthlycllection"), INVALID(0, "savinginterest.calculation.method.invalid");

    private final Integer value;
    private final String code;

    private SavingInterestCalculationMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static SavingInterestCalculationMethod fromInt(final Integer frequency) {

        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.INVALID;
        switch (frequency) {
            case 1:
                savingInterestCalculationMethod = SavingInterestCalculationMethod.AVERAGEBAL;
            break;
            case 2:
                savingInterestCalculationMethod = SavingInterestCalculationMethod.MINBAL;
            break;
            case 3:
                savingInterestCalculationMethod = SavingInterestCalculationMethod.MONTHLYCOLLECTION;
            break;
            default:
                savingInterestCalculationMethod = SavingInterestCalculationMethod.INVALID;
            break;
        }
        return savingInterestCalculationMethod;
    }

	public boolean isAverageBalance() {
		return this.value.equals(SavingInterestCalculationMethod.AVERAGEBAL.value);
	}

	public boolean isMonthlyCollection() {
		return this.value.equals(SavingInterestCalculationMethod.MONTHLYCOLLECTION.value);
	}
}
