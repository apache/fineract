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

import java.math.BigDecimal;
import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

public final class ChargeRequestBuilders {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final String DEFAULT_LOCALE = "en";
    private static final String CLIENT_CHARGE_LOCALE = "en_GB";

    private ChargeRequestBuilders() {}

    public static ChargeCreateRequest loanDisbursementFee(double amount) {
        return loanDisbursementFee(amount, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanDisbursementFee(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue());
    }

    public static ChargeCreateRequest loanDisbursementPercentageFee(double percentage) {
        return loanDisbursementPercentageFee(percentage, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanDisbursementPercentageFee(double percentage, String currencyCode) {
        return baseLoanCharge(percentage, currencyCode)//
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue())//
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());
    }

    public static ChargeCreateRequest loanSpecifiedDueDateFee(double amount) {
        return loanSpecifiedDueDateFee(amount, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanSpecifiedDueDateFee(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
    }

    public static ChargeCreateRequest loanSpecifiedDueDatePenalty(double amount) {
        return loanSpecifiedDueDatePenalty(amount, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanSpecifiedDueDatePenalty(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .penalty(true);
    }

    public static ChargeCreateRequest loanSpecifiedDueDatePercentageOfInterestFee(double percentage) {
        return baseLoanCharge(percentage, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());
    }

    public static ChargeCreateRequest loanSpecifiedDueDateAccountTransferFee(double amount, boolean penalty) {
        return loanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType.FLAT, amount, penalty);
    }

    public static ChargeCreateRequest loanInstallmentFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.INSTALMENT_FEE.getValue());
    }

    public static ChargeCreateRequest loanOverdueFeePercentageOfAmountAndInterest(double percentage) {
        return loanOverdueFee(percentage).chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
    }

    public static ChargeCreateRequest loanOverdueFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.OVERDUE_INSTALLMENT.getValue())//
                .penalty(true);
    }

    /** A disbursement-time loan charge with an explicit calculation type. */
    public static ChargeCreateRequest loanDisbursementCharge(ChargeCalculationType chargeCalculationType, double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.DISBURSEMENT.getValue())//
                .chargeCalculationType(chargeCalculationType.getValue());
    }

    /** A specified-due-date loan charge with an explicit calculation type. */
    public static ChargeCreateRequest loanSpecifiedDueDateCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())//
                .chargeCalculationType(chargeCalculationType.getValue())//
                .penalty(penalty);
    }

    /** An installment-fee loan charge with an explicit calculation type. */
    public static ChargeCreateRequest loanInstallmentCharge(ChargeCalculationType chargeCalculationType, double amount, boolean penalty) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(ChargeTimeType.INSTALMENT_FEE.getValue())//
                .chargeCalculationType(chargeCalculationType.getValue())//
                .penalty(penalty);
    }

    /** Same as {@link #loanSpecifiedDueDateCharge}, but paid by transfer from a linked savings account. */
    public static ChargeCreateRequest loanSpecifiedDueDateAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return loanSpecifiedDueDateCharge(chargeCalculationType, amount, penalty)//
                .chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }

    /** Same as {@link #loanInstallmentCharge}, but paid by transfer from a linked savings account. */
    public static ChargeCreateRequest loanInstallmentAccountTransferCharge(ChargeCalculationType chargeCalculationType, double amount,
            boolean penalty) {
        return loanInstallmentCharge(chargeCalculationType, amount, penalty)//
                .chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }

    public static ChargeCreateRequest clientSpecifiedDueDateFee(double amount) {
        return new ChargeCreateRequest()//
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

    public static PostClientsClientIdChargesRequest clientCharge(Long chargeId, String dueDate, BigDecimal amount) {
        return new PostClientsClientIdChargesRequest()//
                .chargeId(chargeId)//
                .dueDate(dueDate)//
                .amount(amount)//
                .dateFormat(FeignTestConstants.DATETIME_PATTERN)//
                .locale(CLIENT_CHARGE_LOCALE);
    }

    public static PostClientsClientIdChargesChargeIdRequest payClientCharge(String transactionDate, Integer amount) {
        return new PostClientsClientIdChargesChargeIdRequest()//
                .transactionDate(transactionDate)//
                .amount(amount)//
                .dateFormat(FeignTestConstants.DATETIME_PATTERN)//
                .locale(CLIENT_CHARGE_LOCALE);
    }

    /**
     * The waive command ignores the body and waives whatever is outstanding; the amount mirrors what the legacy test
     * sent.
     */
    public static PostClientsClientIdChargesChargeIdRequest waiveClientCharge(Integer amount) {
        return new PostClientsClientIdChargesChargeIdRequest()//
                .amount(amount)//
                .locale(CLIENT_CHARGE_LOCALE);
    }

    private static ChargeCreateRequest baseLoanCharge(double amount, String currencyCode) {
        return new ChargeCreateRequest()//
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
