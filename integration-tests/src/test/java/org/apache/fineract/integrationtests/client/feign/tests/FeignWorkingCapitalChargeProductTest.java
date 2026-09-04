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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.EnumOptionData;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PutChargesChargeIdResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class FeignWorkingCapitalChargeProductTest extends FeignIntegrationTest {

    private static final long APPLIES_TO_LOAN = 1L;
    private static final long APPLIES_TO_WORKING_CAPITAL_LOAN = 5L;
    private static final int APPLIES_TO_OPTION_COUNT = 5;

    private static final int TIME_TYPE_DISBURSEMENT = 1;
    private static final String TIME_TYPE_DISBURSEMENT_CODE = "chargeTimeType.disbursement";
    private static final String TIME_TYPE_DISBURSEMENT_LABEL = "Disbursement";
    private static final int TIME_TYPE_SPECIFIED_DUE_DATE = 2;
    private static final String TIME_TYPE_SPECIFIED_DUE_DATE_CODE = "chargeTimeType.specifiedDueDate";
    private static final String TIME_TYPE_SPECIFIED_DUE_DATE_LABEL = "Specified due date";
    private static final int TIME_TYPE_INSTALMENT_FEE = 8;
    private static final int LEGACY_TIME_TYPE_OPTION_COUNT = 15;

    private static final int CALCULATION_FLAT = 1;
    private static final String CALCULATION_FLAT_CODE = "chargeCalculationType.flat";
    private static final String CALCULATION_FLAT_LABEL = "Flat";
    private static final int CALCULATION_PERCENT_OF_AMOUNT = 2;
    private static final String CALCULATION_PERCENT_OF_AMOUNT_CODE = "chargeCalculationType.percent.of.amount";
    private static final String CALCULATION_PERCENT_OF_AMOUNT_LABEL = "% Amount";
    private static final int CALCULATION_PERCENT_OF_AMOUNT_AND_INTEREST = 3;
    private static final int LEGACY_CALCULATION_OPTION_COUNT = 5;

    private static final int PAYMENT_MODE_REGULAR = 0;
    private static final String PAYMENT_MODE_REGULAR_CODE = "chargepaymentmode.regular";
    private static final int PAYMENT_MODE_ACCOUNT_TRANSFER = 1;

    private static final String CURRENCY_CODE = "USD";

    private static final String TIME_TYPE_ERROR_CODE = "validation.msg.charge.chargeTimeType.is.not.one.of.expected.enumerations";
    private static final String CALCULATION_ERROR_CODE = "validation.msg.charge.chargeCalculationType.is.not.one.of.expected.enumerations";
    private static final String CALCULATION_ERROR_CODE_ON_UPDATE = "validation.msg.charges.chargeCalculationType.is.not.one.of.expected.enumerations";
    private static final String TIME_TYPE_ERROR_CODE_ON_UPDATE = "validation.msg.charges.chargeTimeType.is.not.one.of.expected.enumerations";
    private static final String PAYMENT_MODE_ERROR_CODE = "validation.msg.charge.chargePaymentMode.is.not.one.of.expected.enumerations";
    private static final String PAYMENT_MODE_ERROR_CODE_ON_UPDATE = "validation.msg.charges.chargePaymentMode.is.not.one.of.expected.enumerations";
    private static final String WC_LOAN_CHARGE_TIME_TYPE_UNSUPPORTED = "error.msg.wc.loan.charge.time.type.not.supported";

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String LOAN_DATE = "01 January 2026";

    private FeignChargesHelper chargesHelper;
    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdChargeIds = new ArrayList<>();
    private final List<Long> createdLoanIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        chargesHelper = new FeignChargesHelper(fineractClient());
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanup() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdChargeIds.forEach(this::deleteChargeIfPossible);
        createdChargeIds.clear();
    }

    @Test
    @Order(1)
    void createWorkingCapitalDisbursementFlatCharge_succeeds() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                "chargeAppliesTo=WCP Loans must accept chargeTimeType=Disbursement");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(APPLIES_TO_WORKING_CAPITAL_LOAN, charge.getChargeAppliesTo().getId(), "chargeAppliesTo must remain WCP Loans (5)");
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "chargeTimeType must be Disbursement (1)");
        assertEquals(TIME_TYPE_DISBURSEMENT_CODE, charge.getChargeTimeType().getCode(), "chargeTimeType code");
        assertId(CALCULATION_FLAT, charge.getChargeCalculationType().getId(), "chargeCalculationType must be Flat (1)");
        assertEquals(CALCULATION_FLAT_CODE, charge.getChargeCalculationType().getCode(), "chargeCalculationType code");
        assertEquals(20.0, charge.getAmount().doubleValue(), "amount must round-trip");
        assertTrue(charge.getActive(), "the charge must be created active");
        assertEquals(Boolean.FALSE, charge.getPenalty(), "a fee, not a penalty");
        assertEquals(CURRENCY_CODE, charge.getCurrency().getCode(), "currency");
    }

    @Test
    @Order(2)
    void createWorkingCapitalDisbursementPercentOfAmountCharge_succeeds() {
        final Long chargeId = createExpectingSuccess(
                WorkingCapitalLoanRequestBuilders.disbursementCharge(CALCULATION_PERCENT_OF_AMOUNT, false, 5.0),
                "WCP Loans + Disbursement must accept chargeCalculationType='% amount'");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "chargeTimeType must be Disbursement (1)");
        assertId(CALCULATION_PERCENT_OF_AMOUNT, charge.getChargeCalculationType().getId(), "chargeCalculationType must be % Amount (2)");
        assertEquals(CALCULATION_PERCENT_OF_AMOUNT_CODE, charge.getChargeCalculationType().getCode(), "chargeCalculationType code");
        assertEquals(5.0, charge.getAmount().doubleValue(), "the percentage rate is stored verbatim in m_charge.amount");
    }

    @Test
    @Order(3)
    void createWorkingCapitalDisbursementFlatPenalty_succeeds() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(true, 20.0),
                "a working capital charge product supports the penalty flag");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertEquals(Boolean.TRUE, charge.getPenalty(), "the penalty flag must round-trip");
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "chargeTimeType must be Disbursement (1)");
        assertId(CALCULATION_FLAT, charge.getChargeCalculationType().getId(), "chargeCalculationType must be Flat (1)");
    }

    @Test
    @Order(4)
    void workingCapitalDisbursementCharge_withoutPaymentMode_defaultsToRegular() {
        final ChargeRequest request = WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0);
        assertNull(request.getChargePaymentMode(), "the request must genuinely omit chargePaymentMode");

        final Long chargeId = createExpectingSuccess(request, "chargePaymentMode may be omitted on a WC charge");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(PAYMENT_MODE_REGULAR, charge.getChargePaymentMode().getId(), "an omitted chargePaymentMode must default to Regular");
        assertEquals(PAYMENT_MODE_REGULAR_CODE, charge.getChargePaymentMode().getCode(), "chargePaymentMode code");
    }

    @Test
    @Order(5)
    void workingCapitalDisbursementCharge_withExplicitRegularPaymentMode_isAccepted() {
        final Long chargeId = createExpectingSuccess(
                WorkingCapitalLoanRequestBuilders.disbursementChargeWithPaymentMode(PAYMENT_MODE_REGULAR, false, 20.0),
                "'Regular' is the one allowed chargePaymentMode for a WC charge");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(PAYMENT_MODE_REGULAR, charge.getChargePaymentMode().getId(), "explicit Regular must be stored as Regular (0)");
    }

    @Test
    @Order(6)
    void updateAmountOnWorkingCapitalDisbursementCharge_persistsNewAmount() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                "amount is a maintainable parameter of a WC Disbursement charge");

        final PutChargesChargeIdResponse updateResponse = chargesHelper.updateCharge(chargeId,
                WorkingCapitalLoanRequestBuilders.updateChargeAmount(30.0));
        assertNotNull(updateResponse.getChanges(), "PUT must report what it changed");
        assertEquals(30.0, updateResponse.getChanges().getAmount().doubleValue(), "PUT must report the new amount");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertEquals(30.0, charge.getAmount().doubleValue(), "the new amount must be persisted");
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "chargeTimeType must be untouched by an amount-only update");
    }

    @Test
    @Order(7)
    void template_forWorkingCapitalAppliesTo_offersDisbursementAndSpecifiedDueDate() {
        final ChargeData template = chargesHelper.getChargeTemplate(APPLIES_TO_WORKING_CAPITAL_LOAN, null);

        final List<EnumOptionData> timeTypes = template.getChargeTimeTypeOptions();
        assertEquals(2, timeTypes.size(), "WC time types must be exactly Disbursement and Specified due date");
        assertOption(timeTypes.get(0), TIME_TYPE_DISBURSEMENT, TIME_TYPE_DISBURSEMENT_CODE, TIME_TYPE_DISBURSEMENT_LABEL);
        assertOption(timeTypes.get(1), TIME_TYPE_SPECIFIED_DUE_DATE, TIME_TYPE_SPECIFIED_DUE_DATE_CODE, TIME_TYPE_SPECIFIED_DUE_DATE_LABEL);

        final List<EnumOptionData> calculationTypes = template.getChargeCalculationTypeOptions();
        assertEquals(2, calculationTypes.size(), "the aggregate WC calculation list is the union Flat + % Amount");
        assertOption(calculationTypes.get(0), CALCULATION_FLAT, CALCULATION_FLAT_CODE, CALCULATION_FLAT_LABEL);
        assertOption(calculationTypes.get(1), CALCULATION_PERCENT_OF_AMOUNT, CALCULATION_PERCENT_OF_AMOUNT_CODE,
                CALCULATION_PERCENT_OF_AMOUNT_LABEL);

        final List<EnumOptionData> paymentModes = template.getChargePaymetModeOptions();
        assertEquals(1, paymentModes.size(), "WC payment modes stay Regular-only");
        assertOption(paymentModes.get(0), PAYMENT_MODE_REGULAR, PAYMENT_MODE_REGULAR_CODE, "Regular");

        assertEquals(APPLIES_TO_OPTION_COUNT, template.getChargeAppliesToOptions().size(), "chargeAppliesTo dropdown is unchanged");
        assertTrue(idsOf(template.getChargeAppliesToOptions()).contains(APPLIES_TO_WORKING_CAPITAL_LOAN),
                "WCP Loans (5) must be offered as an applies-to option");
    }

    @Test
    @Order(8)
    void template_forWorkingCapitalDisbursement_offersFlatAndPercentOfAmount() {
        final ChargeData template = chargesHelper.getChargeTemplate(APPLIES_TO_WORKING_CAPITAL_LOAN, (long) TIME_TYPE_DISBURSEMENT);

        final List<EnumOptionData> calculationTypes = template.getChargeCalculationTypeOptions();
        assertEquals(2, calculationTypes.size(), "WC + Disbursement must offer exactly Flat and % Amount");
        assertOption(calculationTypes.get(0), CALCULATION_FLAT, CALCULATION_FLAT_CODE, CALCULATION_FLAT_LABEL);
        assertOption(calculationTypes.get(1), CALCULATION_PERCENT_OF_AMOUNT, CALCULATION_PERCENT_OF_AMOUNT_CODE,
                CALCULATION_PERCENT_OF_AMOUNT_LABEL);
    }

    @Test
    @Order(9)
    void singleChargeTemplate_forWorkingCapitalDisbursementCharge_offersFlatAndPercentOfAmount() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                "a WC Disbursement charge product must exist before its own template can be read");

        final ChargeData template = chargesHelper.getChargeWithTemplate(chargeId);
        assertId(TIME_TYPE_DISBURSEMENT, template.getChargeTimeType().getId(), "the charge itself is a Disbursement charge");
        final List<EnumOptionData> calculationTypes = template.getChargeCalculationTypeOptions();
        assertEquals(2, calculationTypes.size(), "the single-charge template must offer Flat and % Amount");
        assertOption(calculationTypes.get(0), CALCULATION_FLAT, CALCULATION_FLAT_CODE, CALCULATION_FLAT_LABEL);
        assertOption(calculationTypes.get(1), CALCULATION_PERCENT_OF_AMOUNT, CALCULATION_PERCENT_OF_AMOUNT_CODE,
                CALCULATION_PERCENT_OF_AMOUNT_LABEL);
    }

    @Test
    @Order(10)
    void createWorkingCapitalDisbursementCharge_withPercentOfAmountAndInterest_isRejected() {
        final CallFailedRuntimeException failure = chargesHelper.createChargeExpectingError(
                WorkingCapitalLoanRequestBuilders.disbursementCharge(CALCULATION_PERCENT_OF_AMOUNT_AND_INTEREST, false, 5.0));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(CALCULATION_ERROR_CODE), errorCodesOf(failure),
                "only the calculation type is invalid — Disbursement is a valid WC time type");
        assertEquals("The parameter `chargeCalculationType` must be one of [ 1, 2 ] .", messageOf(failure, CALCULATION_ERROR_CODE),
                "the advertised allow-list is [ FLAT, PERCENT_OF_AMOUNT ] in ordinal order");
    }

    @Test
    @Order(11)
    void createWorkingCapitalCharge_withInstalmentFeeTimeType_isRejected() {
        final CallFailedRuntimeException failure = chargesHelper.createChargeExpectingError(
                WorkingCapitalLoanRequestBuilders.workingCapitalCharge(TIME_TYPE_INSTALMENT_FEE, CALCULATION_FLAT, false, 20.0));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(TIME_TYPE_ERROR_CODE, CALCULATION_ERROR_CODE), errorCodesOf(failure),
                "an unsupported WC time type leaves no legal calculation type, so both parameters are reported");
        assertEquals("The parameter `chargeTimeType` must be one of [ 1, 2 ] .", messageOf(failure, TIME_TYPE_ERROR_CODE),
                "the advertised allow-list is [ DISBURSEMENT, SPECIFIED_DUE_DATE ] in ordinal order");
        assertEquals("The parameter `chargeCalculationType` must be one of [  ] .", messageOf(failure, CALCULATION_ERROR_CODE),
                "an unsupported time type advertises an empty calculation allow-list -- the lookup fails closed");
    }

    @Test
    @Order(12)
    void createWorkingCapitalSpecifiedDueDateFlatCharge_stillSucceeds() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 20.0),
                "WCP Loans + Specified due date + Flat stays valid");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(TIME_TYPE_SPECIFIED_DUE_DATE, charge.getChargeTimeType().getId(), "chargeTimeType must be Specified due date (2)");
        assertId(CALCULATION_FLAT, charge.getChargeCalculationType().getId(), "chargeCalculationType must be Flat (1)");
        assertId(PAYMENT_MODE_REGULAR, charge.getChargePaymentMode().getId(), "WC charges default to Regular");
    }

    @Test
    @Order(13)
    void createWorkingCapitalSpecifiedDueDateCharge_withPercentOfAmount_stillRejected() {
        final CallFailedRuntimeException failure = chargesHelper.createChargeExpectingError(
                WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(CALCULATION_PERCENT_OF_AMOUNT, false, 5.0));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(CALCULATION_ERROR_CODE), errorCodesOf(failure), "only the calculation type is invalid");
        assertEquals("The parameter `chargeCalculationType` must be one of [ 1 ] .", messageOf(failure, CALCULATION_ERROR_CODE),
                "Specified due date must keep advertising Flat only");
    }

    @Test
    @Order(14)
    void template_forWorkingCapitalSpecifiedDueDate_stillOffersFlatOnly() {
        final ChargeData template = chargesHelper.getChargeTemplate(APPLIES_TO_WORKING_CAPITAL_LOAN, (long) TIME_TYPE_SPECIFIED_DUE_DATE);

        final List<EnumOptionData> calculationTypes = template.getChargeCalculationTypeOptions();
        assertEquals(1, calculationTypes.size(), "WC + Specified due date stays Flat-only");
        assertOption(calculationTypes.get(0), CALCULATION_FLAT, CALCULATION_FLAT_CODE, CALCULATION_FLAT_LABEL);
    }

    @Test
    @Order(15)
    void template_withoutFilters_isUnchanged() {
        final ChargeData template = chargesHelper.getChargeTemplate(null, null);

        assertEquals(LEGACY_CALCULATION_OPTION_COUNT, template.getChargeCalculationTypeOptions().size(),
                "the legacy calculation dropdown lists every ChargeCalculationType except INVALID");
        assertEquals(LEGACY_TIME_TYPE_OPTION_COUNT, template.getChargeTimeTypeOptions().size(),
                "the legacy time-type dropdown lists every ChargeTimeType except INVALID and SAVINGS_CLOSURE");
        assertEquals(List.of((long) PAYMENT_MODE_REGULAR, (long) PAYMENT_MODE_ACCOUNT_TRANSFER),
                idsOf(template.getChargePaymetModeOptions()), "the legacy payment-mode dropdown keeps both modes");
    }

    @Test
    @Order(16)
    void createTermLoanDisbursementPercentOfAmountCharge_stillSucceeds() {
        final Long chargeId = createExpectingSuccess(ChargeRequestBuilders.loanDisbursementPercentageFee(5.0),
                "term-loan charges are unaffected by the working capital rules");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(APPLIES_TO_LOAN, charge.getChargeAppliesTo().getId(), "chargeAppliesTo must be Loan (1)");
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "chargeTimeType must be Disbursement (1)");
        assertId(CALCULATION_PERCENT_OF_AMOUNT, charge.getChargeCalculationType().getId(), "chargeCalculationType must be % Amount (2)");
    }

    @Test
    @Order(17)
    void addWorkingCapitalDisbursementChargeToLoanAccount_isRejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                    "the WC Disbursement charge product must exist before the account guard can be exercised");
            final Long loanId = createActiveWorkingCapitalLoan();

            final CallFailedRuntimeException failure = wcLoanHelper.addChargeExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.addChargeWithoutDueDate(chargeId, 20.0));

            assertEquals(403, failure.getStatus(), "a domain-rule rejection is reported as 403");
            assertEquals(List.of(WC_LOAN_CHARGE_TIME_TYPE_UNSUPPORTED), errorCodesOf(failure),
                    "the account path must reject an unsupported WC charge time type");
            assertTrue(wcLoanHelper.getCharges(loanId).isEmpty(), "no charge may be persisted on the account");
        });
    }

    @Test
    @Order(18)
    void updateAmountOnWorkingCapitalDisbursementPenalty_persistsNewAmount() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(true, 20.0),
                "a WC Disbursement penalty product must be maintainable after creation");

        final PutChargesChargeIdResponse updateResponse = chargesHelper.updateCharge(chargeId,
                WorkingCapitalLoanRequestBuilders.updateChargeAmount(30.0));
        assertNotNull(updateResponse.getChanges(), "PUT must report what it changed");
        assertEquals(30.0, updateResponse.getChanges().getAmount().doubleValue(), "PUT must report the new amount");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertEquals(30.0, charge.getAmount().doubleValue(), "the new amount must be persisted");
        assertEquals(Boolean.TRUE, charge.getPenalty(), "the penalty flag must survive an amount-only update");
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "chargeTimeType must be untouched by an amount-only update");
    }

    @Test
    @Order(19)
    void updateWorkingCapitalCharge_toSpecifiedDueDateAndFlatTogether_succeeds() {
        final Long chargeId = createExpectingSuccess(
                WorkingCapitalLoanRequestBuilders.disbursementCharge(CALCULATION_PERCENT_OF_AMOUNT, false, 5.0),
                "the {Disbursement, % Amount} product is the starting state of this update");

        final PutChargesChargeIdResponse updateResponse = chargesHelper.updateCharge(chargeId,
                WorkingCapitalLoanRequestBuilders.updateChargeTimeAndCalculationType(TIME_TYPE_SPECIFIED_DUE_DATE, CALCULATION_FLAT));
        assertNotNull(updateResponse.getChanges(), "PUT must report what it changed");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(TIME_TYPE_SPECIFIED_DUE_DATE, charge.getChargeTimeType().getId(), "chargeTimeType must be Specified due date (2)");
        assertId(CALCULATION_FLAT, charge.getChargeCalculationType().getId(), "chargeCalculationType must be Flat (1)");
    }

    @Test
    @Order(20)
    void updateWorkingCapitalSpecifiedDueDateCharge_toPercentOfAmountOnly_isRejected() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 20.0),
                "the {Specified due date, Flat} product is the starting state of this update");

        final CallFailedRuntimeException failure = chargesHelper.updateChargeExpectingError(chargeId,
                WorkingCapitalLoanRequestBuilders.updateChargeCalculationType(CALCULATION_PERCENT_OF_AMOUNT));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(CALCULATION_ERROR_CODE_ON_UPDATE), errorCodesOf(failure), "only the calculation type is invalid");
        assertEquals("The parameter `chargeCalculationType` must be one of [ 1 ] .", messageOf(failure, CALCULATION_ERROR_CODE_ON_UPDATE),
                "Specified due date must keep advertising Flat only on the update path too");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(CALCULATION_FLAT, charge.getChargeCalculationType().getId(), "the rejected update must not have been persisted");
    }

    @Test
    @Order(21)
    void createWorkingCapitalDisbursementCharge_withAccountTransferPaymentMode_isRejected() {
        final CallFailedRuntimeException failure = chargesHelper.createChargeExpectingError(
                WorkingCapitalLoanRequestBuilders.disbursementChargeWithPaymentMode(PAYMENT_MODE_ACCOUNT_TRANSFER, false, 20.0));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(PAYMENT_MODE_ERROR_CODE), errorCodesOf(failure), "only the payment mode is invalid");
        assertEquals("The parameter `chargePaymentMode` must be one of [ 0 ] .", messageOf(failure, PAYMENT_MODE_ERROR_CODE),
                "Regular (0) is the one payment mode a WC charge product may carry");
    }

    @Test
    @Order(22)
    void updateWorkingCapitalDisbursementCharge_toAccountTransferPaymentMode_isRejected() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                "the {Disbursement, Flat, Regular} product is the starting state of this update");

        final CallFailedRuntimeException failure = chargesHelper.updateChargeExpectingError(chargeId,
                WorkingCapitalLoanRequestBuilders.updateChargePaymentMode(PAYMENT_MODE_ACCOUNT_TRANSFER));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(PAYMENT_MODE_ERROR_CODE_ON_UPDATE), errorCodesOf(failure), "only the payment mode is invalid");
        assertEquals("The parameter `chargePaymentMode` must be one of [ 0 ] .", messageOf(failure, PAYMENT_MODE_ERROR_CODE_ON_UPDATE),
                "Regular (0) stays the one payment mode a WC charge product may carry on the update path too");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(PAYMENT_MODE_REGULAR, charge.getChargePaymentMode().getId(), "the rejected update must not have been persisted");
    }

    @Test
    @Order(23)
    void createWorkingCapitalDisbursementCharge_withExplicitNullPaymentMode_defaultsToRegular() {
        final String body = """
                {"chargeAppliesTo": %d, "chargeTimeType": %d, "chargeCalculationType": %d, "chargePaymentMode": null,
                 "name": "%s", "amount": 20, "active": true, "penalty": false, "currencyCode": "%s", "locale": "en"}
                """.formatted(APPLIES_TO_WORKING_CAPITAL_LOAN, TIME_TYPE_DISBURSEMENT, CALCULATION_FLAT,
                Utils.uniqueRandomStringGenerator("WCL_CHARGE_", 8), CURRENCY_CODE);

        final String response = FeignRawHttpHelper.post("/charges", body);
        final Long chargeId = JsonParser.parseString(response).getAsJsonObject().get("resourceId").getAsLong();
        createdChargeIds.add(chargeId);

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(PAYMENT_MODE_REGULAR, charge.getChargePaymentMode().getId(),
                "an explicit null payment mode must fall back to Regular (0), exactly like an omitted one");
        assertEquals(PAYMENT_MODE_REGULAR_CODE, charge.getChargePaymentMode().getCode(), "payment mode code");
    }

    @Test
    @Order(24)
    void updateWorkingCapitalDisbursementCharge_toInstalmentFeeTimeType_isRejected() {
        final Long chargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                "the {Disbursement, Flat} product is the starting state of this update");

        final CallFailedRuntimeException failure = chargesHelper.updateChargeExpectingError(chargeId,
                WorkingCapitalLoanRequestBuilders.updateChargeTimeType(TIME_TYPE_INSTALMENT_FEE));

        assertEquals(400, failure.getStatus(), "a validation rejection is reported as 400");
        assertEquals(List.of(TIME_TYPE_ERROR_CODE_ON_UPDATE, CALCULATION_ERROR_CODE_ON_UPDATE), errorCodesOf(failure),
                "the time type is rejected, and no calculation type is valid for an unsupported time type");
        assertEquals("The parameter `chargeTimeType` must be one of [ 1, 2 ] .", messageOf(failure, TIME_TYPE_ERROR_CODE_ON_UPDATE),
                "the update path must advertise the widened WC allow-list");

        final GetChargesResponse charge = chargesHelper.getCharge(chargeId);
        assertId(TIME_TYPE_DISBURSEMENT, charge.getChargeTimeType().getId(), "the rejected update must not have been persisted");
    }

    @Test
    @Order(25)
    void workingCapitalLoanAccountChargeTemplate_hidesDisbursementChargeProduct() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long disbursementChargeId = createExpectingSuccess(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 20.0),
                    "the WC Disbursement charge product must exist before the template can be asked to hide it");
            final Long specifiedDueDateChargeId = createExpectingSuccess(
                    WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 20.0),
                    "the WC Specified-due-date charge product is the positive control");
            final Long loanId = createActiveWorkingCapitalLoan();

            final List<ChargeData> chargeOptions = wcLoanHelper.getChargeTemplateOptions(loanId);

            final List<Long> ownOptionIds = chargeOptions.stream().map(ChargeData::getId)
                    .filter(id -> disbursementChargeId.equals(id) || specifiedDueDateChargeId.equals(id)).toList();
            assertEquals(List.of(specifiedDueDateChargeId), ownOptionIds,
                    "the account charge template must offer the Specified-due-date product (id " + specifiedDueDateChargeId
                            + ") and must not offer the Disbursement product (id " + disbursementChargeId
                            + "), which POST /working-capital-loans/{loanId}/charges rejects with 403");

            final List<Long> offeredTimeTypeIds = chargeOptions.stream().map(charge -> charge.getChargeTimeType().getId()).distinct()
                    .toList();
            assertEquals(List.of((long) TIME_TYPE_SPECIFIED_DUE_DATE), offeredTimeTypeIds,
                    "Specified due date (2) is the only charge time type a WC loan account can process, so it must be the only one"
                            + " the account template advertises");
        });
    }

    private Long createExpectingSuccess(final ChargeRequest request, final String expectation) {
        final PostChargesResponse response;
        try {
            response = chargesHelper.createCharge(request);
        } catch (final CallFailedRuntimeException failure) {
            throw new AssertionError("Expected POST /charges to be accepted — " + expectation + " — but the platform answered HTTP "
                    + failure.getStatus() + ": " + failure.getResponseBody(), failure);
        }
        assertNotNull(response.getResourceId(), "POST /charges must return the new charge id");
        createdChargeIds.add(response.getResourceId());
        return response.getResourceId();
    }

    private Long createActiveWorkingCapitalLoan() {
        final Long clientId = clientHelper.createClient(LOAN_DATE);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL Charge " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                .getResourceId();
        final BigDecimal principal = BigDecimal.valueOf(9000);
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                principal, BigDecimal.valueOf(18), LOAN_DATE, LOAN_DATE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, principal, LOAN_DATE));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, principal));
        return loanId;
    }

    private void deleteChargeIfPossible(final Long chargeId) {
        try {
            chargesHelper.deleteCharge(chargeId);
        } catch (final CallFailedRuntimeException ignored) {
            // best-effort cleanup
        }
    }

    private static void assertId(final long expected, final Long actual, final String message) {
        assertEquals(Long.valueOf(expected), actual, message);
    }

    private static List<Long> idsOf(final List<EnumOptionData> options) {
        return options.stream().map(EnumOptionData::getId).toList();
    }

    private static void assertOption(final EnumOptionData option, final int expectedId, final String expectedCode,
            final String expectedLabel) {
        assertId(expectedId, option.getId(), "dropdown option id at this position");
        assertEquals(expectedCode, option.getCode(), "option code for id " + expectedId);
        assertEquals(expectedLabel, option.getValue(), "option label for id " + expectedId);
    }

    private static String messageOf(final CallFailedRuntimeException failure, final String globalisationCode) {
        final String body = failure.getResponseBody();
        final JsonElement root = JsonParser.parseString(body);
        assertTrue(root.isJsonObject(), "expected a JSON error body but got: " + body);
        final JsonElement errors = root.getAsJsonObject().get("errors");
        assertTrue(errors != null && errors.isJsonArray(), "expected an 'errors' array in: " + body);
        for (final JsonElement error : errors.getAsJsonArray()) {
            final JsonObject errorObject = error.getAsJsonObject();
            final JsonElement code = errorObject.get("userMessageGlobalisationCode");
            if (code != null && !code.isJsonNull() && globalisationCode.equals(code.getAsString())) {
                return errorObject.get("defaultUserMessage").getAsString();
            }
        }
        throw new AssertionError("No error with code " + globalisationCode + " in: " + body);
    }
}
