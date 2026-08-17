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
package org.apache.fineract.integrationtests;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBatchHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.BatchRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.AmortizationType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestCalculationPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestRateFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestPostingPeriodType;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the Batch API, exercised through the typed Feign client.
 *
 * @see org.apache.fineract.batch.api.BatchApiResource
 */
public class BatchApiTest extends FeignLoanTestBase {

    /** Application table the datatables in this test are attached to. */
    private static final String LOAN_APP_TABLE_NAME = "m_loan";
    private static final String LOAN_RESCHEDULE_REASON_CODE_NAME = "LoanRescheduleReason";
    private static final String PERSON_ENTITY_SUB_TYPE = "PERSON";

    private static final double SMALL_PRINCIPAL = 1000.00;
    private static final double LARGE_PRINCIPAL = 10000000.00;
    private static final double SPECIFIED_DUE_DATE_CHARGE_AMOUNT = 100.0;

    /** Column width of the datatables whose values are short random strings. */
    private static final long SHORT_COLUMN_LENGTH = 10;
    /** Column width of the datatable queried by the literal {@code columnValue1} / {@code columnValue2} filters. */
    private static final long LONG_COLUMN_LENGTH = 15;

    private static final String SAVINGS_SUBMITTED_ON_DATE = "08 January 2013";
    private static final String SAVINGS_APPROVED_ON_DATE = "09 January 2013";
    private static final String SAVINGS_ACTIVATED_ON_DATE = "01 March 2013";

    /**
     * Batch request ids of the datatable requests. Their only requirement is to be unique within a batch and to match
     * the {@code reference} of any request that resolves an id from their response.
     */
    private static final Long GET_LOAN_REQUEST_ID = 4567L;
    private static final Long CREATE_DATATABLE_ENTRY_REQUEST_ID = 4569L;
    private static final Long UPDATE_DATATABLE_ENTRY_REQUEST_ID = 4570L;
    private static final Long GET_DATATABLE_ENTRIES_REQUEST_ID = 4571L;
    private static final Long GET_DATATABLE_ENTRY_REQUEST_ID = 4572L;
    private static final Long QUERY_DATATABLE_ENTRIES_REQUEST_ID = 1L;
    private static final Long UPDATE_QUERIED_DATATABLE_ENTRY_REQUEST_ID = 2L;

    private static final Gson GSON = new Gson();

    private final FeignBatchHelper batchHelper = new FeignBatchHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignDatatableHelper datatableHelper = new FeignDatatableHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsProductHelper savingsProductHelper = new FeignSavingsProductHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    /**
     * Sets up the essential settings for the TEST like contentType, expectedStatusCode. It uses the '@BeforeEach'
     * annotation provided by jUnit.
     */
    @BeforeEach
    public void setup() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @AfterEach
    public void postActions() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOW_CASH_AND_NON_CASH_ACCRUAL,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    /**
     * Tests for the unimplemented command Strategies by returning 501 status code. For a unknownRequest a statusCode
     * 501 is returned back with response.
     *
     * @see org.apache.fineract.batch.command.internal.UnknownCommandStrategy
     */
    @Test
    public void shouldReturnStatusNotImplementedUnknownCommand() {
        final BatchRequest unknownCommandRequest = new BatchRequest().requestId(4711L).relativeUrl("/nirvana").method("POST");

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(List.of(unknownCommandRequest));

        assertEquals(1, responses.size());
        // Verify that only 501 is returned as the status code
        assertEquals(SC_NOT_IMPLEMENTED, responses.get(0).getStatusCode(), "Verify Status code 501");
    }

    /**
     * Tests for the successful response for a createClient request from createClientCommand. A successful response with
     * statusCode '200' is returned back.
     *
     * @see org.apache.fineract.batch.command.internal.CreateClientCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCreateClientCommand() {
        final BatchRequest createClientRequest = BatchRequestBuilders.createClient(4712L, "");

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(List.of(createClientRequest));

        assertEquals(1, responses.size());
        // Verify that a 200 response is returned as the status code
        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status code 200");
    }

    /**
     * Tests for an erroneous response with statusCode '403' if transaction fails. If Query Parameter
     * 'enclosingTransaction' is set to 'true' and if one of the request in BatchRequest fails then all transactions are
     * rolled back.
     *
     * @see org.apache.fineract.batch.command.internal.CreateClientCommandStrategy
     * @see org.apache.fineract.batch.api.BatchApiResource
     * @see org.apache.fineract.batch.service.BatchApiService
     */
    @Test
    public void shouldRollBackAllTransactionsOnFailure() {
        final String firstExternalId = UUID.randomUUID().toString();
        // Create first client request
        final String secondExternalId = UUID.randomUUID().toString();

        // Create second client request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(4713L, firstExternalId),
                BatchRequestBuilders.createClient(4714L, secondExternalId),
                // Duplicates the first external id, which is what makes the batch fail
                BatchRequestBuilders.createClient(4715L, firstExternalId));

        // Create third client request, having same externalID as second client, hence cause of error
        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        // Verifies that none of the clients in the batch was created on the server
        assertEquals(0, clientHelper.countClientsByExternalId(firstExternalId), "No records found with given externalId");
        assertEquals(0, clientHelper.countClientsByExternalId(secondExternalId), "No records found with given externalId");

        assertEquals(1, responses.size());
        // Verifies that none of the client in BatchRequest is created on the server
        // Asserts that all the transactions have been successfully rolled back
        assertEquals(SC_FORBIDDEN, responses.get(0).getStatusCode(), "Verify Status code 403");
    }

    /**
     * Tests that a client information was successfully updated through updateClientCommand. A 'changes' parameter is
     * returned in the response after successful update of client information.
     *
     * @see org.apache.fineract.batch.command.internal.UpdateClientCommandStrategy
     */
    @Test
    public void shouldReflectChangesOnClientUpdate() {
        final Long createClientRequestId = 4716L;
        final Long updateClientRequestId = 4717L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Get the changes parameter from updateClient Response
                BatchRequestBuilders.updateClient(updateClientRequestId, createClientRequestId));

        // Create a clientUpdate Request
        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        final JsonObject changes = bodyAsObject(responses.get(1)).getAsJsonObject("changes");
        assertEquals("TestFirstName", changes.get("firstname").getAsString());
        // Asserts the client information is successfully updated
        assertEquals("TestLastName", changes.get("lastname").getAsString());
    }

    /**
     * Tests that a ApplyLoanCommand was successfully executed and returned a 200(OK) status. It creates a new client
     * and apply a loan to that client. This also verifies the successful resolution of dependencies among two requests.
     *
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForApplyLoanCommand() {
        // Create product
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createClientRequestId = 4718L;
        final Long activateClientRequestId = 4719L;
        final Long applyLoanRequestId = 4720L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create a activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, activateClientRequestId, productId, clientCollateralId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Create Client");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Activate Client");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Apply Loan");
    }

    /**
     * Tests that a new savings accounts was applied to an existing client and a 200(OK) status was returned. It first
     * creates a new client and a savings product, then uses the cliendId and ProductId to apply a savings account.
     *
     * @see org.apache.fineract.batch.command.internal.ApplySavingsCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForApplySavingsCommand() {
        final PostSavingsProductsRequest savingsProduct = SavingsRequestBuilders.defaultSavingsProduct()
                .minRequiredOpeningBalance(BigDecimal.valueOf(5000));
        final Long productId = savingsProductHelper.createSavingsProduct(savingsProduct).getResourceId();

        final Long createClientRequestId = 4720L;
        final Long activateClientRequestId = 4721L;
        final Long applySavingsRequestId = 4722L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create a activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId),
                // Create a applySavings Request
                BatchRequestBuilders.applySavings(applySavingsRequestId, activateClientRequestId, productId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Create Client");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Activate Client");
    }

    /**
     * Tests that a new charge was added to a newly created loan and charges are Collected properly 200(OK) status was
     * returned for successful responses. It first creates a new client and apply a loan, then creates a new charge for
     * the create loan and then fetches all the applied charges
     *
     * @see org.apache.fineract.batch.command.internal.CollectChargesCommandStrategy
     * @see org.apache.fineract.batch.command.internal.CreateChargeCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCollectChargesCommand() {
        // Create product
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createClientRequestId = 4722L;
        final Long activateClientRequestId = 4723L;
        final Long applyLoanRequestId = 4724L;
        final Long collectChargesRequestId = 4725L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create a activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, activateClientRequestId, productId, clientCollateralId),
                BatchRequestBuilders.collectChargesByLoanId(collectChargesRequestId, applyLoanRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        // Create a Collect Charges Request
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Collect Charges");
    }

    /**
     * Tests that a new charge was added to a newly created loan and can be read back by its id.
     *
     * @see org.apache.fineract.batch.command.internal.CreateChargeCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetChargeByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCreateAndGetChargeByIdCommand() {
        // Create product
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        // Create client
        final Long clientId = createClient();
        final Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(SPECIFIED_DUE_DATE_CHARGE_AMOUNT).getResourceId();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long createChargeRequestId = disburseLoanRequestId + 1;
        final Long getChargeByIdRequestId = createChargeRequestId + 1;

        // Create batch requests list
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create charge object and get id
                BatchRequestBuilders.createChargeByLoanId(createChargeRequestId, disburseLoanRequestId, chargeId),
                BatchRequestBuilders.getChargeByLoanIdChargeId(getChargeByIdRequestId, createChargeRequestId));

        // Create batch responses list
        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Create Charge");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Get Charge By Id");
    }

    /**
     * Test for a successful charge adjustment. A '200' status code is expected on successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulChargeAdjustment() {
        // Create product
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        // Create client
        final Long clientId = createClient();
        final Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(SPECIFIED_DUE_DATE_CHARGE_AMOUNT).getResourceId();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long createChargeRequestId = disburseLoanRequestId + 1;
        final Long adjustChargeRequestId = createChargeRequestId + 1;
        final Long getTransactionRequestId = adjustChargeRequestId + 1;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                BatchRequestBuilders.createChargeByLoanId(createChargeRequestId, disburseLoanRequestId, chargeId),
                BatchRequestBuilders.adjustCharge(adjustChargeRequestId, createChargeRequestId),
                BatchRequestBuilders.getTransactionById(getTransactionRequestId, adjustChargeRequestId, true));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        // Create charge object and get id
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Create Charge");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Adjust Charge");
        assertEquals(SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for Get Transaction By Id");

        final JsonObject chargeAdjustment = bodyAsObject(responses.get(5)).getAsJsonObject("type");
        assertEquals("Charge Adjustment", chargeAdjustment.get("value").getAsString());
        assertTrue(chargeAdjustment.get("chargeAdjustment").getAsBoolean());
    }

    /**
     * Tests that a charge can be created, collected, adjusted and read back through the loan and charge external ids.
     *
     * @see org.apache.fineract.batch.command.internal.CollectChargesByLoanExternalIdCommandStrategy
     * @see org.apache.fineract.batch.command.internal.CreateChargeByLoanExternalIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCreateAndGetChargeByExternalIdCommand() {
        // Create product
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        // Create client
        final Long clientId = createClient();
        final Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(SPECIFIED_DUE_DATE_CHARGE_AMOUNT).getResourceId();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long getLoanRequestId = disburseLoanRequestId + 1;
        final Long createChargeRequestId = getLoanRequestId + 1;
        final Long collectChargesRequestId = createChargeRequestId + 1;

        // Create batch requests list
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.transitionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId, today().minusDays(10),
                        "approve"),
                BatchRequestBuilders.transitionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId, today().minusDays(8),
                        "disburse"),
                BatchRequestBuilders.getLoanByExternalIdReference(getLoanRequestId, approveLoanRequestId, "associations=all"),
                BatchRequestBuilders.createChargeByLoanExternalId(createChargeRequestId, getLoanRequestId, chargeId),
                BatchRequestBuilders.collectChargesByLoanExternalId(collectChargesRequestId, getLoanRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Create Charge");
        assertEquals(SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for Collect charges");

        final String loanExternalId = bodyAsObject(responses.get(3)).get("externalId").getAsString();
        final String chargeExternalId = bodyAsObject(responses.get(4)).get("resourceExternalId").getAsString();

        // The loan and charge external ids come from two different responses, so the adjustment and the reads that
        // depend on both have to be posted as a second batch.
        final Long adjustChargeRequestId = createChargeRequestId + 1;
        final Long getTransactionByExternalIdRequestId = adjustChargeRequestId + 1;
        final Long getChargeByIdRequestId = getTransactionByExternalIdRequestId + 1;

        // Create batch requests list
        final List<BatchRequest> adjustChargeRequests = List.of(
                BatchRequestBuilders.adjustChargeByExternalId(adjustChargeRequestId, null, loanExternalId, chargeExternalId),
                BatchRequestBuilders.getTransactionByExternalId(getTransactionByExternalIdRequestId, adjustChargeRequestId, loanExternalId,
                        true),
                BatchRequestBuilders.getChargeByLoanExternalIdChargeExternalId(getChargeByIdRequestId, getTransactionByExternalIdRequestId,
                        loanExternalId, chargeExternalId));

        // Create batch responses list
        final List<BatchResponse> adjustChargeResponses = batchHelper.executeWithoutEnclosingTransaction(adjustChargeRequests);

        assertEquals(SC_OK, adjustChargeResponses.get(0).getStatusCode(), "Verify Status Code 200 for Adjust Charge By External Id");
        // Create charge object and get id
        assertEquals(SC_OK, adjustChargeResponses.get(1).getStatusCode(), "Verify Status Code 200 for Get Transaction By Id");
        assertEquals(SC_OK, adjustChargeResponses.get(2).getStatusCode(), "Verify Status Code 200 for Get Charge By Id");

        final JsonObject chargeAdjustment = bodyAsObject(adjustChargeResponses.get(1)).getAsJsonObject("type");
        assertEquals("Charge Adjustment", chargeAdjustment.get("value").getAsString());
        assertTrue(chargeAdjustment.get("chargeAdjustment").getAsBoolean());
    }

    /**
     * Tests that batch repayment for loans is happening properly. It first creates a new loan and then makes two
     * repayments for it and then verifies that 200(OK) is returned for the repayment requests.
     *
     * @see org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepayment() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createClientRequestId = 4730L;
        final Long activateClientRequestId = 4731L;
        final Long applyLoanRequestId = 4732L;
        final Long approveLoanRequestId = 4733L;
        final Long disburseLoanRequestId = 4734L;
        final Long firstRepaymentRequestId = 4735L;
        final Long secondRepaymentRequestId = 4736L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create a activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, activateClientRequestId, productId, clientCollateralId),
                // Create a approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburseLoan Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create a loanRepay Request
                BatchRequestBuilders.repayLoan(firstRepaymentRequestId, disburseLoanRequestId, "500"),
                // Create a loanRepay Request
                BatchRequestBuilders.repayLoan(secondRepaymentRequestId, disburseLoanRequestId, "500"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for Repayment");
        assertEquals(SC_OK, responses.get(6).getStatusCode(), "Verify Status Code 200 for Repayment");
    }

    /**
     * Tests that batch credit balance refund for loans is happening properly. It first creates a new loan and then
     * makes an overpayment, before creating a credit balance refund to refund a portion of the over-payment.
     *
     * @see org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchCreditBalanceRefund() {
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long repayLoanRequestId = disburseLoanRequestId + 1;
        final Long creditBalanceRefundRequestId = repayLoanRequestId + 1;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createActiveClient(createActiveClientRequestId, ""),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, createActiveClientRequestId, productId, clientCollateralId),
                // Create a approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburseLoan Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Repays more than the outstanding balance, leaving the loan overpaid
                BatchRequestBuilders.repayLoan(repayLoanRequestId, disburseLoanRequestId, "20000"),
                // Create a credit balance refund request
                BatchRequestBuilders.creditBalanceRefund(creditBalanceRefundRequestId, repayLoanRequestId, "500"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Repayment");
        // Create a loanRepay Request which will result in an overpay.
        assertEquals(SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for Credit Balance Refund");
    }

    /**
     * Tests that a failing request in a non-enclosing-transaction batch does not abort the requests after it.
     */
    @Test
    public void partialFailTestForBatchRequest() {
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long fetchLoanInfoRequestId = approveLoanRequestId + 1;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createActiveClient(createActiveClientRequestId, ""),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, createActiveClientRequestId, productId, clientCollateralId),
                // Create a wrong approveLoan Request
                BatchRequestBuilders.approveLoanWithUnknownCommand(approveLoanRequestId, applyLoanRequestId),
                // Fetch loan info
                BatchRequestBuilders.getLoanByReference(fetchLoanInfoRequestId, applyLoanRequestId, null));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_NOT_IMPLEMENTED, responses.get(2).getStatusCode(), "Resource does not exist");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for fetch data after the error");
    }

    /**
     * Tests successful run of batch goodwill credit for loans. It first creates a new loan, approves and disburses the
     * loan. Then a goodwill credit request is made.
     *
     * @see org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchGoodwillCredit() {
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long goodwillCreditRequestId = disburseLoanRequestId + 1;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createActiveClient(createActiveClientRequestId, ""),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, createActiveClientRequestId, productId, clientCollateralId),
                // Create a approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburseLoan Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create a good will credit request.
                BatchRequestBuilders.goodwillCredit(goodwillCreditRequestId, disburseLoanRequestId, "500"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Goodwill credit");
    }

    /**
     * Test for the successful activation of a pending client using 'ActivateClientCommandStrategy'.
     *
     * @see org.apache.fineract.batch.command.internal.ActivateClientCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulClientActivation() {
        final Long createClientRequestId = 4726L;
        final Long activateClientRequestId = 4727L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create an activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Create Client");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Activate Client");
    }

    /**
     * Test for the successful approval and disbursal of a loan.
     *
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulLoanApprovalAndDisburse() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createClientRequestId = 4730L;
        final Long activateClientRequestId = 4731L;
        final Long applyLoanRequestId = 4732L;
        final Long approveLoanRequestId = 4733L;
        final Long disburseLoanRequestId = 4734L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create a activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId),
                // Create an ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, activateClientRequestId, productId, clientCollateralId),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create an disburseLoan Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
    }

    /**
     * Test for the successful create client, apply loan, approval and disbursal of a loan using Batch API with
     * enclosingTransaction.
     *
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulLoanApprovalAndDisburseWithTransaction() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createActiveClientRequestId = 4740L;
        final Long applyLoanRequestId = 4742L;
        final Long approveLoanRequestId = 4743L;
        final Long disburseLoanRequestId = 4744L;

        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createActiveClient(createActiveClientRequestId, ""),
                // Create an ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, createActiveClientRequestId, productId, clientCollateralId),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburseLoan Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId));

        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for create client");
        // Create a createClient Request
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for apply Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for approve Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for disburse Loan");
    }

    /**
     * Test for the successful disbursement and get loan transaction.
     *
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetTransactionByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulDisbursementAndGetTransaction() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        // Create client
        final Long clientId = createClient();

        final Long applyLoanRequestId = 6730L;
        final Long approveLoanRequestId = 6731L;
        final Long disburseLoanRequestId = 6732L;
        final Long getTransactionRequestId = 6733L;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disbursement Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                BatchRequestBuilders.getTransactionById(getTransactionRequestId, disburseLoanRequestId, true));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        // Create an ApplyLoan Request
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        // Create a getTransaction Request
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Transaction By Id");
    }

    /**
     * Test for the successful new loan reschedule request and approval for the same.
     *
     * @see org.apache.fineract.batch.command.internal.CreateLoanRescheduleRequestCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanRescheduleCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulDisbursementAndRescheduleLoan() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        // Create client
        final Long clientId = createClient();
        final Long rescheduleReasonId = codeHelper.retrieveOrCreateCodeValueId(LOAN_RESCHEDULE_REASON_CODE_NAME);

        final Long applyLoanRequestId = 8463L;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long rescheduleLoanRequestId = disburseLoanRequestId + 1;
        final Long approveRescheduleLoanRequestId = rescheduleLoanRequestId + 1;

        final LocalDate disburseLoanDate = today().minusDays(1);
        final LocalDate adjustedDueDate = today().plusDays(40);

        final List<BatchRequest> batchRequests = List.of(
                // Create an ApplyLoan request
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                // Create an approveLoan request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disbursement request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId, disburseLoanDate),
                // Create a reschedule loan request
                BatchRequestBuilders.createRescheduleLoan(rescheduleLoanRequestId, disburseLoanRequestId, disburseLoanDate.plusMonths(1),
                        rescheduleReasonId, adjustedDueDate),
                // Approve reschedule loan request
                BatchRequestBuilders.approveRescheduleLoan(approveRescheduleLoanRequestId, rescheduleLoanRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Create Reschedule Loan request");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Approve Reschedule Loan request");
    }

    /**
     * Test for the successful create client, create, approve and get loan.
     *
     * @see org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulCreateClientCreateApproveAndGetLoan() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = 4731L;
        final Long approveLoanRequestId = 4732L;
        final Long getLoanByIdRequestId = 4733L;

        // Create product
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createActiveClient(createActiveClientRequestId, ""),
                // Create an ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, createActiveClientRequestId, productId, null),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.getLoanByReference(getLoanByIdRequestId, applyLoanRequestId, null));

        // Create createClient Request
        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Create Client");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan By Id");

        final Long loanId = bodyAsObject(responses.get(1)).get("loanId").getAsLong();
        final JsonObject getLoanResponse = bodyAsObject(responses.get(3));
        final JsonObject status = getLoanResponse.getAsJsonObject("status");

        assertEquals(loanId, getLoanResponse.get("id").getAsLong());
        // Get loan by id Request
        assertEquals(LoanStatus.APPROVED.getCode(), status.get("code").getAsString());
        assertEquals("Approved", status.get("value").getAsString());
    }

    /**
     * Test for the successful create, approve and get loan, with and without a query parameter.
     *
     * @see org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulCreateApproveAndGetLoan() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        // Create client
        final Long clientId = createClient();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long getLoanByIdRequestId = 5732L;
        final Long getLoanByIdWithQueryParametersRequestId = 5733L;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.getLoanByReference(getLoanByIdRequestId, applyLoanRequestId, null),
                BatchRequestBuilders.getLoanByReference(getLoanByIdWithQueryParametersRequestId, applyLoanRequestId,
                        "associations=repaymentSchedule,transactions"));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        // Create an ApplyLoan Request
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Get Loan By Id without query parameter");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan By Id with query parameter");

        final Long loanId = bodyAsObject(responses.get(0)).get("loanId").getAsLong();
        final JsonObject getLoanResponse = bodyAsObject(responses.get(2));
        final JsonObject status = getLoanResponse.getAsJsonObject("status");

        assertEquals(loanId, getLoanResponse.get("id").getAsLong());
        // Get loan by id Request without query param
        assertEquals(LoanStatus.APPROVED.getCode(), status.get("code").getAsString());
        // Get loan by id Request with query param
        assertEquals("Approved", status.get("value").getAsString());

        // Repayment schedule will not be available in the response
        assertFalse(responses.get(2).getBody().contains("repaymentSchedule"));

        // Repayment schedule information will be available in the response based on the query parameter
        assertTrue(responses.get(3).getBody().contains("repaymentSchedule"));
    }

    /**
     * Test for the successful get loan and get datatable entry.
     *
     * @see org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulGetDataTableEntry() {
        final Long loanId = setupLoanAccount();
        // creating datatable with m_loan association
        final String datatableName = createLoanDatatable();
        try {
            final List<BatchRequest> batchRequests = List.of(
                    BatchRequestBuilders.getLoanByLoanId(GET_LOAN_REQUEST_ID, null, loanId, "associations=repaymentSchedule,transactions"),
                    BatchRequestBuilders.getDatatableEntries(GET_DATATABLE_ENTRIES_REQUEST_ID, null, datatableName, loanId,
                            "genericResultSet=true"));

            // Get loan by id Request with query param
            final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for get loan");
            // Get datatable batch request
            assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for datatable");

            final String getLoanResponse = responses.get(0).getBody();
            final String getDatatableResponse = responses.get(1).getBody();

            assertEquals(loanId, bodyAsObject(responses.get(0)).get("id").getAsLong());

            // Repayment schedule and transactions are available in the response based on the query parameter
            assertTrue(getLoanResponse.contains("repaymentSchedule"));
            assertTrue(getLoanResponse.contains("transactions"));

            // The generic result set carries the column headers along with the data
            assertTrue(getDatatableResponse.contains("columnHeaders"));
            // datatable info will be available in the response based on the query parameter
            assertTrue(getDatatableResponse.contains("data"));
        } finally {
            deleteDatatable(datatableName);
        }
    }

    /**
     * Test for the successful create and update datatable entry.
     *
     * @see org.apache.fineract.batch.command.internal.CreateDatatableEntryCommandStrategy
     * @see org.apache.fineract.batch.command.internal.UpdateDatatableEntryOneToManyCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdAndDataTableIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulCreateDataTableEntry() {
        // creating datatable with m_loan association
        final Long loanId = setupLoanAccount();
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5);
        final String columnName1 = Utils.randomStringGenerator("COL1_", 5);
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5);
        createTwoStringColumnDatatable(datatableName, columnName1, columnName2, true, SHORT_COLUMN_LENGTH);
        try {
            final Long existingEntryId = createDatatableEntry(datatableName, loanId, columnName1, Utils.randomStringGenerator("VAL1_", 3),
                    columnName2, Utils.randomStringGenerator("VAL2_", 3));

            final List<BatchRequest> batchRequests = List.of(
                    BatchRequestBuilders.createDatatableEntry(CREATE_DATATABLE_ENTRY_REQUEST_ID, datatableName, loanId,
                            List.of(columnName1, columnName2)),
                    BatchRequestBuilders.updateDatatableEntryByEntryId(UPDATE_DATATABLE_ENTRY_REQUEST_ID, CREATE_DATATABLE_ENTRY_REQUEST_ID,
                            datatableName, loanId, existingEntryId, List.of(columnName1)),
                    BatchRequestBuilders.getDatatableEntries(GET_DATATABLE_ENTRIES_REQUEST_ID, CREATE_DATATABLE_ENTRY_REQUEST_ID,
                            datatableName, loanId, null),
                    BatchRequestBuilders.getDatatableEntryByAppTableId(GET_DATATABLE_ENTRY_REQUEST_ID, CREATE_DATATABLE_ENTRY_REQUEST_ID,
                            datatableName, loanId, "$.resourceId", null));

            // Create a datatable entry so that it can be updated using BatchApi
            final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for create datatable entry");
            assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for update datatable entry");
            assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for get datatable entries");
            assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for get datatable entry by id");

            final Long createdEntryId = bodyAsObject(responses.get(0)).get("resourceId").getAsLong();
            // Create datatable entry batch request
            final String getDatatableEntriesResponse = responses.get(2).getBody();
            final JsonArray datatableEntries = JsonParser.parseString(getDatatableEntriesResponse).getAsJsonArray();
            assertEquals(2, datatableEntries.size());

            // Ensure both resourceIds are available in response
            assertTrue(getDatatableEntriesResponse.contains("\"id\": %d".formatted(createdEntryId)));
            assertTrue(getDatatableEntriesResponse.contains("\"id\": %d".formatted(existingEntryId)));
        } finally {
            deleteDatatableWithEntriesOf(datatableName, loanId);
        }
    }

    /**
     * Test for the successful get loan and get datatable entry where get datatable request has no query param.
     *
     * @see org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulGetDatatableEntryWithNoQueryParam() {
        final Long loanId = setupLoanAccount();
        // creating datatable with m_loan association
        final String datatableName = createLoanDatatable();
        try {
            // Get loan by id Request with query param
            final List<BatchRequest> batchRequests = List.of(
                    BatchRequestBuilders.getLoanByLoanId(GET_LOAN_REQUEST_ID, null, loanId, "associations=repaymentSchedule,transactions"),
                    BatchRequestBuilders.getDatatableEntries(GET_DATATABLE_ENTRIES_REQUEST_ID, null, datatableName, loanId, null));

            // Get datatable batch request
            final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Get Loan");
            assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Get Datatable");

            final String getLoanResponse = responses.get(0).getBody();
            assertEquals(loanId, bodyAsObject(responses.get(0)).get("id").getAsLong());
            assertTrue(getLoanResponse.contains("repaymentSchedule"));
            assertTrue(getLoanResponse.contains("transactions"));
        } finally {
            deleteDatatable(datatableName);
        }
    }

    /**
     * Test for the successful merchant issued and payout refund transaction.
     *
     * @see org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulTransactionMerchantIssuedAndPayoutRefund() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientId = createClient();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long merchantIssuedRefundRequestId = 5733L;
        final Long payoutRefundRequestId = 5734L;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disbursement request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId, today().minusDays(1)),
                // Create a merchant issued refund request
                BatchRequestBuilders.merchantIssuedRefund(merchantIssuedRefundRequestId, applyLoanRequestId, "10"),
                // Create a payout refund request
                BatchRequestBuilders.payoutRefund(payoutRefundRequestId, applyLoanRequestId, "10"));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        // Create an ApplyLoan Request
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for merchant issued refund");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for payout refund");
    }

    /**
     * Test for the successful repayment reversal transaction.
     *
     * @see org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepaymentReversal() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        // Create client
        final Long clientId = createClient();
        final LocalDate reversalDate = today();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long repayLoanRequestId = 5733L;
        final Long repayReversalRequestId = 5734L;
        final Long getLoanRequestId = 5735L;

        final List<BatchRequest> batchRequests = List.of(
                // Create an apply loan request
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                // Create an approve loan request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburse loan request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create a repayment request.
                BatchRequestBuilders.repayLoan(repayLoanRequestId, disburseLoanRequestId, "500"),
                // Create a repayment reversal request
                BatchRequestBuilders.adjustTransaction(repayReversalRequestId, repayLoanRequestId, "0", reversalDate),
                // Get loan transactions request
                BatchRequestBuilders.getLoanByReference(getLoanRequestId, applyLoanRequestId, "associations=transactions"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for repayment reversal");

        final JsonObject repayment = transactionAt(responses.get(5), 1);
        assertEquals("Repayment", repayment.getAsJsonObject("type").get("value").getAsString());
        assertTrue(repayment.get("manuallyReversed").getAsBoolean());
        assertEquals(reversalDate, asLocalDate(repayment.getAsJsonArray("reversedOnDate")));
    }

    /**
     * Test for the successful repayment reversal transaction using loan external id and transaction external id.
     *
     * @see org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepaymentReversalUsingExternalId() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        // Create client
        final Long clientId = createClient();
        final LocalDate reversalDate = today();
        final String loanExternalId = UUID.randomUUID().toString();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long getLoanBeforeTxnRequestId = disburseLoanRequestId + 1;
        final Long repayLoanRequestId = getLoanBeforeTxnRequestId + 1;
        final Long getLoanAfterTxnRequestId = repayLoanRequestId + 1;
        final Long repayReversalRequestId = getLoanAfterTxnRequestId + 1;
        final Long getLoanAfterReversalRequestId = repayReversalRequestId + 1;

        final List<BatchRequest> batchRequests = List.of(
                // Create an apply loan request
                BatchRequestBuilders.applyLoanWithClientIdAndExternalId(applyLoanRequestId, clientId, productId, loanExternalId),
                // Create an approve loan request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburse loan request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Get loan transactions request
                BatchRequestBuilders.getLoanByReference(getLoanBeforeTxnRequestId, disburseLoanRequestId, "associations=transactions"),
                // Create a repayment request by external id
                BatchRequestBuilders.createTransactionByLoanExternalId(repayLoanRequestId, getLoanBeforeTxnRequestId, "repayment", "500",
                        today()),
                // Get loan transactions request
                BatchRequestBuilders.getLoanByReference(getLoanAfterTxnRequestId, repayLoanRequestId, "associations=transactions"));

        // Because loanExternalId & transactionExternalId are coming from 2 different responses, there is no easy way to
        // use them as reference in 1 batch api call.
        // So we are splitting repayment & reversal into 2 different batch api invocations
        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        final JsonObject loanBeforeTransaction = bodyAsObject(responses.get(3));
        final String loanExternalIdInResponse = loanBeforeTransaction.get("externalId").getAsString();
        final Long loanId = loanBeforeTransaction.get("id").getAsLong();
        final String transactionExternalId = bodyAsObject(responses.get(4)).get("resourceExternalId").getAsString();

        assertEquals(loanExternalId, loanExternalIdInResponse);
        assertNotNull(transactionExternalId);

        final List<BatchRequest> reversalRequests = List.of(
                // Create a repayment reversal request by external id
                BatchRequestBuilders.adjustTransactionByExternalId(repayReversalRequestId, null, loanExternalIdInResponse,
                        transactionExternalId, "0", reversalDate),
                // Get loan transactions request
                BatchRequestBuilders.getLoanByLoanId(getLoanAfterReversalRequestId, repayReversalRequestId, loanId,
                        "associations=transactions"));

        final List<BatchResponse> reversalResponses = batchHelper.executeWithoutEnclosingTransaction(reversalRequests);

        assertEquals(SC_OK, reversalResponses.get(0).getStatusCode(), "Verify Status Code 200 for repayment reversal");

        final JsonObject repayment = transactionAt(reversalResponses.get(1), 1);
        assertEquals("Repayment", repayment.getAsJsonObject("type").get("value").getAsString());
        assertTrue(repayment.get("manuallyReversed").getAsBoolean());
        assertEquals(reversalDate, asLocalDate(repayment.getAsJsonArray("reversedOnDate")));
    }

    /**
     * Test for the successful repayment chargeback transaction.
     *
     * @see org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepaymentChargeback() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientId = createClient();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long repayLoanRequestId = 5733L;
        final Long chargebackRequestId = 5734L;
        final Long getLoanRequestId = 5735L;

        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create a repayment request
                BatchRequestBuilders.repayLoan(repayLoanRequestId, disburseLoanRequestId, "500"),
                // Create a repayment chargeback request
                BatchRequestBuilders.chargebackTransaction(chargebackRequestId, repayLoanRequestId, "500"),
                // Get loan transactions request
                BatchRequestBuilders.getLoanByReference(getLoanRequestId, applyLoanRequestId, "associations=transactions"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for repayment chargeback");

        final JsonObject repayment = transactionAt(responses.get(5), 1);
        assertEquals("Repayment", repayment.getAsJsonObject("type").get("value").getAsString());

        final JsonArray transactionRelations = repayment.getAsJsonArray("transactionRelations");
        assertEquals(1, transactionRelations.size());
        assertEquals("CHARGEBACK", transactionRelations.get(0).getAsJsonObject().get("relationType").getAsString());
    }

    /**
     * Tests successful run of batch goodwill credit reversal for loans.
     *
     * @see org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchGoodwillCreditReversal() {
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        final Long clientId = createClient();
        final LocalDate reversalDate = today();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long goodwillCreditRequestId = 5733L;
        final Long goodwillCreditReversalRequestId = 5734L;
        final Long getLoanRequestId = 5735L;

        // Create client
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create a good will credit request
                BatchRequestBuilders.goodwillCredit(goodwillCreditRequestId, disburseLoanRequestId, "500"),
                // Create a good will credit reversal request
                BatchRequestBuilders.adjustTransaction(goodwillCreditReversalRequestId, goodwillCreditRequestId, "0", reversalDate),
                BatchRequestBuilders.getLoanByReference(getLoanRequestId, applyLoanRequestId, "associations=transactions"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for goodwill credit reversal");

        final JsonObject goodwillCredit = transactionAt(responses.get(5), 1);
        // Create a disburse loan request
        assertEquals("Goodwill Credit", goodwillCredit.getAsJsonObject("type").get("value").getAsString());
        assertTrue(goodwillCredit.get("manuallyReversed").getAsBoolean());
        // Get loan transactions request
        assertEquals(reversalDate, asLocalDate(goodwillCredit.getAsJsonArray("reversedOnDate")));
    }

    /**
     * Test for the successful merchant issued refund and payout refund reversal transaction.
     *
     * @see org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulTransactionMerchantIssuedAndPayoutRefundReversal() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientId = createClient();
        final LocalDate reversalDate = today();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long merchantIssuedRefundRequestId = 5733L;
        final Long payoutRefundRequestId = 5734L;
        final Long merchantIssuedRefundReversalRequestId = 5735L;
        final Long payoutRefundReversalRequestId = 5736L;
        final Long getLoanRequestId = 5737L;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId, reversalDate.minusDays(1)),
                // Create a merchant issued refund request
                BatchRequestBuilders.merchantIssuedRefund(merchantIssuedRefundRequestId, applyLoanRequestId, "10"),
                // Create a payout refund request
                BatchRequestBuilders.payoutRefund(payoutRefundRequestId, applyLoanRequestId, "10"),
                // Create a merchant issued refund reversal request
                BatchRequestBuilders.adjustTransaction(merchantIssuedRefundReversalRequestId, merchantIssuedRefundRequestId, "0",
                        reversalDate),
                // Create a payout refund reversal request
                BatchRequestBuilders.adjustTransaction(payoutRefundReversalRequestId, payoutRefundRequestId, "0", reversalDate),
                BatchRequestBuilders.getLoanByReference(getLoanRequestId, applyLoanRequestId, "associations=transactions"));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for merchant issued refund reversal");
        // Create an apply loan request
        assertEquals(SC_OK, responses.get(6).getStatusCode(), "Verify Status Code 200 for payout refund reversal");

        final JsonObject merchantIssuedRefund = transactionAt(responses.get(7), 1);
        // Create an approve loan request
        final JsonObject payoutRefund = transactionAt(responses.get(7), 2);

        assertEquals("Merchant Issued Refund", merchantIssuedRefund.getAsJsonObject("type").get("value").getAsString());
        // Create a disburse loan request
        assertEquals("Payout Refund", payoutRefund.getAsJsonObject("type").get("value").getAsString());
        assertTrue(merchantIssuedRefund.get("manuallyReversed").getAsBoolean());
        assertTrue(payoutRefund.get("manuallyReversed").getAsBoolean());
        assertEquals(reversalDate, asLocalDate(merchantIssuedRefund.getAsJsonArray("reversedOnDate")));
        // Get loan transactions request
        assertEquals(reversalDate, asLocalDate(payoutRefund.getAsJsonArray("reversedOnDate")));
    }

    @Test
    public void shouldReturnOkStatusOnModifyingSavingAccount() {
        final String startDate = "10 April 2022";
        final Long clientId = createClient(startDate);
        final Long savingsId = createActiveSavingsAccount(clientId, startDate, startDate, startDate);

        final Long getSavingsAccountRequestId = 1L;
        final Long depositRequestId = 2L;
        final Long holdAmountRequestId = 3L;

        final BatchRequest getSavingsAccountRequest = BatchRequestBuilders.getSavingsAccount(getSavingsAccountRequestId, null, savingsId,
                "chargeStatus=all");

        final List<BatchRequest> depositRequests = List.of(getSavingsAccountRequest,
                BatchRequestBuilders.depositSavingsAccount(depositRequestId, getSavingsAccountRequestId, 100F),
                BatchRequestBuilders.holdAmountOnSavingsAccount(holdAmountRequestId, getSavingsAccountRequestId, 10F));

        final List<BatchResponse> depositResponses = batchHelper.executeEnclosingTransaction(depositRequests);

        assertEquals(SC_OK, depositResponses.get(1).getStatusCode(), "Verify Status Code 200 for deposit saving account");
        assertEquals(SC_OK, depositResponses.get(2).getStatusCode(), "Verify Status Code 200 for hold amount on saving account");

        final Long holdAmountTransactionId = bodyAsObject(depositResponses.get(2)).get("resourceId").getAsLong();

        final Long releaseAmountRequestId = 2L;
        final Long withdrawRequestId = 3L;

        final List<BatchRequest> withdrawRequests = List.of(getSavingsAccountRequest,
                BatchRequestBuilders.releaseAmountOnSavingsAccount(releaseAmountRequestId, getSavingsAccountRequestId,
                        holdAmountTransactionId),
                BatchRequestBuilders.withdrawSavingsAccount(withdrawRequestId, getSavingsAccountRequestId, 80F));

        final List<BatchResponse> withdrawResponses = batchHelper.executeEnclosingTransaction(withdrawRequests);

        assertEquals(SC_OK, withdrawResponses.get(1).getStatusCode(), "Verify Status Code 200 for release amount on saving account");
        assertEquals(SC_OK, withdrawResponses.get(2).getStatusCode(), "Verify Status Code 200 for withdraw saving account");
    }

    /**
     * Test for finding a one-to-one datatable entry by the query API and updating one of its columns.
     */
    @Test
    public void shouldFindOneToOneDatatableEntryByQueryAPIAndUpdateOneOfItsColumn() {
        final Long loanId = setupLoanAccount();
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5).toLowerCase();
        final String columnName1 = Utils.randomStringGenerator("COL1_", 5).toLowerCase();
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5).toLowerCase();
        createTwoStringColumnDatatable(datatableName, columnName1, columnName2, false, SHORT_COLUMN_LENGTH);
        try {
            final String columnValue1 = Utils.randomStringGenerator("VAL1_", 3);
            final String columnValue2 = Utils.randomStringGenerator("VAL2_", 3);
            createDatatableEntry(datatableName, loanId, columnName1, columnValue1, columnName2, columnValue2);

            final String updatedColumnValue2 = columnValue2 + "1";
            final List<BatchRequest> batchRequests = List.of(
                    BatchRequestBuilders.queryDatatableEntries(QUERY_DATATABLE_ENTRIES_REQUEST_ID, datatableName, columnName1, columnValue1,
                            "loan_id"),
                    BatchRequestBuilders.updateDatatableEntry(UPDATE_QUERIED_DATATABLE_ENTRY_REQUEST_ID, QUERY_DATATABLE_ENTRIES_REQUEST_ID,
                            datatableName, "$.[0].loan_id", columnName2, updatedColumnValue2));

            // Create a datatable entry so that it can be updated using BatchApi
            final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for query datatable entries");
            assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for update datatable entry");

            final JsonObject changes = bodyAsObject(responses.get(1)).getAsJsonObject("changes");
            assertEquals(updatedColumnValue2, changes.get(columnName2).getAsString());
        } finally {
            deleteDatatableWithEntriesOf(datatableName, loanId);
        }
    }

    /**
     * Test when a datatable entry was not found by the query API, and the update consequently fails.
     */
    @Test
    public void shouldNotFindAnyDatatableEntryByQueryAPIAndFailsToUpdateItsColumn() {
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5).toLowerCase();
        final String columnName1 = Utils.randomStringGenerator("COL1_", 5).toLowerCase();
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5).toLowerCase();
        createTwoStringColumnDatatable(datatableName, columnName1, columnName2, false, LONG_COLUMN_LENGTH);
        try {
            final List<BatchRequest> batchRequests = List.of(
                    BatchRequestBuilders.queryDatatableEntries(QUERY_DATATABLE_ENTRIES_REQUEST_ID, datatableName, columnName1,
                            "columnValue1", "loan_id"),
                    BatchRequestBuilders.updateDatatableEntry(UPDATE_QUERIED_DATATABLE_ENTRY_REQUEST_ID, QUERY_DATATABLE_ENTRIES_REQUEST_ID,
                            datatableName, "$.[0].loan_id", columnName2, "columnValue2"));

            final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            final BatchResponse updateResponse = responses.get(0);
            assertEquals(UPDATE_QUERIED_DATATABLE_ENTRY_REQUEST_ID, updateResponse.getRequestId());
            assertEquals(SC_BAD_REQUEST, updateResponse.getStatusCode(), "Verify Status Code 400 for update datatable entry");
            assertTrue(updateResponse.getBody().contains("The referenced JSON path is invalid"),
                    "Expected an invalid JSON path error but was: " + updateResponse.getBody());
        } finally {
            deleteDatatable(datatableName);
        }
    }

    /**
     * Test for finding a one-to-many datatable entry by the query API and updating one of its columns.
     */
    @Test
    public void shouldFindOneToManyDatatableEntryByQueryAPIAndUpdateOneOfItsColumn() {
        final Long loanId = setupLoanAccount();
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5).toLowerCase();
        final String columnName1 = Utils.randomStringGenerator("COL1_", 5).toLowerCase();
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5).toLowerCase();
        createTwoStringColumnDatatable(datatableName, columnName1, columnName2, true, SHORT_COLUMN_LENGTH);
        try {
            final String columnValue1 = Utils.randomStringGenerator("VAL1_", 3);
            final String columnValue2 = Utils.randomStringGenerator("VAL2_", 3);
            final Long existingEntryId = createDatatableEntry(datatableName, loanId, columnName1, columnValue1, columnName2, columnValue2);

            final List<BatchRequest> createAndReadRequests = List.of(
                    BatchRequestBuilders.createDatatableEntry(CREATE_DATATABLE_ENTRY_REQUEST_ID, datatableName, loanId,
                            List.of(columnName1, columnName2)),
                    BatchRequestBuilders.getDatatableEntries(GET_DATATABLE_ENTRIES_REQUEST_ID, CREATE_DATATABLE_ENTRY_REQUEST_ID,
                            datatableName, loanId, null));

            // Create a datatable entry so that it can be updated using BatchApi
            final List<BatchResponse> createAndReadResponses = batchHelper.executeEnclosingTransaction(createAndReadRequests);

            assertEquals(SC_OK, createAndReadResponses.get(0).getStatusCode(), "Verify Status Code 200 for create datatable entry");
            assertEquals(SC_OK, createAndReadResponses.get(1).getStatusCode(), "Verify Status Code 200 for get datatable entries");

            final Long createdEntryId = bodyAsObject(createAndReadResponses.get(0)).get("resourceId").getAsLong();
            // Create datatable entry batch request
            final String getDatatableEntriesResponse = createAndReadResponses.get(1).getBody();
            final JsonArray datatableEntries = JsonParser.parseString(getDatatableEntriesResponse).getAsJsonArray();
            assertEquals(2, datatableEntries.size());

            // Ensure both resourceIds are available in response
            assertTrue(getDatatableEntriesResponse.contains("\"id\": %d".formatted(createdEntryId)));
            assertTrue(getDatatableEntriesResponse.contains("\"id\": %d".formatted(existingEntryId)));

            final String updatedColumnValue2 = columnValue2 + "1";
            final List<BatchRequest> queryAndUpdateRequests = List.of(
                    BatchRequestBuilders.queryDatatableEntries(QUERY_DATATABLE_ENTRIES_REQUEST_ID, datatableName, columnName1, columnValue1,
                            "id,loan_id"),
                    BatchRequestBuilders.updateDatatableEntry(UPDATE_QUERIED_DATATABLE_ENTRY_REQUEST_ID, QUERY_DATATABLE_ENTRIES_REQUEST_ID,
                            datatableName, "$.[0].loan_id", "$.[0].id", columnName2, updatedColumnValue2));

            final List<BatchResponse> queryAndUpdateResponses = batchHelper.executeEnclosingTransaction(queryAndUpdateRequests);

            assertEquals(SC_OK, queryAndUpdateResponses.get(0).getStatusCode(), "Verify Status Code 200 for query datatable entries");
            assertEquals(SC_OK, queryAndUpdateResponses.get(1).getStatusCode(), "Verify Status Code 200 for update datatable entry");

            final JsonObject changes = bodyAsObject(queryAndUpdateResponses.get(1)).getAsJsonObject("changes");
            assertEquals(updatedColumnValue2, changes.get(columnName2).getAsString());
        } finally {
            deleteDatatableWithEntriesOf(datatableName, loanId);
        }
    }

    /**
     * Tests that a loan information was successfully updated through updateLoanCommand. In this test, we are marking an
     * active loan account as fraud.
     *
     * @see org.apache.fineract.batch.command.internal.ModifyLoanApplicationCommandStrategy
     */
    @Test
    public void shouldReflectChangesOnLoanUpdate() {
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        final Long clientId = createClient();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long updateLoanRequestId = disburseLoanRequestId + 1;

        // Create batch requests list
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                BatchRequestBuilders.markLoanAsFraud(updateLoanRequestId, disburseLoanRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for update loan application");

        // Get the changes parameter from updateLoan Response
        final JsonObject changes = bodyAsObject(responses.get(3)).getAsJsonObject("changes");
        assertTrue(changes.get("fraud").getAsBoolean());
    }

    /**
     * Tests that a loan information was successfully updated through updateLoanCommand using external id. In this test,
     * we are marking an active loan account as fraud.
     *
     * @see org.apache.fineract.batch.command.internal.ModifyLoanApplicationByExternalIdCommandStrategy
     */
    @Test
    public void shouldReflectChangesOnLoanUpdateByExternalId() {
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        final Long clientId = createClient();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long updateLoanRequestId = disburseLoanRequestId + 1;
        final Long getLoanRequestId = updateLoanRequestId + 1;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.transitionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId, today().minusDays(10),
                        "approve"),
                BatchRequestBuilders.transitionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId, today().minusDays(8),
                        "disburse"),
                BatchRequestBuilders.markLoanAsFraudByExternalId(updateLoanRequestId, approveLoanRequestId),
                BatchRequestBuilders.getLoanByExternalIdReference(getLoanRequestId, approveLoanRequestId, "associations=all"));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for update loan application");
        assertEquals(SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Get Loan");

        // Get the changes parameter from updateLoan Response
        final JsonObject changes = bodyAsObject(responses.get(3)).getAsJsonObject("changes");
        assertTrue(changes.get("fraud").getAsBoolean());
    }

    /**
     * Tests that a repayment posted as a batch request against a locked loan is rejected with a 409, when the batch is
     * submitted through the legacy un-versioned {@code loans/...} relative URL.
     */
    @Test
    public void shoulRetrieveTheProperErrorDuringLockedLoan_OldRelativePath() {
        assertLockedLoanRepaymentIsRejected(false);
    }

    /**
     * Tests that a repayment posted as a batch request against a locked loan is rejected with a 409, when the batch is
     * submitted through the current {@code v1/loans/...} relative URL.
     */
    @Test
    public void shoulRetrieveTheProperErrorDuringLockedLoan() {
        assertLockedLoanRepaymentIsRejected(true);
    }

    @Test
    public void verifyCalculatingRunningBalanceAfterBatchWithReleaseAmount() {
        final Long clientId = createClient();
        final Long savingsId = createActiveSavingsAccount(clientId, SAVINGS_SUBMITTED_ON_DATE, SAVINGS_APPROVED_ON_DATE,
                SAVINGS_ACTIVATED_ON_DATE);

        final float holdAmount = 10F;
        final float withdrawalAmount = 80F;

        final Long getSavingsAccountRequestId = 1L;
        final Long depositRequestId = 2L;
        final Long holdAmountRequestId = 3L;

        final BatchRequest getSavingsAccountRequest = BatchRequestBuilders.getSavingsAccount(getSavingsAccountRequestId, null, savingsId,
                "chargeStatus=all");

        final List<BatchRequest> depositRequests = List.of(getSavingsAccountRequest,
                BatchRequestBuilders.depositSavingsAccount(depositRequestId, getSavingsAccountRequestId, 300F),
                BatchRequestBuilders.holdAmountOnSavingsAccount(holdAmountRequestId, getSavingsAccountRequestId, holdAmount));

        final List<BatchResponse> depositResponses = batchHelper.executeEnclosingTransaction(depositRequests);

        assertEquals(SC_OK, depositResponses.get(1).getStatusCode(), "Verify Status Code 200 for deposit saving account");
        assertEquals(SC_OK, depositResponses.get(2).getStatusCode(), "Verify Status Code 200 for hold amount on saving account");

        final Long holdAmountTransactionId = bodyAsObject(depositResponses.get(2)).get("resourceId").getAsLong();

        final BigDecimal runningBalanceBeforeBatch = savingsHelper.getSavingsDetails(savingsId).getTransactions().get(0)
                .getRunningBalance();

        final Long releaseAmountRequestId = 2L;
        final Long firstWithdrawRequestId = 3L;
        final Long secondWithdrawRequestId = 4L;

        final List<BatchRequest> withdrawRequests = List.of(getSavingsAccountRequest,
                BatchRequestBuilders.releaseAmountOnSavingsAccount(releaseAmountRequestId, getSavingsAccountRequestId,
                        holdAmountTransactionId),
                BatchRequestBuilders.withdrawSavingsAccount(firstWithdrawRequestId, getSavingsAccountRequestId, withdrawalAmount),
                BatchRequestBuilders.withdrawSavingsAccount(secondWithdrawRequestId, getSavingsAccountRequestId, withdrawalAmount));

        final List<BatchResponse> withdrawResponses = batchHelper.executeEnclosingTransaction(withdrawRequests);

        assertEquals(SC_OK, withdrawResponses.get(0).getStatusCode(), "Verify Status Code 200 for get saving account");
        assertEquals(SC_OK, withdrawResponses.get(1).getStatusCode(), "Verify Status Code 200 for release amount on saving account");
        assertEquals(SC_OK, withdrawResponses.get(2).getStatusCode(), "Verify Status Code 200 for withdraw saving account");

        final List<SavingsAccountTransactionData> transactions = savingsHelper.getSavingsDetails(savingsId).getTransactions();
        final BigDecimal releaseRunningBalance = transactions.get(2).getRunningBalance();
        final BigDecimal firstWithdrawalRunningBalance = transactions.get(1).getRunningBalance();
        final BigDecimal secondWithdrawalRunningBalance = transactions.get(0).getRunningBalance();

        final BigDecimal expectedAfterRelease = runningBalanceBeforeBatch.add(BigDecimal.valueOf(holdAmount));
        assertEquals(0, expectedAfterRelease.compareTo(releaseRunningBalance), "Verify running balance after release amount");
        assertEquals(0, expectedAfterRelease.subtract(BigDecimal.valueOf(withdrawalAmount)).compareTo(firstWithdrawalRunningBalance),
                "Verify running balance after first withdrawal");
        assertEquals(0, expectedAfterRelease.subtract(BigDecimal.valueOf(withdrawalAmount)).subtract(BigDecimal.valueOf(withdrawalAmount))
                .compareTo(secondWithdrawalRunningBalance), "Verify running balance after second withdrawal");
    }

    @Test
    public void shouldReturnOkStatusForBatchChargeOff() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientCollateralId = createClientCollateral();

        final Long createClientRequestId = 4730L;
        final Long activateClientRequestId = 4731L;
        final Long applyLoanRequestId = 4732L;
        final Long approveLoanRequestId = 4733L;
        final Long disburseLoanRequestId = 4734L;
        final Long firstRepaymentRequestId = 4735L;
        final Long secondRepaymentRequestId = 4736L;
        final Long chargeOffRequestId = 4737L;

        // Create a createClient Request
        final List<BatchRequest> batchRequests = List.of(BatchRequestBuilders.createClient(createClientRequestId, ""),
                // Create a activateClient Request
                BatchRequestBuilders.activateClient(activateClientRequestId, createClientRequestId),
                // Create a ApplyLoan Request
                BatchRequestBuilders.applyLoan(applyLoanRequestId, activateClientRequestId, productId, clientCollateralId),
                // Create a approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                // Create a disburseLoan Request
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, approveLoanRequestId),
                // Create a loanRepay Request
                BatchRequestBuilders.repayLoan(firstRepaymentRequestId, disburseLoanRequestId, "500"),
                // Create a loanRepay Request
                BatchRequestBuilders.repayLoan(secondRepaymentRequestId, disburseLoanRequestId, "500"),
                BatchRequestBuilders.chargeOff(chargeOffRequestId, secondRepaymentRequestId));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(7).getStatusCode(), "Verify Status Code 200 for ChargeOff");
    }

    /**
     * Disburses a loan, hard-locks it, and asserts that a batch repayment posted by a user without the loan-checker
     * bypass permission is answered with a 409.
     */
    private void assertLockedLoanRepaymentIsRejected(final boolean apiVersionPrefixedRelativeUrl) {
        // Create product
        final Long productId = createBatchLoanProduct(SMALL_PRINCIPAL);
        // Create client
        final Long clientId = createClient();

        final Long applyLoanRequestId = randomRequestId();
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;

        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                BatchRequestBuilders.transitionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId, today().minusDays(10),
                        "approve"),
                BatchRequestBuilders.transitionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId, today().minusDays(8),
                        "disburse"));

        final List<BatchResponse> responses = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");

        final Long loanId = bodyAsObject(responses.get(2)).get("resourceId").getAsLong();
        placeHardLockOnLoan(loanId);

        final FeignBatchHelper nonByPassBatchHelper = new FeignBatchHelper(FeignUserHelper.getSimpleUserWithoutBypassPermissionClient());
        // Create a repayment Request
        final BatchRequest repaymentRequest = BatchRequestBuilders.repayLoanByLoanId(1L, loanId, "500", apiVersionPrefixedRelativeUrl);

        final ErrorResponse errorResponse = nonByPassBatchHelper
                .executeWithoutEnclosingTransactionExpectingError(List.of(repaymentRequest));
        // verify HTTP 409
        assertEquals(SC_CONFLICT, errorResponse.getHttpStatusCode());
    }

    /**
     * Creates the loan product all batch loan applications in this test are booked against: a 24-month, 2% declining
     * balance product with equal principal payments.
     */
    private Long createBatchLoanProduct(final double principal) {
        return createLoanProduct(new PostLoanProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .digitsAfterDecimal(0)//
                .inMultiplesOf(100)//
                .minPrincipal(1000.00)//
                .principal(principal)//
                .maxPrincipal(10000000.00)//
                .numberOfRepayments(24)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(2.0)//
                .interestRateFrequencyType(InterestRateFrequencyType.MONTHS)//
                .amortizationType(AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .inArrearsTolerance(0)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.name())//
                .accountingRule(1)//
                .isEqualAmortization(false)//
                .isInterestRecalculationEnabled(false)//
                .daysInYearType(DaysInYearType.ACTUAL)//
                .daysInMonthType(DaysInMonthType.ACTUAL)//
                .overdueDaysForNPA(5)//
                .dateFormat(DATETIME_PATTERN)//
                .locale("en_GB"));
    }

    private Long createClientCollateral() {
        final Long clientId = createClient();
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);
        return clientCollateralId;
    }

    private Long createActiveSavingsAccount(final Long clientId, final String submittedOnDate, final String approvedOnDate,
            final String activatedOnDate) {
        final Long productId = savingsProductHelper
                .createSavingsProduct(
                        SavingsRequestBuilders.defaultSavingsProduct().interestPostingPeriodType(InterestPostingPeriodType.DAILY))
                .getResourceId();
        final Long savingsId = savingsHelper.submitApplication(clientId, productId, submittedOnDate).getSavingsId();
        savingsHelper.approveSavings(savingsId, approvedOnDate);
        assertTrue(savingsHelper.getSavingsDetails(savingsId).getStatus().getApproved(), "Savings account is not approved");
        savingsHelper.activateSavings(savingsId, activatedOnDate);
        assertTrue(savingsHelper.getSavingsDetails(savingsId).getStatus().getActive(), "Savings account is not active");
        return savingsId;
    }

    /**
     * Creates the datatable used by the get-datatable batch tests, holding the sample columns of a loan application.
     */
    private String createLoanDatatable() {
        final PostDataTablesRequest request = new PostDataTablesRequest()//
                .datatableName(Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5))//
                .apptableName(LOAN_APP_TABLE_NAME)//
                .entitySubType(PERSON_ENTITY_SUB_TYPE)//
                .multiRow(false)//
                .columns(List.of(stringColumn("Spouse Name", 25L, true), numberColumn("Number of Dependents"),
                        column("Time of Visit", "DateTime", false), column("Date of Approval", "Date", false)));
        return datatableHelper.createDatatable(request).getResourceIdentifier();
    }

    /**
     * Creates a datatable holding two string columns. {@code columnLength} has to accommodate every value the test
     * writes to or filters those columns by; the query API rejects a filter value longer than its column.
     */
    private void createTwoStringColumnDatatable(final String datatableName, final String columnName1, final String columnName2,
            final boolean multiRow, final long columnLength) {
        final PostDataTablesRequest request = new PostDataTablesRequest()//
                .datatableName(datatableName)//
                .apptableName(LOAN_APP_TABLE_NAME)//
                .entitySubType(PERSON_ENTITY_SUB_TYPE)//
                .multiRow(multiRow)//
                .columns(List.of(stringColumn(columnName1, columnLength, true), stringColumn(columnName2, columnLength, false)));
        datatableHelper.createDatatable(request);
    }

    private static PostColumnHeaderData stringColumn(final String name, final long length, final boolean mandatory) {
        return column(name, "String", mandatory).length(length);
    }

    private static PostColumnHeaderData numberColumn(final String name) {
        return column(name, "Number", true);
    }

    private static PostColumnHeaderData column(final String name, final String type, final boolean mandatory) {
        return new PostColumnHeaderData().name(name).type(type).mandatory(mandatory);
    }

    /** Creates one datatable entry directly (not through the batch API) and returns its id. */
    private Long createDatatableEntry(final String datatableName, final Long loanId, final String columnName1, final String columnValue1,
            final String columnName2, final String columnValue2) {
        final Map<String, Object> entry = new LinkedHashMap<>();
        entry.put(columnName1, columnValue1);
        entry.put(columnName2, columnValue2);
        final Long entryId = datatableHelper.createDatatableEntry(datatableName, loanId, GSON.toJson(entry)).getResourceId();
        assertNotNull(entryId, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");
        return entryId;
    }

    /**
     * Delete datatable
     *
     * @param datatableName
     *            the datatable name
     */
    private void deleteDatatable(final String datatableName) {
        final String deletedDatatableName = datatableHelper.deleteDatatable(datatableName).getResourceIdentifier();
        assertEquals(datatableName, deletedDatatableName, "Fail to delete the datatable");
    }

    /** The server refuses to drop a datatable that still holds entries, so the loan's entries go first. */
    private void deleteDatatableWithEntriesOf(final String datatableName, final Long loanId) {
        datatableHelper.deleteDatatableEntries(datatableName, loanId);
        deleteDatatable(datatableName);
    }

    /**
     * Creates a disbursed and partially repaid loan, and returns its id, for the datatable tests to hang entries on.
     */
    private Long setupLoanAccount() {
        final Long productId = createBatchLoanProduct(LARGE_PRINCIPAL);
        final Long clientId = createClient();

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5734L;
        final Long repayLoanRequestId = 5735L;

        // Create product
        final List<BatchRequest> batchRequests = List.of(
                BatchRequestBuilders.applyLoanWithClientId(applyLoanRequestId, clientId, productId),
                // Create an approveLoan Request
                BatchRequestBuilders.approveLoan(approveLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.disburseLoan(disburseLoanRequestId, applyLoanRequestId),
                BatchRequestBuilders.repayLoan(repayLoanRequestId, applyLoanRequestId, "500"));

        // Create client
        final List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

        assertEquals(SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        // Create an ApplyLoan Request
        assertEquals(SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        assertEquals(SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        assertEquals(SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Repay Loan");

        return bodyAsObject(responses.get(0)).get("loanId").getAsLong();
    }

    private static Long randomRequestId() {
        // Create a disburseLoan Request
        return ThreadLocalRandom.current().nextLong(1000, 10_000);
    }

    private static LocalDate today() {
        // Create a repayment Request
        return LocalDate.now(Utils.getZoneIdOfTenant());
    }

    private static JsonObject bodyAsObject(final BatchResponse response) {
        return JsonParser.parseString(response.getBody()).getAsJsonObject();
    }

    private static JsonObject transactionAt(final BatchResponse loanResponse, final int index) {
        return bodyAsObject(loanResponse).getAsJsonArray("transactions").get(index).getAsJsonObject();
    }

    /** The loan API serialises dates as a {@code [year, month, day]} array. */
    private static LocalDate asLocalDate(final JsonArray dateArray) {
        return LocalDate.of(dateArray.get(0).getAsInt(), dateArray.get(1).getAsInt(), dateArray.get(2).getAsInt());
    }
}
