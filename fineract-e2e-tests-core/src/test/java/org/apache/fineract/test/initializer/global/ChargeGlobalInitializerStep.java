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
package org.apache.fineract.test.initializer.global;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.services.ChargesApi;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargePaymentMode;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.data.CurrencyOptions;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@RequiredArgsConstructor
@Component
public class ChargeGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String CURRENCY_CODE = CurrencyOptions.EUR.value;
    public static final String MONTH_DAY_FORMAT = "dd MMM";
    public static final String LOCALE_EN = "en";
    public static final Integer CHARGE_PAYMENT_MODE = ChargePaymentMode.REGULAR.value;
    public static final Enum<ChargeProductAppliesTo> CHARGE_APPLIES_TO_LOAN = ChargeProductAppliesTo.LOAN;
    public static final Enum<ChargeProductAppliesTo> CHARGE_APPLIES_TO_CLIENT = ChargeProductAppliesTo.CLIENT;
    public static final String CHARGE_LOAN_PERCENTAGE_LATE_FEE = "% Late fee";
    public static final String CHARGE_LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST = "% Late fee amount+interest";
    public static final String CHARGE_LOAN_PERCENTAGE_PROCESSING_FEE = "% Processing fee";
    public static final String CHARGE_LOAN_FIXED_LATE_FEE = "Fixed Late fee";
    public static final String CHARGE_LOAN_FIXED_RETURNED_PAYMENT_FEE = "Fixed Returned payment fee";
    public static final String CHARGE_LOAN_SNOOZE_FEE = "Snooze fee";
    public static final String CHARGE_LOAN_NSF_FEE = "NSF fee";
    public static final String CHARGE_LOAN_DISBURSEMENT_PERCENT_FEE = "Disbursement percentage fee";
    public static final String CHARGE_LOAN_TRANCHE_DISBURSEMENT_PERCENT_FEE = "Tranche Disbursement percentage fee";
    public static final String CHARGE_LOAN_INSTALLMENT_FEE_FLAT = "Installment flat fee";
    public static final String CHARGE_LOAN_INSTALLMENT_FEE_PERCENT_AMOUNT = "Installment percentage amount fee";
    public static final String CHARGE_LOAN_INSTALLMENT_FEE_PERCENT_INTEREST = "Installment percentage interest fee";
    public static final String CHARGE_LOAN_INSTALLMENT_FEE_PERCENT_AMOUNT_PLUS_INTEREST = "Installment percentage amount + interest fee";
    public static final String CHARGE_CLIENT_FIXED_FEE = "Fixed fee for Client";
    public static final String CHARGE_DISBURSEMENT_CHARGE = "Disbursement Charge";
    public static final String CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT = "Tranche Disbursement Charge Amount";
    public static final String CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT = "Tranche Disbursement Charge Percent";
    public static final Double CHARGE_AMOUNT_FLAT = 25D;
    public static final Double CHARGE_INSTALLMENT_FEE_AMOUNT_FLAT = 10D;
    public static final Double CHARGE_INSTALLMENT_FEE_AMOUNT_PERCENTAGE = 1D;
    public static final Double CHARGE_AMOUNT_PERCENTAGE = 5D;
    public static final Double CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE = 1.5D;
    public static final Double CHARGE_AMOUNT_INSTALLMENT_PERCENTAGE = 1.5D;
    public static final Double CHARGE_AMOUNT_OVERDUE_PERCENTAGE = 1.5D;
    public static final Integer CHARGE_TIME_TYPE_OVERDUE_FEES = ChargeTimeType.OVERDUE_FEES.value;
    public static final Integer CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE = ChargeTimeType.SPECIFIED_DUE_DATE.value;
    public static final Integer CHARGE_TIME_TYPE_DISBURSEMENT = ChargeTimeType.DISBURSEMENT.value;
    public static final Integer CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT = ChargeTimeType.TRANCHE_DISBURSEMENT.value;
    public static final Integer CHARGE_TIME_TYPE_INSTALLMENT = ChargeTimeType.INSTALLMENT_FEE.value;
    public static final Integer CHARGE_CALCULATION_TYPE_FLAT = ChargeCalculationType.FLAT.value;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT = ChargeCalculationType.PERCENTAGE_AMOUNT.value;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST = ChargeCalculationType.PERCENTAGE_INTEREST.value;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT = ChargeCalculationType.PERCENTAGE_DISBURSEMENT_AMOUNT.value;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST = ChargeCalculationType.PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST.value;

    private final ChargesApi chargesApi;

    @Override
    public void initialize() throws Exception {
        // Loan - % late (overdue) fee
        ChargeRequest requestLoanPercentLate = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_PERCENTAGE_LATE_FEE,
                CHARGE_TIME_TYPE_OVERDUE_FEES, CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_AMOUNT_OVERDUE_PERCENTAGE, true, true);
        Response<PostChargesResponse> responseLoanPercentLate = chargesApi.createCharge(requestLoanPercentLate).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_LATE_CREATE_RESPONSE, responseLoanPercentLate);

        // Loan - % processing fee
        ChargeRequest requestLoanPercentProcessing = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_PERCENTAGE_PROCESSING_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_AMOUNT_PERCENTAGE,
                true, false);
        Response<PostChargesResponse> responseLoanPercentProcessing = chargesApi.createCharge(requestLoanPercentProcessing).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_PROCESSING_CREATE_RESPONSE, responseLoanPercentProcessing);

        // Loan - fixed late (overdue) fee
        ChargeRequest requestLoanFixedLate = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_FIXED_LATE_FEE,
                CHARGE_TIME_TYPE_OVERDUE_FEES, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, true);
        Response<PostChargesResponse> responseLoanFixedLate = chargesApi.createCharge(requestLoanFixedLate).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_FIXED_LATE_CREATE_RESPONSE, responseLoanFixedLate);

        // Loan - fixed returned payment fee
        ChargeRequest requestLoanFixedReturnedPayment = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_FIXED_RETURNED_PAYMENT_FEE, CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT,
                CHARGE_AMOUNT_FLAT, true, false);
        Response<PostChargesResponse> responseLoanFixedReturnedPayment = chargesApi.createCharge(requestLoanFixedReturnedPayment).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_FIXED_RETURNED_PAYMENT_CREATE_RESPONSE, responseLoanFixedReturnedPayment);

        // Loan - snooze fee
        ChargeRequest requestLoanSnooze = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_SNOOZE_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false);
        Response<PostChargesResponse> responseLoanSnooze = chargesApi.createCharge(requestLoanSnooze).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_SNOOZE_FEE_CREATE_RESPONSE, responseLoanSnooze);

        // Loan - NSF fee
        ChargeRequest requestLoanNsf = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_NSF_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, true);
        Response<PostChargesResponse> responseLoanNsf = chargesApi.createCharge(requestLoanNsf).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_NSF_FEE_CREATE_RESPONSE, responseLoanNsf);

        // Loan - Disbursement % fee
        ChargeRequest requestLoanDisbursePercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_DISBURSEMENT_PERCENT_FEE,
                CHARGE_TIME_TYPE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE, true,
                false);
        Response<PostChargesResponse> responseLoanDisbursePercent = chargesApi.createCharge(requestLoanDisbursePercent).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENET_FEE_CREATE_RESPONSE, responseLoanDisbursePercent);

        // Loan - Tranche Disbursement % fee
        ChargeRequest requestLoanTrancheDisbursePercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_TRANCHE_DISBURSEMENT_PERCENT_FEE, CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT, CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE, true, false);
        Response<PostChargesResponse> responseLoanTrancheDisbursePercent = chargesApi.createCharge(requestLoanTrancheDisbursePercent)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_PERCENT_CREATE_RESPONSE,
                responseLoanTrancheDisbursePercent);

        // Loan - Installment % fee Amount + Interest
        final ChargeRequest requestLoanInstallmentPercentAmountPlusInterest = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_INSTALLMENT_FEE_PERCENT_AMOUNT_PLUS_INTEREST, CHARGE_TIME_TYPE_INSTALLMENT,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_INSTALLMENT_FEE_AMOUNT_PERCENTAGE, true, false);
        final Response<PostChargesResponse> responseLoanInstallmentPercentAmountPlusInterest = chargesApi
                .createCharge(requestLoanInstallmentPercentAmountPlusInterest).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE,
                responseLoanInstallmentPercentAmountPlusInterest);

        // Loan - % late (overdue) fee amount+interest
        ChargeRequest requestLoanPercentAmountPlusInterestLate = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST, CHARGE_TIME_TYPE_OVERDUE_FEES,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_AMOUNT_OVERDUE_PERCENTAGE, true, true);
        Response<PostChargesResponse> responseLoanPercentAmountPlusInterestLate = chargesApi
                .createCharge(requestLoanPercentAmountPlusInterestLate).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_LATE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE,
                responseLoanPercentAmountPlusInterestLate);

        // Client - fixed fee
        ChargeRequest requestClientFixed = defaultChargesRequest(CHARGE_APPLIES_TO_CLIENT, CHARGE_CLIENT_FIXED_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false);
        Response<PostChargesResponse> responseClientFixed = chargesApi.createCharge(requestClientFixed).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_CLIENT_FIXED_FEE_CREATE_RESPONSE, responseClientFixed);

        // Loan - Disbursement fixed fee
        ChargeRequest requestDisbursementCharge = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_DISBURSEMENT_CHARGE,
                CHARGE_TIME_TYPE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false);
        Response<PostChargesResponse> responseDisbursementCharge = chargesApi.createCharge(requestDisbursementCharge).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENT_CHARGE_CREATE_RESPONSE, responseDisbursementCharge);

        // Loan - Tranche Disbursement Charge (Flat)
        ChargeRequest requestTrancheDisbursementCharge = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT, CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_FLAT, 10.0,
                true, false);
        Response<PostChargesResponse> responseTrancheDisbursementCharge = chargesApi.createCharge(requestTrancheDisbursementCharge)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_CHARGE_FLAT_CREATE_RESPONSE,
                responseTrancheDisbursementCharge);

        // Loan - Tranche Disbursement Charge (%)
        ChargeRequest requestTrancheDisbursementChargePercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT, CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT, 2.0, true, false);
        Response<PostChargesResponse> responseTrancheDisbursementChargePercent = chargesApi
                .createCharge(requestTrancheDisbursementChargePercent).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT_CREATE_RESPONSE,
                responseTrancheDisbursementChargePercent);

        // Loan - Installment flat fee
        final ChargeRequest requestLoanInstallmentFlat = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_INSTALLMENT_FEE_FLAT,
                CHARGE_TIME_TYPE_INSTALLMENT, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_INSTALLMENT_FEE_AMOUNT_FLAT, true, false);
        final Response<PostChargesResponse> responseLoanInstallmentFlat = chargesApi.createCharge(requestLoanInstallmentFlat).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_FLAT_CREATE_RESPONSE, responseLoanInstallmentFlat);

        // Loan - Installment % fee Amount
        final ChargeRequest requestLoanInstallmentPercentAmount = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_INSTALLMENT_FEE_PERCENT_AMOUNT, CHARGE_TIME_TYPE_INSTALLMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT,
                CHARGE_INSTALLMENT_FEE_AMOUNT_PERCENTAGE, true, false);
        final Response<PostChargesResponse> responseLoanInstallmentPercentAmount = chargesApi
                .createCharge(requestLoanInstallmentPercentAmount).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_CREATE_RESPONSE,
                responseLoanInstallmentPercentAmount);

        // Loan - Installment % fee Interest
        final ChargeRequest requestLoanInstallmentPercentInterest = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_INSTALLMENT_FEE_PERCENT_INTEREST, CHARGE_TIME_TYPE_INSTALLMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST,
                CHARGE_AMOUNT_PERCENTAGE, true, false);
        final Response<PostChargesResponse> responseLoanInstallmentPercentInterest = chargesApi
                .createCharge(requestLoanInstallmentPercentInterest).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST_CREATE_RESPONSE,
                responseLoanInstallmentPercentInterest);
    }

    public static ChargeRequest defaultChargesRequest(Enum<ChargeProductAppliesTo> appliesTo, String name, Integer chargeTimeType,
            Integer chargeCalculationType, Double amount, Boolean isActive, Boolean isPenalty) throws Exception {
        ChargeRequest request = new ChargeRequest();
        Integer chargeAppliesTo;

        if (appliesTo.equals(ChargeProductAppliesTo.CLIENT)) {
            chargeAppliesTo = ChargeProductAppliesTo.CLIENT.value;
        } else if (appliesTo.equals(ChargeProductAppliesTo.LOAN)) {
            chargeAppliesTo = ChargeProductAppliesTo.LOAN.value;
            request.chargePaymentMode(CHARGE_PAYMENT_MODE);
        } else {
            throw new Exception(ErrorMessageHelper.chargeAppliesToIsInvalid(appliesTo));
        }

        request//
                .chargeAppliesTo(chargeAppliesTo)//
                .name(name)//
                .currencyCode(CURRENCY_CODE)//
                .chargeTimeType(chargeTimeType)//
                .chargeCalculationType(chargeCalculationType)//
                .amount(amount)//
                .active(isActive)//
                .penalty(isPenalty)//
                .monthDayFormat(MONTH_DAY_FORMAT)//
                .locale(LOCALE_EN);//

        return request;
    }
}
