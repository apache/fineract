package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.charge.domain.ChargeAppliesTo;
import org.mifosng.platform.charge.domain.ChargeCalculationMethod;
import org.mifosng.platform.charge.domain.ChargeTimeType;

public class ChargeEnumerations {

    public static EnumOptionData chargeTimeType(final int id){
        return chargeTimeType(ChargeTimeType.fromInt(id));
    }

    public static EnumOptionData chargeTimeType(ChargeTimeType type){
        EnumOptionData optionData = null;
        switch (type){
            case DISBURSEMENT:
                optionData = new EnumOptionData(ChargeTimeType.DISBURSEMENT.getValue().longValue(), ChargeTimeType.DISBURSEMENT.getCode(), "Disbursement");
                break;
            default:
                optionData = new EnumOptionData(ChargeTimeType.INVALID.getValue().longValue(), ChargeTimeType.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData chargeAppliesTo(final int id){
        return chargeAppliesTo(ChargeAppliesTo.fromInt(id));
    }

    public static EnumOptionData chargeAppliesTo(ChargeAppliesTo type){
        EnumOptionData optionData = null;
        switch (type){
            case LOAN:
                optionData = new EnumOptionData(ChargeAppliesTo.LOAN.getValue().longValue(), ChargeAppliesTo.LOAN.getCode(), "Loan");
                break;
            default:
                optionData = new EnumOptionData(ChargeAppliesTo.INVALID.getValue().longValue(), ChargeAppliesTo.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }

    public static EnumOptionData chargeCalculationType(final int id){
        return chargeCalculationType(ChargeCalculationMethod.fromInt(id));
    }

    public static EnumOptionData chargeCalculationType(ChargeCalculationMethod type){
        EnumOptionData optionData = null;
        switch (type){
            case FLAT:
                optionData = new EnumOptionData(ChargeCalculationMethod.FLAT.getValue().longValue(), ChargeCalculationMethod.FLAT.getCode(), "Flat");
                break;
            case PERCENT_OF_AMOUNT:
                optionData = new EnumOptionData(ChargeCalculationMethod.PERCENT_OF_AMOUNT.getValue().longValue(), ChargeCalculationMethod.PERCENT_OF_AMOUNT.getCode(), "% Amount");
                break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                optionData = new EnumOptionData(ChargeCalculationMethod.PERCENT_OF_AMOUNT_AND_INTEREST.getValue().longValue(),ChargeCalculationMethod.PERCENT_OF_AMOUNT_AND_INTEREST.getCode(), "% Loan Amount + Interest");
                break;
            case PERCENT_OF_INTEREST:
                optionData = new EnumOptionData(ChargeCalculationMethod.PERCENT_OF_INTEREST.getValue().longValue(), ChargeCalculationMethod.PERCENT_OF_INTEREST.getCode(), "% Interest");
                break;
            default:
                optionData = new EnumOptionData(ChargeCalculationMethod.INVALID.getValue().longValue(), ChargeCalculationMethod.INVALID.getCode(), "Invalid");
                break;
        }
        return optionData;
    }
}
