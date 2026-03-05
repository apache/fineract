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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanApprovalRejectionTest {

    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private static Long delinquencyBucketId;
    private static Long fundId;

    private final WorkingCapitalLoanApplicationHelper applicationHelper = new WorkingCapitalLoanApplicationHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    @BeforeAll
    static void init() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
        fundId = (long) FundsResourceHandler.createFund(requestSpec, responseSpec);
    }

    // ===== AC: User should be able to approve the created loan account (via API) =====

    @Test
    public void testApproveWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate approvedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate));

        final JsonObject data = retrieveLoan(loanId);
        assertEquals("loanStatusType.approved", data.getAsJsonObject("status").get("code").getAsString());
        assertDateEquals(approvedOnDate, data.get("approvedOnDate"));
        // approvedPrincipal should default to proposedPrincipal
        assertNotNull(data.get("approvedPrincipal"));
    }

    // ===== AC: Fields modifiable during approval: Principal, Discount, Date, ExpDisbDate =====

    @Test
    public void testApproveWithPrincipalAndDiscountOverride() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        // Submit with discount = 100
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.valueOf(100)) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = getSubmittedOnDate(loanId);
        final BigDecimal approvedAmount = BigDecimal.valueOf(3000);
        final BigDecimal discountAmount = BigDecimal.valueOf(50); // reduced from 100 to 50

        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, approvedAmount, discountAmount));

        final JsonObject data = retrieveLoan(loanId);
        assertEquals("loanStatusType.approved", data.getAsJsonObject("status").get("code").getAsString());
        assertEqualBigDecimal(approvedAmount, data.get("approvedPrincipal"));
        assertEqualBigDecimal(discountAmount, data.get("discount"));
    }

    @Test
    public void testRejectWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate rejectedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.rejectById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildRejectJson(rejectedOnDate));

        final JsonObject data = retrieveLoan(loanId);
        assertEquals("loanStatusType.rejected", data.getAsJsonObject("status").get("code").getAsString());
        assertDateEquals(rejectedOnDate, data.get("rejectedOnDate"));
    }

    // ===== AC: User should be able to undo the approval; moves back to created state =====

    @Test
    public void testUndoApproval() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(getSubmittedOnDate(loanId)));

        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveJson());

        final JsonObject data = retrieveLoan(loanId);
        assertEquals("loanStatusType.submitted.and.pending.approval", data.getAsJsonObject("status").get("code").getAsString());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testUndoApprovalResetsToCreatedState() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        // Submit with discount = 100
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.valueOf(100)) //
                .buildSubmitJson());

        // Approve with reduced principal and discount
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(getSubmittedOnDate(loanId),
                BigDecimal.valueOf(3000), BigDecimal.valueOf(50)));

        final JsonObject approvedData = retrieveLoan(loanId);
        assertEqualBigDecimal(BigDecimal.valueOf(3000), approvedData.get("approvedPrincipal"));
        assertEqualBigDecimal(BigDecimal.valueOf(50), approvedData.get("discount"));

        // Undo approval
        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveJson());

        final JsonObject undoData = retrieveLoan(loanId);
        assertEquals("loanStatusType.submitted.and.pending.approval", undoData.getAsJsonObject("status").get("code").getAsString());
        // approvedPrincipal should reset to 0 after undo (loan is back in submitted state, not yet approved)
        assertEqualBigDecimal(BigDecimal.ZERO, undoData.get("approvedPrincipal"));

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    // ========== State transition validation tests ==========

    @Test
    public void testApproveAlreadyApprovedLoanFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate submittedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(submittedOnDate));

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(submittedOnDate));
        assertNotNull(ex);
    }

    @Test
    public void testRejectApprovedLoanFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate submittedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(submittedOnDate));

        CallFailedRuntimeException ex = applicationHelper.runRejectExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildRejectJson(submittedOnDate));
        assertNotNull(ex);
    }

    @Test
    public void testUndoNonApprovedLoanFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        CallFailedRuntimeException ex = applicationHelper.runUndoApprovalExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveJson());
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    // ========== Input validation tests ==========

    @Test
    public void testApproveWithoutApprovedOnDateFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(null));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithFutureDateFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(getSubmittedOnDate(loanId).plusDays(10)));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithDateBeforeSubmittedOnDateFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());

        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(submittedOnDate) //
                .buildSubmitJson());

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(submittedOnDate.minusDays(1)));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRejectWithoutRejectedOnDateFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        CallFailedRuntimeException ex = applicationHelper.runRejectExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildRejectJson(null));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithNegativeAmountFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(getSubmittedOnDate(loanId), BigDecimal.valueOf(-100), null));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithAmountExceedingProposedPrincipalFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId); // proposed principal = 5000

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(getSubmittedOnDate(loanId), BigDecimal.valueOf(6000), null));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithoutExpectedDisbursementDateFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        // Build approve JSON without expectedDisbursementDate
        final String json = "{\"locale\":\"en\",\"dateFormat\":\"yyyy-MM-dd\",\"approvedOnDate\":\"" + getSubmittedOnDate(loanId) + "\"}";
        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId, json);
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithDiscountExceedingCreatedValueFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        // Submit with discount = 100
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.valueOf(100)) //
                .buildSubmitJson());

        // Approve with discount = 200 (exceeds creation-time 100) → should fail
        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(getSubmittedOnDate(loanId), null, BigDecimal.valueOf(200)));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    // ========== External-ID endpoint tests ==========

    @Test
    public void testApproveAndUndoByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = UUID.randomUUID().toString();

        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withExternalId(externalId) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveByExternalId(externalId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate));

        JsonObject data = retrieveLoan(loanId);
        assertEquals("loanStatusType.approved", data.getAsJsonObject("status").get("code").getAsString());

        applicationHelper.undoApprovalByExternalId(externalId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveJson());

        data = retrieveLoan(loanId);
        assertEquals("loanStatusType.submitted.and.pending.approval", data.getAsJsonObject("status").get("code").getAsString());
    }

    @Test
    public void testRejectByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = UUID.randomUUID().toString();

        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withExternalId(externalId) //
                .buildSubmitJson());

        final LocalDate rejectedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.rejectByExternalId(externalId, WorkingCapitalLoanApplicationTestBuilder.buildRejectJson(rejectedOnDate));

        final JsonObject data = retrieveLoan(loanId);
        assertEquals("loanStatusType.rejected", data.getAsJsonObject("status").get("code").getAsString());
    }

    // ========== Helper methods ==========

    private Long submitLoan(final Long clientId, final Long productId) {
        return applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());
    }

    private JsonObject retrieveLoan(final Long loanId) {
        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        return new Gson().fromJson(response, JsonObject.class);
    }

    /**
     * Retrieves the submittedOnDate from the server for the given loan. This avoids timezone mismatches between the
     * test JVM and the server (which uses the tenant timezone).
     */
    private LocalDate getSubmittedOnDate(final Long loanId) {
        final JsonObject data = retrieveLoan(loanId);
        return extractDate(data.get("submittedOnDate"));
    }

    private static LocalDate extractDate(final com.google.gson.JsonElement element) {
        assertNotNull(element, "Expected date element");
        if (element.isJsonArray()) {
            final com.google.gson.JsonArray arr = element.getAsJsonArray();
            return LocalDate.of(arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt());
        }
        return LocalDate.parse(element.getAsString());
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final com.google.gson.JsonElement actual) {
        assertNotNull(actual, "Expected value for field");
        assertEquals(0, expected.compareTo(actual.getAsJsonPrimitive().getAsBigDecimal()),
                "Expected " + expected + " but got " + actual.getAsString());
    }

    private static void assertDateEquals(final LocalDate expected, final com.google.gson.JsonElement actual) {
        assertNotNull(actual, "Expected date value");
        if (actual.isJsonArray()) {
            final com.google.gson.JsonArray arr = actual.getAsJsonArray();
            assertEquals(expected.getYear(), arr.get(0).getAsInt());
            assertEquals(expected.getMonthValue(), arr.get(1).getAsInt());
            assertEquals(expected.getDayOfMonth(), arr.get(2).getAsInt());
        } else {
            assertEquals(expected.toString(), actual.getAsString());
        }
    }
}
