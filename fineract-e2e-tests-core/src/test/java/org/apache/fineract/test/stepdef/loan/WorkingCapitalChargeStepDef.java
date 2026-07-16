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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.EnumOptionData;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeProductResolver;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.factory.WorkingCapitalChargeRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalChargeStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String DATE_FORMAT_API = "dd-MM-yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter FORMATTER_API = DateTimeFormatter.ofPattern(DATE_FORMAT_API);
    private static final Long REGULAR_PAYMENT_MODE_ID = 0L;
    private static final Long SPECIFIED_DUE_DATE_ID = 2L;
    private static final Long FLAT_CALCULATION_TYPE_ID = 1L;

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalChargeRequestFactory chargeRequestFactory;
    private final ChargeProductResolver chargeProductResolver;

    @When("Admin creates working capital loan charge")
    public void createWorkingCapitalLoanCharge() {
        createChargeAndStore(chargeRequestFactory.defaultWorkingCapitalChargeRequest());
    }

    @When("Admin creates working capital loan charge as penalty")
    public void createWorkingCapitalLoanChargeAsPenalty() {
        createChargeAndStore(chargeRequestFactory.defaultWorkingCapitalChargeRequest().penalty(true).amount(15.0D));
    }

    @When("Admin creates working capital loan charge without payment mode")
    public void createWorkingCapitalLoanChargeWithoutPaymentMode() {
        createChargeAndStore(chargeRequestFactory.defaultWorkingCapitalChargeRequest().amount(25.0D).chargePaymentMode(null));
    }

    @When("Admin creates working capital loan charge with {string} charge time type and {string} calculation type")
    public void createWorkingCapitalLoanChargeWithParams(String chargeTimeTypeName, String chargeCalcTypeName) {
        final ChargeTimeType timeType = ChargeTimeType.valueOf(chargeTimeTypeName);
        final ChargeCalculationType calcType = ChargeCalculationType.valueOf(chargeCalcTypeName);
        createChargeAndStore(chargeRequestFactory.defaultWorkingCapitalChargeRequest() //
                .chargeTimeType(timeType.value) //
                .chargeCalculationType(calcType.value));
    }

    @When("Admin updates working capital loan charge")
    public void updateWorkingCapitalLoanCharge() {
        final Long id = getChargeId();
        final ChargeRequest request = chargeRequestFactory.defaultWorkingCapitalChargeRequest().amount(30.0D).penalty(true);
        ok(() -> fineractClient.charges().updateCharge(id, request));
    }

    @When("Admin deletes working capital loan charge")
    public void deleteWorkingCapitalLoanCharge() {
        ok(() -> fineractClient.charges().deleteCharge(getChargeId()));
    }

    @When("Admin fails to delete working capital loan charge with status {int} message {string}")
    public void adminFailsToDeleteWorkingCapitalLoanChargeWithMessage(int expectedHttpCode, String expectedErrorMessage) {
        final CallFailedRuntimeException exception = fail(() -> fineractClient.charges().deleteCharge(getChargeId()));
        assertHttpStatus(exception, expectedHttpCode);
        assertErrorMessage(exception, expectedErrorMessage);
    }

    @Then("Admin retrieves working capital loan charge and verifies it is a penalty")
    public void retrieveAndVerifyPenalty() {
        final Long id = getChargeId();
        final GetChargesResponse chargeData = retrieveCharge(id);
        assertThat(chargeData.getPenalty()).as("Charge should be a penalty").isTrue();
        assertThat(chargeData.getActive()).as("Charge should be active").isTrue();
        log.info("Verified WCL charge ID {} is a penalty", id);
    }

    @Then("Admin retrieves working capital loan charge and verifies payment mode is Regular")
    public void retrieveAndVerifyPaymentModeRegular() {
        final Long id = getChargeId();
        final GetChargesResponse chargeData = retrieveCharge(id);
        assertThat(chargeData.getChargePaymentMode()).as("Charge payment mode should not be null").isNotNull();
        assertThat(chargeData.getChargePaymentMode().getId()).as("Payment mode should be Regular (0)").isEqualTo(REGULAR_PAYMENT_MODE_ID);
        log.info("Verified WCL charge ID {} has Regular payment mode", id);
    }

    @Then("Admin retrieves working capital loan charge template by loan id")
    public void getWorkingCapitalLoanChargesTemplateByLoanId() {
        Long loanId = getLoanId();
        Long chargeId = getChargeId();
        Assertions.assertNotNull(chargeId);

        WorkingCapitalLoanChargeData response = ok(
                () -> fineractClient.workingCapitalLoanCharges().retrieveTemplateWorkingCapitalLoanCharge(loanId));

        Assertions.assertNotNull(response.getChargeOptions());

        boolean anyMatch = response.getChargeOptions().stream().anyMatch(cO -> chargeId.equals(cO.getId()));
        Assertions.assertTrue(anyMatch);

    }

    @Then("Admin add working capital loan charge by loan id and charge id with amount {double} and due date {string}")
    public void admin_add_working_capital_loan_charge_by_loan_id_and_charge_id_with_amount(Double amount, String dueDate) {
        Long loanId = getLoanId();
        Assertions.assertNotNull(loanId);
        Long chargeId = getChargeId();
        Assertions.assertNotNull(chargeId);

        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest() //
                .chargeId(chargeId).amount(amount).dueDate(dueDate).dateFormat("dd-MM-yyyy").locale("en");
        PostLoansLoanIdChargesResponse response = ok(() -> fineractClient.workingCapitalLoanCharges().createLoanCharge(loanId, request));
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());

        addLoanChargeId(response.getResourceId());

    }

    @When("Admin adds {string} specified due date charge to working capital loan with {string} due date and {double} transaction amount")
    public void addWorkingCapitalCharge(String chargeType, String dueDate, Double amount) {
        Long loanId = getLoanId();
        Assertions.assertNotNull(loanId);

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductResolver.resolve(chargeProductType);

        LocalDate dueDateParsed = LocalDate.parse(dueDate, FORMATTER);
        String dueDateFormatted = dueDateParsed.format(FORMATTER_API);

        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest() //
                .chargeId(chargeTypeId)//
                .amount(amount)//
                .dueDate(dueDateFormatted)//
                .dateFormat(DATE_FORMAT_API)//
                .locale("en");//
        PostLoansLoanIdChargesResponse response = ok(() -> fineractClient.workingCapitalLoanCharges().createLoanCharge(loanId, request));
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());

        log.debug("Charge response: {}", response);

        testContext().set(TestContextKey.ADD_DUE_DATE_CHARGE_WORKING_CAPITAL_RESPONSE, response);
    }

    @Then("Working Capital Loan has charges with the following data:")
    public void verifyWorkingCapitalLoanChargesWithData(DataTable table) {
        Long loanId = getLoanId();
        Assertions.assertNotNull(loanId);

        List<WorkingCapitalLoanChargeData> charges = ok(
                () -> fineractClient.workingCapitalLoanCharges().retrieveAllWorkingCapitalLoanChargesByLoanId(loanId));
        Assertions.assertNotNull(charges);

        log.debug("Charges list: {}", charges);

        List<List<String>> data = table.asLists();
        List<String> headers = data.get(0);

        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String dueDateExpected = null;

            for (int j = 0; j < headers.size(); j++) {
                if (headers.get(j).equals("Due Date")) {
                    dueDateExpected = expectedValues.get(j);
                    break;
                }
            }

            final String filterDueDate = dueDateExpected;
            List<WorkingCapitalLoanChargeData> filteredCharges = charges.stream()
                    .filter(charge -> charge.getDueDate() != null && filterDueDate.equals(FORMATTER.format(charge.getDueDate()))).toList();

            List<List<String>> actualValuesList = filteredCharges.stream().map(charge -> fetchValuesOfCharge(headers, charge)).toList();

            List<String> convertedExpectedValues = new ArrayList<>();
            for (int j = 0; j < headers.size(); j++) {
                convertedExpectedValues.add(convertToApiCode(headers.get(j), expectedValues.get(j)));
            }

            boolean containsExpectedValues = actualValuesList.stream()
                    .anyMatch(actualValues -> actualValues.equals(convertedExpectedValues));

            assertThat(containsExpectedValues).as(
                    ErrorMessageHelper.wrongValueInLineInChargesTab(String.valueOf(loanId), i, actualValuesList, convertedExpectedValues))
                    .isTrue();
        }
    }

    @Then("Working Capital Loan charge balances has the following data:")
    public void verifyWorkingCapitalLoanChargeBalances(DataTable table) {
        Long loanId = getLoanId();
        Assertions.assertNotNull(loanId);

        final GetWorkingCapitalLoansLoanIdResponse loanResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        Assertions.assertNotNull(loanResponse);
        Assertions.assertNotNull(loanResponse.getBalance());

        List<List<String>> data = table.asLists();
        List<String> headers = data.get(0);

        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            List<String> actualValues = fetchChargeBalanceValues(headers, loanResponse.getBalance());

            assertThat(actualValues)
                    .as(String.format("%nWrong value in Working Capital Loan charge balances of loan %s line %s."
                            + "%nActual values: %s %nExpected values: %s", loanId, i, actualValues, expectedValues))
                    .isEqualTo(expectedValues);
        }
    }

    private List<String> fetchChargeBalanceValues(final List<String> header, final GetBalance balance) {
        final List<String> actualValues = new ArrayList<>();
        for (final String headerName : header) {
            switch (headerName) {
                case "Fee Amount" ->
                    actualValues.add(balance.getFee() == null ? null : new Utils.DoubleFormatter(balance.getFee().doubleValue()).format());
                case "Fee Outstanding" -> actualValues.add(balance.getFeeOutstanding() == null ? null
                        : new Utils.DoubleFormatter(balance.getFeeOutstanding().doubleValue()).format());
                case "Fee Paid" -> actualValues
                        .add(balance.getFeePaid() == null ? null : new Utils.DoubleFormatter(balance.getFeePaid().doubleValue()).format());
                case "Penalty Amount" -> actualValues
                        .add(balance.getPenalty() == null ? null : new Utils.DoubleFormatter(balance.getPenalty().doubleValue()).format());
                case "Penalty Outstanding" -> actualValues.add(balance.getPenaltyOutstanding() == null ? null
                        : new Utils.DoubleFormatter(balance.getPenaltyOutstanding().doubleValue()).format());
                case "Penalty Paid" -> actualValues.add(balance.getPenaltyPaid() == null ? null
                        : new Utils.DoubleFormatter(balance.getPenaltyPaid().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    @Then("Working Capital Loan has the created charges")
    public void verifyWorkingCapitalLoanChargesAreCreated() {
        Long loanId = getLoanId();
        Assertions.assertNotNull(loanId);
        List<WorkingCapitalLoanChargeData> responses = ok(
                () -> fineractClient.workingCapitalLoanCharges().retrieveAllWorkingCapitalLoanChargesByLoanId(loanId));
        Assertions.assertNotNull(responses);
        List<Long> loanChargeIds = getLoanChargeIds();
        for (WorkingCapitalLoanChargeData response : responses) {
            Assertions.assertTrue(loanChargeIds.contains(response.getId()));
        }
        for (Long chargeId : loanChargeIds) {
            ok(() -> fineractClient.workingCapitalLoanCharges().retrieveWorkingCapitalLoanCharge(loanId, chargeId));
        }
    }

    private List<String> fetchValuesOfCharge(final List<String> header, final WorkingCapitalLoanChargeData charge) {
        final List<String> actualValues = new ArrayList<>();
        for (final String headerName : header) {
            switch (headerName) {
                case "Charge Name" -> actualValues.add(charge.getName());
                case "Due Date" -> actualValues.add(charge.getDueDate() == null ? null : FORMATTER.format(charge.getDueDate()));
                case "Amount" -> actualValues
                        .add(charge.getAmount() == null ? null : new Utils.DoubleFormatter(charge.getAmount().doubleValue()).format());
                case "Currency" -> actualValues.add(charge.getCurrency() == null ? null : charge.getCurrency().getCode());
                case "isPenalty" -> actualValues.add(charge.getPenalty() == null ? null : String.valueOf(charge.getPenalty()));
                case "Charge Time Type" ->
                    actualValues.add(charge.getChargeTimeType() == null ? null : charge.getChargeTimeType().getCode());
                case "Charge Calculation Type" ->
                    actualValues.add(charge.getChargeCalculationType() == null ? null : charge.getChargeCalculationType().getCode());
                case "Charge Payment mode" ->
                    actualValues.add(charge.getChargePaymentMode() == null ? null : charge.getChargePaymentMode().getCode());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    private String convertToApiCode(String headerName, String value) {
        if (value == null || "null".equals(value)) {
            return null;
        }
        return switch (headerName) {
            case "Charge Time Type" -> convertChargeTimeTypeToCode(value);
            case "Charge Calculation Type" -> convertChargeCalculationTypeToCode(value);
            case "Charge Payment mode" -> convertChargePaymentModeToCode(value);
            default -> value;
        };
    }

    private String convertChargeTimeTypeToCode(String value) {
        return switch (value) {
            case "Specified due date" -> "chargeTimeType.specifiedDueDate";
            default -> value;
        };
    }

    private String convertChargeCalculationTypeToCode(String value) {
        return switch (value) {
            case "Flat" -> "chargeCalculationType.flat";
            default -> value;
        };
    }

    private String convertChargePaymentModeToCode(String value) {
        return switch (value) {
            case "Regular" -> "chargepaymentmode.regular";
            default -> value;
        };
    }

    @Then("Admin retrieves the charge template for Working Capital Loan")
    public void retrieveChargeTemplateForWcl() {
        final ChargeData templateData = ok(() -> fineractClient.charges()
                .retrieveTemplateCharge(Map.of("chargeAppliesTo", ChargeProductAppliesTo.WORKING_CAPITAL_LOAN.value)));
        testContext().set(TestContextKey.WORKING_CAPITAL_CHARGE_TEMPLATE, templateData);
        log.info("Retrieved charge template for Working Capital Loan");
    }

    @Then("Admin retrieves the charge template for Working Capital Loan with charge time type {string}")
    public void retrieveChargeTemplateForWclWithTimeType(String chargeTimeTypeName) {
        final ChargeTimeType timeType = ChargeTimeType.valueOf(chargeTimeTypeName);
        final ChargeData templateData = ok(() -> fineractClient.charges().retrieveTemplateCharge(
                Map.of("chargeAppliesTo", ChargeProductAppliesTo.WORKING_CAPITAL_LOAN.value, "chargeTimeType", timeType.value)));
        testContext().set(TestContextKey.WORKING_CAPITAL_CHARGE_TEMPLATE, templateData);
        log.info("Retrieved charge template for Working Capital Loan with chargeTimeType={}", chargeTimeTypeName);
    }

    @Then("The charge template chargeTimeTypeOptions contains only Specified due date")
    public void verifyTemplateChargeTimeTypeOptions() {
        assertSingleOption(getChargeTemplate().getChargeTimeTypeOptions(), "chargeTimeTypeOptions", SPECIFIED_DUE_DATE_ID);
        log.info("Verified charge template chargeTimeTypeOptions contains only Specified due date");
    }

    @Then("The charge template chargeCalculationTypeOptions contains only Flat")
    public void verifyTemplateChargeCalculationTypeOptions() {
        assertSingleOption(getChargeTemplate().getChargeCalculationTypeOptions(), "chargeCalculationTypeOptions", FLAT_CALCULATION_TYPE_ID);
        log.info("Verified charge template chargeCalculationTypeOptions contains only Flat");
    }

    @Then("The charge template chargePaymentModeOptions contains only Regular")
    public void verifyTemplateChargePaymentModeOptions() {
        assertSingleOption(getChargeTemplate().getChargePaymetModeOptions(), "chargePaymentModeOptions", REGULAR_PAYMENT_MODE_ID);
        log.info("Verified charge template chargePaymentModeOptions contains only Regular");
    }

    @Then("Creating working capital loan charge with {string} chargeTimeType and {string} chargeCalculationType results an error with the following data:")
    public void createWclChargeWithInvalidParamsFails(String chargeTimeTypeName, String chargeCalcTypeName, DataTable table) {
        final ChargeTimeType timeType = ChargeTimeType.valueOf(chargeTimeTypeName);
        final ChargeCalculationType calcType = ChargeCalculationType.valueOf(chargeCalcTypeName);
        final ChargeRequest request = chargeRequestFactory.defaultWorkingCapitalChargeRequest() //
                .chargeTimeType(timeType.value) //
                .chargeCalculationType(calcType.value);

        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();

        final CallFailedRuntimeException exception = fail(() -> fineractClient.charges().createCharge(request));
        assertHttpStatus(exception, expectedHttpCode);
        assertErrorMessage(exception, expectedErrorMessage);
        log.info("Verified creating WCL charge with chargeTimeType={} and calcType={} failed with status {} and message: {}",
                chargeTimeTypeName, chargeCalcTypeName, exception.getStatus(), expectedErrorMessage);
    }

    @When("Admin makes a charge adjustment for the last added charge with {double} amount on working capital loan")
    public void makeWcChargeAdjustment(final Double amount) {
        final Long loanId = getLoanId();
        final Long loanChargeId = getLastAddedLoanChargeId();
        final PostWorkingCapitalLoansLoanIdChargesChargeIdRequest request = new PostWorkingCapitalLoansLoanIdChargesChargeIdRequest()
                .amount(BigDecimal.valueOf(amount)).locale("en");
        final PostWorkingCapitalLoansLoanIdChargesChargeIdResponse response = ok(
                () -> fineractClient.workingCapitalLoanCharges().adjustLoanCharge(loanId, loanChargeId, request, "adjustment"));
        Assertions.assertNotNull(response);
        testContext().set(TestContextKey.WORKING_CAPITAL_CHARGE_ADJUSTMENT_RESPONSE, response);
        log.debug("WC charge adjustment response: {}", response);
    }

    @When("Admin makes a charge adjustment for the last added fee charge with {double} amount on working capital loan")
    public void makeWcFeeChargeAdjustment(final Double amount) {
        final Long loanId = getLoanId();
        final Long loanChargeId = getLastAddedFeeChargeId(loanId);
        final PostWorkingCapitalLoansLoanIdChargesChargeIdRequest request = new PostWorkingCapitalLoansLoanIdChargesChargeIdRequest()
                .amount(BigDecimal.valueOf(amount)).locale("en");
        final PostWorkingCapitalLoansLoanIdChargesChargeIdResponse response = ok(
                () -> fineractClient.workingCapitalLoanCharges().adjustLoanCharge(loanId, loanChargeId, request, "adjustment"));
        Assertions.assertNotNull(response);
        testContext().set(TestContextKey.WORKING_CAPITAL_CHARGE_ADJUSTMENT_RESPONSE, response);
        log.debug("WC fee charge adjustment response: {}", response);
    }

    @When("Admin makes a charge adjustment for the last added penalty charge with {double} amount on working capital loan")
    public void makeWcPenaltyChargeAdjustment(final Double amount) {
        final Long loanId = getLoanId();
        final Long loanChargeId = getLastAddedPenaltyChargeId(loanId);
        final PostWorkingCapitalLoansLoanIdChargesChargeIdRequest request = new PostWorkingCapitalLoansLoanIdChargesChargeIdRequest()
                .amount(BigDecimal.valueOf(amount)).locale("en");
        final PostWorkingCapitalLoansLoanIdChargesChargeIdResponse response = ok(
                () -> fineractClient.workingCapitalLoanCharges().adjustLoanCharge(loanId, loanChargeId, request, "adjustment"));
        Assertions.assertNotNull(response);
        testContext().set(TestContextKey.WORKING_CAPITAL_CHARGE_ADJUSTMENT_RESPONSE, response);
        log.debug("WC penalty charge adjustment response: {}", response);
    }

    @Then("Making a charge adjustment with {double} amount on working capital loan results an error with the following data:")
    public void makeWcChargeAdjustmentFails(final Double amount, final DataTable table) {
        final Long loanId = getLoanId();
        final Long loanChargeId = getLastAddedLoanChargeId();
        final PostWorkingCapitalLoansLoanIdChargesChargeIdRequest request = new PostWorkingCapitalLoansLoanIdChargesChargeIdRequest()
                .amount(BigDecimal.valueOf(amount)).locale("en");
        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanCharges().adjustLoanCharge(loanId, loanChargeId, request, "adjustment"));
        assertHttpStatus(exception, expectedHttpCode);
        assertErrorMessage(exception, expectedErrorMessage);
        log.info("Verified WC charge adjustment failed with status {} and message: {}", exception.getStatus(), expectedErrorMessage);
    }

    @When("Admin reverts the last charge adjustment on working capital loan")
    public void revertLastWcChargeAdjustment() {
        final Long loanId = getLoanId();
        final GetWorkingCapitalLoanTransactionIdResponse adjustmentTxn = getLastChargeAdjustmentTransaction(loanId, false);
        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();
        ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                adjustmentTxn.getId(), "undo", request));
        log.debug("Reverted WC charge adjustment transaction id={} on loan {}", adjustmentTxn.getId(), loanId);
    }

    @Then("Reverting an already reversed charge adjustment on working capital loan results an error with the following data:")
    public void revertAlreadyRevertedWcChargeAdjustmentFails(final DataTable table) {
        final Long loanId = getLoanId();
        final GetWorkingCapitalLoanTransactionIdResponse adjustmentTxn = getLastChargeAdjustmentTransaction(loanId, null);
        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();
        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, adjustmentTxn.getId(), "undo", request));
        assertHttpStatus(exception, expectedHttpCode);
        assertErrorMessage(exception, expectedErrorMessage);
        log.info("Verified reverting already reversed WC charge adjustment failed with status {} and message: {}", expectedHttpCode,
                expectedErrorMessage);
    }

    @Then("Trying to add working capital loan charge by loan id and charge id with amount {double} and due date {string} results an error with the following data:")
    public void tryAddWorkingCapitalLoanChargeWithError(Double amount, String dueDate, DataTable table) {
        Long loanId = getLoanId();
        Assertions.assertNotNull(loanId);
        Long chargeId = getChargeId();
        Assertions.assertNotNull(chargeId);

        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();

        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest() //
                .chargeId(chargeId).amount(amount).dueDate(dueDate).dateFormat("dd-MM-yyyy").locale("en");

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanCharges().createLoanCharge(loanId, request));
        assertHttpStatus(exception, expectedHttpCode);
        assertErrorMessage(exception, expectedErrorMessage);
        log.info("Verified adding WCL charge to loan {} failed with status {} and message: {}", loanId, exception.getStatus(),
                expectedErrorMessage);
    }

    // Charge Adjustment Helpers
    private Long getLastAddedLoanChargeId() {
        final PostLoansLoanIdChargesResponse response = testContext().get(TestContextKey.ADD_DUE_DATE_CHARGE_WORKING_CAPITAL_RESPONSE);
        Assertions.assertNotNull(response, "No charge has been added to the working capital loan yet");
        return response.getResourceId();
    }

    private Long getLastAddedFeeChargeId(final Long loanId) {
        return getLastAddedChargeIdByPenaltyFlag(loanId, false);
    }

    private Long getLastAddedPenaltyChargeId(final Long loanId) {
        return getLastAddedChargeIdByPenaltyFlag(loanId, true);
    }

    private Long getLastAddedChargeIdByPenaltyFlag(final Long loanId, final boolean isPenalty) {
        final List<WorkingCapitalLoanChargeData> charges = ok(
                () -> fineractClient.workingCapitalLoanCharges().retrieveAllWorkingCapitalLoanChargesByLoanId(loanId));
        Assertions.assertNotNull(charges, "No charges found on loan " + loanId);
        final String chargeType = isPenalty ? "penalty" : "fee";
        return charges.stream().filter(c -> isPenalty == Boolean.TRUE.equals(c.getPenalty()))
                .max(Comparator.comparing(WorkingCapitalLoanChargeData::getId)).map(WorkingCapitalLoanChargeData::getId)
                .orElseThrow(() -> new IllegalStateException("No active " + chargeType + " charge found on loan " + loanId));
    }

    private GetWorkingCapitalLoanTransactionIdResponse getLastChargeAdjustmentTransaction(final Long loanId,
            final Boolean excludeReversed) {
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        Assertions.assertNotNull(body.getContent(), "No WC loan transactions found");
        return body.getContent().stream()
                .filter(t -> t.getType() != null && "loanTransactionType.chargeAdjustment".equals(t.getType().getCode()))
                .filter(t -> excludeReversed == null || !Boolean.TRUE.equals(t.getReversed()))
                .max(Comparator.comparing(GetWorkingCapitalLoanTransactionIdResponse::getId))
                .orElseThrow(() -> new IllegalStateException("No charge adjustment transaction found on loan " + loanId));
    }

    // Charge API Helpers
    private void createChargeAndStore(final ChargeRequest request) {
        final PostChargesResponse response = ok(() -> fineractClient.charges().createCharge(request));
        testContext().set(TestContextKey.WORKING_CAPITAL_CHARGE_ID, response.getResourceId());
        log.info("Created WCL charge with ID: {}", response.getResourceId());
    }

    private GetChargesResponse retrieveCharge(final Long chargeId) {
        final GetChargesResponse chargeData = ok(() -> fineractClient.charges().retrieveOneCharge(chargeId));
        assertThat(chargeData).as("Charge data should not be null").isNotNull();
        return chargeData;
    }

    // Data Extraction Helpers
    private Long getChargeId() {
        return testContext().get(TestContextKey.WORKING_CAPITAL_CHARGE_ID);
    }

    private void addLoanChargeId(Long loanChargeId) {
        List<Long> loanChargeIds = getLoanChargeIds();
        if (!loanChargeIds.contains(loanChargeId)) {
            loanChargeIds.add(loanChargeId);
        }
    }

    private List<Long> getLoanChargeIds() {
        List<Long> ids = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_CHARGE_IDS);
        if (ids == null) {
            ids = new ArrayList<>();
            testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CHARGE_IDS, ids);
        }
        return ids;
    }

    private ChargeData getChargeTemplate() {
        final ChargeData templateData = testContext().get(TestContextKey.WORKING_CAPITAL_CHARGE_TEMPLATE);
        assertThat(templateData).as("Charge template should not be null").isNotNull();
        return templateData;
    }

    private Long getLoanId() {
        PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse);
        return loanResponse.getLoanId();
    }

    // Assertion Helpers
    private void assertHttpStatus(final CallFailedRuntimeException exception, final int expectedStatus) {
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedStatus).isEqualTo(expectedStatus);
    }

    private void assertErrorMessage(final CallFailedRuntimeException exception, final String expectedMessage) {
        assertThat(exception.getMessage()).as("Error message should contain: " + expectedMessage).contains(expectedMessage);
    }

    private void assertSingleOption(final List<EnumOptionData> options, final String optionName, final Long expectedId) {
        assertThat(options).as(optionName + " should not be null or empty").isNotNull().isNotEmpty();
        assertThat(options).hasSize(1);
        assertThat(options.get(0).getId()).as("Only " + optionName + " with ID " + expectedId + " should be available")
                .isEqualTo(expectedId);
    }
}
