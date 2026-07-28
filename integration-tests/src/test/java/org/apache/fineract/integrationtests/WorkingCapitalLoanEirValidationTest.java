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
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Working Capital loan inputs whose derived Daily Payment / Total Days / EIR cannot be validly calculated must be
 * rejected with a clean 4xx, never HTTP 500. Ordered: should the submit-time pre-check ever regress, approving a
 * pathological loan grinds the server through a doomed EIR solve, so the approval-time safety net runs after the submit
 * tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkingCapitalLoanEirValidationTest {

    private static final String EXPECTED_EIR_ERROR_MESSAGE = "Please check the input values - unable to calculate a valid EIR.";
    private static final String EXPECTED_EIR_ERROR_CODE = "unable.to.calculate.valid.eir";
    private static final BigDecimal OVER_CAP_PRODUCT_DISCOUNT = BigDecimal.valueOf(300000);

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    @Test
    @Order(1)
    public void testSubmitAndApproveWithCalculableEirInputsStillSucceeds() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final GetWorkingCapitalLoansLoanIdResponse submitted = applicationHelper.retrieveLoan(loanId);
        assertNotNull(submitted.getStatus());
        assertEquals("loanStatusType.submitted.and.pending.approval", submitted.getStatus().getCode());

        final LocalDate approvedOnDate = submitted.getTimeline().getSubmittedOnDate();
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate));

        final ProjectedAmortizationScheduleData schedule = applicationHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId);
        assertNotNull(schedule.getExpectedPaymentAmount());
        assertEquals(0, new BigDecimal("2.75").compareTo(schedule.getExpectedPaymentAmount()),
                "Daily Payment must be 5500 * 18 / 360 / 100 = 2.75, got: " + schedule.getExpectedPaymentAmount());
        assertEquals(1819, schedule.getOriginalPaymentNumber(),
                "Total Days must be ceil(5000 / 2.75) = 1819, got: " + schedule.getOriginalPaymentNumber());
        assertNotNull(schedule.getEffectiveInterestRate());
        assertTrue(schedule.getEffectiveInterestRate().compareTo(BigDecimal.ZERO) > 0,
                "EIR must converge to a positive rate for calculable inputs, got: " + schedule.getEffectiveInterestRate());

        // cleanup: undo approval so the application can be deleted
        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    @Order(2)
    public void testSubmitWithNonCalculableEirInputsIsRejectedWith400() {
        final Long productId = createProductWithDiscountAllowed();
        final Long clientId = createClient();
        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(9000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) // 18%
                .withTotalPaymentVolume(BigDecimal.valueOf(17)) //
                .withDiscount(BigDecimal.valueOf(1000)) //
                .buildSubmitRequest();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus(), "Non-calculable EIR inputs must be rejected with 400 validation error, got: " + ex.getStatus());
        assertEquals("Validation errors: [principalAmount] " + EXPECTED_EIR_ERROR_MESSAGE, ex.getDeveloperMessage());
        assertNotNull(ex.getResponseBody());
        assertTrue(ex.getResponseBody().contains(EXPECTED_EIR_ERROR_CODE),
                "Expected validation code containing `" + EXPECTED_EIR_ERROR_CODE + "` in: " + ex.getResponseBody());

        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    @Order(3)
    public void testSubmitWithNonCalculableEirInputsWithoutDiscountIsRejectedWith400() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(9000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) // 18%
                .withTotalPaymentVolume(BigDecimal.valueOf(17)) //
                .buildSubmitRequest();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus(),
                "Non-calculable EIR inputs (no discount) must be rejected with 400 validation error, got: " + ex.getStatus());
        assertEquals("Validation errors: [principalAmount] " + EXPECTED_EIR_ERROR_MESSAGE, ex.getDeveloperMessage());
        assertNotNull(ex.getResponseBody());
        assertTrue(ex.getResponseBody().contains(EXPECTED_EIR_ERROR_CODE),
                "Expected validation code containing `" + EXPECTED_EIR_ERROR_CODE + "` in: " + ex.getResponseBody());

        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    @Order(4)
    public void testModifySubmittedLoanIntoNonCalculableEirInputsIsRejectedWith400AndLoanUnchanged() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final var modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withTotalPaymentVolume(BigDecimal.valueOf(17)) //
                .buildModifyRequest();

        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus(),
                "Modify into non-calculable EIR inputs must be rejected with 400 validation error, got: " + ex.getStatus());
        assertEquals("Validation errors: [principalAmount] " + EXPECTED_EIR_ERROR_MESSAGE, ex.getDeveloperMessage());

        final GetWorkingCapitalLoansLoanIdResponse loan = applicationHelper.retrieveLoan(loanId);
        assertNotNull(loan.getTotalPaymentVolume());
        assertEquals(0, BigDecimal.valueOf(5500).compareTo(loan.getTotalPaymentVolume()),
                "Rejected modification must not change totalPaymentVolume, but it is now: " + loan.getTotalPaymentVolume());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Choke-point safety net: approval must never surface HTTP 500. Two branches - if submit's pre-check rejects the
     * inputs (expected today), assert the clean 400; if a pathological loan ever reaches approval, assert the clean
     * 403.
     */
    @Test
    @Order(5)
    public void testApproveLoanWithNonCalculableEirInputsFailsCleanlyNot500() {
        final Long productId = createProductWithDiscountAllowed();
        final Long clientId = createClient();
        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(9000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) // 18%
                .withTotalPaymentVolume(BigDecimal.valueOf(17)) //
                .withDiscount(BigDecimal.valueOf(1000)) //
                .buildSubmitRequest();

        final Long loanId;
        try {
            loanId = applicationHelper.submit(json);
        } catch (CallFailedRuntimeException submitRejection) {
            assertEquals(400, submitRejection.getStatus(),
                    "Submit-time rejection of non-calculable EIR inputs must be a 400 validation error, got: "
                            + submitRejection.getStatus());
            assertTrue(submitRejection.getDeveloperMessage().contains("unable to calculate a valid EIR"),
                    "Expected EIR validation message, got: " + submitRejection.getDeveloperMessage());
            productHelper.deleteWorkingCapitalLoanProductById(productId);
            return;
        }

        // Fallback path: the loan exists with pathological inputs; approval must NOT return 500.
        final LocalDate approvedOnDate = applicationHelper.retrieveLoan(loanId).getTimeline().getSubmittedOnDate();
        int approveStatus;
        String approveErrorDetail;
        try {
            final CallFailedRuntimeException ex = applicationHelper.runApproveExpectingFailure(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate));
            approveStatus = ex.getStatus();
            approveErrorDetail = ex.getDeveloperMessage() != null ? ex.getDeveloperMessage() : ex.getMessage();
        } catch (FeignException rawServerFailure) {
            // An unguarded 500 comes back with an empty body the Fineract error decoder cannot deserialize, so it
            // escapes FeignCalls.fail as a raw FeignException; capture it for the status assertion below.
            approveStatus = rawServerFailure.status();
            approveErrorDetail = rawServerFailure.getMessage();
        }

        assertEquals(403, approveStatus, "Approval of a loan with non-calculable EIR must fail with a clean 403 domain-rule error, got: "
                + approveStatus + " - detail: " + approveErrorDetail);
        assertNotNull(approveErrorDetail);
        assertTrue(approveErrorDetail.contains("EIR"),
                "Error must make clear reference to the EIR calculation problem, got: " + approveErrorDetail);

        final GetWorkingCapitalLoansLoanIdResponse loan = applicationHelper.retrieveLoan(loanId);
        assertNotNull(loan.getStatus());
        assertEquals("loanStatusType.submitted.and.pending.approval", loan.getStatus().getCode(),
                "Loan must remain pending approval after the rejected approval");

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Rate change on an ACTIVE loan into 0.01%: the derived daily payment rounds to 0.00 at the 2-decimal currency
     * scale, so re-deriving Total Days divides by zero - must fail with a clean 403 and leave the loan active.
     */
    @Test
    @Order(6)
    public void testRateChangeIntoNonCalculableEirFailsCleanlyNot500() {
        final Long loanId = createActiveLoan();

        final CallFailedRuntimeException ex = applicationHelper.runUpdateRateExpectingFailure(loanId,
                WorkingCapitalLoanRequestBuilders.updateRate(new BigDecimal("0.01")));

        assertRateChangeRejectedAndLoanStaysActive(loanId, ex);
    }

    /**
     * The submit-time feasibility check must evaluate the discount the loan will inherit from the product, not the
     * (absent) requested one: these inputs are calculable alone but breach the Total Days cap with the inherited
     * default discount.
     */
    @Test
    @Order(7)
    public void testSubmitWithNonCalculableEirInputsViaProductDefaultDiscountIsRejectedWith400() {
        final Long productId = createProduct(builder -> builder //
                .withDiscount(OVER_CAP_PRODUCT_DISCOUNT) //
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.FALSE)));
        final Long clientId = createClient();
        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) // 18%
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus(),
                "Non-calculable effective inputs (product default discount) must be rejected with 400, got: " + ex.getStatus());
        assertEquals("Validation errors: [principalAmount] " + EXPECTED_EIR_ERROR_MESSAGE, ex.getDeveloperMessage());
        assertNotNull(ex.getResponseBody());
        assertTrue(ex.getResponseBody().contains(EXPECTED_EIR_ERROR_CODE),
                "Expected validation code containing `" + EXPECTED_EIR_ERROR_CODE + "` in: " + ex.getResponseBody());

        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Counterpart of the inherited-discount rejection: when the product default discount IS overridable, an omitted
     * discount resolves to zero, so the same submission must be accepted - the pre-check must not over-reject.
     */
    @Test
    @Order(8)
    public void testSubmitSucceedsWhenLargeProductDefaultDiscountIsOverridable() {
        final Long productId = createProduct(builder -> builder //
                .withDiscount(OVER_CAP_PRODUCT_DISCOUNT) //
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)));
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) // 18%
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final GetWorkingCapitalLoansLoanIdResponse loan = applicationHelper.retrieveLoan(loanId);
        assertNotNull(loan.getStatus());
        assertEquals("loanStatusType.submitted.and.pending.approval", loan.getStatus().getCode());
        assertTrue(loan.getProposedDiscountFee() == null || BigDecimal.ZERO.compareTo(loan.getProposedDiscountFee()) == 0,
                "Overridable product default discount must not be inherited, but the loan carries: " + loan.getProposedDiscountFee());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Rate change on an ACTIVE loan into 0.1%: the daily payment rounds UP to a positive 0.02 and the re-derived term
     * (~250,000 days) exceeds the calculable cap while the EIR solver itself would return cleanly via its zero-rate
     * shortcut - only the cap guard stands between this input and a persisted quarter-million-row schedule.
     */
    @Test
    @Order(9)
    public void testRateChangeIntoOverCapTermIsRejectedWith403() {
        final Long loanId = createActiveLoan();

        final CallFailedRuntimeException ex = applicationHelper.runUpdateRateExpectingFailure(loanId,
                WorkingCapitalLoanRequestBuilders.updateRate(new BigDecimal("0.1")));

        assertRateChangeRejectedAndLoanStaysActive(loanId, ex);
    }

    private Long createActiveLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final LocalDate today = Utils.getLocalDateOfTenant();
        final String todayStr = Utils.dateFormatter.format(today);

        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) // 18%
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(today) //
                .withExpectedDisbursementDate(today) //
                .buildSubmitRequest());

        applicationHelper.approveById(loanId, WorkingCapitalLoanRequestBuilders.approve(todayStr, BigDecimal.valueOf(5000), todayStr));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanRequestBuilders.disburse(todayStr, BigDecimal.valueOf(5000)));
        return loanId;
    }

    private void assertRateChangeRejectedAndLoanStaysActive(final Long loanId, final CallFailedRuntimeException ex) {
        assertEquals(403, ex.getStatus(),
                "Rate change into non-calculable EIR territory must fail with a clean 403 domain-rule error, got: " + ex.getStatus());
        assertNotNull(ex.getResponseBody());
        assertTrue(ex.getResponseBody().contains("error.msg.workingcapitalloan.eir.not.calculable"),
                "Expected EIR-not-calculable error code in: " + ex.getResponseBody());
        final String detail = ex.getDeveloperMessage() != null ? ex.getDeveloperMessage() : ex.getMessage();
        assertTrue(detail.contains("unable to calculate a valid EIR"), "Error must reference the EIR calculation problem, got: " + detail);

        final GetWorkingCapitalLoansLoanIdResponse loan = applicationHelper.retrieveLoan(loanId);
        assertNotNull(loan.getStatus());
        assertEquals("loanStatusType.active", loan.getStatus().getCode(), "Loan must remain active after the rejected rate change");
    }

    private Long createProduct() {
        return createProduct(UnaryOperator.identity());
    }

    private Long createProductWithDiscountAllowed() {
        return createProduct(builder -> builder.withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)));
    }

    private Long createProduct(final UnaryOperator<WorkingCapitalLoanProductTestBuilder> customize) {
        final WorkingCapitalLoanProductTestBuilder builder = new WorkingCapitalLoanProductTestBuilder() //
                .withName("WCL Product " + UUID.randomUUID().toString().substring(0, 8)) //
                .withShortName(Utils.uniqueRandomStringGenerator("", 4));
        return productHelper.createWorkingCapitalLoanProduct(customize.apply(builder).build()).getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }
}
