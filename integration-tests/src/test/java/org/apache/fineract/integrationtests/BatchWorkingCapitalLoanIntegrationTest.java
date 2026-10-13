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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionRequest;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachActionData;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.batch.BatchServiceHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalLoanBreachActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalLoanNearBreachActionsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalNearBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the Working Capital Loan Batch API endpoints. Tests that the batch command strategies for
 * working capital loans correctly process submit, modify, approve, disburse, repayment, delete, and GET requests
 * through the batch API, as well as the breach, delinquency and near breach actions.
 */
public class BatchWorkingCapitalLoanIntegrationTest {

    private final BatchServiceHelper batchServiceHelper = new BatchServiceHelper();
    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();
    private final WorkingCapitalNearBreachHelper nearBreachHelper = new WorkingCapitalNearBreachHelper();
    private final WorkingCapitalLoanBreachActionHelper breachActionHelper = new WorkingCapitalLoanBreachActionHelper();
    private final WorkingCapitalLoanNearBreachActionsHelper nearBreachActionsHelper = new WorkingCapitalLoanNearBreachActionsHelper();

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdBreachIds = new ArrayList<>();
    private final List<Long> createdNearBreachIds = new ArrayList<>();
    private final List<Long> createdDelinquencyBucketIds = new ArrayList<>();
    private final List<Long> createdDelinquencyRangeIds = new ArrayList<>();

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdLoanIds.clear();

        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdProductIds.clear();

        for (final Long breachId : createdBreachIds) {
            try {
                breachHelper.delete(breachId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdBreachIds.clear();

        for (final Long nearBreachId : createdNearBreachIds) {
            try {
                nearBreachHelper.delete(nearBreachId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdNearBreachIds.clear();

        for (final Long bucketId : createdDelinquencyBucketIds) {
            try {
                WorkingCapitalLoanDelinquencyRangeScheduleHelper.deleteBucket(bucketId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdDelinquencyBucketIds.clear();

        for (final Long rangeId : createdDelinquencyRangeIds) {
            try {
                ok(() -> FineractFeignClientHelper.getFineractFeignClient().delinquencyRangeAndBucketsManagement().deleteRange(rangeId));
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdDelinquencyRangeIds.clear();
    }

    /**
     * Tests that a working capital loan can be submitted via the Batch API.
     */
    @Test
    public void testSubmitWorkingCapitalLoanViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        final String submitBody = new Gson().toJson(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final BatchRequest submitRequest = buildBatchRequest(1L, null, "v1/working-capital-loans", "POST", submitBody);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(submitRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Expected HTTP 200 for submit working capital loan via batch");
        assertNotNull(responses.get(0).getBody());

        final Long loanId = extractResourceId(responses.get(0).getBody());
        assertNotNull(loanId, "Expected a loanId in batch response body");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests the full lifecycle: submit → approve → disburse → repayment via the Batch API by loan ID.
     */
    @Test
    public void testWorkingCapitalLoanLifecycleViaBatchApiByLoanId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final LocalDate today = Utils.getLocalDateOfTenant();

        final String submitBody = new Gson().toJson(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        // Submit via batch
        final BatchRequest submitRequest = buildBatchRequest(1L, null, "v1/working-capital-loans", "POST", submitBody);
        final List<BatchResponse> submitResponses = batchServiceHelper.handleBatch(submitRequest, false);

        assertEquals(1, submitResponses.size());
        assertEquals(HttpStatus.SC_OK, submitResponses.get(0).getStatusCode(),
                "Expected HTTP 200 for submit working capital loan via batch");
        assertNotNull(submitResponses.get(0).getBody());

        final Long loanId = extractResourceId(submitResponses.get(0).getBody());
        assertNotNull(loanId, "Expected a loanId in batch response body");

        // Approve via batch
        final String approveBody = new Gson()
                .toJson(WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        final BatchRequest approveRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/" + loanId + "?command=approve", "POST",
                approveBody);

        final List<BatchResponse> approveResponses = batchServiceHelper.handleBatch(approveRequest, false);
        assertEquals(1, approveResponses.size());
        assertEquals(HttpStatus.SC_OK, approveResponses.get(0).getStatusCode(),
                "Expected HTTP 200 for approve working capital loan via batch");

        // Verify approved status
        final GetWorkingCapitalLoansLoanIdResponse approvedData = loanHelper.retrieveById(loanId);
        assertNotNull(approvedData.getStatus());
        assertEquals("loanStatusType.approved", approvedData.getStatus().getCode());

        // Disburse via batch
        final String disburseBody = new Gson()
                .toJson(WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));
        final BatchRequest disburseRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/" + loanId + "?command=disburse", "POST",
                disburseBody);

        final List<BatchResponse> disburseResponses = batchServiceHelper.handleBatch(disburseRequest, false);
        assertEquals(1, disburseResponses.size());
        assertEquals(HttpStatus.SC_OK, disburseResponses.get(0).getStatusCode(),
                "Expected HTTP 200 for disburse working capital loan via batch");

        // Verify active status
        final GetWorkingCapitalLoansLoanIdResponse activeData = loanHelper.retrieveById(loanId);
        assertNotNull(activeData.getStatus());
        assertEquals("loanStatusType.active", activeData.getStatus().getCode());

        // Make repayment via batch (transactions endpoint)
        final String repaymentBody = new Gson().toJson(
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(today, BigDecimal.valueOf(500), null, null, null, null));
        final BatchRequest repaymentRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/" + loanId + "/transactions?command=repayment", "POST", repaymentBody);

        final List<BatchResponse> repaymentResponses = batchServiceHelper.handleBatch(repaymentRequest, false);
        assertEquals(1, repaymentResponses.size());
        assertEquals(HttpStatus.SC_OK, repaymentResponses.get(0).getStatusCode(),
                "Expected HTTP 200 for repayment on working capital loan via batch");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests the full lifecycle: submit → approve → disburse → repayment via the Batch API by loan ID. This test runs
     * the entire lifecycle in a single batch API call.
     */
    @Test
    public void testWorkingCapitalLoanLifecycleViaSingleBatchApiCallByLoanId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final LocalDate today = Utils.getLocalDateOfTenant();

        List<BatchRequest> batchRequests = new ArrayList<>();

        final String submitBody = new Gson().toJson(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        // Submit via batch
        final BatchRequest submitRequest = buildBatchRequest(1L, null, "v1/working-capital-loans", "POST", submitBody);
        // Approve via batch
        final String approveBody = new Gson()
                .toJson(WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        final BatchRequest approveRequest = buildBatchRequest(2L, 1L, "v1/working-capital-loans/$.resourceId?command=approve", "POST",
                approveBody);
        // Disburse via batch
        final String disburseBody = new Gson()
                .toJson(WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));
        final BatchRequest disburseRequest = buildBatchRequest(3L, 1L, "v1/working-capital-loans/$.resourceId?command=disburse", "POST",
                disburseBody);
        // Make repayment via batch (transactions endpoint)
        final String repaymentBody = new Gson().toJson(
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(today, BigDecimal.valueOf(500), null, null, null, null));
        final BatchRequest repaymentRequest = buildBatchRequest(4L, 1L,
                "v1/working-capital-loans/$.resourceId/transactions?command=repayment", "POST", repaymentBody);

        batchRequests.add(submitRequest);
        batchRequests.add(approveRequest);
        batchRequests.add(disburseRequest);
        batchRequests.add(repaymentRequest);

        final List<BatchResponse> batchResponses = batchServiceHelper.handleBatch(batchRequests, true);
        assertEquals(4, batchResponses.size());
        for (BatchResponse batchResponse : batchResponses) {
            assertEquals(HttpStatus.SC_OK, batchResponse.getStatusCode(), "Expected HTTP 200 for submit working capital loan via batch");
        }

        final Long loanId = extractResourceId(batchResponses.get(0).getBody());
        assertNotNull(loanId, "Expected a loanId in batch response body");

        DocumentContext approveResponseJson = JsonPath.parse(batchResponses.get(1).getBody());
        assertDoesNotThrow(() -> approveResponseJson.read("$.changes.status"), "Expected a status in approval batch response body");
        assertEquals("APPROVED", approveResponseJson.read("$.changes.status"),
                "Expected status to be APPROVED in approval batch response body");

        DocumentContext disbursementResponseJson = JsonPath.parse(batchResponses.get(2).getBody());
        assertDoesNotThrow(() -> disbursementResponseJson.read("$.changes.actualDisbursementDate"),
                "Expected a disbursement date in disbursement batch response body");

        DocumentContext repaymentResponseJson = JsonPath.parse(batchResponses.get(3).getBody());
        assertDoesNotThrow(() -> repaymentResponseJson.read("$.changes.transactionDate"),
                "Expected a repayment date in repayment batch response body");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan can be modified via the Batch API by loan ID.
     */
    @Test
    public void testModifyWorkingCapitalLoanViaBatchApiByLoanId() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final String modifyBody = new Gson().toJson(new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(6000)) //
                .withSubmittedOnNote("Modified via batch") //
                .buildModifyRequest());

        final BatchRequest modifyRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/" + loanId, "PUT", modifyBody);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(modifyRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Expected HTTP 200 for modify working capital loan via batch");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan can be modified via the Batch API by external ID.
     */
    @Test
    public void testModifyWorkingCapitalLoanViaBatchApiByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-batch-" + UUID.randomUUID().toString().substring(0, 8);

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final String modifyBody = new Gson().toJson(new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(5500)) //
                .withSubmittedOnNote("Modified via batch by external id") //
                .buildModifyRequest());

        final BatchRequest modifyRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/external-id/" + externalId, "PUT",
                modifyBody);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(modifyRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for modify working capital loan by external id via batch");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan can be deleted via the Batch API by loan ID.
     */
    @Test
    public void testDeleteWorkingCapitalLoanViaBatchApiByLoanId() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());
        assertNotNull(loanId);

        final BatchRequest deleteRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/" + loanId, "DELETE", "{}");

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(deleteRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Expected HTTP 200 for delete working capital loan via batch");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan can be deleted via the Batch API by external ID.
     */
    @Test
    public void testDeleteWorkingCapitalLoanViaBatchApiByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-del-batch-" + UUID.randomUUID().toString().substring(0, 8);

        Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final BatchRequest deleteRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/external-id/" + externalId, "DELETE",
                "{}");

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(deleteRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for delete working capital loan by external id via batch");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan can be retrieved via the Batch API by loan ID.
     */
    @Test
    public void testGetWorkingCapitalLoanByIdViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final BatchRequest getRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/" + loanId, "GET", null);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(getRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(), "Expected HTTP 200 for get working capital loan by id via batch");
        assertNotNull(responses.get(0).getBody());

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan can be retrieved via the Batch API by external ID.
     */
    @Test
    public void testGetWorkingCapitalLoanByExternalIdViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-get-batch-" + UUID.randomUUID().toString().substring(0, 8);

        Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final BatchRequest getRequest = buildBatchRequest(1L, null, "v1/working-capital-loans/external-id/" + externalId, "GET", null);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(getRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for get working capital loan by external id via batch");
        assertNotNull(responses.get(0).getBody());

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests the full lifecycle using the external ID flavour of the state-transition and transaction endpoints.
     */
    @Test
    public void testWorkingCapitalLoanLifecycleViaBatchApiByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-lc-batch-" + UUID.randomUUID().toString().substring(0, 8);
        final LocalDate today = Utils.getLocalDateOfTenant();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        // Approve via batch by external id
        final String approveBody = new Gson()
                .toJson(WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        final BatchRequest approveRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/external-id/" + externalId + "?command=approve", "POST", approveBody);

        final List<BatchResponse> approveResponses = batchServiceHelper.handleBatch(approveRequest, false);
        assertEquals(1, approveResponses.size());
        assertEquals(HttpStatus.SC_OK, approveResponses.get(0).getStatusCode(), "Expected HTTP 200 for approve by external id via batch");

        // Disburse via batch by external id
        final String disburseBody = new Gson()
                .toJson(WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));
        final BatchRequest disburseRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/external-id/" + externalId + "?command=disburse", "POST", disburseBody);

        final List<BatchResponse> disburseResponses = batchServiceHelper.handleBatch(disburseRequest, false);
        assertEquals(1, disburseResponses.size());
        assertEquals(HttpStatus.SC_OK, disburseResponses.get(0).getStatusCode(), "Expected HTTP 200 for disburse by external id via batch");

        // Make repayment via batch using external id transactions endpoint
        final String repaymentBody = new Gson().toJson(
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(today, BigDecimal.valueOf(500), null, null, null, null));
        final BatchRequest repaymentRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/external-id/" + externalId + "/transactions?command=repayment", "POST", repaymentBody);

        final List<BatchResponse> repaymentResponses = batchServiceHelper.handleBatch(repaymentRequest, false);
        assertEquals(1, repaymentResponses.size());
        assertEquals(HttpStatus.SC_OK, repaymentResponses.get(0).getStatusCode(),
                "Expected HTTP 200 for repayment by external id via batch");

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan transaction can be retrieved via the Batch API by loan ID and transaction ID.
     */
    @Test
    public void testGetWorkingCapitalLoanTransactionByIdViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final LocalDate today = Utils.getLocalDateOfTenant();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));

        final long transactionId = loanHelper.retrieveTransactionsByLoanId(loanId).getContent().getFirst().getId();

        final BatchRequest getTransactionRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/" + loanId + "/transactions/" + transactionId, "GET", null);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(getTransactionRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for get working capital loan transaction by id via batch");
        assertNotNull(responses.get(0).getBody());

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan transaction can be retrieved via the Batch API by loan ID and external
     * transaction ID.
     */
    @Test
    public void testGetWorkingCapitalLoanTransactionByIdAndExternalTransactionIdViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String txnExternalId = "wcl-txn-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final LocalDate today = Utils.getLocalDateOfTenant();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000),
                null, null, null, null, null, null, null, null, txnExternalId));

        final BatchRequest getTransactionRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/" + loanId + "/transactions/external-id/" + txnExternalId, "GET", null);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(getTransactionRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for get working capital loan transaction by id via batch");
        assertNotNull(responses.get(0).getBody());

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan transaction can be retrieved via the Batch API by loan ID and transaction ID.
     */
    @Test
    public void testGetWorkingCapitalLoanTransactionByLoanExternalIdAndTransactionIdViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String loanExternalId = "wcl-txn-batch-" + UUID.randomUUID().toString().substring(0, 8);
        final LocalDate today = Utils.getLocalDateOfTenant();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(loanExternalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));

        final long transactionId = loanHelper.retrieveTransactionsByLoanId(loanId).getContent().getFirst().getId();

        final BatchRequest getTransactionRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/external-id/" + loanExternalId + "/transactions/" + transactionId, "GET", null);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(getTransactionRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for get working capital loan transaction by id via batch");
        assertNotNull(responses.get(0).getBody());

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a working capital loan transaction can be retrieved via the Batch API by loan external ID and
     * transaction external ID.
     */
    @Test
    public void testGetWorkingCapitalLoanTransactionByExternalIdViaBatchApi() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String loanExternalId = "wcl-txn-batch-" + UUID.randomUUID().toString().substring(0, 8);
        final String txnExternalId = "wcl-txn-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final LocalDate today = Utils.getLocalDateOfTenant();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(loanExternalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .buildSubmitRequest());

        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000),
                null, null, null, null, null, null, null, null, txnExternalId));

        final BatchRequest getTransactionRequest = buildBatchRequest(1L, null,
                "v1/working-capital-loans/external-id/" + loanExternalId + "/transactions/external-id/" + txnExternalId, "GET", null);

        final List<BatchResponse> responses = batchServiceHelper.handleBatch(getTransactionRequest, false);

        assertEquals(1, responses.size());
        assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                "Expected HTTP 200 for get working capital loan transaction by external id via batch");
        assertNotNull(responses.get(0).getBody());

        createdLoanIds.add(loanId);
        createdProductIds.add(productId);
    }

    /**
     * Tests that a delinquency action can be batched by loan ID: submit → approve → disburse → pause in a single
     * enclosing-transaction batch call, the pause resolving the loan created by the first sub-request.
     */
    @Test
    public void testDelinquencyActionViaSingleBatchApiCallByLoanId() {
        final Long productId = createProductWithDelinquencyBucket();
        final Long clientId = createClient();
        final LocalDate today = Utils.getLocalDateOfTenant();

        final List<BatchRequest> batchRequests = buildActiveLoanBatchRequests(clientId, productId, null, today);
        final String pauseBody = new Gson()
                .toJson(WorkingCapitalLoanDelinquencyActionHelper.buildActionRequest("pause", today, today.plusDays(10)));
        batchRequests.add(buildBatchRequest(4L, 1L, "v1/working-capital-loans/$.resourceId/delinquency-actions", "POST", pauseBody));

        final List<BatchResponse> batchResponses = batchServiceHelper.handleBatch(batchRequests, true);
        final Long loanId = assertAllSucceededAndGetLoanId(batchResponses, 4);

        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(1, actions.size());
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.getFirst().getAction());
        assertEquals(today, actions.getFirst().getStartDate());
        assertEquals(today.plusDays(10), actions.getFirst().getEndDate());
    }

    /**
     * Tests that a breach action can be batched by loan external ID: submit → approve → disburse → pause in a single
     * enclosing-transaction batch call, the pause addressing the loan through the external ID sent on submit.
     */
    @Test
    public void testBreachActionViaSingleBatchApiCallByLoanExternalId() {
        final Long productId = createProductWithBreach(null);
        final Long clientId = createClient();
        final String externalId = UUID.randomUUID().toString();
        final LocalDate today = Utils.getLocalDateOfTenant();

        final List<BatchRequest> batchRequests = buildActiveLoanBatchRequests(clientId, productId, externalId, today);
        final PostWorkingCapitalLoansBreachActionRequest pauseRequest = new PostWorkingCapitalLoansBreachActionRequest();
        pauseRequest.setAction("pause");
        pauseRequest.setStartDate(today.toString());
        pauseRequest.setEndDate(today.plusDays(10).toString());
        pauseRequest.setDateFormat("yyyy-MM-dd");
        pauseRequest.setLocale("en");
        batchRequests.add(buildBatchRequest(4L, 1L, "v1/working-capital-loans/external-id/" + externalId + "/breach-actions", "POST",
                new Gson().toJson(pauseRequest)));

        final List<BatchResponse> batchResponses = batchServiceHelper.handleBatch(batchRequests, true);
        final Long loanId = assertAllSucceededAndGetLoanId(batchResponses, 4);

        final List<WorkingCapitalLoanBreachActionData> actions = breachActionHelper.retrieveBreachActions(loanId);
        assertEquals(1, actions.size());
        assertEquals(WorkingCapitalLoanBreachActionData.ActionEnum.PAUSE, actions.getFirst().getAction());
        assertEquals(today, actions.getFirst().getStartDate());
        assertEquals(today.plusDays(10), actions.getFirst().getEndDate());
    }

    /**
     * Tests that a near breach action can be batched by loan ID: submit → approve → disburse → reschedule in a single
     * enclosing-transaction batch call, the reschedule resolving the loan created by the first sub-request.
     */
    @Test
    public void testNearBreachActionViaSingleBatchApiCallByLoanId() {
        final Long nearBreachId = nearBreachHelper
                .create(new WorkingCapitalNearBreachRequest().nearBreachName(Utils.randomStringGenerator("NearBreach", 12))
                        .nearBreachThreshold(BigDecimal.valueOf(20)).nearBreachFrequency(7).nearBreachFrequencyType("DAYS"))
                .getResourceId();
        createdNearBreachIds.add(nearBreachId);
        final Long productId = createProductWithBreach(nearBreachId);
        final Long clientId = createClient();
        final LocalDate today = Utils.getLocalDateOfTenant();

        final List<BatchRequest> batchRequests = buildActiveLoanBatchRequests(clientId, productId, null, today);
        final String rescheduleBody = new Gson().toJson(Map.of("action", "RESCHEDULE", "nearBreachThreshold", BigDecimal.valueOf(40),
                "nearBreachFrequency", 15, "nearBreachFrequencyType", "DAYS", "locale", "en"));
        batchRequests.add(buildBatchRequest(4L, 1L, "v1/working-capital-loans/$.resourceId/near-breach-actions", "POST", rescheduleBody));

        final List<BatchResponse> batchResponses = batchServiceHelper.handleBatch(batchRequests, true);
        final Long loanId = assertAllSucceededAndGetLoanId(batchResponses, 4);

        final List<WorkingCapitalLoanNearBreachActionData> history = nearBreachActionsHelper.getNearBreachChangeActionsById(loanId);
        assertEquals(1, history.size());
        assertEquals(0, BigDecimal.valueOf(40).compareTo(history.getFirst().getThreshold()));
        assertEquals(15, history.getFirst().getFrequency());
        assertEquals("DAYS", history.getFirst().getFrequencyType());
    }

    /**
     * Builds the submit → approve → disburse sub-requests that leave a working capital loan active, the approve and
     * disburse steps referencing the submit through {@code $.resourceId}.
     */
    private List<BatchRequest> buildActiveLoanBatchRequests(final Long clientId, final Long productId, final String externalId,
            final LocalDate today) {
        final WorkingCapitalLoanApplicationTestBuilder builder = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withSubmittedOnDate(today);
        if (externalId != null) {
            builder.withExternalId(externalId);
        }
        final String submitBody = new Gson().toJson(builder.buildSubmitRequest());
        final String approveBody = new Gson()
                .toJson(WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(today, BigDecimal.valueOf(5000), null));
        final String disburseBody = new Gson()
                .toJson(WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(today, BigDecimal.valueOf(5000)));

        final List<BatchRequest> batchRequests = new ArrayList<>();
        batchRequests.add(buildBatchRequest(1L, null, "v1/working-capital-loans", "POST", submitBody));
        batchRequests.add(buildBatchRequest(2L, 1L, "v1/working-capital-loans/$.resourceId?command=approve", "POST", approveBody));
        batchRequests.add(buildBatchRequest(3L, 1L, "v1/working-capital-loans/$.resourceId?command=disburse", "POST", disburseBody));
        return batchRequests;
    }

    private Long assertAllSucceededAndGetLoanId(final List<BatchResponse> batchResponses, final int expectedSize) {
        assertEquals(expectedSize, batchResponses.size());
        for (final BatchResponse batchResponse : batchResponses) {
            assertEquals(HttpStatus.SC_OK, batchResponse.getStatusCode(),
                    "Expected HTTP 200 for request " + batchResponse.getRequestId() + ": " + batchResponse.getBody());
        }
        final Long loanId = extractResourceId(batchResponses.getFirst().getBody());
        assertNotNull(loanId, "Expected a loanId in batch response body");
        createdLoanIds.add(loanId);
        return loanId;
    }

    private BatchRequest buildBatchRequest(final Long requestId, final Long reference, final String relativeUrl, final String method,
            final String body) {
        final BatchRequest br = new BatchRequest();
        br.setRequestId(requestId);
        br.setReference(reference);
        br.setRelativeUrl(relativeUrl);
        br.setMethod(method);
        if (body != null) {
            br.setBody(body);
        }
        return br;
    }

    private Long submitAndTrack(final org.apache.fineract.client.models.PostWorkingCapitalLoansRequest request) {
        final Long loanId = loanHelper.submit(request);
        assertNotNull(loanId);
        return loanId;
    }

    private Long createProduct() {
        final String uniqueName = "WCL Batch Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        assertNotNull(productId);
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithDelinquencyBucket() {
        final PostDelinquencyRangeResponse range = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(30).locale("en"));
        assertNotNull(range);
        createdDelinquencyRangeIds.add(range.getResourceId());
        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(List.of(range.getResourceId()), 30, 0, new BigDecimal("3"), 1);
        assertNotNull(bucket);
        createdDelinquencyBucketIds.add(bucket.getResourceId());
        final String uniqueName = "WCL Batch Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDelinquencyBucketId(bucket.getResourceId()).build()).getResourceId();
        assertNotNull(productId);
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithBreach(final Long nearBreachId) {
        final Long breachId = breachHelper
                .create(new WorkingCapitalBreachRequest().name(Utils.randomStringGenerator("Breach", 12)).breachFrequency(60)
                        .breachFrequencyType("DAYS").breachAmountCalculationType("PERCENTAGE").breachAmount(BigDecimal.valueOf(10)));
        createdBreachIds.add(breachId);
        final String uniqueName = "WCL Batch Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final WorkingCapitalLoanProductTestBuilder builder = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withBreachId(breachId);
        if (nearBreachId != null) {
            builder.withNearBreachId(nearBreachId);
        }
        final Long productId = productHelper.createWorkingCapitalLoanProduct(builder.build()).getResourceId();
        assertNotNull(productId);
        createdProductIds.add(productId);
        return productId;
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private Long extractResourceId(final String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            final var map = new Gson().fromJson(responseBody, java.util.Map.class);
            final Object resourceId = map.get("resourceId");
            if (resourceId instanceof Number) {
                return ((Number) resourceId).longValue();
            }
        } catch (final Exception ignored) {
            // not a JSON object or no resourceId field
        }
        return null;
    }
}
