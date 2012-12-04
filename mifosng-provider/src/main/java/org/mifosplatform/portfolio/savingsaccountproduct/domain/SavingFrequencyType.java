package org.mifosplatform.portfolio.savingsaccountproduct.domain;

public enum SavingFrequencyType {

    INVALID(0, "savingFrequencyType.invalid"), DAILY(1, "savingFrequencyType.daily"), MONTHLY(2, "savingFrequencyType.monthly"), QUATERLY(
            3, "savingFrequencyType.quaterly"), HALFYEARLY(4, "savingFrequencyType.halfyearly"), YEARLY(5, "savingFrequencyType.yearly");

    private final Integer value;
    private final String code;

    private SavingFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static SavingFrequencyType fromInt(final Integer frequency) {

        SavingFrequencyType savingFrequencyType = SavingFrequencyType.INVALID;
        switch (frequency) {
            case 1:
                savingFrequencyType = SavingFrequencyType.DAILY;
            break;
            case 2:
                savingFrequencyType = SavingFrequencyType.MONTHLY;
            break;
            case 3:
                savingFrequencyType = SavingFrequencyType.QUATERLY;
            break;
            case 4:
                savingFrequencyType = SavingFrequencyType.HALFYEARLY;
            break;
            case 5:
                savingFrequencyType = SavingFrequencyType.YEARLY;
            break;
            default:
                savingFrequencyType = SavingFrequencyType.INVALID;
            break;
        }
        return savingFrequencyType;
    }

}
