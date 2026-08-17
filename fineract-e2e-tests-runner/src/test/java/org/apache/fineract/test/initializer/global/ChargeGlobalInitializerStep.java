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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.client.models.ChargeCreateResponse;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargePaymentMode;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.data.CurrencyOptions;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ParallelExecutionHelper;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChargeGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String CURRENCY_CODE = CurrencyOptions.EUR.value;
    public static final String MONTH_DAY_FORMAT = "dd MMM";
    public static final String LOCALE_EN = "en";
    public static final Integer CHARGE_PAYMENT_MODE = ChargePaymentMode.REGULAR.value;
    public static final Enum<ChargeProductAppliesTo> CHARGE_APPLIES_TO_LOAN = ChargeProductAppliesTo.LOAN;
    public static final Enum<ChargeProductAppliesTo> CHARGE_APPLIES_TO_CLIENT = ChargeProductAppliesTo.CLIENT;
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

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        List<ChargeData> existingCharges = new ArrayList<>();
        try {
            existingCharges = fineractClient.charges().retrieveAllCharges(Map.of());
        } catch (Exception e) {
            log.debug("Could not retrieve existing charges, will create them", e);
        }

        final List<ChargeData> charges = existingCharges;

        List<Runnable> items = List.of(
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_LATE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_PERCENTAGE_LATE_FEE.getName(),
                                CHARGE_TIME_TYPE_OVERDUE_FEES, CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_AMOUNT_OVERDUE_PERCENTAGE,
                                true, true)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_PROCESSING_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_PERCENTAGE_PROCESSING_FEE.getName(),
                                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST,
                                CHARGE_AMOUNT_PERCENTAGE, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_FIXED_LATE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_FIXED_LATE_FEE.getName(),
                                CHARGE_TIME_TYPE_OVERDUE_FEES, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, true)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_FIXED_RETURNED_PAYMENT_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_FIXED_RETURNED_PAYMENT_FEE.getName(), CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE,
                                CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_SNOOZE_FEE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_SNOOZE_FEE.getName(),
                                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_NSF_FEE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_NSF_FEE.getName(),
                                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, true)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENET_FEE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getName(), CHARGE_TIME_TYPE_DISBURSEMENT,
                                CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_PERCENT_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getName(), CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT,
                                CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT, CHARGE_AMOUNT_DISBURSEMENT_PERCENTAGE, true,
                                false)),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.getName(),
                                CHARGE_TIME_TYPE_INSTALLMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST,
                                CHARGE_INSTALLMENT_FEE_AMOUNT_PERCENTAGE, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_PERCENT_LATE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST.getName(), CHARGE_TIME_TYPE_OVERDUE_FEES,
                                CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST, CHARGE_AMOUNT_OVERDUE_PERCENTAGE, true,
                                true)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_CLIENT_FIXED_FEE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_CLIENT, ChargeProductType.CLIENT_TEST_CHARGE_FEE.getName(),
                                CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENT_CHARGE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_DISBURSEMENT_CHARGE.getName(),
                                CHARGE_TIME_TYPE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_AMOUNT_FLAT, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_CHARGE_FLAT_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT.getName(),
                                CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_FLAT, 10.0, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT.getName(),
                                CHARGE_TIME_TYPE_TRANCHE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_DISBURSEMENT_AMOUNT, 2.0, true,
                                false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_FLAT_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN, ChargeProductType.LOAN_INSTALLMENT_FEE_FLAT.getName(),
                                CHARGE_TIME_TYPE_INSTALLMENT, CHARGE_CALCULATION_TYPE_FLAT, CHARGE_INSTALLMENT_FEE_AMOUNT_FLAT, true,
                                false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT.getName(), CHARGE_TIME_TYPE_INSTALLMENT,
                                CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, CHARGE_INSTALLMENT_FEE_AMOUNT_PERCENTAGE, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST.getName(), CHARGE_TIME_TYPE_INSTALLMENT,
                                CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, CHARGE_AMOUNT_PERCENTAGE, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_LOAN_DISBURSEMENT_PERCENTAGE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, CHARGE_APPLIES_TO_LOAN,
                                ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_AMOUNT_PLUS_INTEREST_FEE.getName(),
                                CHARGE_TIME_TYPE_DISBURSEMENT, CHARGE_CALCULATION_TYPE_PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST,
                                CHARGE_AMOUNT_PERCENTAGE, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, ChargeProductAppliesTo.WORKING_CAPITAL_LOAN,
                                ChargeProductType.WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE.getName(), CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE,
                                CHARGE_CALCULATION_TYPE_FLAT, 15.0, true, false)),
                () -> TestContext.INSTANCE.set(TestContextKey.CHARGE_FOR_WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY_CREATE_RESPONSE,
                        createChargeIfNotExists(charges, ChargeProductAppliesTo.WORKING_CAPITAL_LOAN,
                                ChargeProductType.WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY.getName(), CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE,
                                CHARGE_CALCULATION_TYPE_FLAT, 20.0, true, true)));

        ParallelExecutionHelper.runInParallel(items);
    }

    private ChargeCreateResponse createChargeIfNotExists(List<ChargeData> existingCharges, Enum<ChargeProductAppliesTo> appliesTo,
            String name, Integer chargeTimeType, Integer chargeCalculationType, Double amount, Boolean isActive, Boolean isPenalty) {
        ChargeCreateRequest request = defaultChargesRequest(appliesTo, name, chargeTimeType, chargeCalculationType, amount, isActive,
                isPenalty);

        try {
            return ok(() -> fineractClient.charges().createCharge(request, Map.of()));
        } catch (CallFailedRuntimeException e) {
            if (e.getStatus() == 403 && e.getDeveloperMessage() != null && e.getDeveloperMessage().contains("already exists")) {
                log.debug("Charge '{}' already exists, retrieving existing charge", name);
                ChargeData existing = existingCharges.stream().filter(c -> name.equals(c.getName())).findFirst().orElse(null);
                if (existing != null) {
                    ChargeCreateResponse response = new ChargeCreateResponse();
                    response.setResourceId(existing.getId());
                    return response;
                }
            }
            throw e;
        }
    }

    public static ChargeCreateRequest defaultChargesRequest(Enum<ChargeProductAppliesTo> appliesTo, String name, Integer chargeTimeType,
            Integer chargeCalculationType, Double amount, Boolean isActive, Boolean isPenalty) {
        ChargeCreateRequest request = new ChargeCreateRequest();
        Integer chargeAppliesTo;

        if (appliesTo.equals(ChargeProductAppliesTo.CLIENT)) {
            chargeAppliesTo = ChargeProductAppliesTo.CLIENT.value;
        } else if (appliesTo.equals(ChargeProductAppliesTo.LOAN)) {
            chargeAppliesTo = ChargeProductAppliesTo.LOAN.value;
            request.chargePaymentMode(CHARGE_PAYMENT_MODE);
        } else if (appliesTo.equals(ChargeProductAppliesTo.WORKING_CAPITAL_LOAN)) {
            chargeAppliesTo = ChargeProductAppliesTo.WORKING_CAPITAL_LOAN.value;
            request.chargePaymentMode(CHARGE_PAYMENT_MODE);
        } else {
            throw new IllegalArgumentException(ErrorMessageHelper.chargeAppliesToIsInvalid(appliesTo));
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
