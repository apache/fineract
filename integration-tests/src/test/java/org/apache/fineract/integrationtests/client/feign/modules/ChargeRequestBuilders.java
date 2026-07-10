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
package org.apache.fineract.integrationtests.client.feign.modules;

import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

public final class ChargeRequestBuilders {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final String DEFAULT_LOCALE = "en";

    private ChargeRequestBuilders() {}

    public static ChargeRequest loanDisbursementFee(double amount) {
        return loanDisbursementFee(amount, DEFAULT_CURRENCY);
    }

    public static ChargeRequest loanDisbursementFee(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue());
    }

    public static ChargeRequest loanDisbursementPercentageFee(double percentage) {
        return loanDisbursementPercentageFee(percentage, DEFAULT_CURRENCY);
    }

    public static ChargeRequest loanDisbursementPercentageFee(double percentage, String currencyCode) {
        return baseLoanCharge(percentage, currencyCode)//
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue())//
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());
    }

    public static ChargeRequest loanSpecifiedDueDateFee(double amount) {
        return loanSpecifiedDueDateFee(amount, DEFAULT_CURRENCY);
    }

    public static ChargeRequest loanSpecifiedDueDateFee(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
    }

    public static ChargeRequest loanSpecifiedDueDatePenalty(double amount) {
        return loanSpecifiedDueDatePenalty(amount, DEFAULT_CURRENCY);
    }

    public static ChargeRequest loanSpecifiedDueDatePenalty(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .penalty(true);
    }

    public static ChargeRequest loanSpecifiedDueDatePercentageOfInterestFee(double percentage) {
        return baseLoanCharge(percentage, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());
    }

    public static ChargeRequest loanSpecifiedDueDateAccountTransferFee(double amount, boolean penalty) {
        return loanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType.FLAT, amount, penalty);
    }

    public static ChargeRequest loanInstallmentFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.INSTALMENT_FEE.getValue());
    }

    public static ChargeRequest loanOverdueFeePercentageOfAmountAndInterest(double percentage) {
        return loanOverdueFee(percentage).chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
    }

    public static ChargeRequest loanOverdueFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.OVERDUE_INSTALLMENT.getValue())//
                .penalty(true);
    }

    /** A disbursement-time loan charge with an explicit calculation type. */
    public static ChargeRequest loanDisbursementCharge(ChargeCalculationType chargeCalculationType, double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue())//
                .chargeCalculationType(chargeCalculationType.getValue());
    }

    /** A specified-due-date loan charge with an explicit calculation type. */
    public static ChargeRequest loanSpecifiedDueDateCharge(ChargeCalculationType chargeCalculationType, double amount, boolean penalty) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .chargeCalculationType(chargeCalculationType.getValue())//
                .penalty(penalty);
    }

    /** An installment-fee loan charge with an explicit calculation type. */
    public static ChargeRequest loanInstallmentCharge(ChargeCalculationType chargeCalculationType, double amount, boolean penalty) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.INSTALMENT_FEE.getValue())//
                .chargeCalculationType(chargeCalculationType.getValue())//
                .penalty(penalty);
    }

    /** Same as {@link #loanSpecifiedDueDateCharge}, but paid by transfer from a linked savings account. */
    public static ChargeRequest loanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return loanSpecifiedDueDateCharge(chargeCalculationType, amount, penalty)//
                .chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }

    /** Same as {@link #loanInstallmentCharge}, but paid by transfer from a linked savings account. */
    public static ChargeRequest loanInstallmentAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return loanInstallmentCharge(chargeCalculationType, amount, penalty)//
                .chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }

    public static ChargeRequest clientSpecifiedDueDateFee(double amount) {
        return new ChargeRequest()//
                .name(Utils.uniqueRandomStringGenerator("Charge_Client_", 6))//
                .chargeAppliesTo(ChargeAppliesTo.CLIENT.getValue())//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue())//
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue())//
                .currencyCode(DEFAULT_CURRENCY)//
                .amount(amount)//
                .active(true)//
                .locale(DEFAULT_LOCALE);
    }

    private static ChargeRequest baseLoanCharge(double amount, String currencyCode) {
        return new ChargeRequest()//
                .name(Utils.uniqueRandomStringGenerator("Charge_Loan_", 6))//
                .chargeAppliesTo(ChargeAppliesTo.LOAN.getValue())//
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue())//
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue())//
                .currencyCode(currencyCode)//
                .amount(amount)//
                .active(true)//
                .locale(DEFAULT_LOCALE);
    }
}
