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

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.CreateChargeRequest;
import org.apache.fineract.client.models.CreateChargeResponse;
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
    public static final String CHARGE_LOAN_INSTALLMENT_PERCENT_FEE = "Installment percentage fee";
    public static final String CHARGE_CLIENT_FIXED_FEE = "Fixed fee for Client";
    public static final String CHARGE_DISBURSEMENT_CHARGE = "Disbursement Charge";
    public static final String CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT = "Tranche Disbursement Charge Amount";
    public static final String CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT = "Tranche Disbursement Charge Percent";
    public static final Double CHARGE_AMOUNT_FLAT = 25D;
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
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT = ChargeCalculationType.PERCENTAGE_DISBURSEMENT_AMOUNT.value;
    public static final Integer CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST = ChargeCalculationType.PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST.value;

    private final ChargesApi chargesApi;

    @Override
    public void initialize() throws Exception {
        // Loan - % late (overdue) fee
        CreateChargeRequest requestLoanPercentLate = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_PERCENTAGE_LATE_FEE,
                CHARGE_TIME_TYPE_OVERDUE_FEES, CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_AMOUNT_OVERDUE_PERCENTAGE, true, true);
        Response<CreateChargeResponse> responseLoanPercentLate = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanPercentLate).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_LATE_CREATE_RESPONSE, responseLoanPercentLate);

        // Loan - % processing fee
        CreateChargeRequest requestLoanPercentProcessing = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_PERCENTAGE_PROCESSING_FEE, CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_AMOUNT_PERCENTAGE, true, false);
        Response<CreateChargeResponse> responseLoanPercentProcessing = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanPercentProcessing).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_PROCESSING_CREATE_RESPONSE, responseLoanPercentProcessing);

        // Loan - fixed late (overdue) fee
        CreateChargeRequest requestLoanFixedLate = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_FIXED_LATE_FEE,
                CHARGE_TIME_TYPE_OVERDUE_FEES, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, true);
        Response<CreateChargeResponse> responseLoanFixedLate = chargesApi.createCharge(UUID.randomUUID().toString(), requestLoanFixedLate)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_FIXED_LATE_CREATE_RESPONSE, responseLoanFixedLate);

        // Loan - fixed returned payment fee
        CreateChargeRequest requestLoanFixedReturnedPayment = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_FIXED_RETURNED_PAYMENT_FEE, CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT,
                CHARGE_AMOUNT_FLAT, true, false);
        Response<CreateChargeResponse> responseLoanFixedReturnedPayment = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanFixedReturnedPayment).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_FIXED_RETURNED_PAYMENT_CREATE_RESPONSE, responseLoanFixedReturnedPayment);

        // Loan - snooze fee
        CreateChargeRequest requestLoanSnooze = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_SNOOZE_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false);
        Response<CreateChargeResponse> responseLoanSnooze = chargesApi.createCharge(UUID.randomUUID().toString(), requestLoanSnooze)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_SNOOZE_FEE_CREATE_RESPONSE, responseLoanSnooze);

        // Loan - NSF fee
        CreateChargeRequest requestLoanNsf = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_NSF_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, true);
        Response<CreateChargeResponse> responseLoanNsf = chargesApi.createCharge(UUID.randomUUID().toString(), requestLoanNsf).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_NSF_FEE_CREATE_RESPONSE, responseLoanNsf);

        // Loan - Disbursement % fee
        CreateChargeRequest requestLoanDisbursePercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_LOAN_DISBURSEMENT_PERCENT_FEE,
                CHARGE_TIME_TYPE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE, true,
                false);
        Response<CreateChargeResponse> responseLoanDisbursePercent = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanDisbursePercent).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENET_FEE_CREATE_RESPONSE, responseLoanDisbursePercent);

        // Loan - Tranche Disbursement % fee
        CreateChargeRequest requestLoanTrancheDisbursePercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_TRANCHE_DISBURSEMENT_PERCENT_FEE, CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT, CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE, true, false);
        Response<CreateChargeResponse> responseLoanTrancheDisbursePercent = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanTrancheDisbursePercent).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_PERCENT_CREATE_RESPONSE,
                responseLoanTrancheDisbursePercent);

        // Loan - Installment % fee
        CreateChargeRequest requestLoanInstallmentPercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_INSTALLMENT_PERCENT_FEE, CHARGE_TIME_TYPE_INSTALLMENT,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_AMOUNT_INSTALLMENT_PERCENTAGE, true, false);
        Response<CreateChargeResponse> responseLoanInstallmentPercent = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanInstallmentPercent).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_CREATE_RESPONSE, responseLoanInstallmentPercent);

        // Loan - % late (overdue) fee amount+interest
        CreateChargeRequest requestLoanPercentAmountPlusInterestLate = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST, CHARGE_TIME_TYPE_OVERDUE_FEES,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_AMOUNT_OVERDUE_PERCENTAGE, true, true);
        Response<CreateChargeResponse> responseLoanPercentAmountPlusInterestLate = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestLoanPercentAmountPlusInterestLate).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_LATE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE,
                responseLoanPercentAmountPlusInterestLate);

        // Client - fixed fee
        CreateChargeRequest requestClientFixed = defaultChargesRequest(CHARGE_APPLIES_TO_CLIENT, CHARGE_CLIENT_FIXED_FEE,
                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false);
        Response<CreateChargeResponse> responseClientFixed = chargesApi.createCharge(UUID.randomUUID().toString(), requestClientFixed)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_CLIENT_FIXED_FEE_CREATE_RESPONSE, responseClientFixed);

        // Loan - Disbursement fixed fee
        CreateChargeRequest requestDisbursementCharge = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN, CHARGE_DISBURSEMENT_CHARGE,
                CHARGE_TIME_TYPE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false);
        Response<CreateChargeResponse> responseDisbursementCharge = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestDisbursementCharge).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENT_CHARGE_CREATE_RESPONSE, responseDisbursementCharge);

        // Loan - Tranche Disbursement Charge (Flat)
        CreateChargeRequest requestTrancheDisbursementCharge = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT, CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_FLAT, 10.0,
                true, false);
        Response<CreateChargeResponse> responseTrancheDisbursementCharge = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestTrancheDisbursementCharge).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_CHARGE_FLAT_CREATE_RESPONSE,
                responseTrancheDisbursementCharge);

        // Loan - Tranche Disbursement Charge (%)
        CreateChargeRequest requestTrancheDisbursementChargePercent = defaultChargesRequest(CHARGE_APPLIES_TO_LOAN,
                CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT, CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT,
                CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT, 2.0, true, false);
        Response<CreateChargeResponse> responseTrancheDisbursementChargePercent = chargesApi
                .createCharge(UUID.randomUUID().toString(), requestTrancheDisbursementChargePercent).execute();
        TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT_CREATE_RESPONSE,
                responseTrancheDisbursementChargePercent);
    }

    public static CreateChargeRequest defaultChargesRequest(Enum<ChargeProductAppliesTo> appliesTo, String name, Integer chargeTimeType,
            Integer chargeCalculationType, Double amount, Boolean isActive, Boolean isPenalty) throws Exception {
        CreateChargeRequest request = new CreateChargeRequest();
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
                .amount(new BigDecimal(amount))//
                .active(isActive)//
                .penalty(isPenalty)//
                .monthDayFormat(MONTH_DAY_FORMAT)//
                .locale(LOCALE_EN);//

        return request;
    }
}
