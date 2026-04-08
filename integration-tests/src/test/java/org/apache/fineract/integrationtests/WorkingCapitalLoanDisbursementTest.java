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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanDisbursementTest {

    private final WorkingCapitalLoanApplicationHelper applicationHelper = new WorkingCapitalLoanApplicationHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final Long createdClientId = createClient();

    private static final String CLEANUP_EMPTY_COMMAND_JSON = "{\"locale\":\"en\",\"dateFormat\":\"yyyy-MM-dd\"}";

    @AfterEach
    void cleanupEntities() {
        // Loans: undo disbursal -> undo approval -> delete
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be disbursed / client inactive / loan already removed)
            }
            try {
                applicationHelper.undoApprovalById(loanId, CLEANUP_EMPTY_COMMAND_JSON);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be approved / already removed)
            }
            try {
                applicationHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may be in non-deletable state / already removed)
            }
        }
        createdLoanIds.clear();

        // Products
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (product may be already removed)
            }
        }
        createdProductIds.clear();
    }

    @Test
    public void testDisburseWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate actualDisbursementDate = LocalDate.now(ZoneId.systemDefault());
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(actualDisbursementDate,
                BigDecimal.valueOf(5000));
        applicationHelper.disburseById(loanId, disburseJson);

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = JsonParser.parseString(response).getAsJsonObject();
        assertStatus(data, "loanStatusType.active");
        assertTrue(data.has("balance") && !data.get("balance").isJsonNull(), "GET loan after disburse should include balance");
        final JsonObject balance = data.getAsJsonObject("balance");
        assertEqualBigDecimal(BigDecimal.valueOf(5000), balance.get("principalOutstanding"));

        assertTrue(data.has("disbursementDetails") && data.get("disbursementDetails").isJsonArray(),
                "GET loan after disburse should include disbursementDetails array");
        assertFalse(data.getAsJsonArray("disbursementDetails").isEmpty(), "disbursementDetails should not be empty");
        final JsonObject disbursement = data.getAsJsonArray("disbursementDetails").get(0).getAsJsonObject();
        assertTrue(disbursement.has("actualDisbursementDate"));
        assertDateEquals(actualDisbursementDate, disbursement.get("actualDisbursementDate"));
        assertTrue(disbursement.has("actualAmount"));
        assertEqualBigDecimal(BigDecimal.valueOf(5000), disbursement.get("actualAmount"));

        assertTrue(data.has("transactions"), "GET loan after disburse should include transactions");
        assertTrue(data.get("transactions").isJsonArray());
        assertEquals(1, data.getAsJsonArray("transactions").size(), "After disburse there should be one transaction");
        final JsonObject txn = data.getAsJsonArray("transactions").get(0).getAsJsonObject();
        assertTrue(txn.has("type") && txn.has("transactionAmount"));
        assertEquals("loanTransactionType.disbursement", txn.getAsJsonObject("type").get("code").getAsString());
        assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.get("transactionAmount"));
        assertTrue(txn.has("reversed") && !txn.get("reversed").getAsBoolean(), "Disbursement transaction should not be reversed");
        assertTrue(txn.has("principalPortion"), "Transaction should include allocation principalPortion");
        assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.get("principalPortion"));
        assertTrue(txn.has("feeChargesPortion"), "Transaction should include allocation feeChargesPortion");
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("feeChargesPortion"));
        assertTrue(txn.has("penaltyChargesPortion"), "Transaction should include allocation penaltyChargesPortion");
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("penaltyChargesPortion"));
    }

    @Test
    public void testDisburseWithAllRequestFieldsAndVerifyResponse() {
        final Long productId = createProductWithDiscountAllowed();

        final BigDecimal approvedPrincipal = BigDecimal.valueOf(10000);
        final BigDecimal approvedDiscount = BigDecimal.valueOf(50);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(approvedPrincipal) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, approvedPrincipal, approvedDiscount));

        final LocalDate actualDisbursementDate = LocalDate.now(ZoneId.systemDefault());
        final BigDecimal transactionAmount = BigDecimal.valueOf(8000);
        final BigDecimal discountAmount = BigDecimal.valueOf(30);
        final String note = "Disbursal note for test";
        final Integer paymentTypeId = 1;
        final String accountNumber = "acc-" + UUID.randomUUID().toString().substring(0, 8);
        final String checkNumber = "chk-123";
        final String routingCode = "rte-456";
        final String receiptNumber = "rec-789";
        final String bankNumber = "bnk-001";

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(actualDisbursementDate, transactionAmount,
                discountAmount, note, paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
        applicationHelper.disburseById(loanId, disburseJson);

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = JsonParser.parseString(response).getAsJsonObject();

        assertStatus(data, "loanStatusType.active");
        assertTrue(data.has("balance") && !data.get("balance").isJsonNull(), "GET loan after disburse should include balance");
        assertEqualBigDecimal(transactionAmount.add(discountAmount), data.getAsJsonObject("balance").get("principalOutstanding"));
        assertEqualBigDecimal(discountAmount, data.get("discount"));
        assertTrue(data.has("id"));
        assertEquals(loanId.longValue(), data.get("id").getAsLong());
        assertTrue(data.has("client") && !data.get("client").isJsonNull());
        assertTrue(data.has("product") && !data.get("product").isJsonNull());

        if (data.has("timeline") && !data.get("timeline").isJsonNull()) {
            final JsonObject timeline = data.getAsJsonObject("timeline");
            assertTrue(timeline.has("actualDisbursementDate"));
            assertDateEquals(actualDisbursementDate, timeline.get("actualDisbursementDate"));
            assertTrue(timeline.has("approvedOnDate"));
            assertTrue(timeline.has("actualMaturityDate"), "timeline should include actualMaturityDate (null until fully paid)");
            assertTrue(timeline.get("actualMaturityDate").isJsonNull() || timeline.get("actualMaturityDate") == null,
                    "Expected actualMaturityDate to be null after disbursement");
            assertTrue(timeline.has("disbursementDetails") && timeline.get("disbursementDetails").isJsonArray(),
                    "timeline should include disbursementDetails list");
            assertFalse(timeline.getAsJsonArray("disbursementDetails").isEmpty(), "timeline disbursementDetails should not be empty");
        }
        assertTrue(data.has("disbursementDetails") && data.get("disbursementDetails").isJsonArray(),
                "GET loan after disburse should include disbursementDetails array");
        assertFalse(data.getAsJsonArray("disbursementDetails").isEmpty(), "disbursementDetails should not be empty");
        final JsonObject disbursement = data.getAsJsonArray("disbursementDetails").get(0).getAsJsonObject();
        assertTrue(disbursement.has("expectedDisbursementDate"), "disbursementDetails should include expectedDisbursementDate");
        assertTrue(disbursement.has("expectedAmount"), "disbursementDetails should include expectedAmount");
        assertTrue(disbursement.has("actualDisbursementDate"));
        assertDateEquals(actualDisbursementDate, disbursement.get("actualDisbursementDate"));
        assertTrue(disbursement.has("actualAmount"));
        assertEqualBigDecimal(transactionAmount, disbursement.get("actualAmount"));

        assertTrue(data.has("transactions") && data.get("transactions").isJsonArray());
        assertEquals(1, data.getAsJsonArray("transactions").size());
        final JsonObject txn = data.getAsJsonArray("transactions").get(0).getAsJsonObject();
        assertEqualBigDecimal(transactionAmount, txn.get("transactionAmount"));
        assertTrue(txn.has("principalPortion"), "Transaction should include allocation principalPortion");
        assertEqualBigDecimal(transactionAmount, txn.get("principalPortion"));
        assertTrue(txn.has("feeChargesPortion"), "Transaction should include allocation feeChargesPortion");
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("feeChargesPortion"));
        assertTrue(txn.has("penaltyChargesPortion"), "Transaction should include allocation penaltyChargesPortion");
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("penaltyChargesPortion"));
        assertTrue(txn.has("paymentDetailData") && !txn.get("paymentDetailData").isJsonNull(),
                "Transaction should include paymentDetailData");
        final JsonObject paymentDetailData = txn.getAsJsonObject("paymentDetailData");
        assertEquals(accountNumber, paymentDetailData.get("accountNumber").getAsString());
        assertEquals(checkNumber, paymentDetailData.get("checkNumber").getAsString());
        assertEquals(routingCode, paymentDetailData.get("routingCode").getAsString());
        assertEquals(receiptNumber, paymentDetailData.get("receiptNumber").getAsString());
        assertEquals(bankNumber, paymentDetailData.get("bankNumber").getAsString());
    }

    @Test
    public void testUndoDisburseWorkingCapitalLoan() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate actualDisbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(actualDisbursementDate, BigDecimal.valueOf(5000)));

        applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson());

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = JsonParser.parseString(response).getAsJsonObject();
        assertStatus(data, "loanStatusType.approved");
        assertEqualBigDecimal(BigDecimal.valueOf(5000), data.get("approvedPrincipal"));
        assertTrue(data.has("balance") && !data.get("balance").isJsonNull(), "GET loan after undo should include balance");
        assertEqualBigDecimal(BigDecimal.valueOf(5000), data.getAsJsonObject("balance").get("principalOutstanding"));

        assertTrue(data.has("disbursementDetails") && data.get("disbursementDetails").isJsonArray(),
                "GET loan after undo should include disbursementDetails array");
        assertFalse(data.getAsJsonArray("disbursementDetails").isEmpty(), "disbursementDetails should not be empty");
        final JsonObject disbursement = data.getAsJsonArray("disbursementDetails").get(0).getAsJsonObject();
        assertTrue(!disbursement.has("actualDisbursementDate") || disbursement.get("actualDisbursementDate").isJsonNull(),
                "Expected actualDisbursementDate to be absent or null after undo");
        assertTrue(!disbursement.has("actualAmount") || disbursement.get("actualAmount").isJsonNull(),
                "Expected actualAmount to be absent or null after undo");
        assertTrue(data.has("timeline") && !data.get("timeline").isJsonNull(), "GET loan after undo should include timeline");
        final JsonObject timeline = data.getAsJsonObject("timeline");
        assertTrue(timeline.has("actualMaturityDate"), "timeline should include actualMaturityDate (null until fully paid)");
        assertTrue(timeline.get("actualMaturityDate").isJsonNull() || timeline.get("actualMaturityDate") == null,
                "Expected actualMaturityDate to be null after undo");

        assertTrue(data.has("transactions") && data.get("transactions").isJsonArray(), "Expected transactions array in response");
        assertEquals(1, data.getAsJsonArray("transactions").size(), "Undo disburse should keep transaction history");
        final JsonObject txn = data.getAsJsonArray("transactions").get(0).getAsJsonObject();
        assertTrue(txn.has("reversed") && txn.get("reversed").getAsBoolean(), "Expected transaction to be reversed");
    }

    @Test
    public void testUndoDisbursalWithNote() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000)));

        applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson("Undo disbursal note"));

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = JsonParser.parseString(response).getAsJsonObject();
        assertStatus(data, "loanStatusType.approved");
    }

    @Test
    public void testUpdateDiscountAfterDisbursement() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String businessDate = disbursementDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        BusinessDateHelper.runAt(businessDate, () -> applicationHelper.updateDiscountById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(25), "post-disburse")));

        final JsonObject data = JsonParser.parseString(applicationHelper.retrieveById(loanId)).getAsJsonObject();
        assertEqualBigDecimal(BigDecimal.valueOf(25), data.get("discount"));
        assertEqualBigDecimal(BigDecimal.valueOf(5025), data.getAsJsonObject("balance").get("principalOutstanding"));

        final CallFailedRuntimeException[] secondAddHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(businessDate, () -> secondAddHolder[0] = applicationHelper.runUpdateDiscountByIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(20), null)));
        final CallFailedRuntimeException secondAddEx = secondAddHolder[0];
        assertEquals(400, secondAddEx.getStatus());
        assertNotNull(secondAddEx.getDeveloperMessage());
        assertTrue(secondAddEx.getDeveloperMessage().contains("discount") || secondAddEx.getDeveloperMessage().contains("already"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementWithDateDifferentFromDisbursementDateFails() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String wrongBusinessDate = disbursementDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(wrongBusinessDate, () -> exHolder[0] = applicationHelper.runUpdateDiscountByIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(25), null)));
        final CallFailedRuntimeException ex = exHolder[0];
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("transaction.date.must.be.equal.disbursement.date"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementFailsIfDiscountWasAlreadySetBeforeDisbursement() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.valueOf(40)) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String businessDate = disbursementDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(businessDate, () -> exHolder[0] = applicationHelper.runUpdateDiscountByIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(20), null)));
        final CallFailedRuntimeException ex = exHolder[0];
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("discount") || ex.getDeveloperMessage().contains("already set before disbursement"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementFailsWhenDiscountWasSetAtApproval() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(40)));

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String businessDate = disbursementDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(businessDate, () -> exHolder[0] = applicationHelper.runUpdateDiscountByIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(20), null)));
        final CallFailedRuntimeException ex = exHolder[0];
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("discount") || ex.getDeveloperMessage().contains("already set before disbursement"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementFailsWhenProductDisallowsDiscountOverride() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String businessDate = disbursementDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(businessDate, () -> exHolder[0] = applicationHelper.runUpdateDiscountByIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(20), null)));
        final CallFailedRuntimeException ex = exHolder[0];
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("override.not.allowed.by.product"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementByExternalId() {
        final Long productId = createProductWithDiscountAllowed();
        final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String businessDate = disbursementDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        BusinessDateHelper.runAt(businessDate, () -> applicationHelper.updateDiscountByExternalId(loanExternalId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(25), "post-disburse")));

        final JsonObject data = JsonParser.parseString(applicationHelper.retrieveById(loanId)).getAsJsonObject();
        assertEqualBigDecimal(BigDecimal.valueOf(25), data.get("discount"));
        assertEqualBigDecimal(BigDecimal.valueOf(5025), data.getAsJsonObject("balance").get("principalOutstanding"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementByExternalIdFailsWhenBusinessDateDifferentFromDisbursementDate() {
        final Long productId = createProductWithDiscountAllowed();
        final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String wrongBusinessDate = disbursementDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(wrongBusinessDate,
                () -> exHolder[0] = applicationHelper.runUpdateDiscountByExternalIdExpectingFailure(loanExternalId,
                        WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(25), null)));

        final CallFailedRuntimeException ex = exHolder[0];
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("transaction.date.must.be.equal.disbursement.date"));
    }

    @Test
    public void testUpdateDiscountAfterDisbursementByExternalIdFailsIfDiscountWasAlreadySetBeforeDisbursement() {
        final Long productId = createProductWithDiscountAllowed();
        final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.valueOf(40)) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));

        final String businessDate = disbursementDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(businessDate,
                () -> exHolder[0] = applicationHelper.runUpdateDiscountByExternalIdExpectingFailure(loanExternalId,
                        WorkingCapitalLoanDisbursementTestBuilder.buildUpdateDiscountJson(BigDecimal.valueOf(20), null)));

        final CallFailedRuntimeException ex = exHolder[0];
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("discount") || ex.getDeveloperMessage().contains("already set before disbursement"));
    }

    @Test
    public void testDisburseWithMissingActualDisbursementDate() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(null, BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("actualDisbursementDate")
                && (ex.getDeveloperMessage().contains("mandatory") || ex.getDeveloperMessage().contains("null")));
    }

    @Test
    public void testDisburseWithMissingTransactionAmount() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                null);
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(
                ex.getDeveloperMessage().contains("transactionAmount")
                        && (ex.getDeveloperMessage().contains("mandatory") || ex.getDeveloperMessage().contains("null")),
                "Expected message about mandatory transactionAmount: " + ex.getDeveloperMessage());
    }

    @Test
    public void testDisburseWithTransactionAmountExceedingApproved() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(6000));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("amount.cannot.exceed.approved.principal"));
    }

    @Test
    public void testDisburseWithNegativeTransactionAmount() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(-100));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().toLowerCase().contains("transactionamount") || ex.getDeveloperMessage().contains("positive")
                || ex.getDeveloperMessage().contains("greater"));
    }

    @Test
    public void testDisburseWithFutureDate() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate futureDate = LocalDate.now(ZoneId.systemDefault()).plusDays(30);
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(futureDate, BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("future.date") || ex.getDeveloperMessage().contains("actualDisbursementDate"));
    }

    @Test
    public void testDisburseWithDateBeforeApproval() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate beforeApproval = approvedOnDate.minusDays(1);
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(beforeApproval, BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("before.approval") || ex.getDeveloperMessage().contains("actualDisbursementDate"));
    }

    @Test
    public void testDisburseWithActualDateBeforeSubmittedDate() {
        final Long productId = createProduct();

        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withSubmittedOnDate(submittedOnDate) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(submittedOnDate, BigDecimal.valueOf(5000), null));

        final LocalDate actualDateBeforeSubmitted = submittedOnDate.minusDays(1);
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(actualDateBeforeSubmitted,
                BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("submitted") || ex.getDeveloperMessage().contains("actualDisbursementDate"),
                "Expected message about actual date before submitted: " + ex.getDeveloperMessage());
    }

    @Test
    public void testDisburseWithNoteExceedingLength() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));

        final String longNote = "a".repeat(1001);
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000), null, longNote, null, null, null, null, null, null, null);
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("note") || ex.getDeveloperMessage().toLowerCase().contains("length"),
                "Expected message about note length: " + ex.getDeveloperMessage());
    }

    @Test
    public void testDisburseWithDiscountExceedingCreated() {
        final Long productId = createProductWithDiscountAllowed();

        final BigDecimal approvedPrincipal = BigDecimal.valueOf(5000);
        final BigDecimal approvedDiscount = BigDecimal.valueOf(20);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(approvedPrincipal) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), approvedPrincipal, approvedDiscount));

        final BigDecimal discountAmountExceeding = BigDecimal.valueOf(25);
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                approvedPrincipal, discountAmountExceeding, null, null, null, null, null, null, null);
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("discount") && ex.getDeveloperMessage().contains("exceed"));
    }

    @Test
    public void testDisburseWithDuplicateTransactionExternalId() {
        final Long productId = createProduct();

        final String sharedExternalId = "wcl-txn-ext-" + UUID.randomUUID();

        final Long loanId1 = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());
        applicationHelper.approveById(loanId1, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId1,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000),
                        null, null, null, null, null, null, null, null, sharedExternalId));

        final Long loanId2 = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(3000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());
        applicationHelper.approveById(loanId2, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(3000), null));

        final String disburseJson2 = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(3000), null, null, null, null, null, null, null, null, sharedExternalId);
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId2, disburseJson2);
        assertEquals(400, ex.getStatus());
        assertTrue(ex.getDeveloperMessage().contains("externalId") && ex.getDeveloperMessage().toLowerCase().contains("already"),
                "Expected duplicate transaction externalId error: " + ex.getDeveloperMessage());
    }

    @Test
    public void testDisburseWhenLoanNotApproved() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("Transition") || ex.getDeveloperMessage().contains("not allowed")
                || ex.getDeveloperMessage().contains("status"));
    }

    @Test
    public void testDisburseNonExistentLoan() {
        final long nonExistentLoanId = 999_999_999L;
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> applicationHelper.disburseById(nonExistentLoanId, disburseJson));
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testUndoDisbursalWhenLoanNotDisbursed() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));

        final CallFailedRuntimeException ex = applicationHelper.runUndoDisbursalExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson());
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("Transition") || ex.getDeveloperMessage().contains("not allowed")
                || ex.getDeveloperMessage().contains("status"));
    }

    @Test
    public void testUndoDisbursalNonExistentLoan() {
        final long nonExistentLoanId = 999_999_999L;
        final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class, () -> applicationHelper
                .undoDisbursalById(nonExistentLoanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson()));
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testUndoDisbursalWithNoteExceedingLength() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000)));

        final String longNote = "a".repeat(1001);
        final CallFailedRuntimeException ex = applicationHelper.runUndoDisbursalExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson(longNote));
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("note") || ex.getDeveloperMessage().toLowerCase().contains("length"),
                "Expected message about note length: " + ex.getDeveloperMessage());
    }

    @Test
    public void testGetTransactionsListAfterDisburse() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000)));

        final String json = applicationHelper.retrieveTransactionsByLoanIdRaw(loanId);
        assertNotNull(json);
        final JsonObject page = JsonParser.parseString(json).getAsJsonObject();
        assertTrue(page.has("content"), "Response should have content array");
        assertTrue(page.has("totalElements"));
        final JsonArray content = page.getAsJsonArray("content");
        assertEquals(1, content.size(), "After one disburse there should be one transaction");
        assertEquals(1L, page.get("totalElements").getAsLong());
        final JsonObject txn = content.get(0).getAsJsonObject();
        assertTrue(txn.has("id") && txn.has("type") && txn.has("transactionAmount"));
        assertEquals("loanTransactionType.disbursement", txn.getAsJsonObject("type").get("code").getAsString());
        assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.get("transactionAmount"));
        assertTrue(txn.has("principalPortion"));
        assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.get("principalPortion"));
        assertTrue(txn.has("feeChargesPortion"));
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("feeChargesPortion"));
        assertTrue(txn.has("penaltyChargesPortion"));
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("penaltyChargesPortion"));
        assertFalse(txn.get("reversed").getAsBoolean());
    }

    @Test
    public void testGetTransactionByIdAfterDisburse() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(6000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(6000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(6000)));

        final String listJson = applicationHelper.retrieveTransactionsByLoanIdRaw(loanId);
        final JsonArray content = JsonParser.parseString(listJson).getAsJsonObject().getAsJsonArray("content");
        assertEquals(1, content.size());
        final long transactionId = content.get(0).getAsJsonObject().get("id").getAsLong();

        final String txnJson = applicationHelper.retrieveTransactionByLoanIdAndTransactionIdRaw(loanId, transactionId);
        final JsonObject txn = JsonParser.parseString(txnJson).getAsJsonObject();
        assertEquals(transactionId, txn.get("id").getAsLong());
        assertEqualBigDecimal(BigDecimal.valueOf(6000), txn.get("transactionAmount"));
        assertEqualBigDecimal(BigDecimal.valueOf(6000), txn.get("principalPortion"));
        assertTrue(txn.has("transactionDate") && txn.has("reversed"));
        assertTrue(txn.has("type"), "GET transaction should include type");
        assertEquals("loanTransactionType.disbursement", txn.getAsJsonObject("type").get("code").getAsString());
        assertTrue(txn.has("submittedOnDate"), "GET transaction should include submittedOnDate");
        assertFalse(txn.has("interestPortion"), "WCL has no interest");
        assertTrue(txn.has("feeChargesPortion"), "GET transaction should include allocation feeChargesPortion");
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("feeChargesPortion"));
        assertTrue(txn.has("penaltyChargesPortion"), "GET transaction should include allocation penaltyChargesPortion");
        assertEqualBigDecimal(BigDecimal.ZERO, txn.get("penaltyChargesPortion"));

    }

    @Test
    public void testGetTransactionsListEmptyWhenNotDisbursed() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));

        final String json = applicationHelper.retrieveTransactionsByLoanIdRaw(loanId);
        final JsonObject page = JsonParser.parseString(json).getAsJsonObject();
        assertTrue(page.has("content"));
        assertTrue(page.getAsJsonArray("content").isEmpty(), "Before disburse transactions list should be empty");

    }

    @Test
    public void testGetTransactionByNonExistentIdReturns404() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000)));

        final long nonExistentTransactionId = 999_999L;
        final CallFailedRuntimeException ex = applicationHelper.runRetrieveTransactionByLoanIdAndTransactionIdExpectingFailure(loanId,
                nonExistentTransactionId);
        assertEquals(404, ex.getStatus());
    }

    @Test
    public void testGetTransactionsByLoanExternalId() {
        final Long productId = createProduct();

        final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000)));

        final String json = applicationHelper.retrieveTransactionsByLoanExternalIdRaw(loanExternalId);
        assertNotNull(json);
        final JsonObject page = JsonParser.parseString(json).getAsJsonObject();
        assertTrue(page.has("content") && page.has("totalElements"));
        final JsonArray content = page.getAsJsonArray("content");
        assertEquals(1, content.size());
        final JsonObject txn = content.get(0).getAsJsonObject();
        assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.get("transactionAmount"));
        assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.get("principalPortion"));
    }

    @Test
    public void testGetTransactionByLoanIdAndTransactionExternalId() {
        final Long productId = createProduct();

        final String txnExternalId = "wcl-txn-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(7000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(7000), null));
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(7000),
                        null, null, null, null, null, null, null, null, txnExternalId));

        final String txnJson = applicationHelper.retrieveTransactionByLoanIdAndTransactionExternalIdRaw(loanId, txnExternalId);
        final JsonObject txn = JsonParser.parseString(txnJson).getAsJsonObject();
        assertEqualBigDecimal(BigDecimal.valueOf(7000), txn.get("transactionAmount"));
        assertEqualBigDecimal(BigDecimal.valueOf(7000), txn.get("principalPortion"));
        assertTrue(txn.has("externalId") && txnExternalId.equals(txn.get("externalId").getAsString()));
    }

    @Test
    public void testStateTransitionByLoanExternalId_ApproveAndDisburse() {
        final Long productId = createProduct();

        final String loanExternalId = "wcl-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        final String approveJson = WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000), null);
        applicationHelper.approveByExternalId(loanExternalId, approveJson);

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000));
        applicationHelper.disburseByExternalId(loanExternalId, disburseJson);

        final String response = applicationHelper.retrieveById(loanId);
        final JsonObject data = JsonParser.parseString(response).getAsJsonObject();
        assertStatus(data, "loanStatusType.active");
        assertTrue(data.has("balance") && !data.get("balance").isJsonNull(), "GET loan after disburse should include balance");
        assertEqualBigDecimal(BigDecimal.valueOf(5000), data.getAsJsonObject("balance").get("principalOutstanding"));
    }

    @Test
    public void testGetTransactionByExternalLoanIdAndTransactionId() {
        final Long productId = createProduct();

        final String loanExternalId = "wcl-lext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(8000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(8000), null));
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(8000)));

        final String listJson = applicationHelper.retrieveTransactionsByLoanIdRaw(loanId);
        final long transactionId = JsonParser.parseString(listJson).getAsJsonObject().getAsJsonArray("content").get(0).getAsJsonObject()
                .get("id").getAsLong();

        final String txnJson = applicationHelper.retrieveTransactionByExternalLoanIdAndTransactionIdRaw(loanExternalId, transactionId);
        final JsonObject txn = JsonParser.parseString(txnJson).getAsJsonObject();
        assertEquals(transactionId, txn.get("id").getAsLong());
        assertEqualBigDecimal(BigDecimal.valueOf(8000), txn.get("transactionAmount"));
    }

    @Test
    public void testGetTransactionByExternalLoanIdAndTransactionExternalId() {
        final Long productId = createProduct();

        final String loanExternalId = "wcl-lext2-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final String txnExternalId = "wcl-text-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(9000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withExternalId(loanExternalId) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(9000), null));
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(9000),
                        null, null, null, null, null, null, null, null, txnExternalId));

        final String txnJson = applicationHelper.retrieveTransactionByExternalLoanIdAndTransactionExternalIdRaw(loanExternalId,
                txnExternalId);
        final JsonObject txn = JsonParser.parseString(txnJson).getAsJsonObject();
        assertEqualBigDecimal(BigDecimal.valueOf(9000), txn.get("transactionAmount"));
        assertEquals(txnExternalId, txn.get("externalId").getAsString());
    }

    @Test
    public void testDisburseWithInvalidPaymentTypeId() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));

        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000), null, null, 0, null, null, null, null, null, null);
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("paymentTypeId") || ex.getDeveloperMessage().toLowerCase().contains("payment"),
                "Expected message about invalid paymentTypeId: " + ex.getDeveloperMessage());
    }

    @Test
    public void testDisburseWithPaymentDetailsStringExceedingLength() {
        final Long productId = createProduct();

        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));

        final String longAccountNumber = "a".repeat(51);
        final String disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(LocalDate.now(ZoneId.systemDefault()),
                BigDecimal.valueOf(5000), null, null, null, longAccountNumber, null, null, null, null, null);
        final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("accountNumber") || ex.getDeveloperMessage().toLowerCase().contains("length"),
                "Expected message about accountNumber length: " + ex.getDeveloperMessage());
    }

    @Test
    public void testDisburseGeneratesAmortizationSchedule() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final BigDecimal disbursementAmount = BigDecimal.valueOf(5000);
        final BigDecimal discountAmount = BigDecimal.valueOf(25);
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate,
                disbursementAmount, discountAmount, null, null, null, null, null, null, null));

        final JsonObject schedule = retrieveAmortizationScheduleByLoanId(loanId);
        assertDateEquals(disbursementDate, schedule.get("expectedDisbursementDate"));
        assertEqualBigDecimal(disbursementAmount, schedule.get("netDisbursementAmount"));
        assertEqualBigDecimal(discountAmount, schedule.get("originationFeeAmount"));
        assertTrue(schedule.has("payments") && schedule.get("payments").isJsonArray(), "Schedule should contain payments");
        assertFalse(schedule.getAsJsonArray("payments").isEmpty(), "Schedule payments should not be empty after disburse");
    }

    @Test
    public void testUndoDisbursalRegeneratesAmortizationScheduleToExpectedDate() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(createdClientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .buildSubmitJson());

        final JsonObject beforeDisburse = JsonParser.parseString(applicationHelper.retrieveById(loanId)).getAsJsonObject();
        final JsonObject firstDisbursementDetail = beforeDisburse.getAsJsonArray("disbursementDetails").get(0).getAsJsonObject();
        final LocalDate expectedDateBeforeDisburse = parseDate(firstDisbursementDetail.get("expectedDisbursementDate"));

        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder
                .buildApproveJson(LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(5000), null));

        final LocalDate actualDisbursementDate = LocalDate.now(ZoneId.systemDefault());
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(actualDisbursementDate, BigDecimal.valueOf(5000)));
        final JsonObject scheduleAfterDisburse = retrieveAmortizationScheduleByLoanId(loanId);
        assertDateEquals(actualDisbursementDate, scheduleAfterDisburse.get("expectedDisbursementDate"));

        applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson());

        final JsonObject scheduleAfterUndo = retrieveAmortizationScheduleByLoanId(loanId);
        assertDateEquals(expectedDateBeforeDisburse, scheduleAfterUndo.get("expectedDisbursementDate"));
        assertTrue(scheduleAfterUndo.has("payments") && scheduleAfterUndo.get("payments").isJsonArray(),
                "Schedule should still exist after undo");
        assertFalse(scheduleAfterUndo.getAsJsonArray("payments").isEmpty(), "Schedule payments should not be empty after undo");
    }

    private static void assertStatus(final JsonObject data, final String expectedStatusCode) {
        assertTrue(data.has("status") && !data.get("status").isJsonNull());
        assertEquals(expectedStatusCode, data.getAsJsonObject("status").get("code").getAsString());
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final JsonElement actual) {
        assertNotNull(actual, "Expected value for field");
        assertFalse(actual.isJsonNull(), "Expected non-null value");
        assertEquals(0, expected.compareTo(actual.getAsJsonPrimitive().getAsBigDecimal()),
                "Expected " + expected + " but got " + actual.getAsString());
    }

    private static void assertDateEquals(final LocalDate expected, final JsonElement actual) {
        assertNotNull(actual, "Expected date value");
        assertFalse(actual.isJsonNull(), "Expected non-null date");
        if (actual.isJsonArray()) {
            final JsonArray arr = actual.getAsJsonArray();
            assertEquals(expected.getYear(), arr.get(0).getAsInt());
            assertEquals(expected.getMonthValue(), arr.get(1).getAsInt());
            assertEquals(expected.getDayOfMonth(), arr.get(2).getAsInt());
        } else {
            assertEquals(expected.format(DateTimeFormatter.ISO_LOCAL_DATE), actual.getAsString());
        }
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithDiscountAllowed() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withAllowAttributeOverrides(java.util.Map.of("discountDefault", Boolean.TRUE)) //
                .build()) //
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private JsonObject retrieveAmortizationScheduleByLoanId(final Long loanId) {
        final String json = applicationHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static LocalDate parseDate(final JsonElement dateElement) {
        if (dateElement == null || dateElement.isJsonNull()) {
            return null;
        }
        if (dateElement.isJsonArray()) {
            final JsonArray arr = dateElement.getAsJsonArray();
            return LocalDate.of(arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt());
        }
        return LocalDate.parse(dateElement.getAsString());
    }

    private Long submitAndTrack(final String submitJson) {
        final Long loanId = applicationHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
