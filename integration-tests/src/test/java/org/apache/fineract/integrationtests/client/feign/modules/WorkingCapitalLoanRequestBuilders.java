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
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import org.apache.fineract.client.models.MarkWorkingCapitalLoanAsFraudRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.NearBreachFrequencyTypeEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRateRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.integrationtests.common.Utils;

public final class WorkingCapitalLoanRequestBuilders {

    private static final String LOCALE = "en";
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private static final Integer CHARGE_APPLIES_TO_WORKING_CAPITAL_LOAN = 5;
    private static final Integer CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE = 2;
    private static final Integer CHARGE_CALCULATION_TYPE_FLAT = 1;
    private static final String CHARGE_CURRENCY_CODE = "USD";

    private WorkingCapitalLoanRequestBuilders() {}

    public static PostWorkingCapitalLoansRequest submitApplication(Long clientId, Long productId, BigDecimal principal,
            BigDecimal periodPaymentRate, String submittedOnDate, String expectedDisbursementDate) {
        return new PostWorkingCapitalLoansRequest().clientId(clientId).productId(productId).principalAmount(principal)
                .periodPaymentRate(periodPaymentRate).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .totalPaymentVolume(BigDecimal.valueOf(100000)).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    /**
     * Submit overload carrying the loan-level {@code discount}. Only usable on a product whose {@code discountDefault}
     * attribute is overridable, otherwise the application validator rejects the override.
     */
    public static PostWorkingCapitalLoansRequest submitApplicationWithDiscount(Long clientId, Long productId, BigDecimal principal,
            BigDecimal periodPaymentRate, String submittedOnDate, String expectedDisbursementDate, BigDecimal discount) {
        return submitApplication(clientId, productId, principal, periodPaymentRate, submittedOnDate, expectedDisbursementDate)
                .discount(discount);
    }

    /**
     * Modify (PUT) request changing only the requested principal of a submitted application.
     */
    public static PutWorkingCapitalLoansLoanIdRequest modifyPrincipal(BigDecimal principalAmount) {
        return new PutWorkingCapitalLoansLoanIdRequest().principalAmount(principalAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest approve(String approvedOnDate, BigDecimal approvedAmount,
            String expectedDisbursementDate) {
        return new PostWorkingCapitalLoansLoanIdRequest().approvedOnDate(approvedOnDate).approvedLoanAmount(approvedAmount)
                .expectedDisbursementDate(expectedDisbursementDate).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest approveWithDiscount(String approvedOnDate, BigDecimal approvedAmount,
            String expectedDisbursementDate, BigDecimal discountAmount) {
        return new PostWorkingCapitalLoansLoanIdRequest().approvedOnDate(approvedOnDate).approvedLoanAmount(approvedAmount)
                .expectedDisbursementDate(expectedDisbursementDate).discountAmount(discountAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest disburse(String actualDisbursementDate, BigDecimal transactionAmount) {
        return new PostWorkingCapitalLoansLoanIdRequest().actualDisbursementDate(actualDisbursementDate)
                .transactionAmount(transactionAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest disburseWithDiscount(String actualDisbursementDate, BigDecimal transactionAmount,
            BigDecimal discountAmount) {
        return new PostWorkingCapitalLoansLoanIdRequest().actualDisbursementDate(actualDisbursementDate)
                .transactionAmount(transactionAmount).discountAmount(discountAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest undoDisbursal() {
        return new PostWorkingCapitalLoansLoanIdRequest().locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest emptyCommand() {
        return new PostWorkingCapitalLoansLoanIdRequest().locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    /**
     * The effective date is required, as it is for every other dated action here: a caller running under a simulated
     * business date must state it, since the system date the tests otherwise default to is unrelated to it.
     */
    public static PutWorkingCapitalLoansLoanIdRateRequest updateRate(BigDecimal newRate, String effectiveDate) {
        return new PutWorkingCapitalLoansLoanIdRateRequest().periodPaymentRate(newRate).effectiveDate(effectiveDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdNearBreachActionsRequest createNearBreachRescheduleAction(BigDecimal threshold,
            Integer frequency, String frequencyType) {
        return new PostWorkingCapitalLoansLoanIdNearBreachActionsRequest()
                .action(PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.ActionEnum.RESCHEDULE).nearBreachThreshold(threshold)
                .nearBreachFrequency(frequency).nearBreachFrequencyType(NearBreachFrequencyTypeEnum.fromValue(frequencyType))
                .locale(LOCALE);
    }

    public static PostWorkingCapitalLoanTransactionsRequest repayment(BigDecimal amount, String transactionDate) {
        return new PostWorkingCapitalLoanTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoanTransactionsRequest goodwillCredit(BigDecimal amount, String transactionDate) {
        return new PostWorkingCapitalLoanTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoanTransactionsRequest discountFee(BigDecimal amount, Long relatedResourceId) {
        return new PostWorkingCapitalLoanTransactionsRequest().transactionAmount(amount).relatedResourceId(relatedResourceId).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoanTransactionsRequest discountFeeAdjustment(BigDecimal amount, String transactionDate,
            Long relatedResourceId) {
        return new PostWorkingCapitalLoanTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate)
                .relatedResourceId(relatedResourceId).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static ExecuteWorkingCapitalLoanTransactionCommandRequest undoTransaction() {
        return new ExecuteWorkingCapitalLoanTransactionCommandRequest();
    }

    public static ChargeRequest specifiedDueDateCharge(boolean penalty, double amount) {
        return new ChargeRequest().chargeAppliesTo(CHARGE_APPLIES_TO_WORKING_CAPITAL_LOAN)
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE).chargeCalculationType(CHARGE_CALCULATION_TYPE_FLAT)
                .name(Utils.uniqueRandomStringGenerator("WCL_CHARGE_", 8)).amount(amount).active(true).currencyCode(CHARGE_CURRENCY_CODE)
                .locale(LOCALE).penalty(penalty);
    }

    public static PostLoansLoanIdChargesRequest addCharge(Long chargeId, double amount, String dueDate) {
        return new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(amount).dueDate(dueDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdChargesChargeIdRequest chargeAdjustment(BigDecimal amount) {
        return new PostWorkingCapitalLoansLoanIdChargesChargeIdRequest().amount(amount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoanTransactionsRequest creditBalanceRefund(BigDecimal amount, String transactionDate) {
        return repayment(amount, transactionDate);
    }

    public static PostWorkingCapitalLoanTransactionsRequest discountFeeAdjustment(Long relatedDiscountTransactionId, BigDecimal amount,
            String transactionDate) {
        return new PostWorkingCapitalLoanTransactionsRequest().relatedResourceId(relatedDiscountTransactionId).transactionAmount(amount)
                .transactionDate(transactionDate).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoanTransactionsRequest payoutRefund(BigDecimal amount, String transactionDate) {
        return repayment(amount, transactionDate);
    }

    public static PostWorkingCapitalLoanTransactionsRequest chargeOff(String transactionDate, String note) {
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest()
                .transactionDate(transactionDate).locale(LOCALE).dateFormat(DATE_FORMAT);
        if (note != null) {
            request.note(note);
        }
        return request;
    }

    public static PostWorkingCapitalLoanTransactionsRequest undoChargeOff(String note) {
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest().locale(LOCALE);
        if (note != null) {
            request.note(note);
        }
        return request;
    }

    public static ExecuteWorkingCapitalLoanTransactionCommandRequest reversal() {
        return new ExecuteWorkingCapitalLoanTransactionCommandRequest();
    }

    public static PostWorkingCapitalLoansDelinquencyActionRequest delinquencyPause(String startDate, String endDate) {
        return new PostWorkingCapitalLoansDelinquencyActionRequest().action("pause").startDate(startDate).endDate(endDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansDelinquencyActionRequest delinquencyReschedule(Integer frequency, String frequencyType) {
        return new PostWorkingCapitalLoansDelinquencyActionRequest().action("reschedule").frequency(frequency).frequencyType(frequencyType)
                .locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansBreachActionRequest breachPause(String startDate, String endDate) {
        return new PostWorkingCapitalLoansBreachActionRequest().action("pause").startDate(startDate).endDate(endDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansBreachActionRequest breachReset() {
        return new PostWorkingCapitalLoansBreachActionRequest().action("reset").restartPeriodFromResetDate(Boolean.FALSE).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static MarkWorkingCapitalLoanAsFraudRequest markAsFraud(boolean fraud) {
        return new MarkWorkingCapitalLoanAsFraudRequest().fraud(fraud);
    }

    public static PostWorkingCapitalLoanTransactionsRequest undoWriteOff() {
        return new PostWorkingCapitalLoanTransactionsRequest().locale(LOCALE).dateFormat(DATE_FORMAT);
    }
}
