/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.charge.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public final class ChargeEnumerations {

    private ChargeEnumerations() {

    }

    public static EnumOptionData chargeTimeType(final int id) {
        return chargeTimeType(ChargeTimeType.fromInt(id));
    }

    public static EnumOptionData chargeTimeType(final ChargeTimeType type) {
        EnumOptionData optionData = switch (type) {
            case DISBURSEMENT -> new EnumOptionData(ChargeTimeType.DISBURSEMENT.getValue().longValue(),
                    ChargeTimeType.DISBURSEMENT.getCode(), "Disbursement");
            case SPECIFIED_DUE_DATE -> new EnumOptionData(ChargeTimeType.SPECIFIED_DUE_DATE.getValue().longValue(),
                    ChargeTimeType.SPECIFIED_DUE_DATE.getCode(), "Specified due date");
            case SAVINGS_ACTIVATION -> new EnumOptionData(ChargeTimeType.SAVINGS_ACTIVATION.getValue().longValue(),
                    ChargeTimeType.SAVINGS_ACTIVATION.getCode(), "Savings Activation");
            case SAVINGS_CLOSURE -> new EnumOptionData(ChargeTimeType.SAVINGS_CLOSURE.getValue().longValue(),
                    ChargeTimeType.SAVINGS_CLOSURE.getCode(), "Savings Closure");
            case WITHDRAWAL_FEE -> new EnumOptionData(ChargeTimeType.WITHDRAWAL_FEE.getValue().longValue(),
                    ChargeTimeType.WITHDRAWAL_FEE.getCode(), "Withdrawal Fee");
            case ANNUAL_FEE ->
                new EnumOptionData(ChargeTimeType.ANNUAL_FEE.getValue().longValue(), ChargeTimeType.ANNUAL_FEE.getCode(), "Annual Fee");
            case MONTHLY_FEE ->
                new EnumOptionData(ChargeTimeType.MONTHLY_FEE.getValue().longValue(), ChargeTimeType.MONTHLY_FEE.getCode(), "Monthly Fee");
            case WEEKLY_FEE ->
                new EnumOptionData(ChargeTimeType.WEEKLY_FEE.getValue().longValue(), ChargeTimeType.WEEKLY_FEE.getCode(), "Weekly Fee");
            case INSTALMENT_FEE -> new EnumOptionData(ChargeTimeType.INSTALMENT_FEE.getValue().longValue(),
                    ChargeTimeType.INSTALMENT_FEE.getCode(), "Installment Fee");
            case OVERDUE_INSTALLMENT -> new EnumOptionData(ChargeTimeType.OVERDUE_INSTALLMENT.getValue().longValue(),
                    ChargeTimeType.OVERDUE_INSTALLMENT.getCode(), "Overdue Fees");
            case OVERDRAFT_FEE -> new EnumOptionData(ChargeTimeType.OVERDRAFT_FEE.getValue().longValue(),
                    ChargeTimeType.OVERDRAFT_FEE.getCode(), "Overdraft Fee");
            case TRANCHE_DISBURSEMENT -> new EnumOptionData(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().longValue(),
                    ChargeTimeType.TRANCHE_DISBURSEMENT.getCode(), "Tranche Disbursement");
            case SHAREACCOUNT_ACTIVATION -> new EnumOptionData(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue().longValue(),
                    ChargeTimeType.SHAREACCOUNT_ACTIVATION.getCode(), "Share Account Activate");
            case SHARE_PURCHASE -> new EnumOptionData(ChargeTimeType.SHARE_PURCHASE.getValue().longValue(),
                    ChargeTimeType.SHARE_PURCHASE.getCode(), "Share Purchase");
            case SHARE_REDEEM -> new EnumOptionData(ChargeTimeType.SHARE_REDEEM.getValue().longValue(),
                    ChargeTimeType.SHARE_REDEEM.getCode(), "Share Redeem");
            case SAVINGS_NOACTIVITY_FEE -> new EnumOptionData(ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getValue().longValue(),
                    ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getCode(), "Saving No Activity Fee");
            case DOWN_PAYMENT -> new EnumOptionData(ChargeTimeType.DOWN_PAYMENT.getValue().longValue(),
                    ChargeTimeType.DOWN_PAYMENT.getCode(), "Down Payment");
            default -> new EnumOptionData(ChargeTimeType.INVALID.getValue().longValue(), ChargeTimeType.INVALID.getCode(), "Invalid");
        };
        return optionData;
    }

    public static EnumOptionData chargeAppliesTo(final int id) {
        return chargeAppliesTo(ChargeAppliesTo.fromInt(id));
    }

    public static EnumOptionData chargeAppliesTo(final ChargeAppliesTo type) {
        EnumOptionData optionData = switch (type) {
            case LOAN -> new EnumOptionData(ChargeAppliesTo.LOAN.getValue().longValue(), ChargeAppliesTo.LOAN.getCode(), "Loan");
            case SAVINGS ->
                new EnumOptionData(ChargeAppliesTo.SAVINGS.getValue().longValue(), ChargeAppliesTo.SAVINGS.getCode(), "Savings");
            case CLIENT -> new EnumOptionData(ChargeAppliesTo.CLIENT.getValue().longValue(), ChargeAppliesTo.CLIENT.getCode(), "Client");
            case SHARES -> new EnumOptionData(ChargeAppliesTo.SHARES.getValue().longValue(), ChargeAppliesTo.SHARES.getCode(), "Shares");
            case WORKING_CAPITAL_LOAN -> new EnumOptionData(ChargeAppliesTo.WORKING_CAPITAL_LOAN.getValue().longValue(),
                    ChargeAppliesTo.WORKING_CAPITAL_LOAN.getCode(), "Working Capital Loan");
            default -> new EnumOptionData(ChargeAppliesTo.INVALID.getValue().longValue(), ChargeAppliesTo.INVALID.getCode(), "Invalid");
        };
        return optionData;
    }

    public static EnumOptionData chargeCalculationType(final int id) {
        return chargeCalculationType(ChargeCalculationType.fromInt(id));
    }

    public static EnumOptionData chargeCalculationType(final ChargeCalculationType type) {
        EnumOptionData optionData = switch (type) {
            case FLAT ->
                new EnumOptionData(ChargeCalculationType.FLAT.getValue().longValue(), ChargeCalculationType.FLAT.getCode(), "Flat");
            case PERCENT_OF_AMOUNT -> new EnumOptionData(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue().longValue(),
                    ChargeCalculationType.PERCENT_OF_AMOUNT.getCode(), "% Amount");
            case PERCENT_OF_AMOUNT_AND_INTEREST ->
                new EnumOptionData(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getCode(), "% Loan Amount + Interest");
            case PERCENT_OF_INTEREST -> new EnumOptionData(ChargeCalculationType.PERCENT_OF_INTEREST.getValue().longValue(),
                    ChargeCalculationType.PERCENT_OF_INTEREST.getCode(), "% Interest");
            case PERCENT_OF_DISBURSEMENT_AMOUNT ->
                new EnumOptionData(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue().longValue(),
                        ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getCode(), "% Disbursement Amount");
            default -> new EnumOptionData(ChargeCalculationType.INVALID.getValue().longValue(), ChargeCalculationType.INVALID.getCode(),
                    "Invalid");
        };
        return optionData;
    }

    public static EnumOptionData chargePaymentMode(final int id) {
        return chargePaymentMode(ChargePaymentMode.fromInt(id));
    }

    public static EnumOptionData chargePaymentMode(final ChargePaymentMode type) {
        EnumOptionData optionData = switch (type) {
            case ACCOUNT_TRANSFER -> new EnumOptionData(ChargePaymentMode.ACCOUNT_TRANSFER.getValue().longValue(),
                    ChargePaymentMode.ACCOUNT_TRANSFER.getCode(), "Account transfer");
            default -> new EnumOptionData(ChargePaymentMode.REGULAR.getValue().longValue(), ChargePaymentMode.REGULAR.getCode(), "Regular");
        };
        return optionData;
    }

    public static EnumOptionData feeFrequencyType(final int id) {
        return feeFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData feeFrequencyType(final PeriodFrequencyType frequencyType) {
        EnumOptionData optionData;
        switch (frequencyType) {
            case DAYS -> optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                    PeriodFrequencyType.DAYS.getCode(), "Daily");
            case WEEKS -> optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                    PeriodFrequencyType.WEEKS.getCode(), "Weekly");
            case MONTHS -> optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                    PeriodFrequencyType.MONTHS.getCode(), "Monthly");
            case YEARS -> optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                    PeriodFrequencyType.YEARS.getCode(), "Yearly");
            case WHOLE_TERM -> optionData = new EnumOptionData(PeriodFrequencyType.WHOLE_TERM.getValue().longValue(),
                    PeriodFrequencyType.WHOLE_TERM.getCode(), "Whole term");
            default -> throw new UnsupportedOperationException(frequencyType + " is not supported");
        }
        return optionData;
    }

}
