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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import jakarta.ws.rs.HttpMethod;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;
import org.apache.fineract.integrationtests.common.savings.SavingsTransactionData;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for {@link org.apache.fineract.integrationtests.BatchApiTest}. It takes care of creation of
 * {@code BatchRequest} list and posting this list to the server.
 *
 * @author Rishabh Shukla
 *
 * @see org.apache.fineract.integrationtests.BatchApiTest
 */
public final class BatchHelper {

    private static final Logger LOG = LoggerFactory.getLogger(BatchHelper.class);
    private static final String BATCH_API_URL = "/fineract-provider/api/v1/batches?" + Utils.TENANT_IDENTIFIER;
    private static final String BATCH_API_URL_EXT = BATCH_API_URL + "&enclosingTransaction=true";

    private static final String BATCH_API_WITHOUT_ENCLOSING_URL_EXT = BATCH_API_URL + "&enclosingTransaction=false";
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final Gson GSON = new JSON().getGson();

    private BatchHelper() {

    }

    /**
     * Returns a JSON String for a list of {@code BatchRequest}s
     *
     * @param batchRequests
     * @return JSON String of BatchRequest
     */
    public static String toJsonString(final List<BatchRequest> batchRequests) {
        return new Gson().toJson(batchRequests);
    }

    /**
     * Returns a Map from Json String
     *
     * @param
     * @return Map
     */
    public static Map generateMapFromJsonString(final String jsonString) {
        return new Gson().fromJson(jsonString, Map.class);
    }

    /**
     * Returns the converted string response into JSON.
     *
     * @param json
     * @return {@code List<BatchResponse>}
     */
    private static List<BatchResponse> fromJsonString(final String json) {
        return new Gson().fromJson(json, new TypeToken<List<BatchResponse>>() {}.getType());
    }

    /**
     * Returns a list of BatchResponse with query parameter enclosing transaction set to false by posting the jsonified
     * BatchRequest to the server.
     *
     * @param requestSpec
     * @param responseSpec
     * @param jsonifiedBatchRequests
     * @return a list of BatchResponse
     */
    public static List<BatchResponse> postBatchRequestsWithoutEnclosingTransaction(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String jsonifiedBatchRequests) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, BATCH_API_WITHOUT_ENCLOSING_URL_EXT,
                jsonifiedBatchRequests, null);
        LOG.info("BatchHelper Response {}", response);
        return BatchHelper.fromJsonString(response);
    }

    public static ErrorResponse postBatchRequestsWithoutEnclosingTransactionError(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String jsonifiedBatchRequests) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, BATCH_API_WITHOUT_ENCLOSING_URL_EXT,
                jsonifiedBatchRequests, null);
        LOG.info("BatchHelper Response {}", response);
        return GSON.fromJson(response, ErrorResponse.class);
    }

    /**
     * Returns a list of BatchResponse with query parameter enclosing transaction set to true by posting the jsonified
     * BatchRequest to the server.
     *
     * @param requestSpec
     * @param responseSpec
     * @param jsonifiedBatchRequests
     * @return a list of BatchResponse
     */
    public static List<BatchResponse> postBatchRequestsWithEnclosingTransaction(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String jsonifiedBatchRequests) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, BATCH_API_URL_EXT, jsonifiedBatchRequests, null);
        return BatchHelper.fromJsonString(response);
    }

    /**
     * Returns a BatchResponse based on the given BatchRequest, by posting the request to the server.
     *
     * @param
     * @return {@code List<BatchResponse>}
     */
    public static List<BatchResponse> postWithSingleRequest(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final BatchRequest br) {

        final List<BatchRequest> batchRequests = new ArrayList<>();
        batchRequests.add(br);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(requestSpec, responseSpec,
                jsonifiedRequest);

        // Verifies that the response result is there
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.size() > 0);

        return response;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.CreateClientCommandStrategy} Request as
     * one of the request in Batch.
     *
     * @param
     * @param externalId
     * @return BatchRequest
     */
    public static BatchRequest createClientRequest(final Long requestId, final String externalId) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl("v1/clients");
        br.setMethod("POST");

        final String extId;
        if (externalId.equals("")) {
            extId = "ext" + String.valueOf((10000 * secureRandom.nextDouble())) + String.valueOf((10000 * secureRandom.nextDouble()));
        } else {
            extId = externalId;
        }

        final String body = "{ \"officeId\": 1, \"legalFormId\":1, \"firstname\": \"Petra\", \"lastname\": \"Yton\"," + "\"externalId\": "
                + extId + ",  \"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en\","
                + "\"active\": false, \"submittedOnDate\": \"04 March 2009\"}";

        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.CreateClientCommandStrategy} Request as
     * one of the request in Batch.
     *
     * @param
     * @param externalId
     * @return BatchRequest
     */
    public static BatchRequest createActiveClientRequest(final Long requestId, final String externalId) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl("v1/clients");
        br.setMethod("POST");

        final String extId;
        if (externalId.equals("")) {
            extId = "ext" + String.valueOf((10000 * secureRandom.nextDouble())) + String.valueOf((10000 * secureRandom.nextDouble()));
        } else {
            extId = externalId;
        }

        final String body = "{ \"officeId\": 1, \"legalFormId\":1, \"firstname\": \"Petra\", \"lastname\": \"Yton\"," + "\"externalId\": \""
                + externalId + "\",  \"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en\","
                + "\"active\": true, \"activationDate\": \"04 March 2010\", \"submittedOnDate\": \"04 March 2010\"}";

        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.UpdateClientCommandStrategy} Request with
     * given requestId and reference.
     *
     * @param
     * @param
     * @return BatchRequest
     */
    public static BatchRequest updateClientRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/clients/$.clientId");
        br.setMethod("PUT");
        br.setReference(reference);
        br.setBody("{\"firstname\": \"TestFirstName\", \"lastname\": \"TestLastName\"}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy} Request with
     * given requestId and reference.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param productId
     *            the product ID
     * @return BatchRequest the batch request
     */
    public static BatchRequest applyLoanRequest(final Long requestId, final Long reference, final Integer productId,
            final Integer clientCollateralId) {
        return applyLoanRequest(requestId, reference, productId, clientCollateralId, LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10),
                "10,000.00");
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy} Request with
     * given requestId and reference.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param productId
     *            the product ID
     * @param date
     *            the loan submitted on date
     * @param loanAmount
     *            the loan amount
     * @return BatchRequest the batch request
     */
    public static BatchRequest applyLoanRequest(final Long requestId, final Long reference, final Integer productId,
            final Integer clientCollateralId, final LocalDate date, final String loanAmount) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans");
        br.setMethod("POST");
        br.setReference(reference);
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String body = "{\"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en_GB\", \"clientId\": \"$.clientId\"," + "\"productId\": "
                + productId + ", \"principal\": \"" + loanAmount + "\", \"loanTermFrequency\": 10,"
                + "\"loanTermFrequencyType\": 2, \"loanType\": \"individual\", \"numberOfRepayments\": 10,"
                + "\"repaymentEvery\": 1, \"repaymentFrequencyType\": 2, \"interestRatePerPeriod\": 10,"
                + "\"amortizationType\": 1, \"interestType\": 0, \"interestCalculationPeriodType\": 1,"
                + "\"transactionProcessingStrategyCode\": \"mifos-standard-strategy\", \"expectedDisbursementDate\": \"" + dateString
                + "\",";

        if (clientCollateralId != null) {
            body = body + "\"collateral\": [{\"clientCollateralId\": \"" + clientCollateralId + "\", \"quantity\": \"1\"}],";
        }

        body = body + "\"submittedOnDate\": \"" + dateString + "\"}";

        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy} request with
     * given clientId and product id.
     *
     * @param requestId
     *            the request id
     * @param clientId
     *            the client id
     * @param productId
     *            the product id
     * @return {@link BatchRequest}
     */
    public static BatchRequest applyLoanRequestWithClientId(final Long requestId, final Integer clientId, final Integer productId) {
        return applyLoanRequestWithClientIdAndExternalId(requestId, clientId, productId, UUID.randomUUID().toString());
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy} request with
     * given clientId, external id and product id.
     *
     * @param requestId
     *            the request id
     * @param clientId
     *            the client id
     * @param productId
     *            the product id
     * @param externalId
     *            the external id
     * @return {@link BatchRequest}
     */
    public static BatchRequest applyLoanRequestWithClientIdAndExternalId(final Long requestId, final Integer clientId,
            final Integer productId, final String externalId) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans");
        br.setMethod("POST");

        String body = String.format("{\"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en_GB\", \"clientId\": %s, "
                + "\"productId\": %s, \"principal\": \"10,000.00\", \"loanTermFrequency\": 10,"
                + "\"loanTermFrequencyType\": 2, \"loanType\": \"individual\", \"numberOfRepayments\": 10,"
                + "\"repaymentEvery\": 1, \"repaymentFrequencyType\": 2, \"interestRatePerPeriod\": 10,"
                + "\"amortizationType\": 1, \"interestType\": 0, \"interestCalculationPeriodType\": 1,"
                + "\"transactionProcessingStrategyCode\": \"mifos-standard-strategy\", \"expectedDisbursementDate\": \"10 Jun 2013\","
                + "\"submittedOnDate\": \"10 Jun 2013\", \"externalId\": \"%s\"}", clientId, productId, externalId);

        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApplySavingsCommandStrategy} Request with
     * given requestId and reference.
     *
     * @param requestId
     * @param reference
     * @param productId
     * @return BatchRequest
     */
    public static BatchRequest applySavingsRequest(final Long requestId, final Long reference, final Integer productId) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/savingsaccounts");
        br.setMethod("POST");
        br.setReference(reference);

        final String body = "{\"clientId\": \"$.clientId\", \"productId\": " + productId + ","
                + "\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"submittedOnDate\": \"01 March 2011\"}";
        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.CreateChargeCommandStrategy} Request with
     * given requestId and reference based on loan id
     *
     * @param requestId
     *            the batch request id.
     * @param reference
     *            the reference id.
     * @param chargeId
     *            the charge id used for getting charge type.
     * @return BatchRequest
     */
    public static BatchRequest createChargeByLoanIdRequest(final Long requestId, final Long reference, final Integer chargeId) {
        return createChargeRequest(requestId, reference, "v1/loans/$.loanId/charges", chargeId);
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.CreateChargeByLoanExternalIdCommandStrategy} Request with given
     * requestId and reference based on loan external id
     *
     * @param requestId
     *            the batch request id.
     * @param reference
     *            the reference id.
     * @param chargeId
     *            the charge id used for getting charge type.
     * @return BatchRequest
     */
    public static BatchRequest createChargeByLoanExternalIdRequest(final Long requestId, final Long reference, final Integer chargeId) {
        return createChargeRequest(requestId, reference, "v1/loans/external-id/$.externalId/charges", chargeId);
    }

    /**
     * Creates and returns a Batch Request with given requestId and reference
     *
     * @param requestId
     *            the batch request id.
     * @param reference
     *            the reference id.
     * @param relativeUrl
     *            the relative url reference.
     * @param chargeId
     *            the charge id used for getting charge type.
     * @return BatchRequest
     */
    private static BatchRequest createChargeRequest(final Long requestId, final Long reference, final String relativeUrl,
            final Integer chargeId) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod("POST");
        br.setReference(reference);

        final String dateFormat = "dd MMMM yyyy";
        final String dateString = LocalDate.now(Utils.getZoneIdOfTenant()).format(DateTimeFormatter.ofPattern(dateFormat));

        final String body = String.format(
                "{\"chargeId\": \"%d\", \"locale\": \"en\", \"amount\": \"11.15\", " + "\"dateFormat\": \"%s\", \"dueDate\": \"%s\"}",
                chargeId, dateFormat, dateString);
        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.AdjustChargeCommandStrategy} Request with
     * given requestId and reference
     *
     * @param requestId
     *            the batch request id.
     * @param reference
     *            the reference id.
     * @return BatchRequest
     */
    public static BatchRequest adjustChargeRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/$.loanId/charges/$.resourceId?command=adjustment");
        br.setMethod("POST");
        br.setReference(reference);
        br.setBody("{\"amount\":7.00,\"locale\":\"en\"}");

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.AdjustChargeByChargeExternalIdCommandStrategy} Request with
     * given requestId and reference
     *
     * @param requestId
     *            the batch request id.
     * @param reference
     *            the reference id.
     * @param loanExternalId
     *            the loan external id.
     * @param chargeExternalId
     *            the charge external id
     * @return BatchRequest
     */
    public static BatchRequest adjustChargeByExternalIdRequest(final Long requestId, final Long reference, final String loanExternalId,
            final String chargeExternalId) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl(
                String.format("v1/loans/external-id/%s/charges/external-id/%s?command=adjustment", loanExternalId, chargeExternalId));
        br.setMethod("POST");
        br.setReference(reference);
        br.setBody("{\"amount\":7.00,\"locale\":\"en\"}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.CollectChargesCommandStrategy} Request
     * with given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference id
     * @return BatchRequest
     */
    public static BatchRequest collectChargesByLoanIdRequest(final Long requestId, final Long reference) {
        return collectChargesRequest(requestId, reference, "v1/loans/$.loanId/charges");
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.CollectChargesByLoanExternalIdCommandStrategy} Request with
     * given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference id
     * @return BatchRequest
     */
    public static BatchRequest collectChargesByLoanExternalIdRequest(final Long requestId, final Long reference) {
        return collectChargesRequest(requestId, reference, "v1/loans/external-id/$.externalId/charges");
    }

    /**
     * Creates and returns a Batch Request with given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference id
     * @param relativeUrl
     *            the relative url
     * @return BatchRequest
     */
    private static BatchRequest collectChargesRequest(final Long requestId, final Long reference, final String relativeUrl) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setReference(reference);
        br.setMethod("GET");
        br.setBody("{ }");

        return br;
    }

    /**
     * Creates and returns a Batch request with given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @param relativeUrl
     *            the relative url
     * @return the {@link BatchRequest}
     */
    private static BatchRequest getChargeById(final Long requestId, final Long reference, final String relativeUrl) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(reference);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.GetChargeByIdCommandStrategy} request
     * with given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getChargeByLoanIdChargeId(final Long requestId, final Long reference) {
        return getChargeById(requestId, reference, "v1/loans/$.loanId/charges/$.resourceId");
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.GetChargeByChargeExternalIdCommandStrategy} request with given
     * requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getChargeByLoanExternalIdChargeExternalId(final Long requestId, final Long reference,
            final String loanExternalId, final String chargeExternalId) {
        return getChargeById(requestId, reference,
                String.format("v1/loans/external-id/%s/charges/external-id/%s", loanExternalId, chargeExternalId));
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ActivateClientCommandStrategy} Request
     * with given requestId and reference.
     *
     *
     * @param requestId
     * @param reference
     * @return BatchRequest
     */
    public static BatchRequest activateClientRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/clients/$.clientId?command=activate");
        br.setReference(reference);
        br.setMethod("POST");
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"activationDate\": \"01 March 2011\"}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy} Request with
     * given requestId and reference.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @return BatchRequest the batch request
     */
    public static BatchRequest approveLoanRequest(final Long requestId, final Long reference) {
        return approveLoanRequest(requestId, reference, LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10));
    }

    /**
     * Creates a wrong {@link org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy} Request with given
     * requestId and reference.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @return BatchRequest the batch request
     */
    public static BatchRequest approveLoanWrongRequest(final Long requestId, final Long reference) {
        return approveLoanWrongRequest(requestId, reference, LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10));
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy} Request with
     * given requestId and reference.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param date
     *            the approved on date
     * @return BatchRequest the batch request
     */
    public static BatchRequest approveLoanRequest(final Long requestId, final Long reference, LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/$.loanId?command=approve");
        br.setReference(reference);
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"approvedOnDate\": \"" + dateString + "\","
                + "\"note\": \"Loan approval note\"}");

        return br;
    }

    public static BatchRequest approveLoanWrongRequest(final Long requestId, final Long reference, LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/$.loanId?command=approveX");
        br.setReference(reference);
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"approvedOnDate\": \"" + dateString + "\","
                + "\"note\": \"Loan approval note\"}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy} Request with
     * given requestId and reference.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @return BatchRequest the batch request
     */
    public static BatchRequest disburseLoanRequest(final Long requestId, final Long reference) {
        return disburseLoanRequest(requestId, reference, LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(8));
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy} Request with
     * given requestId and reference.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param date
     *            the actual disbursement date
     * @return BatchRequest the batch request
     */
    public static BatchRequest disburseLoanRequest(final Long requestId, final Long reference, final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/$.loanId?command=disburse");
        br.setReference(reference);
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"actualDisbursementDate\": \"" + dateString + "\"}");

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.LoanStateTransistionsByExternalIdCommandStrategy} Request with
     * given requestId and reference.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param date
     *            the actual disbursement date
     * @param command
     *            the action to transistion
     * @return BatchRequest the batch request
     */
    public static BatchRequest transistionLoanStateByExternalId(final Long requestId, final Long reference, final LocalDate date,
            final String command) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/external-id/$.resourceExternalId?command=" + command);
        br.setReference(reference);
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        if ("disburse".equals(command)) {
            br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"actualDisbursementDate\": \"" + dateString + "\"}");
        } else if ("approve".equals(command)) {
            br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"approvedOnDate\": \"" + dateString + "\","
                    + "\"note\": \"Loan approval note\"}");
        }

        return br;
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} Request with given requestId.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param amount
     *            the amount
     * @return BatchRequest the batch request
     */
    public static BatchRequest repayLoanRequest(final Long requestId, final Long reference, final String amount) {
        return createTransactionRequest(requestId, reference, "repayment", amount, LocalDate.now(Utils.getZoneIdOfTenant()));
    }

    public static BatchRequest repayLoanRequestWithGivenLoanId(final Long requestId, final Integer loanId, final String amount,
            final LocalDate date) {
        return createTransactionRequestWithGivenLoanId(requestId, loanId, "repayment", amount, date);
    }

    public static BatchRequest oldRepayLoanRequestWithGivenLoanId(final Long requestId, final Integer loanId, final String amount,
            final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl(String.format("loans/" + loanId + "/transactions?command=%s", "repayment"));
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": %s}",
                dateString, amount));

        return br;
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} Request with given requestId.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param amount
     *            the amount
     * @param date
     *            the transaction date
     * @return BatchRequest the batch request
     */
    public static BatchRequest createTransactionRequest(final Long requestId, final Long reference, final String transactionCommand,
            final String amount, final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl(String.format("v1/loans/$.loanId/transactions?command=%s", transactionCommand));
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", "
                + "\"transactionDate\": \"%s\",  \"transactionAmount\": %s, \"note\":null}", dateString, amount));

        return br;
    }

    public static BatchRequest createTransactionRequestWithGivenLoanId(final Long requestId, final Integer loanId,
            final String transactionCommand, final String amount, final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl(String.format("v1/loans/" + loanId + "/transactions?command=%s", transactionCommand));
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": %s}",
                dateString, amount));

        return br;
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} Request with given requestId.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference ID
     * @param transactionCommand
     *            the transaction command to process
     * @param amount
     *            the amount
     * @param date
     *            the transaction date
     * @return BatchRequest the batch request
     */
    public static BatchRequest createTransactionRequestByLoanExternalId(final Long requestId, final Long reference,
            final String transactionCommand, final String amount, final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl(String.format("v1/loans/external-id/$.externalId/transactions?command=%s", transactionCommand));
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": %s}",
                dateString, amount));

        return br;
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} request with given request ID.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest creditBalanceRefundRequest(final Long requestId, final Long reference, final String amount) {
        return createTransactionRequest(requestId, reference, "creditBalanceRefund", amount, LocalDate.now(Utils.getZoneIdOfTenant()));
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} request with given request ID for goodwill
     * credit transaction.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest goodwillCreditRequest(final Long requestId, final Long reference, final String amount) {
        return createTransactionRequest(requestId, reference, "goodwillCredit", amount, LocalDate.now(Utils.getZoneIdOfTenant()));
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} request with given request ID for merchant
     * issued refund transaction.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest merchantIssuedRefundRequest(final Long requestId, final Long reference, final String amount) {
        return createTransactionRequest(requestId, reference, "merchantIssuedRefund", amount, LocalDate.now(Utils.getZoneIdOfTenant()));
    }

    /**
     * Creates and returns a {@link CreateTransactionLoanCommandStrategy} request with given request ID for payout
     * refund transaction.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest payoutRefundRequest(final Long requestId, final Long reference, final String amount) {
        return createTransactionRequest(requestId, reference, "payoutRefund", amount, LocalDate.now(Utils.getZoneIdOfTenant()));
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.CreateLoanRescheduleRequestCommandStrategy} request with given
     * request ID.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            teh reference
     * @param rescheduleFromDate
     *            the reschedule from date
     * @param rescheduleReasonId
     *            the reschedule reason code value id
     *
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest createRescheduleLoanRequest(final Long requestId, final Long reference, final LocalDate rescheduleFromDate,
            final Integer rescheduleReasonId) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl("v1/rescheduleloans");
        br.setMethod("POST");
        final LocalDate today = LocalDate.now(Utils.getZoneIdOfTenant());
        final LocalDate adjustedDueDate = LocalDate.now(Utils.getZoneIdOfTenant()).plusDays(40);
        final String submittedOnDate = today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final String rescheduleFromDateString = rescheduleFromDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final String adjustedDueDateString = adjustedDueDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", "
                + "\"submittedOnDate\": \"%s\",  \"rescheduleFromDate\": \"%s\", \"rescheduleReasonId\": %d, \"adjustedDueDate\": \"%s\", \"loanId\": \"$.loanId\"}",
                submittedOnDate, rescheduleFromDateString, rescheduleReasonId, adjustedDueDateString));

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.ApproveLoanRescheduleCommandStrategy}
     * request with given request ID.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            teh reference
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest approveRescheduleLoanRequest(final Long requestId, final Long reference) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl("v1/rescheduleloans/$.resourceId?command=approve");
        br.setMethod("POST");
        final LocalDate approvedOnDate = LocalDate.now(Utils.getZoneIdOfTenant());
        final String approvedOnDateString = approvedOnDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"approvedOnDate\": \"%s\"}",
                approvedOnDateString));

        return br;
    }

    /**
     * Checks that the client with given externalId is not created on the server.
     *
     * @param requestSpec
     * @param responseSpec
     * @param externalId
     */
    public static void verifyClientNotCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId) {
        LOG.info("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String CLIENT_URL = "/fineract-provider/api/v1/clients?externalId=" + externalId + "&" + Utils.TENANT_IDENTIFIER;
        final Integer responseRecords = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "totalFilteredRecords");
        Assertions.assertEquals((long) 0, (long) responseRecords, "No records found with given externalId");
    }

    /**
     * Creates and returns a {@link GetTransactionByIdCommandStrategy} request with given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @param subResourceId
     *            whether the subResourceId is used
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getTransactionByIdRequest(final Long requestId, final Long reference, final Boolean subResourceId) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl;
        if (subResourceId) {
            relativeUrl = "v1/loans/$.loanId/transactions/$.subResourceId";
        } else {
            relativeUrl = "v1/loans/$.loanId/transactions/$.resourceId";
        }

        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(reference);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a {@link GetTransactionByExternalIdCommandStrategy} request with given requestId and
     * reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getTransactionByExternalIdRequest(final Long requestId, final Long reference, final String loanExternalId,
            final Boolean subResourceExternalId) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl;
        if (subResourceExternalId) {
            relativeUrl = String.format("v1/loans/external-id/%s/transactions/external-id/$.subResourceExternalId", loanExternalId);
        } else {
            relativeUrl = String.format("v1/loans/external-id/%s/transactions/external-id/$.resourceExternalId", loanExternalId);
        }

        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(reference);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy} request with
     * given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @param queryParameter
     *            the query parameters
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getLoanByIdRequest(final Long requestId, final Long reference, final String queryParameter) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "v1/loans/$.loanId";
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(reference);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.GetLoanByExternalIdCommandStrategy}
     * request with given requestId and reference.
     *
     * @param requestId
     *            the request id
     * @param reference
     *            the reference
     * @param queryParameter
     *            the query parameters
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getLoanByExternalIdRequest(final Long requestId, final Long reference, final String queryParameter) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "v1/loans/external-id/$.resourceExternalId";
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(requestId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(reference);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy} request with
     * given loan id and query param.
     *
     * @param loanId
     *            the loan id
     * @param queryParameter
     *            the query parameters
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getLoanByIdRequest(final Long loanId, final String queryParameter) {
        return getLoanByIdRequest(loanId, 4567L, null, queryParameter);
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.GetLoanByIdCommandStrategy} request with
     * given loan id and query param.
     *
     * @param loanId
     *            the loan id
     * @param requestId
     *            the request id
     * @param referenceId
     *            the reference id
     * @param queryParameter
     *            the query parameters
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getLoanByIdRequest(final Long loanId, final Long requestId, final Long referenceId,
            final String queryParameter) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("v1/loans/%s", loanId);
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(requestId);
        br.setReference(referenceId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a batch request to get datatable entry.
     *
     * @param loanId
     *            the loan id
     * @param datatableName
     *            the name of datatable
     * @param queryParameter
     *            the query parameters
     * @param referenceId
     *            the reference id
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getDatatableByIdRequest(final Long loanId, final String datatableName, final String queryParameter,
            final Long referenceId) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("v1/datatables/%s/%s", datatableName, loanId);
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(4571L);
        br.setReference(referenceId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a batch request to get datatable entry.
     *
     * @param loanId
     *            the loan id
     * @param datatableName
     *            the name of datatable
     * @param appTableId
     *            the app table id
     * @param queryParameter
     *            the query parameters
     * @param referenceId
     *            the reference id
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getDatatableEntryByIdRequest(final Long loanId, final String datatableName, final String appTableId,
            final String queryParameter, final Long referenceId) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("v1/datatables/%s/%s/%s", datatableName, loanId, appTableId);
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(4572L);
        br.setReference(referenceId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a batch request to create datatable entry.
     *
     * @param entityId
     *            the entity id
     * @param datatableName
     *            the name of datatable
     * @param columnNames
     *            the column names
     * @return the {@link BatchRequest}
     */
    public static BatchRequest createDatatableEntryRequest(final String entityId, final String datatableName,
            final List<String> columnNames) {
        final BatchRequest br = new BatchRequest();
        final String relativeUrl = String.format("v1/datatables/%s/%s", datatableName, entityId);
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.putAll(columnNames.stream().collect(Collectors.toMap(v -> v, v -> Utils.randomStringGenerator("VAL_", 3))));
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("CreateDataTableEntry map : {}", datatableEntryRequestJsonString);

        br.setRequestId(4569L);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.POST);
        br.setBody(datatableEntryRequestJsonString);

        return br;
    }

    /**
     * Creates and returns a batch request to create datatable entry.
     *
     * @param loanId
     *            the loan id
     * @param datatableName
     *            the name of datatable
     * @param datatableEntryId
     *            the resource id of the datatable entry
     * @param columnNames
     *            the column names
     * @return the {@link BatchRequest}
     */
    public static BatchRequest updateDatatableEntryByEntryIdRequest(final Long loanId, final String datatableName,
            final Long datatableEntryId, final List<String> columnNames) {
        final BatchRequest br = new BatchRequest();
        final String relativeUrl = String.format("v1/datatables/%s/%s/%s", datatableName, loanId, datatableEntryId);
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.putAll(columnNames.stream().collect(Collectors.toMap(v -> v, v -> Utils.randomStringGenerator("VAL_", 3))));
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("UpdateDataTableEntry map : {}", datatableEntryRequestJsonString);

        br.setRequestId(4570L);
        br.setReference(4569L);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.PUT);
        br.setBody(datatableEntryRequestJsonString);

        return br;
    }

    public static BatchRequest deleteDatatableEntryRequest(final String entityId, final String datatableName,
            final String datatableEntryId) {
        final BatchRequest br = new BatchRequest();
        final String relativeUrl = datatableEntryId == null ? String.format("v1/datatables/%s/%s", datatableName, entityId)
                : String.format("v1/datatables/%s/%s/%s", datatableName, entityId, datatableEntryId);
        br.setRequestId(4570L);
        br.setReference(4569L);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.DELETE);
        return br;
    }

    /**
     * Creates and returns a batch request to create an adjust transaction request.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the amount
     * @param date
     *            the date
     * @return the {@link BatchRequest}
     */
    public static BatchRequest createAdjustTransactionRequest(final Long requestId, final Long reference, final String amount,
            final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl("v1/loans/$.loanId/transactions/$.resourceId");
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": %s}",
                dateString, amount));

        return br;
    }

    /**
     * Creates and returns a batch request to create an adjust transaction request using external id.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param loanExternalId
     *            the loan external id
     * @param transactionExternalId
     *            the transaction external id
     * @param amount
     *            the amount
     * @param date
     *            the date
     * @return the {@link BatchRequest}
     */
    public static BatchRequest createAdjustTransactionByExternalIdRequest(final Long requestId, final Long reference,
            final String loanExternalId, final String transactionExternalId, final String amount, final LocalDate date) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl(String.format("v1/loans/external-id/%s/transactions/external-id/%s", loanExternalId, transactionExternalId));
        br.setMethod("POST");
        String dateString = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"%s\",  \"transactionAmount\": %s}",
                dateString, amount));

        return br;
    }

    /**
     * Creates and returns a batch request to create a chargeback transaction request.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the amount
     * @return the {@link BatchRequest}
     */
    public static BatchRequest createChargebackTransactionRequest(final Long requestId, final Long reference, final String amount) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl("v1/loans/$.loanId/transactions/$.resourceId?command=chargeback");
        br.setMethod("POST");
        br.setBody(String.format("{\"locale\": \"en\", \"transactionAmount\": %s, \"paymentTypeId\": 2}", amount));

        return br;

    }

    /**
     * Creates and returns a batch request to update a Loan account as fraud.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @return the {@link BatchRequest}
     */
    public static BatchRequest createLoanRequestMarkAsFraud(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/$.loanId?command=markAsFraud");
        br.setMethod("PUT");
        br.setReference(reference);
        br.setBody("{\"fraud\": \"true\"}");

        return br;
    }

    /**
     * Creates and returns a batch request to update a Loan account as fraud by loan external id.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @return the {@link BatchRequest}
     */
    public static BatchRequest modifyLoanByExternalIdRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("v1/loans/external-id/$.resourceExternalId?command=markAsFraud");
        br.setMethod("PUT");
        br.setReference(reference);
        br.setBody("{\"fraud\": \"true\"}");

        return br;
    }

    /**
     * Creates and returns a batch request to query datatable entry.
     *
     * @param datatableName
     * @param columnName
     * @param columnValue
     * @param columnResult
     * @return
     */
    public static BatchRequest queryDatatableEntries(final String datatableName, final String columnName, final String columnValue,
            final String columnResult) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("v1/datatables/%s/query", datatableName);
        relativeUrl += "?columnFilter=" + columnName + "&" + "valueFilter=" + columnValue + "&" + "resultColumns=" + columnResult;

        br.setRequestId(1L);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a batch request to update datatable entry.
     *
     * @param datatableName
     * @param resourceId
     * @param columnName
     * @param columnValue
     * @return
     */
    public static BatchRequest updateDatatableEntry(final String datatableName, final String resourceId, final String columnName,
            final String columnValue) {
        final BatchRequest br = new BatchRequest();
        final String relativeUrl = String.format("v1/datatables/%s/%s", datatableName, resourceId);
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(columnName, columnValue);
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("UpdateDataTableEntry map : {}", datatableEntryRequestJsonString);

        br.setRequestId(2L);
        br.setReference(1L);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.PUT);
        br.setBody(datatableEntryRequestJsonString);

        return br;
    }

    /**
     * Creates and returns a batch request to update datatable entry.
     *
     * @param datatableName
     * @param resourceId
     * @param subResourceId
     * @param columnName
     * @param columnValue
     * @return
     */
    public static BatchRequest updateDatatableEntry(final String datatableName, final String resourceId, final String subResourceId,
            final String columnName, final String columnValue) {
        final BatchRequest br = new BatchRequest();
        final String relativeUrl = String.format("v1/datatables/%s/%s/%s", datatableName, resourceId, subResourceId);
        final Map<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(columnName, columnValue);
        final String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("UpdateDataTableEntry map : {}", datatableEntryRequestJsonString);

        br.setRequestId(2L);
        br.setReference(1L);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.PUT);
        br.setBody(datatableEntryRequestJsonString);

        return br;
    }

    /**
     * Creates and returns a batch request to get saving account.
     *
     * @param accountId
     *            the saving account id
     * @param queryParameter
     *            the query parameters
     * @param referenceId
     *            the reference id
     * @return the {@link BatchRequest}
     */
    public static BatchRequest getSavingAccount(final Long requestId, final Long accountId, final String queryParameter,
            final Long referenceId) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("v1/savingsaccounts/%s", accountId);
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(requestId);
        br.setReference(referenceId);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.SavingsAccountTransactionCommandStrategy}
     * request with given request ID.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the transaction amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest depositSavingAccount(final Long requestId, final Long reference, final float amount) {
        final LocalDate transactionDate = LocalDate.now(Utils.getZoneIdOfTenant());
        final String transactionDateString = transactionDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String json = String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", "
                        + "\"transactionDate\": \"%s\", \"transactionAmount\": \"%s\", \"paymentTypeId\": \"1\"}",
                transactionDateString, amount);
        return commandSavingAccount(requestId, null, reference, json, "deposit");
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.SavingsAccountTransactionCommandStrategy}
     * request with given request ID.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the transaction amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest withdrawSavingAccount(final Long requestId, final Long reference, final float amount) {
        final LocalDate transactionDate = LocalDate.now(Utils.getZoneIdOfTenant());
        final String transactionDateString = transactionDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String json = String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", "
                        + "\"transactionDate\": \"%s\", \"transactionAmount\": \"%s\", \"paymentTypeId\": \"1\"}",
                transactionDateString, amount);
        return commandSavingAccount(requestId, null, reference, json, "withdrawal");
    }

    public static BatchRequest depositSavingAccount(final Long requestId, final Long savingsId, SavingsTransactionData transactionData) {
        return commandSavingAccount(requestId, savingsId, transactionData, "deposit");
    }

    public static BatchRequest withdrawSavingAccount(final Long requestId, final Long savingsId, SavingsTransactionData transactionData) {
        return commandSavingAccount(requestId, savingsId, transactionData, "withdrawal");
    }

    public static BatchRequest commandSavingAccount(Long requestId, Long savingsId, SavingsTransactionData transactionData,
            String command) {
        String json = transactionData.getJson();
        return commandSavingAccount(requestId, savingsId, null, json, command);
    }

    public static BatchRequest commandSavingAccount(Long requestId, Long savingsId, Long reference, String body, String command) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        String id;
        if (reference != null) {
            br.setReference(reference);
            id = "$.id";
        } else {
            id = savingsId.toString();
        }
        br.setRelativeUrl("v1/savingsaccounts/" + id + "/transactions?command=" + command);
        br.setMethod(HttpMethod.POST);
        br.setBody(body);
        return br;
    }

    /**
     * Creates and returns a {@link org.apache.fineract.batch.command.internal.SavingsAccountTransactionCommandStrategy}
     * request with given request ID.
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param amount
     *            the transaction amount
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest holdAmountOnSavingAccount(final Long requestId, final Long reference, final float amount) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl("v1/savingsaccounts/$.id/transactions?command=holdAmount");
        br.setMethod(HttpMethod.POST);
        final LocalDate transactionDate = LocalDate.now(Utils.getZoneIdOfTenant());
        final String transactionDateString = transactionDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        br.setBody(String.format(
                "{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", "
                        + "\"transactionDate\": \"%s\", \"transactionAmount\": \"%s\", \"reasonForBlock\": \"test\"}",
                transactionDateString, amount));

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.SavingsAccountAdjustTransactionCommandStrategy} request with
     * given request ID.
     *
     *
     * @param requestId
     *            the request ID
     * @param reference
     *            the reference
     * @param transactionId
     *            the transactionId
     * @return BatchRequest the created {@link BatchRequest}
     */
    public static BatchRequest releaseAmountOnSavingAccount(final Long requestId, final Long reference, final Long transactionId) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl("v1/savingsaccounts/$.id/transactions/" + transactionId + "?command=releaseAmount");
        br.setMethod(HttpMethod.POST);
        br.setBody("{\"isBulk\": \"false\"}");

        return br;
    }
}
