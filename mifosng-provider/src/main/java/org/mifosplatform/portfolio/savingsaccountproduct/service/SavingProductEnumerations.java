package org.mifosplatform.portfolio.savingsaccountproduct.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsLockinPeriodEnum;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;

public class SavingProductEnumerations {

    public static EnumOptionData savingProductType(final int id) {
        return savingProductType(SavingProductType.fromInt(id));
    }

    public static EnumOptionData tenureTypeEnum(final int id) {
        return tenureTypeEnum(TenureTypeEnum.fromInt(id));
    }

    public static EnumOptionData interestFrequencyType(final int id) {
        return interestFrequencyType(SavingFrequencyType.fromInt(id));
    }

    public static EnumOptionData savingInterestType(final int id) {
        return savingInterestType(SavingsInterestType.fromInt(id));
    }

    public static EnumOptionData savingInterestCalculationMethod(final int id) {
        return savingInterestCalculationMethod(SavingInterestCalculationMethod.fromInt(id));
    }

    public static EnumOptionData savingsLockinPeriod(final int id) {
        return savingsLockinPeriod(SavingsLockinPeriodEnum.fromInt(id));
    }

    public static EnumOptionData savingsLockinPeriod(SavingsLockinPeriodEnum type) {
        final String codePrefix = "savings.product.";
        EnumOptionData optionData = null;
        switch (type) {
            case DAYS:
                optionData = new EnumOptionData(SavingsLockinPeriodEnum.DAYS.getValue().longValue(), codePrefix
                        + SavingsLockinPeriodEnum.DAYS.getCode(), "Days");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SavingsLockinPeriodEnum.MONTHS.getValue().longValue(), codePrefix
                        + SavingsLockinPeriodEnum.MONTHS.getCode(), "Months");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SavingsLockinPeriodEnum.WEEKS.getValue().longValue(), codePrefix
                        + SavingsLockinPeriodEnum.WEEKS.getCode(), "Weeks");
            break;
            case YEARS:
                optionData = new EnumOptionData(SavingsLockinPeriodEnum.YEARS.getValue().longValue(), codePrefix
                        + SavingsLockinPeriodEnum.YEARS.getCode(), "Years");
            break;
            default:
                optionData = new EnumOptionData(SavingsLockinPeriodEnum.INVALID.getValue().longValue(), codePrefix
                        + SavingsLockinPeriodEnum.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData savingInterestCalculationMethod(SavingInterestCalculationMethod type) {
        final String codePrefix = "savings.interestCalculation.method.";
        EnumOptionData optionData = null;
        switch (type) {
            case MINBAL:
                optionData = new EnumOptionData(SavingInterestCalculationMethod.MINBAL.getValue().longValue(), codePrefix
                        + SavingInterestCalculationMethod.MINBAL.getCode(), "Minimumbalance");
            break;
            case AVERAGEBAL:
                optionData = new EnumOptionData(SavingInterestCalculationMethod.AVERAGEBAL.getValue().longValue(), codePrefix
                        + SavingInterestCalculationMethod.AVERAGEBAL.getCode(), "AverageBalance");
            break;
            case INVALID:
                optionData = new EnumOptionData(SavingInterestCalculationMethod.INVALID.getValue().longValue(), codePrefix
                        + SavingInterestCalculationMethod.INVALID.getCode(), "Invalid");
            break;
            case MONTHLYCOLLECTION:
                optionData = new EnumOptionData(SavingInterestCalculationMethod.MONTHLYCOLLECTION.getValue().longValue(), codePrefix
                        + SavingInterestCalculationMethod.MONTHLYCOLLECTION.getCode(), "MonthlyCollection");
            break;
            default:
                optionData = new EnumOptionData(SavingInterestCalculationMethod.INVALID.getValue().longValue(), codePrefix
                        + SavingInterestCalculationMethod.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData savingInterestType(SavingsInterestType type) {
        final String codePrefix = "savings.interest.type.";
        EnumOptionData optionData = null;
        switch (type) {
            case COMPOUNDING:
                optionData = new EnumOptionData(SavingsInterestType.COMPOUNDING.getValue().longValue(), codePrefix
                        + SavingsInterestType.COMPOUNDING.getCode(), "Compounding");
            break;
            case SIMPLE:
                optionData = new EnumOptionData(SavingsInterestType.SIMPLE.getValue().longValue(), codePrefix
                        + SavingsInterestType.SIMPLE.getCode(), "Simple");
            break;
            default:
                optionData = new EnumOptionData(SavingsInterestType.INVALID.getValue().longValue(), codePrefix
                        + SavingsInterestType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData interestFrequencyType(SavingFrequencyType type) {
        final String codePrefix = "saving.product.frequency.period.";
        EnumOptionData optionData = null;
        switch (type) {
            case DAILY:
                optionData = new EnumOptionData(SavingFrequencyType.DAILY.getValue().longValue(), codePrefix
                        + SavingFrequencyType.DAILY.getCode(), "Days");
            break;
            case MONTHLY:
                optionData = new EnumOptionData(SavingFrequencyType.MONTHLY.getValue().longValue(), codePrefix
                        + SavingFrequencyType.MONTHLY.getCode(), "Months");
            break;
            case QUATERLY:
                optionData = new EnumOptionData(SavingFrequencyType.QUATERLY.getValue().longValue(), codePrefix
                        + SavingFrequencyType.QUATERLY.getCode(), "Months");
            break;
            case HALFYEARLY:
                optionData = new EnumOptionData(SavingFrequencyType.HALFYEARLY.getValue().longValue(), codePrefix
                        + SavingFrequencyType.HALFYEARLY.getCode(), "Months");
            break;
            case YEARLY:
                optionData = new EnumOptionData(SavingFrequencyType.YEARLY.getValue().longValue(), codePrefix
                        + SavingFrequencyType.YEARLY.getCode(), "Yearly");
            break;
            default:
                optionData = new EnumOptionData(SavingFrequencyType.INVALID.getValue().longValue(), SavingFrequencyType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData tenureTypeEnum(final TenureTypeEnum type) {
        final String codePrefix = "savings.tenure.";
        EnumOptionData optionData = null;
        switch (type) {
            case FIXED_PERIOD:
                optionData = new EnumOptionData(TenureTypeEnum.FIXED_PERIOD.getValue().longValue(), codePrefix
                        + TenureTypeEnum.FIXED_PERIOD.getCode(), "FixedPeriod");
            break;
            case PERPETUAL:
                optionData = new EnumOptionData(TenureTypeEnum.PERPETUAL.getValue().longValue(), codePrefix
                        + TenureTypeEnum.PERPETUAL.getCode(), "Perpetual");
            break;
            default:
                optionData = new EnumOptionData(TenureTypeEnum.INVALID.getValue().longValue(), codePrefix
                        + TenureTypeEnum.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData savingProductType(final SavingProductType type) {
        final String codePrefix = "savings.product.";
        EnumOptionData optionData = null;
        switch (type) {
        // FIXME - Madhukar - spelling mistake on 'Recurring'
            case RECURRING:
                optionData = new EnumOptionData(SavingProductType.RECURRING.getValue().longValue(), codePrefix
                        + SavingProductType.RECURRING.getCode(), "Recurring");
            break;
            case REGULAR:
                optionData = new EnumOptionData(SavingProductType.REGULAR.getValue().longValue(), codePrefix
                        + SavingProductType.REGULAR.getCode(), "Regular");
            break;
            case INVALID:
                optionData = new EnumOptionData(SavingProductType.INVALID.getValue().longValue(), codePrefix
                        + SavingProductType.INVALID.getCode(), "Invalid");
            break;
            default:
                optionData = new EnumOptionData(SavingProductType.INVALID.getValue().longValue(), SavingProductType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }
}
