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

import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.integrationtests.common.Utils;

public final class ChargeRequestBuilders {

    private static final int CHARGE_APPLIES_TO_LOAN = 1;
    private static final int CHARGE_APPLIES_TO_CLIENT = 3;

    private static final int CHARGE_TIME_TYPE_DISBURSEMENT = 1;
    private static final int CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE = 2;
    private static final int CHARGE_TIME_TYPE_INSTALLMENT_FEE = 8;
    private static final int CHARGE_TIME_TYPE_OVERDUE_INSTALLMENT_FEE = 9;

    private static final int CHARGE_CALCULATION_TYPE_FLAT = 1;
    private static final int CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT = 2;
    private static final int CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST = 4;

    private static final int CHARGE_PAYMENT_MODE_REGULAR = 0;
    private static final int CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER = 1;

    private static final String DEFAULT_CURRENCY = "USD";
    private static final String DEFAULT_LOCALE = "en";

    private ChargeRequestBuilders() {}

    public static ChargeCreateRequest loanDisbursementFee(double amount) {
        return loanDisbursementFee(amount, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanDisbursementFee(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(CHARGE_TIME_TYPE_DISBURSEMENT);
    }

    public static ChargeCreateRequest loanDisbursementPercentageFee(double percentage) {
        return loanDisbursementPercentageFee(percentage, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanDisbursementPercentageFee(double percentage, String currencyCode) {
        return baseLoanCharge(percentage, currencyCode)//
                .chargeTimeType(CHARGE_TIME_TYPE_DISBURSEMENT)//
                .chargeCalculationType(CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT);
    }

    public static ChargeCreateRequest loanSpecifiedDueDateFee(double amount) {
        return loanSpecifiedDueDateFee(amount, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanSpecifiedDueDateFee(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE);
    }

    public static ChargeCreateRequest loanSpecifiedDueDatePenalty(double amount) {
        return loanSpecifiedDueDatePenalty(amount, DEFAULT_CURRENCY);
    }

    public static ChargeCreateRequest loanSpecifiedDueDatePenalty(double amount, String currencyCode) {
        return baseLoanCharge(amount, currencyCode)//
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE)//
                .penalty(true);
    }

    public static ChargeCreateRequest loanSpecifiedDueDatePercentageAmountAndInterestFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE)//
                .chargeCalculationType(CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST);
    }

    public static ChargeCreateRequest loanSpecifiedDueDateAccountTransferFee(double amount, boolean penalty) {
        return loanSpecifiedDueDateFee(amount).chargePaymentMode(CHARGE_PAYMENT_MODE_ACCOUNT_TRANSFER).penalty(penalty);
    }

    public static ChargeCreateRequest loanInstallmentFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(CHARGE_TIME_TYPE_INSTALLMENT_FEE);
    }

    public static ChargeCreateRequest loanOverdueFee(double amount) {
        return baseLoanCharge(amount, DEFAULT_CURRENCY)//
                .chargeTimeType(CHARGE_TIME_TYPE_OVERDUE_INSTALLMENT_FEE)//
                .penalty(true);
    }

    public static ChargeCreateRequest clientSpecifiedDueDateFee(double amount) {
        return new ChargeCreateRequest()//
                .name(Utils.uniqueRandomStringGenerator("Charge_Client_", 6))//
                .chargeAppliesTo(CHARGE_APPLIES_TO_CLIENT)//
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE)//
                .chargeCalculationType(CHARGE_CALCULATION_TYPE_FLAT)//
                .chargePaymentMode(CHARGE_PAYMENT_MODE_REGULAR)//
                .currencyCode(DEFAULT_CURRENCY)//
                .amount(amount)//
                .active(true)//
                .locale(DEFAULT_LOCALE);
    }

    private static ChargeCreateRequest baseLoanCharge(double amount, String currencyCode) {
        return new ChargeCreateRequest()//
                .name(Utils.uniqueRandomStringGenerator("Charge_Loan_", 6))//
                .chargeAppliesTo(CHARGE_APPLIES_TO_LOAN)//
                .chargeCalculationType(CHARGE_CALCULATION_TYPE_FLAT)//
                .chargePaymentMode(CHARGE_PAYMENT_MODE_REGULAR)//
                .currencyCode(currencyCode)//
                .amount(amount)//
                .active(true)//
                .locale(DEFAULT_LOCALE);
    }
}
