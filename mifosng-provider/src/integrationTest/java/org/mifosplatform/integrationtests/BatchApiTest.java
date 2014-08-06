/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.integrationtests.common.BatchHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsProductHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Test class for
 * {@link org.mifosplatform.batch.command.CommandStrategyProvider}. This tests
 * the response provided by commandStrategy by injecting it with a
 * {@code BatchRequest}.
 * 
 * @author RishabhShukla
 * 
 * @see org.mifosplatform.integrationtests.common.BatchHelper
 * @see org.mifosplatform.batch.domain.BatchRequest
 */
public class BatchApiTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    public BatchApiTest() {
        super();
    }

    /**
     * Sets up the essential settings for the TEST like contentType,
     * expectedStatusCode. It uses the '@Before' annotation provided by jUnit.
     */
    @Before
    public void setup() {

        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    /**
     * Tests for the unimplemented command Strategies by returning 501 status
     * code. For a unknownRequest a statusCode 501 is returned back with
     * response.
     * 
     * @see org.mifosplatform.batch.command.internal.UnknownCommandStrategy
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
            Assert.assertEquals("Verify Status code 501", (long) 501, (long) resp.getStatusCode());
        }
    }

    /**
     * Tests for the successful response for a createClient request from
     * createClientCommand. A successful response with statusCode '200' is
     * returned back.
     * 
     * @see org.mifosplatform.batch.command.internal.CreateClientCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForCreateClientCommand() {

        final BatchRequest br = BatchHelper.createClientRequest(4712L, "");

        final List<BatchResponse> response = BatchHelper.postWithSingleRequest(this.requestSpec, this.responseSpec, br);

        // Verify that a 200 response is returned as the status code
        for (BatchResponse resp : response) {
            Assert.assertEquals("Verify Status code 200", (long) 200, (long) resp.getStatusCode());
        }
    }

    /**
     * Tests for an erroneous response with statusCode '501' if transaction
     * fails. If Query Parameter 'enclosingTransaction' is set to 'true' and if
     * one of the request in BatchRequest fails then all transactions are rolled
     * back.
     * 
     * @see org.mifosplatform.batch.command.internal.CreateClientCommandStrategy
     * @see org.mifosplatform.batch.api.BatchApiResource
     * @see org.mifosplatform.batch.service.BatchApiService
     */
    @Test
    public void shouldRollBackAllTransactionsOnFailure() {

        // Create first client request
        final BatchRequest br1 = BatchHelper.createClientRequest(4713L, "TestExtId11");

        // Create second client request
        final BatchRequest br2 = BatchHelper.createClientRequest(4714L, "TestExtId12");

        // Create third client request, having same externalID as second client,
        // hence cause of error
        final BatchRequest br3 = BatchHelper.createClientRequest(4715L, "TestExtId11");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Verifies that none of the client in BatchRequest is created on the
        // server
        BatchHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, "TestExtId11");
        BatchHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, "TestExtId12");

        // Asserts that all the transactions have been successfully rolled back
        Assert.assertEquals(response.size(), 1);
        Assert.assertEquals("Verify Status code 400", (long) 400, (long) response.get(0).getStatusCode());
    }

    /**
     * Tests that a client information was successfully updated through
     * updateClientCommand. A 'changes' parameter is returned in the response
     * after successful update of client information.
     * 
     * @see org.mifosplatform.batch.command.internal.UpdateClientCommandStrategy
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
        Assert.assertEquals("Verify Firstname", "TestFirstName", changes.get("firstname").getAsString());
        Assert.assertEquals("Verify Lastname", "TestLastName", changes.get("lastname").getAsString());
    }

    /**
     * Tests that a ApplyLoanCommand was successfully executed and returned a
     * 200(OK) status. It creates a new client and apply a loan to that client.
     * This also verifies the successful resolution of dependencies among two
     * requests.
     * 
     * @see org.mifosplatform.batch.command.internal.ApplyLoanCommandStrategy
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

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4718L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4719L, 4718L);

        // Create a ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4720L, 4719L, productId);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        // Get the clientId parameter from createClient Response
        final JsonElement clientId = new FromJsonHelper().parse(response.get(0).getBody()).getAsJsonObject().get("clientId");

        Assert.assertEquals("Verify Status Code 200" + clientId.getAsString(), 200L, (long) response.get(1).getStatusCode());
    }

    /**
     * Tests that a new savings accounts was applied to an existing client and a
     * 200(OK) status was returned. It first creates a new client and a savings
     * product, then uses the cliendId and ProductId to apply a savings account.
     * 
     * @see org.mifosplatform.batch.command.internal.ApplySavingsCommandStrategy
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

        Assert.assertEquals("Verify Status Code 200", 200L, (long) response.get(1).getStatusCode());
    }

    /**
     * Tests that a new charge was added to a newly created loan and charges are
     * Collected properly 200(OK) status was returned for successful responses.
     * It first creates a new client and apply a loan, then creates a new charge
     * for the create loan and then fetches all the applied charges
     * 
     * @see org.mifosplatform.batch.command.internal.CollectChargesCommandStrategy
     * @see org.mifosplatform.batch.command.internal.CreateChargeCommandStrategy
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

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4722L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4723L, 4722L);

        // Create a ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4724L, 4723L, productId);

        // Create a Collect Charges Request
        final BatchRequest br4 = BatchHelper.collectChargesRequest(4725L, 4724L);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assert.assertEquals("Verify Status Code 200 for Create Loan Charge", 200L, (long) response.get(3).getStatusCode());
    }

    /**
     * Test for the successful activation of a pending client using
     * 'ActivateClientCommandStrategy'. A '200' status code is expected on
     * successful activation.
     * 
     * @see org.mifosplatform.batch.command.internal.ActivateClientCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusOnSuccessfulClientActivation() {

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4726L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4727L, 4726L);

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(this.requestSpec, this.responseSpec,
                jsonifiedRequest);

        Assert.assertEquals("Verify Status Code 200 for Create Client", 200L, (long) response.get(0).getStatusCode());
        Assert.assertEquals("Verify Status Code 200 for Activate Client", 200L, (long) response.get(1).getStatusCode());
    }

    /**
     * Test for the successful approval and disbursal of a loan using
     * 'ApproveLoanCommandStrategy' and 'DisburseLoanCommandStrategy'. A '200'
     * status code is expected on successful activation.
     * 
     * @see org.mifosplatform.batch.command.internal.ApproveLoanCommandStrategy
     * @see org.mifosplatform.batch.command.internal.DisburseLoanCommandStrategy
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

        final Integer productId = new LoanTransactionHelper(this.requestSpec, this.responseSpec).getLoanProductId(loanProductJSON);

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createClientRequest(4730L, "");

        // Create a activateClient Request
        final BatchRequest br2 = BatchHelper.activateClientRequest(4731L, 4730L);

        // Create a ApplyLoan Request
        final BatchRequest br3 = BatchHelper.applyLoanRequest(4732L, 4731L, productId);

        // Create a approveLoan Request
        final BatchRequest br4 = BatchHelper.approveLoanRequest(4733L, 4732L);

        // Create a disburseLoan Request
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

        Assert.assertEquals("Verify Status Code 200 for Approve Loan", 200L, (long) response.get(3).getStatusCode());
        Assert.assertEquals("Verify Status Code 200 for Disburse Loan", 200L, (long) response.get(4).getStatusCode());
    }
}