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

import static java.lang.Integer.parseInt;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.fineract.batch.command.internal.AdjustLoanTransactionCommandStrategy;
import org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy;
import org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdAndDataTableIdCommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;
import org.apache.fineract.integrationtests.common.loans.LoanAccountLockHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class BatchApiTest {

    private static final Logger LOG = LoggerFactory.getLogger(BatchApiTest.class);

    /**
     * The response specification
     */
    private ResponseSpecification responseSpec;

    /**
     * The request specification
     */
    private RequestSpecification requestSpec;

    /**
     * The datatable helper
     */
    private DatatableHelper datatableHelper;

    /**
     * Loan app datatable
     */
    private static final String LOAN_APP_TABLE_NAME = "m_loan";

    /**
     * Sets up the essential settings for the TEST like contentType, expectedStatusCode. It uses the '@BeforeEach'
     * annotation provided by jUnit.
     */
    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(this.requestSpec, this.responseSpec, true);
    }

    @AfterEach
    public void postActions() {
        GlobalConfigurationHelper.updateIsAutomaticExternalIdGenerationEnabled(this.requestSpec, this.responseSpec, false);
    }

    /**
     * Tests for the unimplemented command Strategies by returning 501 status code. For a unknownRequest a statusCode
     * 501 is returned back with response.
     *
     * @see org.apache.fineract.batch.command.internal.UnknownCommandStrategy
     */
    @Test
    public void shouldReturnStatusNotImplementedUnknownCommand() {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(4711L);
        br.setRelativeUrl("/nirvana");
        br.setMethod("POST");

        final List<BatchResponse> response = BatchHelper.postWithSingleRequest(this.requestSpec, this.responseSpec, br);

        // Verify that only 501 is returned as the status code
        for (BatchResponse resp : response) {
            Assertions.assertEquals((long) 501, (long) resp.getStatusCode(), "Verify Status code 501");
        }
    }

    /**
     * Tests for the successful response for a createClient request from createClientCommand. A successful response with
     * statusCode '200' is returned back.
     *
     * @see org.apache.fineract.batch.command.internal.CreateClientCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCreateClientCommand() {

        final BatchRequest br = BatchHelper.createClientRequest(4712L, "");

        final List<BatchResponse> response = BatchHelper.postWithSingleRequest(this.requestSpec, this.responseSpec, br);

        // Verify that a 200 response is returned as the status code
        for (BatchResponse resp : response) {
            Assertions.assertEquals((long) 200, (long) resp.getStatusCode(), "Verify Status code 200");
        }
    }

    /**
     * Tests for an erroneous response with statusCode '501' if transaction fails. If Query Parameter
     * 'enclosingTransaction' is set to 'true' and if one of the request in BatchRequest fails then all transactions are
     * rolled back.
     *
     * @see org.apache.fineract.batch.command.internal.CreateClientCommandStrategy
     * @see org.apache.fineract.batch.api.BatchApiResource
     * @see org.apache.fineract.batch.service.BatchApiService
     */
    @Test
    public void shouldRollBackAllTransactionsOnFailure() {
        // Create first client request
        final BatchRequest br1 = BatchHelper.createClientRequest(4713L, "TestExtId11");

        // Create second client request
        final BatchRequest br2 = BatchHelper.createClientRequest(4714L, "TestExtId12");

        // Create third client request, having same externalID as second client, hence cause of error
        final BatchRequest br3 = BatchHelper.createClientRequest(4715L, "TestExtId11");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Verifies that none of the client in BatchRequest is created on the server
        BatchHelper.verifyClientNotCreatedOnServer(this.requestSpec, this.responseSpec, "TestExtId11");
        BatchHelper.verifyClientNotCreatedOnServer(this.requestSpec, this.responseSpec, "TestExtId12");

        // Asserts that all the transactions have been successfully rolled back
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(SC_FORBIDDEN, response.get(0).getStatusCode(), "Verify Status code 403");
    }

    /**
     * Tests that a client information was successfully updated through updateClientCommand. A 'changes' parameter is
     * returned in the response after successful update of client information.
     *
     * @see org.apache.fineract.batch.command.internal.UpdateClientCommandStrategy
     */
    @Test
    public void shouldReflectChangesOnClientUpdate() {

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4716L, "");

        // Create a clientUpdate Request
        final BatchRequest br2 = BatchHelper.updateClientRequest(4717L, 4716L);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Get the changes parameter from updateClient Response
        final JsonObject changes = new FromJsonHelper().parse(response.get(1).getBody()).getAsJsonObject().get("changes").getAsJsonObject();

        // Asserts the client information is successfully updated
        Assertions.assertEquals("TestFirstName", changes.get("firstname").getAsString());
        Assertions.assertEquals("TestLastName", changes.get("lastname").getAsString());
    }

    /**
     * Tests that a ApplyLoanCommand was successfully executed and returned a 200(OK) status. It creates a new client
     * and apply a loan to that client. This also verifies the successful resolution of dependencies among two requests.
     *
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForApplyLoanCommand() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4718L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4719L, 4718L);

        // Create a ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4720L, 4719L, productId, clientCollateralId);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Get the clientId parameter from createClient Response
        final JsonElement clientId = new FromJsonHelper().parse(response.get(0).getBody()).getAsJsonObject().get("clientId");

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(1).getStatusCode(),
                "Verify Status Code 200" + clientId.getAsString());
    }

    /**
     * Tests that a new savings accounts was applied to an existing client and a 200(OK) status was returned. It first
     * creates a new client and a savings product, then uses the cliendId and ProductId to apply a savings account.
     *
     * @see org.apache.fineract.batch.command.internal.ApplySavingsCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForApplySavingsCommand() {

        final SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance("5000").build();

        final Integer productId = SavingsProductHelper.createSavingsProduct(savingsProductJSON, this.requestSpec, this.responseSpec);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4720L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4721L, 4720L);

        // Create a applySavings Request
        final BatchRequest br3 = BatchHelper.applySavingsRequest(4722L, 4721L, productId);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(1).getStatusCode(), "Verify Status Code 200");
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

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4722L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4723L, 4722L);

        // Create a ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4724L, 4723L, productId, clientCollateralId);

        // Create a Collect Charges Request
        final BatchRequest br4 = BatchHelper.collectChargesByLoanIdRequest(4725L, 4724L);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(3).getStatusCode(), "Verify Status Code 200 for Create Loan Charge");
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
    public void shouldReturnOkStatusForCreateAndGetChargeByIdCommand() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long createChargeRequestId = disburseLoanRequestId + 1;
        final Long getChargeByIdRequestId = createChargeRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create charge object and get id
        final Integer chargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON());

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        final BatchRequest createChargeRequest = BatchHelper.createChargeByLoanIdRequest(createChargeRequestId, disburseLoanRequestId,
                chargeId);

        final BatchRequest getChargeByIdRequest = BatchHelper.getChargeByLoanIdChargeId(getChargeByIdRequestId, createChargeRequestId);

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                createChargeRequest, getChargeByIdRequest);

        // Create batch responses list
        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Create Charge");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Get Charge By Id");
    }

    /**
     * Test for a successful charge adjustment. A '200' status code is expected on successful responses.
     *
     * @see AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulChargeAdjustment() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long createChargeRequestId = disburseLoanRequestId + 1;
        final Long adjustChargeRequestId = createChargeRequestId + 1;
        final Long getTransactionRequestId = adjustChargeRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create charge object and get id
        final Integer chargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON());

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        final BatchRequest createChargeRequest = BatchHelper.createChargeByLoanIdRequest(createChargeRequestId, disburseLoanRequestId,
                chargeId);

        final BatchRequest adjustChargeRequest = BatchHelper.adjustChargeRequest(adjustChargeRequestId, createChargeRequestId);

        final BatchRequest getTransactionRequest = BatchHelper.getTransactionByIdRequest(getTransactionRequestId, adjustChargeRequestId,
                true);

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                createChargeRequest, adjustChargeRequest, getTransactionRequest);

        // Create batch responses list
        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Create Charge");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Adjust Charge");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for Get Transaction By Id");

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonObject chargeAdjustment = jsonHelper.parse(responses.get(5).getBody()).getAsJsonObject().get("type").getAsJsonObject();

        Assertions.assertEquals("Charge Adjustment", chargeAdjustment.get("value").getAsString());
        Assertions.assertTrue(chargeAdjustment.get("chargeAdjustment").getAsBoolean());
    }

    /**
     * Tests that a new charge was added to a newly created loan and charges are Collected properly 200(OK) status was
     * returned for successful responses. It first creates a new client and apply a loan, then creates a new charge for
     * the create loan and then fetches all the applied charges using external id
     *
     * @see org.apache.fineract.batch.command.internal.CollectChargesCommandStrategy
     * @see org.apache.fineract.batch.command.internal.CreateChargeCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCreateAndGetChargeByExternalIdCommand() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long getLoanRequestId = disburseLoanRequestId + 1;
        final Long createChargeRequestId = getLoanRequestId + 1;
        final Long collectChargesRequestId = createChargeRequestId + 1;
        final Long adjustChargeRequestId = createChargeRequestId + 1;
        final Long getTransactionByExternalIdRequestId = adjustChargeRequestId + 1;
        final Long getChargeByIdRequestId = getTransactionByExternalIdRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create charge object and get id
        final Integer chargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON());

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.transistionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10), "approve");

        final BatchRequest disburseLoanRequest = BatchHelper.transistionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(8), "disburse");

        final BatchRequest getLoanRequest = BatchHelper.getLoanByExternalIdRequest(getLoanRequestId, approveLoanRequestId,
                "associations=all");

        final BatchRequest createChargeRequest = BatchHelper.createChargeByLoanExternalIdRequest(createChargeRequestId, getLoanRequestId,
                chargeId);

        final BatchRequest collectChargesRequest = BatchHelper.collectChargesByLoanExternalIdRequest(collectChargesRequestId,
                getLoanRequestId);

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest, getLoanRequest,
                createChargeRequest, collectChargesRequest);

        // Create batch responses list
        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Create Charge");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(5).getStatusCode(), "Verify Status Code 200 for Collect charges");

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final String loanExternalId = jsonHelper.parse(responses.get(3).getBody()).getAsJsonObject().get("externalId").getAsString();
        final String chargeExternalId = jsonHelper.parse(responses.get(4).getBody()).getAsJsonObject().get("resourceExternalId")
                .getAsString();

        final BatchRequest adjustChargeByExternalId = BatchHelper.adjustChargeByExternalIdRequest(adjustChargeRequestId, null,
                loanExternalId, chargeExternalId);
        final BatchRequest getTransactionByExternalIdRequest = BatchHelper
                .getTransactionByExternalIdRequest(getTransactionByExternalIdRequestId, adjustChargeRequestId, loanExternalId, true);
        final BatchRequest getChargeByIdRequest = BatchHelper.getChargeByLoanExternalIdChargeExternalId(getChargeByIdRequestId,
                getTransactionByExternalIdRequestId, loanExternalId, chargeExternalId);

        // Create batch responses list
        final List<BatchResponse> adjustChargeAndGetResponses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec,
                this.responseSpec,
                BatchHelper.toJsonString(Arrays.asList(adjustChargeByExternalId, getTransactionByExternalIdRequest, getChargeByIdRequest)));

        Assertions.assertEquals(HttpStatus.SC_OK, adjustChargeAndGetResponses.get(0).getStatusCode(),
                "Verify Status Code 200 for Adjust Charge By External Id");
        Assertions.assertEquals(HttpStatus.SC_OK, adjustChargeAndGetResponses.get(1).getStatusCode(),
                "Verify Status Code 200 for Get Transaction By Id");
        Assertions.assertEquals(HttpStatus.SC_OK, adjustChargeAndGetResponses.get(2).getStatusCode(),
                "Verify Status Code 200 for Get Charge By Id");

        final JsonObject chargeAdjustment = jsonHelper.parse(adjustChargeAndGetResponses.get(1).getBody()).getAsJsonObject().get("type")
                .getAsJsonObject();

        Assertions.assertEquals("Charge Adjustment", chargeAdjustment.get("value").getAsString());
        Assertions.assertTrue(chargeAdjustment.get("chargeAdjustment").getAsBoolean());
    }

    /**
     * Tests that batch repayment for loans is happening properly. Collected properly 200(OK) status was returned for
     * successful responses. It first creates a new loan and then makes two repayments for it and then verifies that
     * 200(OK) is returned for the repayment requests.
     *
     * @see CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepayment() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4730L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4731L, 4730L);

        // Create a ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4732L, 4731L, productId, clientCollateralId);

        // Create a approveLoan Request
        final BatchRequest br4 = BatchHelper.approveLoanRequest(4733L, 4732L);

        // Create a disburseLoan Request
        final BatchRequest br5 = BatchHelper.disburseLoanRequest(4734L, 4733L);

        // Create a loanRepay Request
        final BatchRequest br6 = BatchHelper.repayLoanRequest(4735L, 4734L, "500");

        // Create a loanRepay Request
        final BatchRequest br7 = BatchHelper.repayLoanRequest(4736L, 4734L, "500");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);
        batchRequests.add(br5);
        batchRequests.add(br6);
        batchRequests.add(br7);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(5).getStatusCode(), "Verify Status Code 200 for Repayment");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(6).getStatusCode(), "Verify Status Code 200 for Repayment");
    }

    /**
     * Tests that batch credit balance refund for loans is happening properly. Collected properly 200(OK) status was
     * returned for successful responses. It first creates a new loan and then makes an overpayment, before creating a
     * credit balance refund to refund a portion of the over-payment.
     *
     * @see CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchCreditBalanceRefund() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long repayLoanRequestId = disburseLoanRequestId + 1;
        final Long creditBalanceRefundRequestId = repayLoanRequestId + 1;

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createActiveClientRequest(createActiveClientRequestId, "");

        // Create a ApplyLoan Request
        final BatchRequest br2 = BatchHelper.applyLoanRequest(applyLoanRequestId, createActiveClientRequestId, productId,
                clientCollateralId);

        // Create a approveLoan Request
        final BatchRequest br3 = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburseLoan Request
        final BatchRequest br4 = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a loanRepay Request which will result in an overpay.
        final BatchRequest br5 = BatchHelper.repayLoanRequest(repayLoanRequestId, disburseLoanRequestId, "20000");

        // Create a credit balance refund request
        final BatchRequest br6 = BatchHelper.creditBalanceRefundRequest(creditBalanceRefundRequestId, repayLoanRequestId, "500");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);
        batchRequests.add(br5);
        batchRequests.add(br6);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(4).getStatusCode(), "Verify Status Code 200 for Repayment");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(5).getStatusCode(),
                "Verify Status Code 200 for Credit Balance Refund");
    }

    @Test
    public void partialFailTestForBatchRequest() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long fetchLoanInfoRequestId = disburseLoanRequestId + 1;

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createActiveClientRequest(createActiveClientRequestId, "");

        // Create a ApplyLoan Request
        final BatchRequest br2 = BatchHelper.applyLoanRequest(applyLoanRequestId, createActiveClientRequestId, productId,
                clientCollateralId);

        // Create a wrong approveLoan Request
        final BatchRequest br3 = BatchHelper.approveLoanWrongRequest(approveLoanRequestId, applyLoanRequestId);

        // Fetch loan info
        final BatchRequest br4 = BatchHelper.getLoanByIdRequest(fetchLoanInfoRequestId, applyLoanRequestId, null);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, (long) response.get(2).getStatusCode(), "Resource doesn not exists");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(3).getStatusCode(),
                "Verify Status Code 200 for fetch data after the error");
    }

    /**
     * Tests successful run of batch goodwill credit for loans. 200(OK) status is returned for successful responses. It
     * first creates a new loan, approves and disburses the loan. Then a goodwill credit request is made
     *
     * @see CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchGoodwillCredit() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long goodwillCreditRequestId = disburseLoanRequestId + 1;

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createActiveClientRequest(createActiveClientRequestId, "");

        // Create a ApplyLoan Request
        final BatchRequest br2 = BatchHelper.applyLoanRequest(applyLoanRequestId, createActiveClientRequestId, productId,
                clientCollateralId);

        // Create a approveLoan Request
        final BatchRequest br3 = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburseLoan Request
        final BatchRequest br4 = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a good will credit request.
        final BatchRequest br5 = BatchHelper.goodwillCreditRequest(goodwillCreditRequestId, disburseLoanRequestId, "500");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);
        batchRequests.add(br5);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(4).getStatusCode(), "Verify Status Code 200 for Goodwill credit");
    }

    /**
     * Test for the successful activation of a pending client using 'ActivateClientCommandStrategy'. A '200' status code
     * is expected on successful activation.
     *
     * @see org.apache.fineract.batch.command.internal.ActivateClientCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulClientActivation() {

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4726L, "");

        // Create an activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4727L, 4726L);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(0).getStatusCode(), "Verify Status Code 200 for Create Client");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(1).getStatusCode(), "Verify Status Code 200 for Activate Client");
    }

    /**
     * Test for the successful approval and disbursal of a loan using 'ApproveLoanCommandStrategy' and
     * 'DisburseLoanCommandStrategy'. A '200' status code is expected on successful activation.
     *
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulLoanApprovalAndDisburse() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4730L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4731L, 4730L);

        // Create an ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4732L, 4731L, productId, clientCollateralId);

        // Create an approveLoan Request
        final BatchRequest br4 = BatchHelper.approveLoanRequest(4733L, 4732L);

        // Create an disburseLoan Request
        final BatchRequest br5 = BatchHelper.disburseLoanRequest(4734L, 4733L);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);
        batchRequests.add(br5);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(3).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(4).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
    }

    /**
     * Test for the successful create client, apply loan,approval and disbursal of a loan using Batch API with
     * enclosingTransaction. A '200' status code is expected on successful activation.
     *
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulLoanApprovalAndDisburseWithTransaction() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                String.valueOf(clientID), collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createActiveClientRequest(4740L, "");

        // Create an ApplyLoan Request
        final BatchRequest br2 = BatchHelper.applyLoanRequest(4742L, 4740L, productId, clientCollateralId);

        // Create an approveLoan Request
        final BatchRequest br3 = BatchHelper.approveLoanRequest(4743L, 4742L);

        // Create a disburseLoan Request
        final BatchRequest br4 = BatchHelper.disburseLoanRequest(4744L, 4743L);

        final List<BatchRequest> batchRequests = Arrays.asList(br1, br2, br3, br4);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(0).getStatusCode(), "Verify Status Code 200 for create client");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(1).getStatusCode(), "Verify Status Code 200 for apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(2).getStatusCode(), "Verify Status Code 200 for approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(3).getStatusCode(), "Verify Status Code 200 for disburse Loan");
    }

    /**
     * Test for the successful disbursement and get loan. A '200' status code is expected on successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetLoanTransactionByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulDisbursementAndGetTransaction() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = 6730L;
        final Long approveLoanRequestId = 6731L;
        final Long disburseLoanRequestId = 6732L;
        final Long getTransactionRequestId = 6733L;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an ApplyLoan Request
        final BatchRequest batchRequest1 = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approveLoan Request
        final BatchRequest batchRequest2 = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disbursement Request
        final BatchRequest batchRequest3 = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a getTransaction Request
        final BatchRequest batchRequest4 = BatchHelper.getTransactionByIdRequest(getTransactionRequestId, disburseLoanRequestId, true);

        final List<BatchRequest> batchRequests = Arrays.asList(batchRequest1, batchRequest2, batchRequest3, batchRequest4);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Transaction By Id");
    }

    /**
     * Test for the successful new loan reschedule request and approval for the same. A '200' status code is expected on
     * successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.CreateLoanRescheduleRequestCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanRescheduleCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulDisbursementAndRescheduleLoan() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long createActiveClientRequestId = 8462L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long rescheduleLoanRequestId = disburseLoanRequestId + 1;
        final Long approveRescheduleLoanRequestId = rescheduleLoanRequestId + 1;

        // Create product
        LOG.info("LoanProduct {}", loanProductJSON);
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        /* Retrieve/Create Code Values for the Code "LoanRescheduleReason = 23" */
        final HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(23, this.requestSpec, this.responseSpec);

        final Integer codeValueId = (Integer) codeValue.get("id");

        // Create an ApplyLoan request
        final BatchRequest applyLoanRequestWithClientId = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approveLoan request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disbursement request
        final LocalDate disburseLoanDate = LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(1);
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId,
                disburseLoanDate);

        // Create a reschedule loan request
        final BatchRequest rescheduleLoanRequest = BatchHelper.createRescheduleLoanRequest(rescheduleLoanRequestId, disburseLoanRequestId,
                disburseLoanDate.plusMonths(1), codeValueId);

        // Approve reschedule loan request
        final BatchRequest approveRescheduleLoanRequest = BatchHelper.approveRescheduleLoanRequest(approveRescheduleLoanRequestId,
                rescheduleLoanRequestId);

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequestWithClientId, approveLoanRequest, disburseLoanRequest,
                rescheduleLoanRequest, approveRescheduleLoanRequest);

        LOG.info("shouldReturnOkStatusOnSuccessfulDisbursementAndRescheduleLoan Request - {}", BatchHelper.toJsonString(batchRequests));
        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(),
                "Verify Status Code 200 for Create Reschedule Loan request");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(4).getStatusCode(),
                "Verify Status Code 200 for Approve Reschedule Loan request");
    }

    /**
     * Test for the successful create client, create, approve and get loan. A '200' status code is expected on
     * successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.CreateClientCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulCreateClientCreateApproveAndGetLoan() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = 4731L;
        final Long approveLoanRequestId = 4732L;
        final Long getLoanByIdRequestId = 4733L;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create createClient Request
        final BatchRequest batchRequest1 = BatchHelper.createActiveClientRequest(createActiveClientRequestId, "");

        // Create an ApplyLoan Request
        final BatchRequest batchRequest2 = BatchHelper.applyLoanRequest(applyLoanRequestId, createActiveClientRequestId, productId, null);

        // Create an approveLoan Request
        final BatchRequest batchRequest3 = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Get loan by id Request
        final BatchRequest batchRequest4 = BatchHelper.getLoanByIdRequest(getLoanByIdRequestId, applyLoanRequestId, null);

        final List<BatchRequest> batchRequests = Arrays.asList(batchRequest1, batchRequest2, batchRequest3, batchRequest4);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Create Client");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan By Id");

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(responses.get(1).getBody()).getAsJsonObject());
        final Long loanIdInGetResponse = jsonHelper.extractLongNamed("id", jsonHelper.parse(responses.get(3).getBody()).getAsJsonObject());
        final JsonObject statusInGetResponse = jsonHelper.parse(responses.get(3).getBody()).getAsJsonObject().get("status")
                .getAsJsonObject();

        Assertions.assertEquals(loanId, loanIdInGetResponse);
        Assertions.assertEquals(LoanStatus.APPROVED.getCode(), jsonHelper.extractStringNamed("code", statusInGetResponse));
        Assertions.assertEquals("Approved", jsonHelper.extractStringNamed("value", statusInGetResponse));
    }

    /**
     * Test for the successful creat, approve and get loan. A '200' status code is expected on successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulCreateApproveAndGetLoan() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long getLoanByIdRequestId = 5732L;
        final Long getLoanByIdWithQueryParametersRequestId = 5733L;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an ApplyLoan Request
        final BatchRequest batchRequest1 = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approveLoan Request
        final BatchRequest batchRequest2 = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Get loan by id Request without query param
        final BatchRequest batchRequest3 = BatchHelper.getLoanByIdRequest(getLoanByIdRequestId, applyLoanRequestId, null);

        // Get loan by id Request with query param
        final BatchRequest batchRequest4 = BatchHelper.getLoanByIdRequest(getLoanByIdWithQueryParametersRequestId, applyLoanRequestId,
                "associations=repaymentSchedule,transactions");

        final List<BatchRequest> batchRequests = Arrays.asList(batchRequest1, batchRequest2, batchRequest3, batchRequest4);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(),
                "Verify Status Code 200 for Get Loan By Id without query parameter");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(),
                "Verify Status Code 200 for Get Loan By Id with query parameter");

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(responses.get(0).getBody()).getAsJsonObject());
        final Long loanIdInGetResponse = jsonHelper.extractLongNamed("id", jsonHelper.parse(responses.get(2).getBody()).getAsJsonObject());
        final JsonObject statusInGetResponse = jsonHelper.parse(responses.get(2).getBody()).getAsJsonObject().get("status")
                .getAsJsonObject();

        Assertions.assertEquals(loanId, loanIdInGetResponse);
        Assertions.assertEquals(LoanStatus.APPROVED.getCode(), jsonHelper.extractStringNamed("code", statusInGetResponse));
        Assertions.assertEquals("Approved", jsonHelper.extractStringNamed("value", statusInGetResponse));

        // Repayment schedule will not be available in the response
        Assertions.assertFalse(responses.get(2).getBody().contains("repaymentSchedule"));

        // Repayment schedule information will be available in the response based on the query parameter
        Assertions.assertTrue(responses.get(3).getBody().contains("repaymentSchedule"));
    }

    /**
     * Test for the successful get loan and get datatable entry. A '200' status code is expected on successful
     * responses.
     *
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulGetDataTableEntry() {
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(setupAccount()).getAsJsonObject());
        final String datatableName = this.datatableHelper.createDatatable(LOAN_APP_TABLE_NAME, false);
        try {

            // Get loan by id Request with query param
            final BatchRequest getLoanBatchRequest = BatchHelper.getLoanByIdRequest(loanId, "associations=repaymentSchedule,transactions");

            // Get datatable batch request
            final BatchRequest getDatatableBatchRequest = BatchHelper.getDatatableByIdRequest(loanId, datatableName,
                    "genericResultSet=true", null);

            final List<BatchRequest> batchRequestsGetLoan = Arrays.asList(getLoanBatchRequest, getDatatableBatchRequest);

            final List<BatchResponse> responsesGetLoan = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec,
                    this.responseSpec, BatchHelper.toJsonString(batchRequestsGetLoan));

            final String getLoanResponse = responsesGetLoan.get(0).getBody();
            final String getDatatableResponse = responsesGetLoan.get(1).getBody();

            Assertions.assertEquals(HttpStatus.SC_OK, responsesGetLoan.get(0).getStatusCode(), "Verify Status Code 200 for get loan");
            Assertions.assertEquals(HttpStatus.SC_OK, responsesGetLoan.get(1).getStatusCode(), "Verify Status Code 200 for datatable");

            final Long loanIdInGetResponse = jsonHelper.extractLongNamed("id", jsonHelper.parse(getLoanResponse).getAsJsonObject());
            Assertions.assertEquals(loanId, loanIdInGetResponse);

            // Repayment schedule information will be available in the response based on the query parameter
            Assertions.assertTrue(getLoanResponse.contains("repaymentSchedule"));

            // Transaction will be available in the response based on the query parameter
            Assertions.assertTrue(getLoanResponse.contains("transactions"));

            // datatable info will be available in the response based on the query parameter
            Assertions.assertTrue(getDatatableResponse.contains("columnHeaders"));

            // datatable info will be available in the response based on the query parameter
            Assertions.assertTrue(getDatatableResponse.contains("data"));
        } finally {
            deleteDatatable(datatableName);
        }
    }

    /**
     * Test for the successful create and update datatable entry. A '200' status code is expected on successful
     * responses.
     *
     * @see org.apache.fineract.batch.command.internal.CreateDatatableEntryCommandStrategy
     * @see org.apache.fineract.batch.command.internal.UpdateDatatableEntryOneToManyCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetDatatableEntryByAppTableIdCommandStrategy
     * @see GetDatatableEntryByAppTableIdAndDataTableIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulCreateDataTableEntry() {
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(setupAccount()).getAsJsonObject());
        // creating datatable with m_loan association
        final Map<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5);
        final String columnName1 = Utils.randomStringGenerator("COL1_", 5);
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5);
        columnMap.put("datatableName", datatableName);
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", true);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName1, "String", true, 10, null);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName2, "String", false, 10, null);
        columnMap.put("columns", datatableColumnsList);
        final String datatableRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("CreateDataTable map : {}", datatableRequestJsonString);

        this.datatableHelper.createDatatable(datatableRequestJsonString, "");

        // Create a datatable entry so that it can be updated using BatchApi
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(columnName1, Utils.randomStringGenerator("VAL1_", 3));
        datatableEntryMap.put(columnName2, Utils.randomStringGenerator("VAL2_", 3));
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("CreateDataTableEntry map : {}", datatableEntryRequestJsonString);

        final Map<String, Object> datatableEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, loanId.intValue(),
                false, datatableEntryRequestJsonString);
        final Integer datatableEntryResourceId = (Integer) datatableEntryResponse.get("resourceId");
        assertNotNull(datatableEntryResourceId, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Create datatable entry batch request
        final BatchRequest createDatatableEntryRequest = BatchHelper.createDatatableEntryRequest(loanId.toString(), datatableName,
                Arrays.asList(columnName1, columnName2));

        // Update datatable entry batch request
        final BatchRequest updateDatatableEntryByEntryIdRequest = BatchHelper.updateDatatableEntryByEntryIdRequest(loanId, datatableName,
                Long.valueOf(datatableEntryResourceId), Arrays.asList(columnName1));

        // Get datatable entries batch request
        final BatchRequest getDatatableEntriesRequest = BatchHelper.getDatatableByIdRequest(loanId, datatableName, null,
                updateDatatableEntryByEntryIdRequest.getReference());

        // Get datatable entry by app table id batch request
        final BatchRequest getDatatableEntryByIdRequest = BatchHelper.getDatatableEntryByIdRequest(loanId, datatableName, "$.resourceId",
                null, updateDatatableEntryByEntryIdRequest.getReference());

        final List<BatchRequest> batchRequestsDatatableEntries = Arrays.asList(createDatatableEntryRequest,
                updateDatatableEntryByEntryIdRequest, getDatatableEntriesRequest, getDatatableEntryByIdRequest);
        LOG.info("Batch Request : {}", BatchHelper.toJsonString(batchRequestsDatatableEntries));

        final List<BatchResponse> responseDatatableBatch = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec,
                this.responseSpec, BatchHelper.toJsonString(batchRequestsDatatableEntries));

        LOG.info("Batch Response : {}", new Gson().toJson(responseDatatableBatch));

        final BatchResponse batchResponse1 = responseDatatableBatch.get(0);
        final BatchResponse batchResponse2 = responseDatatableBatch.get(1);
        final BatchResponse batchResponse3 = responseDatatableBatch.get(2);
        final BatchResponse batchResponse4 = responseDatatableBatch.get(3);

        Assertions.assertEquals(HttpStatus.SC_OK, batchResponse1.getStatusCode(), "Verify Status Code 200 for create datatable entry");
        Assertions.assertEquals(HttpStatus.SC_OK, batchResponse2.getStatusCode(), "Verify Status Code 200 for update datatable entry");
        Assertions.assertEquals(HttpStatus.SC_OK, batchResponse3.getStatusCode(), "Verify Status Code 200 for get datatable entries");
        Assertions.assertEquals(HttpStatus.SC_OK, batchResponse4.getStatusCode(), "Verify Status Code 200 for get datatable entry by id");

        final String getDatatableEntriesResponse = batchResponse3.getBody();

        final Long createDatatableEntryId = jsonHelper.extractLongNamed("resourceId",
                jsonHelper.parse(batchResponse1.getBody()).getAsJsonObject());

        final JsonArray datatableEntries = jsonHelper.parse(getDatatableEntriesResponse).getAsJsonArray();
        Assertions.assertEquals(2, datatableEntries.size());

        // Ensure both resourceIds are available in response
        Assertions.assertTrue(getDatatableEntriesResponse.contains(String.format("\"id\": %d", createDatatableEntryId)));
        Assertions.assertTrue(getDatatableEntriesResponse.contains(String.format("\"id\": %d", datatableEntryResourceId)));
    }

    /**
     * Test for the successful get loan and get datatable entry where get datatable request have no query param. A '200'
     * status code is expected on successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulGetDatatableEntryWithNoQueryParam() {
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(setupAccount()).getAsJsonObject());
        final String datatableName = this.datatableHelper.createDatatable(LOAN_APP_TABLE_NAME, false);
        try {
            // Get loan by id Request with query param
            final BatchRequest getLoanBatchRequest = BatchHelper.getLoanByIdRequest(loanId, "associations=repaymentSchedule,transactions");

            // Get datatable batch request
            final BatchRequest getDatatableBatchRequest = BatchHelper.getDatatableByIdRequest(loanId, datatableName, null, null);

            final List<BatchRequest> batchRequestsGetLoan = Arrays.asList(getLoanBatchRequest, getDatatableBatchRequest);

            final List<BatchResponse> responsesGetLoan = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec,
                    this.responseSpec, BatchHelper.toJsonString(batchRequestsGetLoan));

            final String getLoanResponse = responsesGetLoan.get(0).getBody();

            Assertions.assertEquals(HttpStatus.SC_OK, responsesGetLoan.get(0).getStatusCode(), "Verify Status Code 200 for Get Loan");
            Assertions.assertEquals(HttpStatus.SC_OK, responsesGetLoan.get(1).getStatusCode(), "Verify Status Code 200 for Get Datatable");

            final Long loanIdInGetResponse = jsonHelper.extractLongNamed("id", jsonHelper.parse(getLoanResponse).getAsJsonObject());
            Assertions.assertEquals(loanId, loanIdInGetResponse);

            Assertions.assertTrue(getLoanResponse.contains("repaymentSchedule"));

            Assertions.assertTrue(getLoanResponse.contains("transactions"));
        } finally {
            deleteDatatable(datatableName);
        }

    }

    /**
     * Test for the successful merchant issued and payout refund transaction. A '200' status code is expected on
     * successful responses.
     *
     * @see org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy
     * @see org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulTransactionMerchantIssuedAndPayoutRefund() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long merchantIssuedRefundRequestId = 5733L;
        final Long payoutRefundRequestId = 5734L;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an ApplyLoan Request
        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approveLoan Request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disbursement request
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(1));

        // Create a merchant issued refund request
        final BatchRequest merchantIssuedRefundRequest = BatchHelper.merchantIssuedRefundRequest(merchantIssuedRefundRequestId,
                applyLoanRequestId, "10");

        // Create a payout refund request
        final BatchRequest payoutRefundRequest = BatchHelper.payoutRefundRequest(payoutRefundRequestId, applyLoanRequestId, "10");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                merchantIssuedRefundRequest, payoutRefundRequest);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for merchant issued refund");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for payout refund");
    }

    /**
     * Test for the successful repayment reversal transaction. A '200' status code is expected on successful responses.
     *
     * @see AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepaymentReversal() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final LocalDate date = LocalDate.now(Utils.getZoneIdOfTenant());
        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long repayLoanRequestId = 5733L;
        final Long repayReversalRequestId = 5734L;
        final Long getLoanRequestId = 5735L;

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an apply loan request
        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approve loan request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburse loan request
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a repayment request.
        final BatchRequest repaymentRequest = BatchHelper.repayLoanRequest(repayLoanRequestId, disburseLoanRequestId, "500");

        // Create a repayment reversal request
        final BatchRequest repaymentReversalRequest = BatchHelper.createAdjustTransactionRequest(repayReversalRequestId, repayLoanRequestId,
                "0", date);

        // Get loan transactions request
        final BatchRequest getLoanTransactionsRequest = BatchHelper.getLoanByIdRequest(getLoanRequestId, applyLoanRequestId,
                "associations=transactions");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest, repaymentRequest,
                repaymentReversalRequest, getLoanTransactionsRequest);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonObject repayment = jsonHelper.parse(responses.get(5).getBody()).getAsJsonObject().get("transactions").getAsJsonArray()
                .get(2).getAsJsonObject();
        final JsonArray dateArray = repayment.get("reversedOnDate").getAsJsonArray();
        final LocalDate reversedOnDate = LocalDate.of(dateArray.get(0).getAsInt(), dateArray.get(1).getAsInt(),
                dateArray.get(2).getAsInt());

        Assertions.assertEquals(HttpStatus.SC_OK, (long) responses.get(4).getStatusCode(), "Verify Status Code 200 for repayment reversal");
        Assertions.assertEquals("Repayment", repayment.get("type").getAsJsonObject().get("value").getAsString());
        Assertions.assertTrue(repayment.get("manuallyReversed").getAsBoolean());
        Assertions.assertEquals(date, reversedOnDate);
    }

    /**
     * Test for the successful repayment reversal transaction using loan external id and transaction external id. A
     * '200' status code is expected on successful responses.
     *
     * @see AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepaymentReversalUsingExternalId() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final LocalDate date = LocalDate.now(Utils.getZoneIdOfTenant());
        final Long applyLoanRequestId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long getLoanBeforeTxnRequestId = disburseLoanRequestId + 1;
        final Long repayLoanRequestId = getLoanBeforeTxnRequestId + 1;
        final Long getLoanAfterTxnRequestId = repayLoanRequestId + 1;
        final Long repayReversalRequestId = getLoanAfterTxnRequestId + 1;
        final Long getLoanAfterReversal = repayReversalRequestId + 1;

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);
        final String loanExternalId = UUID.randomUUID().toString();

        // Create an apply loan request
        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientIdAndExternalId(applyLoanRequestId, clientId, productId,
                loanExternalId);

        // Create an approve loan request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburse loan request
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Get loan transactions request
        final BatchRequest getLoanTransactionsRequestBeforeTxn = BatchHelper.getLoanByIdRequest(getLoanBeforeTxnRequestId,
                disburseLoanRequestId, "associations=transactions");

        // Create a repayment request by external id
        final BatchRequest repaymentRequest = BatchHelper.createTransactionRequestByLoanExternalId(repayLoanRequestId,
                getLoanBeforeTxnRequestId, "repayment", "500", LocalDate.now(Utils.getZoneIdOfTenant()));

        // Get loan transactions request
        final BatchRequest getLoanTransactionsRequestAfterTxn = BatchHelper.getLoanByIdRequest(getLoanAfterTxnRequestId, repayLoanRequestId,
                "associations=transactions");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                getLoanTransactionsRequestBeforeTxn, repaymentRequest, getLoanTransactionsRequestAfterTxn);

        // Because loanExternalId & transactionExternalId are coming from 2 different responses, there is no easy way to
        // use them as reference in 1 batch api call.
        // So we are splitting repayment & reversal into 2 different batch api invocations
        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final String loanExternalIdDisburseLoanResponse = jsonHelper.parse(responses.get(3).getBody()).getAsJsonObject().get("externalId")
                .getAsString();
        final Long loanId = jsonHelper.parse(responses.get(3).getBody()).getAsJsonObject().get("id").getAsLong();
        final String transactionExternalId = jsonHelper.parse(responses.get(4).getBody()).getAsJsonObject().get("resourceExternalId")
                .getAsString();
        Assertions.assertNotNull(loanExternalIdDisburseLoanResponse);
        Assertions.assertEquals(loanExternalId, loanExternalIdDisburseLoanResponse);
        Assertions.assertNotNull(transactionExternalId);

        // Create a repayment reversal request by external id
        final BatchRequest repaymentReversalRequest = BatchHelper.createAdjustTransactionByExternalIdRequest(repayReversalRequestId, null,
                loanExternalIdDisburseLoanResponse, transactionExternalId, "0", date);

        final BatchRequest getLoanByIdWithTransactions = BatchHelper.getLoanByIdRequest(loanId, getLoanAfterReversal,
                repayReversalRequestId, "associations=transactions");

        final List<BatchRequest> reversalAndGetBatchRequest = Arrays.asList(repaymentReversalRequest, getLoanByIdWithTransactions);

        final List<BatchResponse> reversalResponses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec,
                this.responseSpec, BatchHelper.toJsonString(reversalAndGetBatchRequest));

        final JsonObject repayment = jsonHelper.parse(reversalResponses.get(1).getBody()).getAsJsonObject().get("transactions")
                .getAsJsonArray().get(2).getAsJsonObject();

        final JsonArray dateArray = repayment.get("reversedOnDate").getAsJsonArray();
        final LocalDate reversedOnDate = LocalDate.of(dateArray.get(0).getAsInt(), dateArray.get(1).getAsInt(),
                dateArray.get(2).getAsInt());

        Assertions.assertEquals(HttpStatus.SC_OK, (long) reversalResponses.get(0).getStatusCode(),
                "Verify Status Code 200 for repayment reversal");
        Assertions.assertEquals("Repayment", repayment.get("type").getAsJsonObject().get("value").getAsString());

        Assertions.assertTrue(repayment.get("manuallyReversed").getAsBoolean());
        Assertions.assertEquals(date, reversedOnDate);
    }

    /**
     * Test for the successful repayment chargeback transaction. A '200' status code is expected on successful
     * responses.
     *
     * @see AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchRepaymentChargeback() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long repayLoanRequestId = 5733L;
        final Long repayReversalRequestId = 5734L;
        final Long getLoanRequestId = 5735L;

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an apply loan request
        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approve loan request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburse loan request
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a repayment request
        final BatchRequest repaymentRequest = BatchHelper.repayLoanRequest(repayLoanRequestId, disburseLoanRequestId, "500");

        // Create a repayment chargeback request
        final BatchRequest repaymentChargebackRequest = BatchHelper.createChargebackTransactionRequest(repayReversalRequestId,
                repayLoanRequestId, "500");

        // Get loan transactions request
        final BatchRequest getLoanTransactionsRequest = BatchHelper.getLoanByIdRequest(getLoanRequestId, applyLoanRequestId,
                "associations=transactions");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest, repaymentRequest,
                repaymentChargebackRequest, getLoanTransactionsRequest);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonObject repayment = jsonHelper.parse(responses.get(5).getBody()).getAsJsonObject().get("transactions").getAsJsonArray()
                .get(2).getAsJsonObject();

        Assertions.assertEquals(HttpStatus.SC_OK, (long) responses.get(4).getStatusCode(),
                "Verify Status Code 200 for repayment chargeback");
        Assertions.assertEquals("Repayment", repayment.get("type").getAsJsonObject().get("value").getAsString());
        final JsonArray transactionRelations = repayment.get("transactionRelations").getAsJsonArray();
        Assertions.assertEquals(1, transactionRelations.size());
        Assertions.assertEquals("CHARGEBACK", transactionRelations.get(0).getAsJsonObject().get("relationType").getAsString());
    }

    /**
     * Tests successful run of batch goodwill credit reversal for loans. A '200' status code is expected on successful
     * responses.
     *
     * @see AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchGoodwillCreditReversal() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        final LocalDate date = LocalDate.now(Utils.getZoneIdOfTenant());
        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long goodwillCreditRequestId = 5733L;
        final Long goodwillCreditReversalRequestId = 5734L;
        final Long getLoanRequestId = 5735L;

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an apply loan request
        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approve loan request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburse loan request
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a good will credit request
        final BatchRequest goodwillCreditRequest = BatchHelper.goodwillCreditRequest(goodwillCreditRequestId, disburseLoanRequestId, "500");

        // Create a good will credit reversal request
        final BatchRequest goodwillCreditReversalRequest = BatchHelper.createAdjustTransactionRequest(goodwillCreditReversalRequestId,
                goodwillCreditRequestId, "0", date);

        // Get loan transactions request
        final BatchRequest getLoanTransactionsRequest = BatchHelper.getLoanByIdRequest(getLoanRequestId, applyLoanRequestId,
                "associations=transactions");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                goodwillCreditRequest, goodwillCreditReversalRequest, getLoanTransactionsRequest);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonObject goodWillCredit = jsonHelper.parse(responses.get(5).getBody()).getAsJsonObject().get("transactions")
                .getAsJsonArray().get(2).getAsJsonObject();
        final JsonArray dateArray = goodWillCredit.get("reversedOnDate").getAsJsonArray();
        final LocalDate reversedOnDate = LocalDate.of(dateArray.get(0).getAsInt(), dateArray.get(1).getAsInt(),
                dateArray.get(2).getAsInt());

        Assertions.assertEquals(HttpStatus.SC_OK, (long) responses.get(4).getStatusCode(),
                "Verify Status Code 200 for goodwill credit reversal");
        Assertions.assertEquals("Goodwill Credit", goodWillCredit.get("type").getAsJsonObject().get("value").getAsString());
        Assertions.assertTrue(goodWillCredit.get("manuallyReversed").getAsBoolean());
        Assertions.assertEquals(date, reversedOnDate);
    }

    /**
     * Test for the successful merchant issued refund and payout refund reversal transaction. A '200' status code is
     * expected on successful responses.
     *
     * @see AdjustLoanTransactionCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulTransactionMerchantIssuedAndPayoutRefundReversal() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final LocalDate date = LocalDate.now(Utils.getZoneIdOfTenant());
        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5732L;
        final Long merchantIssuedRefundRequestId = 5733L;
        final Long payoutRefundRequestId = 5734L;
        final Long merchantIssuedRefundReversalRequestId = 5735L;
        final Long payoutRefundReversalRequestId = 5736L;
        final Long getLoanRequestId = 5737L;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an apply loan request
        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approve loan request
        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburse loan request
        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId,
                date.minusDays(1));

        // Create a merchant issued refund request
        final BatchRequest merchantIssuedRefundRequest = BatchHelper.merchantIssuedRefundRequest(merchantIssuedRefundRequestId,
                applyLoanRequestId, "10");

        // Create a payout refund request
        final BatchRequest payoutRefundRequest = BatchHelper.payoutRefundRequest(payoutRefundRequestId, applyLoanRequestId, "10");

        // Create a merchant issued refund reversal request
        final BatchRequest merchantIssuedRefundReversalRequest = BatchHelper
                .createAdjustTransactionRequest(merchantIssuedRefundReversalRequestId, merchantIssuedRefundRequestId, "0", date);

        // Create a payout refund reversal request
        final BatchRequest payoutRefundReversalRequest = BatchHelper.createAdjustTransactionRequest(payoutRefundReversalRequestId,
                payoutRefundRequestId, "0", date);

        // Get loan transactions request
        final BatchRequest getLoanTransactionsRequest = BatchHelper.getLoanByIdRequest(getLoanRequestId, applyLoanRequestId,
                "associations=transactions");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                merchantIssuedRefundRequest, payoutRefundRequest, merchantIssuedRefundReversalRequest, payoutRefundReversalRequest,
                getLoanTransactionsRequest);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonObject merchantIssuedRefund = jsonHelper.parse(responses.get(7).getBody()).getAsJsonObject().get("transactions")
                .getAsJsonArray().get(2).getAsJsonObject();
        final JsonObject payoutRefund = jsonHelper.parse(responses.get(7).getBody()).getAsJsonObject().get("transactions").getAsJsonArray()
                .get(3).getAsJsonObject();
        final JsonArray merchantIssuedDateArray = merchantIssuedRefund.get("reversedOnDate").getAsJsonArray();
        final LocalDate merchantIssuedDate = LocalDate.of(merchantIssuedDateArray.get(0).getAsInt(),
                merchantIssuedDateArray.get(1).getAsInt(), merchantIssuedDateArray.get(2).getAsInt());
        final JsonArray payoutRefundDateArray = payoutRefund.getAsJsonObject().get("reversedOnDate").getAsJsonArray();
        final LocalDate payoutRefundDate = LocalDate.of(payoutRefundDateArray.get(0).getAsInt(), payoutRefundDateArray.get(1).getAsInt(),
                payoutRefundDateArray.get(2).getAsInt());

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(5).getStatusCode(),
                "Verify Status Code 200 for merchant issued refund reversal");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(6).getStatusCode(), "Verify Status Code 200 for payout refund reversal");
        Assertions.assertEquals("Merchant Issued Refund", merchantIssuedRefund.get("type").getAsJsonObject().get("value").getAsString());
        Assertions.assertEquals("Payout Refund", payoutRefund.get("type").getAsJsonObject().get("value").getAsString());
        Assertions.assertTrue(merchantIssuedRefund.get("manuallyReversed").getAsBoolean());
        Assertions.assertTrue(payoutRefund.get("manuallyReversed").getAsBoolean());
        Assertions.assertEquals(date, merchantIssuedDate);
        Assertions.assertEquals(date, payoutRefundDate);
    }

    @Test
    public void shouldReturnOkStatusOnModifyingSavingAccount() {
        final String startDate = "10 April 2022";
        final SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final String savingsProductJSON = savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        final Integer savingsProductID = SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID, "INDIVIDUAL",
                startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final BatchRequest getSavingAccountRequest = BatchHelper.getSavingAccount(1L, Long.valueOf(savingsId), "chargeStatus=all", null);
        final BatchRequest depositSavingAccountRequest = BatchHelper.depositSavingAccount(2L, 1L, 100F);
        final BatchRequest holdAmountOnSavingAccountRequest = BatchHelper.holdAmountOnSavingAccount(3L, 1L, 10F);

        final List<BatchRequest> batchRequests1 = Arrays.asList(getSavingAccountRequest, depositSavingAccountRequest,
                holdAmountOnSavingAccountRequest);
        final List<BatchResponse> responses1 = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests1));

        Assertions.assertEquals(HttpStatus.SC_OK, responses1.get(1).getStatusCode(), "Verify Status Code 200 for deposit saving account");
        Assertions.assertEquals(HttpStatus.SC_OK, responses1.get(2).getStatusCode(),
                "Verify Status Code 200 for hold amount on saving account");
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long holdAmountTransactionId = jsonHelper.parse(responses1.get(2).getBody()).getAsJsonObject().get("resourceId").getAsLong();

        final BatchRequest releaseAmountOnSavingAccountRequest = BatchHelper.releaseAmountOnSavingAccount(2L, 1L, holdAmountTransactionId);
        final BatchRequest withdrawSavingAccountRequest = BatchHelper.withdrawSavingAccount(3L, 1L, 80F);

        final List<BatchRequest> batchRequests2 = Arrays.asList(getSavingAccountRequest, releaseAmountOnSavingAccountRequest,
                withdrawSavingAccountRequest);
        final List<BatchResponse> responses2 = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests2));

        Assertions.assertEquals(HttpStatus.SC_OK, responses2.get(1).getStatusCode(),
                "Verify Status Code 200 for release amount on saving account");
        Assertions.assertEquals(HttpStatus.SC_OK, responses2.get(2).getStatusCode(), "Verify Status Code 200 for withdraw saving account");
    }

    /**
     * Test for finding datatable entry by the query API and update its value
     */
    @Test
    public void shouldFindOneToOneDatatableEntryByQueryAPIAndUpdateOneOfItsColumn() {
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5).toLowerCase();

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(setupAccount()).getAsJsonObject());
        // creating datatable with m_loan association
        final Map<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();

        final String columnName1 = Utils.randomStringGenerator("COL1_", 5).toLowerCase();
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5).toLowerCase();
        columnMap.put("datatableName", datatableName);
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", false);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName1, "String", true, 10, null);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName2, "String", false, 10, null);
        columnMap.put("columns", datatableColumnsList);
        final String datatableRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("CreateDataTable map : {}", datatableRequestJsonString);

        this.datatableHelper.createDatatable(datatableRequestJsonString, "");

        // Create a datatable entry so that it can be updated using BatchApi
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        String columnValue1 = Utils.randomStringGenerator("VAL1_", 3);
        String columnValue2 = Utils.randomStringGenerator("VAL2_", 3);
        datatableEntryMap.put(columnName1, columnValue1);
        datatableEntryMap.put(columnName2, columnValue2);
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("CreateDataTableEntry map : {}", datatableEntryRequestJsonString);

        final Map<String, Object> datatableEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, loanId.intValue(),
                false, datatableEntryRequestJsonString);
        final Integer datatableEntryResourceId = (Integer) datatableEntryResponse.get("resourceId");
        assertNotNull(datatableEntryResourceId, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        final BatchRequest queryDatatableEntriesRequest = BatchHelper.queryDatatableEntries(datatableName, columnName1, columnValue1,
                "loan_id");
        final BatchRequest updateDatatableEntry = BatchHelper.updateDatatableEntry(datatableName, "$.[0].loan_id", columnName2,
                columnValue2 + "1");

        final List<BatchRequest> batchRequestsToQueryAndUpdateDatatableEntries = Arrays.asList(queryDatatableEntriesRequest,
                updateDatatableEntry);
        LOG.info("Batch Request : {}", BatchHelper.toJsonString(batchRequestsToQueryAndUpdateDatatableEntries));

        final List<BatchResponse> responseOfQuertAndUpdateDatatableBatch = BatchHelper.postBatchRequestsWithEnclosingTransaction(
                this.requestSpec, this.responseSpec, BatchHelper.toJsonString(batchRequestsToQueryAndUpdateDatatableEntries));

        LOG.info("Batch Response : {}", new Gson().toJson(responseOfQuertAndUpdateDatatableBatch));

        final BatchResponse batchQueryAndUpdateResponse1 = responseOfQuertAndUpdateDatatableBatch.get(0);
        final BatchResponse batchQueryAndUpdateResponse2 = responseOfQuertAndUpdateDatatableBatch.get(1);
        Assertions.assertEquals(HttpStatus.SC_OK, batchQueryAndUpdateResponse1.getStatusCode(),
                "Verify Status Code 200 for create datatable entry");
        Assertions.assertEquals(HttpStatus.SC_OK, batchQueryAndUpdateResponse2.getStatusCode(),
                "Verify Status Code 200 for update datatable entry");

        final JsonObject changes = jsonHelper.parse(batchQueryAndUpdateResponse2.getBody()).getAsJsonObject().get("changes")
                .getAsJsonObject();
        Assertions.assertEquals(changes.get(columnName2).getAsString(), columnValue2 + "1");
    }

    /**
     * Test when datatable entry was not found by the query API, and the update fails
     */
    @Test
    public void shouldNotFindAnyDatatableEntryByQueryAPIAndFailsToUpdateItsColumn() {
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5).toLowerCase();

        // creating datatable with m_loan association
        final Map<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();

        final String columnName1 = Utils.randomStringGenerator("COL1_", 5).toLowerCase();
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5).toLowerCase();
        columnMap.put("datatableName", datatableName);
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", false);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName1, "String", true, 15, null);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName2, "String", false, 15, null);
        columnMap.put("columns", datatableColumnsList);
        final String datatableRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("CreateDataTable map : {}", datatableRequestJsonString);

        this.datatableHelper.createDatatable(datatableRequestJsonString, "");

        final BatchRequest queryDatatableEntriesRequest = BatchHelper.queryDatatableEntries(datatableName, columnName1, "columnValue1",
                "loan_id");
        final BatchRequest updateDatatableEntry = BatchHelper.updateDatatableEntry(datatableName, "$.[0].loan_id", columnName2,
                "columnValue2");

        final List<BatchRequest> batchRequestsToQueryAndUpdateDatatableEntries = Arrays.asList(queryDatatableEntriesRequest,
                updateDatatableEntry);
        LOG.info("Batch Request : {}", BatchHelper.toJsonString(batchRequestsToQueryAndUpdateDatatableEntries));

        final List<BatchResponse> responseOfQueryAndUpdateDatatableBatch = BatchHelper.postBatchRequestsWithEnclosingTransaction(
                this.requestSpec, this.responseSpec, BatchHelper.toJsonString(batchRequestsToQueryAndUpdateDatatableEntries));

        LOG.info("Batch Response : {}", new Gson().toJson(responseOfQueryAndUpdateDatatableBatch));

        final BatchResponse updateResponse = responseOfQueryAndUpdateDatatableBatch.get(0);

        Assertions.assertEquals(2L, updateResponse.getRequestId());
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, updateResponse.getStatusCode(),
                "Verify Status Code 400 for update datatable entry");
        MatcherAssert.assertThat(updateResponse.getBody(), containsString("The referenced JSON path is invalid"));
    }

    /**
     * Test for finding datatable entry by the query API and update its value
     */
    @Test
    public void shouldFindOneToManyDatatableEntryByQueryAPIAndUpdateOneOfItsColumn() {
        final String datatableName = Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5).toLowerCase();

        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long loanId = jsonHelper.extractLongNamed("loanId", jsonHelper.parse(setupAccount()).getAsJsonObject());
        // creating datatable with m_loan association
        final Map<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();

        final String columnName1 = Utils.randomStringGenerator("COL1_", 5).toLowerCase();
        final String columnName2 = Utils.randomStringGenerator("COL2_", 5).toLowerCase();
        columnMap.put("datatableName", datatableName);
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", true);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName1, "String", true, 10, null);
        DatatableHelper.addDatatableColumn(datatableColumnsList, columnName2, "String", false, 10, null);
        columnMap.put("columns", datatableColumnsList);
        final String datatableRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("CreateDataTable map : {}", datatableRequestJsonString);

        this.datatableHelper.createDatatable(datatableRequestJsonString, "");

        // Create a datatable entry so that it can be updated using BatchApi
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        String columnValue1 = Utils.randomStringGenerator("VAL1_", 3);
        String columnValue2 = Utils.randomStringGenerator("VAL2_", 3);
        datatableEntryMap.put(columnName1, columnValue1);
        datatableEntryMap.put(columnName2, columnValue2);
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("CreateDataTableEntry map : {}", datatableEntryRequestJsonString);

        final Map<String, Object> datatableEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, loanId.intValue(),
                false, datatableEntryRequestJsonString);
        final Integer datatableEntryResourceId = (Integer) datatableEntryResponse.get("resourceId");
        assertNotNull(datatableEntryResourceId, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Create datatable entry batch request
        final BatchRequest createDatatableEntryRequest = BatchHelper.createDatatableEntryRequest(loanId.toString(), datatableName,
                Arrays.asList(columnName1, columnName2));

        // Get datatable entries batch request
        final BatchRequest getDatatableEntriesRequest = BatchHelper.getDatatableByIdRequest(loanId, datatableName, null,
                createDatatableEntryRequest.getReference());

        final List<BatchRequest> batchRequestsDatatableEntries = Arrays.asList(createDatatableEntryRequest, getDatatableEntriesRequest);
        LOG.info("Batch Request : {}", BatchHelper.toJsonString(batchRequestsDatatableEntries));

        final List<BatchResponse> responseDatatableBatch = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec,
                this.responseSpec, BatchHelper.toJsonString(batchRequestsDatatableEntries));

        LOG.info("Batch Response : {}", new Gson().toJson(responseDatatableBatch));

        final BatchResponse batchResponse1 = responseDatatableBatch.get(0);
        final BatchResponse batchResponse2 = responseDatatableBatch.get(1);
        Assertions.assertEquals(HttpStatus.SC_OK, batchResponse1.getStatusCode(), "Verify Status Code 200 for create datatable entry");
        Assertions.assertEquals(HttpStatus.SC_OK, batchResponse2.getStatusCode(), "Verify Status Code 200 for update datatable entry");

        final String getDatatableEntriesResponse = batchResponse2.getBody();

        final Long createDatatableEntryId = jsonHelper.extractLongNamed("resourceId",
                jsonHelper.parse(batchResponse1.getBody()).getAsJsonObject());

        final JsonArray datatableEntries = jsonHelper.parse(getDatatableEntriesResponse).getAsJsonArray();
        Assertions.assertEquals(2, datatableEntries.size());

        // Ensure both resourceIds are available in response
        Assertions.assertTrue(getDatatableEntriesResponse.contains(String.format("\"id\": %d", createDatatableEntryId)));
        Assertions.assertTrue(getDatatableEntriesResponse.contains(String.format("\"id\": %d", datatableEntryResourceId)));

        final BatchRequest queryDatatableEntriesRequest = BatchHelper.queryDatatableEntries(datatableName, columnName1, columnValue1,
                "id,loan_id");
        final BatchRequest updateDatatableEntry = BatchHelper.updateDatatableEntry(datatableName, "$.[0].loan_id", "$.[0].id", columnName2,
                columnValue2 + "1");

        final List<BatchRequest> batchRequestsToQueryAndUpdateDatatableEntries = Arrays.asList(queryDatatableEntriesRequest,
                updateDatatableEntry);
        LOG.info("Batch Request : {}", BatchHelper.toJsonString(batchRequestsToQueryAndUpdateDatatableEntries));

        final List<BatchResponse> responseOfQuertAndUpdateDatatableBatch = BatchHelper.postBatchRequestsWithEnclosingTransaction(
                this.requestSpec, this.responseSpec, BatchHelper.toJsonString(batchRequestsToQueryAndUpdateDatatableEntries));

        LOG.info("Batch Response : {}", new Gson().toJson(responseOfQuertAndUpdateDatatableBatch));

        final BatchResponse batchQueryAndUpdateResponse1 = responseOfQuertAndUpdateDatatableBatch.get(0);
        final BatchResponse batchQueryAndUpdateResponse2 = responseOfQuertAndUpdateDatatableBatch.get(1);
        Assertions.assertEquals(HttpStatus.SC_OK, batchQueryAndUpdateResponse1.getStatusCode(),
                "Verify Status Code 200 for create datatable entry");
        Assertions.assertEquals(HttpStatus.SC_OK, batchQueryAndUpdateResponse2.getStatusCode(),
                "Verify Status Code 200 for update datatable entry");

        final JsonObject changes = jsonHelper.parse(batchQueryAndUpdateResponse2.getBody()).getAsJsonObject().get("changes")
                .getAsJsonObject();
        Assertions.assertEquals(changes.get(columnName2).getAsString(), columnValue2 + "1");
    }

    /**
     * Tests that a loan information was successfully updated through updateLoanCommand. A 'changes' parameter is
     * returned as part of response after successful update of loan information. In this test, we are marking an active
     * loan account as fraud.
     *
     * @see org.apache.fineract.batch.command.internal.ModifyLoanApplicationCommandStrategy
     */
    @Test
    public void shouldReflectChangesOnLoanUpdate() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = RandomUtils.nextLong(100, 1000);
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long updateLoanRequestId = disburseLoanRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        final BatchRequest disburseLoanRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        final BatchRequest updateLoanRequest = BatchHelper.createLoanRequestMarkAsFraud(updateLoanRequestId, disburseLoanRequestId);

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest,
                updateLoanRequest);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Get the changes parameter from updateLoan Response
        final JsonObject changes = new FromJsonHelper().parse(response.get(3).getBody()).getAsJsonObject().get("changes").getAsJsonObject();

        Assertions.assertEquals(HttpStatus.SC_OK, response.get(3).getStatusCode(), "Verify Status Code 200 for update loan application");
        Assertions.assertEquals("true", changes.get("fraud").getAsString());
    }

    /**
     * Tests that a loan information was successfully updated through updateLoanCommand using external id. A 'changes'
     * parameter is returned as part of response after successful update of loan information. In this test, we are
     * marking an active loan account as fraud.
     *
     * @see org.apache.fineract.batch.command.internal.ModifyLoanApplicationByExternalIdCommandStrategy
     */
    @Test
    public void shouldReflectChangesOnLoanUpdateByExternalId() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = RandomUtils.nextLong(100, 1000);
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long updateLoanRequestId = disburseLoanRequestId + 1;
        final Long getLoanRequestId = updateLoanRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.transistionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10), "approve");

        final BatchRequest disburseLoanRequest = BatchHelper.transistionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(8), "disburse");

        final BatchRequest updateLoanRequest = BatchHelper.modifyLoanByExternalIdRequest(updateLoanRequestId, approveLoanRequestId);

        final BatchRequest getLoanRequest = BatchHelper.getLoanByExternalIdRequest(getLoanRequestId, approveLoanRequestId,
                "associations=all");

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest, updateLoanRequest,
                getLoanRequest);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for update loan application");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(4).getStatusCode(), "Verify Status Code 200 for Get Loan");

        // Get the changes parameter from updateLoan Response
        final JsonObject changes = new FromJsonHelper().parse(responses.get(3).getBody()).getAsJsonObject().get("changes")
                .getAsJsonObject();
        Assertions.assertEquals("true", changes.get("fraud").getAsString());
    }

    /**
     * Tests that a loan is hard locked and if a repayment triggered in as a batch request, it returns the proper error
     *
     * @see org.apache.fineract.batch.command.internal.ModifyLoanApplicationByExternalIdCommandStrategy
     */
    @Test
    public void shoulRetrieveTheProperErrorDuringLockedLoan_OldRelativePath() {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        LoanAccountLockHelper loanAccountLockHelper = new LoanAccountLockHelper(this.requestSpec, responseSpec);
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = RandomUtils.nextLong(100, 1000);
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long getLoanRequestId = disburseLoanRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.transistionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10), "approve");

        final BatchRequest disburseLoanRequest = BatchHelper.transistionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(8), "disburse");

        final BatchRequest getLoanRequest = BatchHelper.getLoanByExternalIdRequest(getLoanRequestId, approveLoanRequestId,
                "associations=all");

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest, getLoanRequest);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan");

        final Long loanId = new FromJsonHelper().parse(responses.get(2).getBody()).getAsJsonObject().get("resourceId").getAsLong();

        loanAccountLockHelper.placeSoftLockOnLoanAccount(loanId.intValue(), "LOAN_COB_CHUNK_PROCESSING");

        RequestSpecification requestSpec = UserHelper.getSimpleUserWithoutBypassPermission(this.requestSpec, this.responseSpec);

        // Create a repayment Request
        final BatchRequest br = new BatchRequest();

        br.setRequestId(1L);
        br.setRelativeUrl(String.format("loans/" + loanId + "/transactions?command=repayment"));
        br.setMethod("POST");
        String dateString = LocalDate.now(Utils.getZoneIdOfTenant()).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": \"500\"}",
                dateString));

        final String jsonifiedRepaymentRequest = BatchHelper.toJsonString(List.of(br));

        // verify HTTP 409
        ResponseSpecification conflictResponseSpec = new ResponseSpecBuilder().expectStatusCode(409).build();
        ErrorResponse errorResponse = BatchHelper.postBatchRequestsWithoutEnclosingTransactionError(requestSpec, conflictResponseSpec,
                jsonifiedRepaymentRequest);
        assertEquals(409, parseInt(errorResponse.getHttpStatusCode()));
    }

    /**
     * Tests that a loan is hard locked and if a repayment triggered in as a batch request, it returns the proper error
     *
     * @see org.apache.fineract.batch.command.internal.ModifyLoanApplicationByExternalIdCommandStrategy
     */
    @Test
    public void shoulRetrieveTheProperErrorDuringLockedLoan() {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        LoanAccountLockHelper loanAccountLockHelper = new LoanAccountLockHelper(this.requestSpec, responseSpec);
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = RandomUtils.nextLong(100, 1000);
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long getLoanRequestId = disburseLoanRequestId + 1;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        final BatchRequest applyLoanRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        final BatchRequest approveLoanRequest = BatchHelper.transistionLoanStateByExternalId(approveLoanRequestId, applyLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10), "approve");

        final BatchRequest disburseLoanRequest = BatchHelper.transistionLoanStateByExternalId(disburseLoanRequestId, approveLoanRequestId,
                LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(8), "disburse");

        final BatchRequest getLoanRequest = BatchHelper.getLoanByExternalIdRequest(getLoanRequestId, approveLoanRequestId,
                "associations=all");

        // Create batch requests list
        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanRequest, approveLoanRequest, disburseLoanRequest, getLoanRequest);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Get Loan");

        final Long loanId = new FromJsonHelper().parse(responses.get(2).getBody()).getAsJsonObject().get("resourceId").getAsLong();

        loanAccountLockHelper.placeSoftLockOnLoanAccount(loanId.intValue(), "LOAN_COB_CHUNK_PROCESSING");

        RequestSpecification requestSpec = UserHelper.getSimpleUserWithoutBypassPermission(this.requestSpec, this.responseSpec);

        // Create a repayment Request
        final BatchRequest br = new BatchRequest();

        br.setRequestId(1L);
        br.setRelativeUrl(String.format("v1/loans/" + loanId + "/transactions?command=repayment"));
        br.setMethod("POST");
        String dateString = LocalDate.now(Utils.getZoneIdOfTenant()).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": \"500\"}",
                dateString));

        final String jsonifiedRepaymentRequest = BatchHelper.toJsonString(List.of(br));

        // verify HTTP 409
        ResponseSpecification conflictResponseSpec = new ResponseSpecBuilder().expectStatusCode(409).build();
        ErrorResponse errorResponse = BatchHelper.postBatchRequestsWithoutEnclosingTransactionError(requestSpec, conflictResponseSpec,
                jsonifiedRepaymentRequest);
        assertEquals(409, parseInt(errorResponse.getHttpStatusCode()));
    }

    @Test
    public void verifyCalculatingRunningBalanceAfterBatchWithReleaseAmount() {
        final SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);
        final String savingsProductJSON = savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        final Integer savingsProductID = SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final float holdAmount = 10F;
        final float withdrawalAmount = 80F;
        final BatchRequest getSavingAccountRequest = BatchHelper.getSavingAccount(1L, Long.valueOf(savingsId), "chargeStatus=all", null);
        final BatchRequest depositSavingAccountRequest = BatchHelper.depositSavingAccount(2L, 1L, 300F);
        final BatchRequest holdAmountOnSavingAccountRequest = BatchHelper.holdAmountOnSavingAccount(3L, 1L, holdAmount);

        final List<BatchRequest> batchRequests1 = Arrays.asList(getSavingAccountRequest, depositSavingAccountRequest,
                holdAmountOnSavingAccountRequest);
        final List<BatchResponse> responses1 = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests1));

        Assertions.assertEquals(HttpStatus.SC_OK, responses1.get(1).getStatusCode(), "Verify Status Code 200 for deposit saving account");
        Assertions.assertEquals(HttpStatus.SC_OK, responses1.get(2).getStatusCode(),
                "Verify Status Code 200 for hold amount on saving account");
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final Long holdAmountTransactionId = jsonHelper.parse(responses1.get(2).getBody()).getAsJsonObject().get("resourceId").getAsLong();

        HashMap accountDetails = savingsAccountHelper.getSavingsDetails(savingsId);
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) accountDetails.get("transactions");
        final float runningBalanceBeforeBatch = (float) transactions.get(0).get("runningBalance");

        final BatchRequest releaseAmountOnSavingAccountRequest = BatchHelper.releaseAmountOnSavingAccount(2L, 1L, holdAmountTransactionId);
        final BatchRequest withdrawSavingAccountRequest1 = BatchHelper.withdrawSavingAccount(3L, 1L, withdrawalAmount);
        final BatchRequest withdrawSavingAccountRequest2 = BatchHelper.withdrawSavingAccount(4L, 1L, withdrawalAmount);

        final List<BatchRequest> batchRequests2 = Arrays.asList(getSavingAccountRequest, releaseAmountOnSavingAccountRequest,
                withdrawSavingAccountRequest1, withdrawSavingAccountRequest2);
        final List<BatchResponse> responses2 = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests2));

        Assertions.assertEquals(HttpStatus.SC_OK, responses2.get(0).getStatusCode(),
                "Verify Status Code 200 for release amount on saving account");
        Assertions.assertEquals(HttpStatus.SC_OK, responses2.get(1).getStatusCode(), "Verify Status Code 200 for withdraw saving account");
        Assertions.assertEquals(HttpStatus.SC_OK, responses2.get(2).getStatusCode(), "Verify Status Code 200 for withdraw saving account");

        accountDetails = savingsAccountHelper.getSavingsDetails(savingsId);
        transactions = (ArrayList<HashMap<String, Object>>) accountDetails.get("transactions");

        final HashMap<String, Object> transactionRelease = transactions.get(2);
        final HashMap<String, Object> transactionWithdrawal1 = transactions.get(1);
        final HashMap<String, Object> transactionWithdrawal2 = transactions.get(0);

        assertEquals(runningBalanceBeforeBatch + holdAmount, transactionRelease.get("runningBalance"),
                "Verify running balance after release amount");
        assertEquals(runningBalanceBeforeBatch + holdAmount - withdrawalAmount, transactionWithdrawal1.get("runningBalance"),
                "Verify running balance after first withdrawal");
        assertEquals(runningBalanceBeforeBatch + holdAmount - withdrawalAmount - withdrawalAmount,
                transactionWithdrawal2.get("runningBalance"), "Verify running balance after second withdrawal");
    }

    /**
     * Delete datatable
     *
     * @param datatableName
     *            the datatable name
     */
    private void deleteDatatable(final String datatableName) {
        String deletedDatatableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDatatableName, "Fail to delete the datatable");
    }

    /**
     * Setup account to test get loan and get datatable batch call
     *
     * @return the response body
     */
    private String setupAccount() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Long applyLoanRequestId = 5730L;
        final Long approveLoanRequestId = 5731L;
        final Long disburseLoanRequestId = 5734L;
        final Long repayLoanRequestId = 5735L;

        // Create product
        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        // Create an ApplyLoan Request
        final BatchRequest applyLoanBatchRequest = BatchHelper.applyLoanRequestWithClientId(applyLoanRequestId, clientId, productId);

        // Create an approveLoan Request
        final BatchRequest approveLoanBatchRequest = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburseLoan Request
        final BatchRequest disburseLoanBatchRequest = BatchHelper.disburseLoanRequest(disburseLoanRequestId, applyLoanRequestId);

        // Create a repayment Request
        final BatchRequest repaymentBatchRequest = BatchHelper.repayLoanRequest(repayLoanRequestId, applyLoanRequestId, "500");

        final List<BatchRequest> batchRequests = Arrays.asList(applyLoanBatchRequest, approveLoanBatchRequest, disburseLoanBatchRequest,
                repaymentBatchRequest);

        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                BatchHelper.toJsonString(batchRequests));

        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Verify Status Code 200 for Apply Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(), "Verify Status Code 200 for Approve Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(2).getStatusCode(), "Verify Status Code 200 for Disburse Loan");
        Assertions.assertEquals(HttpStatus.SC_OK, responses.get(3).getStatusCode(), "Verify Status Code 200 for Repay Loan");

        return responses.get(0).getBody();
    }
}
