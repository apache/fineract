/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargePaymentMode;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;

public class ChargeEnumerations {

    public static EnumOptionData chargeTimeType(final int id) {
        return chargeTimeType(ChargeTimeType.fromInt(id));
    }

    public static EnumOptionData chargeTimeType(final ChargeTimeType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case DISBURSEMENT:
                optionData = new EnumOptionData(ChargeTimeType.DISBURSEMENT.getValue().longValue(), ChargeTimeType.DISBURSEMENT.getCode(),
                        "Disbursement");
            break;
            case SPECIFIED_DUE_DATE:
                optionData = new EnumOptionData(ChargeTimeType.SPECIFIED_DUE_DATE.getValue().longValue(),
                        ChargeTimeType.SPECIFIED_DUE_DATE.getCode(), "Specified due date");
            break;
            case SAVINGS_ACTIVATION:
                optionData = new EnumOptionData(ChargeTimeType.SAVINGS_ACTIVATION.getValue().longValue(),
                        ChargeTimeType.SAVINGS_ACTIVATION.getCode(), "Savings Activation");
            break;
            case SAVINGS_CLOSURE:
                optionData = new EnumOptionData(ChargeTimeType.SAVINGS_CLOSURE.getValue().longValue(),
                        ChargeTimeType.SAVINGS_CLOSURE.getCode(), "Savings Closure");
            break;
            case WITHDRAWAL_FEE:
                optionData = new EnumOptionData(ChargeTimeType.WITHDRAWAL_FEE.getValue().longValue(),
                        ChargeTimeType.WITHDRAWAL_FEE.getCode(), "Withdrawal Fee");
            break;
            case ANNUAL_FEE:
                optionData = new EnumOptionData(ChargeTimeType.ANNUAL_FEE.getValue().longValue(), ChargeTimeType.ANNUAL_FEE.getCode(),
                        "Annual Fee");
            break;
            case MONTHLY_FEE:
                optionData = new EnumOptionData(ChargeTimeType.MONTHLY_FEE.getValue().longValue(), ChargeTimeType.MONTHLY_FEE.getCode(),
                        "Monthly Fee");
            break;
            case WEEKLY_FEE:
            	optionData = new EnumOptionData(ChargeTimeType.WEEKLY_FEE.getValue().longValue(), ChargeTimeType.WEEKLY_FEE.getCode(),
            			"Weekly Fee");
            break;	
            case INSTALMENT_FEE:
                optionData = new EnumOptionData(ChargeTimeType.INSTALMENT_FEE.getValue().longValue(),
                        ChargeTimeType.INSTALMENT_FEE.getCode(), "Installment Fee");
            break;
            case OVERDUE_INSTALLMENT:
                optionData = new EnumOptionData(ChargeTimeType.OVERDUE_INSTALLMENT.getValue().longValue(),
                        ChargeTimeType.OVERDUE_INSTALLMENT.getCode(), "Overdue Fees");
            break;
            case OVERDRAFT_FEE:
                optionData = new EnumOptionData(ChargeTimeType.OVERDRAFT_FEE.getValue().longValue(),
                        ChargeTimeType.OVERDRAFT_FEE.getCode(), "Overdraft Fee");
            break;
            default:
                optionData = new EnumOptionData(ChargeTimeType.INVALID.getValue().longValue(), ChargeTimeType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData chargeAppliesTo(final int id) {
        return chargeAppliesTo(ChargeAppliesTo.fromInt(id));
    }

    public static EnumOptionData chargeAppliesTo(final ChargeAppliesTo type) {
        EnumOptionData optionData = null;
        switch (type) {
            case LOAN:
                optionData = new EnumOptionData(ChargeAppliesTo.LOAN.getValue().longValue(), ChargeAppliesTo.LOAN.getCode(), "Loan");
            break;
            case SAVINGS:
                optionData = new EnumOptionData(ChargeAppliesTo.SAVINGS.getValue().longValue(), ChargeAppliesTo.SAVINGS.getCode(),
                        "Savings");
            break;
            default:
                optionData = new EnumOptionData(ChargeAppliesTo.INVALID.getValue().longValue(), ChargeAppliesTo.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData chargeCalculationType(final int id) {
        return chargeCalculationType(ChargeCalculationType.fromInt(id));
    }

    public static EnumOptionData chargeCalculationType(final ChargeCalculationType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case FLAT:
                optionData = new EnumOptionData(ChargeCalculationType.FLAT.getValue().longValue(), ChargeCalculationType.FLAT.getCode(),
                        "Flat");
            break;
            case PERCENT_OF_AMOUNT:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_AMOUNT.getCode(), "% Amount");
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getCode(), "% Loan Amount + Interest");
            break;
            case PERCENT_OF_INTEREST:
                optionData = new EnumOptionData(ChargeCalculationType.PERCENT_OF_INTEREST.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_INTEREST.getCode(), "% Interest");
            break;
            default:
                optionData = new EnumOptionData(ChargeCalculationType.INVALID.getValue().longValue(),
                        ChargeCalculationType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData chargePaymentMode(final int id) {
        return chargePaymentMode(ChargePaymentMode.fromInt(id));
    }

    public static EnumOptionData chargePaymentMode(final ChargePaymentMode type) {
        EnumOptionData optionData = null;
        switch (type) {
            case ACCOUNT_TRANSFER:
                optionData = new EnumOptionData(ChargePaymentMode.ACCOUNT_TRANSFER.getValue().longValue(),
                        ChargePaymentMode.ACCOUNT_TRANSFER.getCode(), "Account transfer");
            break;
            default:
                optionData = new EnumOptionData(ChargePaymentMode.REGULAR.getValue().longValue(), ChargePaymentMode.REGULAR.getCode(),
                        "Regular");
            break;
        }
        return optionData;
    }

}
