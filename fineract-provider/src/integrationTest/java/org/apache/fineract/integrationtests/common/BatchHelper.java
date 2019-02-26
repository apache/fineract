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

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.junit.Assert;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Helper class for {@link org.apache.fineract.integrationtests.BatchApiTest}. It
 * takes care of creation of {@code BatchRequest} list and posting this list to
 * the server.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.apache.fineract.integrationtests.BatchApiTest
 */
public class BatchHelper {

    private static final String BATCH_API_URL = "/fineract-provider/api/v1/batches?" + Utils.TENANT_IDENTIFIER;
    private static final String BATCH_API_URL_EXT = BATCH_API_URL + "&enclosingTransaction=true";

    private BatchHelper() {
        super();
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
     * Returns the converted string response into JSON.
     * 
     * @param json
     * @return List<BatchResponse>
     */
    private static List<BatchResponse> fromJsonString(final String json) {
        return new Gson().fromJson(json, new TypeToken<List<BatchResponse>>() {}.getType());
    }

    /**
     * Returns a list of BatchResponse with query parameter enclosing
     * transaction set to false by posting the jsonified BatchRequest to the
     * server.
     * 
     * @param requestSpec
     * @param responseSpec
     * @param jsonifiedBatchRequests
     * @return a list of BatchResponse
     */
    public static List<BatchResponse> postBatchRequestsWithoutEnclosingTransaction(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String jsonifiedBatchRequests) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, BATCH_API_URL, jsonifiedBatchRequests, null);
        return BatchHelper.fromJsonString(response);
    }

    /**
     * Returns a list of BatchResponse with query parameter enclosing
     * transaction set to true by posting the jsonified BatchRequest to the
     * server.
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
     * Returns a BatchResponse based on the given BatchRequest, by posting the
     * request to the server.
     * 
     * @param BatchRequest
     * @return List<BatchResponse>
     */
    public static List<BatchResponse> postWithSingleRequest(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final BatchRequest br) {

        final List<BatchRequest> batchRequests = new ArrayList<>();
        batchRequests.add(br);

        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(requestSpec, responseSpec,
                jsonifiedRequest);

        // Verifies that the response result is there
        Assert.assertNotNull(response);
        Assert.assertTrue(response.size() > 0);

        return response;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.CreateClientCommandStrategy}
     * Request as one of the request in Batch.
     * 
     * @param reqId
     * @param externalId
     * @return BatchRequest
     */
    public static BatchRequest createClientRequest(final Long requestId, final String externalId) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl("clients");
        br.setMethod("POST");

        final String extId;
        if (externalId.equals("")) {
            extId = "ext" + String.valueOf((10000 * Math.random())) + String.valueOf((10000 * Math.random()));
        } else {
            extId = externalId;
        }

        final String body = "{ \"officeId\": 1, \"firstname\": \"Petra\", \"lastname\": \"Yton\"," + "\"externalId\": " + extId
                + ",  \"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en\"," + "\"active\": false, \"submittedOnDate\": \"04 March 2009\"}";

        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.UpdateClientCommandStrategy}
     * Request with given requestId and reference.
     * 
     * @param reqId
     * @param clientId
     * @return BatchRequest
     */
    public static BatchRequest updateClientRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("clients/$.clientId");
        br.setMethod("PUT");
        br.setReference(reference);
        br.setBody("{\"firstname\": \"TestFirstName\", \"lastname\": \"TestLastName\"}");

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.ApplyLoanCommandStrategy}
     * Request with given requestId and reference.
     * 
     * @param requestId
     * @param reference
     * @param productId
     * @return BatchRequest
     */
    public static BatchRequest applyLoanRequest(final Long requestId, final Long reference, final Integer productId) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("loans");
        br.setMethod("POST");
        br.setReference(reference);

        final String body = "{\"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en_GB\", \"clientId\": \"$.clientId\"," + "\"productId\": "
                + productId + ", \"principal\": \"10,000.00\", \"loanTermFrequency\": 12,"
                + "\"loanTermFrequencyType\": 2, \"loanType\": \"individual\", \"numberOfRepayments\": 10,"
                + "\"repaymentEvery\": 1, \"repaymentFrequencyType\": 2, \"interestRatePerPeriod\": 10,"
                + "\"amortizationType\": 1, \"interestType\": 0, \"interestCalculationPeriodType\": 1,"
                + "\"transactionProcessingStrategyId\": 1, \"expectedDisbursementDate\": \"10 Jun 2013\","
                + "\"submittedOnDate\": \"10 Jun 2013\"}";
        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.ApplySavingsCommandStrategy}
     * Request with given requestId and reference.
     * 
     * @param requestId
     * @param reference
     * @param productId
     * @return BatchRequest
     */
    public static BatchRequest applySavingsRequest(final Long requestId, final Long reference, final Integer productId) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("savingsaccounts");
        br.setMethod("POST");
        br.setReference(reference);

        final String body = "{\"clientId\": \"$.clientId\", \"productId\": " + productId + ","
                + "\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"submittedOnDate\": \"01 March 2011\"}";
        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.CreateChargeCommandStrategy}
     * Request with given requestId and reference
     * 
     * @param requestId
     * @param reference
     * @return BatchRequest
     */
    public static BatchRequest createChargeRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setRelativeUrl("loans/$.loanId/charges");
        br.setMethod("POST");
        br.setReference(reference);

        final String body = "{\"chargeId\": \"2\", \"locale\": \"en\", \"amount\": \"100\", "
                + "\"dateFormat\": \"dd MMMM yyyy\", \"dueDate\": \"29 April 2013\"}";
        br.setBody(body);

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.CollectChargesCommandStrategy}
     * Request with given requestId and reference.
     * 
     * @param requestId
     * @param reference
     * @return BatchRequest
     */
    public static BatchRequest collectChargesRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("loans/$.loanId/charges");
        br.setReference(reference);
        br.setMethod("GET");
        br.setBody("{ }");

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.ActivateClientCommandStrategy}
     * Request with given requestId and reference.
     * 
     * 
     * @param requestId
     * @param reference
     * @return BatchRequest
     */
    public static BatchRequest activateClientRequest(final Long requestId, final Long reference) {

        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("clients/$.clientId?command=activate");
        br.setReference(reference);
        br.setMethod("POST");
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"activationDate\": \"01 March 2011\"}");

        return br;
    }
    
    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.ApproveLoanCommandStrategy}
     * Request with given requestId and reference.
     * 
     * 
     * @param requestId
     * @param reference
     * @return BatchRequest
     */
    public static BatchRequest approveLoanRequest(final Long requestId, final Long reference) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("loans/$.loanId?command=approve");
        br.setReference(reference);
        br.setMethod("POST");
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"approvedOnDate\": \"12 September 2013\"," 
                + "\"note\": \"Loan approval note\"}");

        return br;
    }

    /**
     * Creates and returns a
     * {@link org.apache.fineract.batch.command.internal.DisburseLoanCommandStrategy}
     * Request with given requestId and reference.
     * 
     * 
     * @param requestId
     * @param reference
     * @return BatchRequest
     */
    public static BatchRequest disburseLoanRequest(final Long requestId, final Long reference) {
        final BatchRequest br = new BatchRequest();

        br.setRequestId(requestId);
        br.setRelativeUrl("loans/$.loanId?command=disburse");
        br.setReference(reference);
        br.setMethod("POST");
        br.setBody("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"actualDisbursementDate\": \"15 September 2013\"}");

        return br;
    }
    
    /**
     * Checks that the client with given externalId is not created on the
     * server.
     * 
     * @param requestSpec
     * @param responseSpec
     * @param externalId
     */
    public static void verifyClientCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId) {
        System.out.println("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String CLIENT_URL = "/fineract-provider/api/v1/clients?externalId=" + externalId + "&" + Utils.TENANT_IDENTIFIER;
        final Integer responseRecords = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "totalFilteredRecords");
        Assert.assertEquals("No records found with given externalId", (long) responseRecords, (long) 0);
    }
}