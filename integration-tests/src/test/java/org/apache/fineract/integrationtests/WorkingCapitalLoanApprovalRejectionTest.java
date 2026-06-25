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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanApprovalRejectionTest {

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    // ===== AC: User should be able to approve the created loan account (via API) =====

    @Test
    public void testApproveWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate approvedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate));

        final GetWorkingCapitalLoansLoanIdResponse data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.approved", data.getStatus().getCode());
        assertEquals(approvedOnDate, data.getTimeline().getApprovedOnDate());
        // approvedPrincipal should default to proposedPrincipal
        assertNotNull(data.getApprovedPrincipal());
    }

    // ===== AC: Fields modifiable during approval: Principal, Discount, Date, ExpDisbDate =====

    @Test
    public void testApproveWithPrincipalAndDiscountOverride() {
        final Long productId = createProductWithDiscountOverride();
        final Long clientId = createClient();

        // Submit with discount = 100
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withDiscount(BigDecimal.valueOf(100)) //
                .buildSubmitRequest());

        final LocalDate approvedOnDate = getSubmittedOnDate(loanId);
        final BigDecimal approvedAmount = BigDecimal.valueOf(3000);
        final BigDecimal discountAmount = BigDecimal.valueOf(50); // reduced from 100 to 50

        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, approvedAmount, discountAmount));

        final GetWorkingCapitalLoansLoanIdResponse data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.approved", data.getStatus().getCode());
        assertEqualBigDecimal(approvedAmount, data.getApprovedPrincipal());
        assertEqualBigDecimal(BigDecimal.valueOf(100), data.getProposedDiscountFee());
        assertEqualBigDecimal(discountAmount, data.getApprovedDiscountFee());
        assertNull(data.getDiscountFee());
    }

    @Test
    public void testRejectWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate rejectedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.rejectById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildRejectRequest(rejectedOnDate));

        final GetWorkingCapitalLoansLoanIdResponse data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.rejected", data.getStatus().getCode());
        assertEquals(rejectedOnDate, data.getTimeline().getRejectedOnDate());
    }

    // ===== AC: User should be able to undo the approval; moves back to created state =====

    @Test
    public void testUndoApproval() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(getSubmittedOnDate(loanId)));

        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());

        final GetWorkingCapitalLoansLoanIdResponse data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.submitted.and.pending.approval", data.getStatus().getCode());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testUndoApprovalResetsToCreatedState() {
        final Long productId = createProductWithDiscountOverride();
        final Long clientId = createClient();

        // Submit with discount = 100
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withDiscount(BigDecimal.valueOf(100)) //
                .buildSubmitRequest());

        // Approve with reduced principal and discount
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(getSubmittedOnDate(loanId),
                BigDecimal.valueOf(3000), BigDecimal.valueOf(50)));

        final GetWorkingCapitalLoansLoanIdResponse approvedData = retrieveLoan(loanId);
        assertEqualBigDecimal(BigDecimal.valueOf(3000), approvedData.getApprovedPrincipal());
        assertEqualBigDecimal(BigDecimal.valueOf(100), approvedData.getProposedDiscountFee());
        assertEqualBigDecimal(BigDecimal.valueOf(50), approvedData.getApprovedDiscountFee());
        assertNull(approvedData.getDiscountFee());

        // Undo approval
        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());

        final GetWorkingCapitalLoansLoanIdResponse undoData = retrieveLoan(loanId);
        assert undoData.getStatus() != null;
        assertEquals("loanStatusType.submitted.and.pending.approval", undoData.getStatus().getCode());
        // approvedPrincipal should reset to 0 after undo (loan is back in submitted state, not yet approved)
        assertEqualBigDecimal(BigDecimal.ZERO, undoData.getApprovedPrincipal());

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
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate));

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate));
        assertNotNull(ex);
    }

    @Test
    public void testRejectApprovedLoanFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final LocalDate submittedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate));

        CallFailedRuntimeException ex = applicationHelper.runRejectExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildRejectRequest(submittedOnDate));
        assertNotNull(ex);
    }

    @Test
    public void testUndoNonApprovedLoanFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        CallFailedRuntimeException ex = applicationHelper.runUndoApprovalExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
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
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(null));
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
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(getSubmittedOnDate(loanId).plusDays(10)));
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
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withSubmittedOnDate(submittedOnDate) //
                .buildSubmitRequest());

        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate.minusDays(1)));
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
                WorkingCapitalLoanApplicationTestBuilder.buildRejectRequest(null));
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
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(getSubmittedOnDate(loanId), BigDecimal.valueOf(-100), null));
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
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(getSubmittedOnDate(loanId), BigDecimal.valueOf(6000), null));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithoutExpectedDisbursementDateFails() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = submitLoan(clientId, productId);

        final var request = new PostWorkingCapitalLoansLoanIdRequest().locale("en").dateFormat("yyyy-MM-dd")
                .approvedOnDate(getSubmittedOnDate(loanId).toString());
        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId, request);
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithDiscountExceedingCreatedValueFails() {
        final Long productId = createProductWithDiscountOverride();
        final Long clientId = createClient();

        // Submit with discount = 100
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withDiscount(BigDecimal.valueOf(100)) //
                .buildSubmitRequest());

        // Approve with discount = 200 (exceeds creation-time 100) → should fail
        CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(getSubmittedOnDate(loanId), null, BigDecimal.valueOf(200)));
        assertNotNull(ex);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testApproveWithDiscountFailsWhenProductDisallowsDiscountOverride() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        final Long loanId = submitLoan(clientId, productId);

        final CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveRequest(getSubmittedOnDate(loanId), BigDecimal.valueOf(5000), BigDecimal.valueOf(10)));
        assertNotNull(ex);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("override.not.allowed.by.product"));

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
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withExternalId(externalId) //
                .buildSubmitRequest());

        final LocalDate approvedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.approveByExternalId(externalId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate));

        GetWorkingCapitalLoansLoanIdResponse data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.approved", data.getStatus().getCode());

        applicationHelper.undoApprovalByExternalId(externalId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());

        data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.submitted.and.pending.approval", data.getStatus().getCode());
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
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withExternalId(externalId) //
                .buildSubmitRequest());

        final LocalDate rejectedOnDate = getSubmittedOnDate(loanId);
        applicationHelper.rejectByExternalId(externalId, WorkingCapitalLoanApplicationTestBuilder.buildRejectRequest(rejectedOnDate));

        final GetWorkingCapitalLoansLoanIdResponse data = retrieveLoan(loanId);
        assert data.getStatus() != null;
        assertEquals("loanStatusType.rejected", data.getStatus().getCode());
    }

    // ========== Helper methods ==========

    private Long submitLoan(final Long clientId, final Long productId) {
        return applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .buildSubmitRequest());
    }

    private GetWorkingCapitalLoansLoanIdResponse retrieveLoan(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        return response;
    }

    /**
     * Retrieves the submittedOnDate from the server for the given loan. This avoids timezone mismatches between the
     * test JVM and the server (which uses the tenant timezone).
     */
    private LocalDate getSubmittedOnDate(final Long loanId) {
        return retrieveLoan(loanId).getTimeline().getSubmittedOnDate();
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
    }

    private Long createProductWithDiscountOverride() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withAllowAttributeOverrides(Map.of("discountDefault", true)) //
                .build()).getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final BigDecimal actual) {
        assertNotNull(actual, "Expected value for field");
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
    }
}
