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
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanCreditBalanceRefundTest {

    private static final String WC_CBR_TXN_EVENT = "WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent";
    private static final String CLEANUP_EMPTY_COMMAND_JSON = "{\"locale\":\"en\",\"dateFormat\":\"yyyy-MM-dd\"}";

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final FeignExternalEventHelper externalEventHelper = new FeignExternalEventHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final Long createdClientId = createClient();

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseJson());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.undoApprovalById(loanId, CLEANUP_EMPTY_COMMAND_JSON);
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
    }

    @Test
    public void testCreditBalanceRefundUpdatesBalanceAndTransaction() {
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createOverpaidLoan(BigDecimal.valueOf(200), disbursementDate);
        final JsonObject balanceBeforeRefund = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject()
                .getAsJsonObject("balance");
        final LocalDate refundDate = disbursementDate.plusDays(2);
        BusinessDateHelper.runAt(refundDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeCreditBalanceRefundByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildCreditBalanceRefundJson(refundDate, BigDecimal.valueOf(150), null, "cbr", 1, "cbr-account")));

        final JsonObject loanData = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject();

        assertTrue(loanData.has("status") && !loanData.get("status").isJsonNull());
        assertEquals("loanStatusType.overpaid", loanData.getAsJsonObject("status").get("code").getAsString());

        final JsonObject balance = loanData.getAsJsonObject("balance");
        assertNotNull(balance);
        assertEqualBigDecimal(balanceBeforeRefund.get("principalOutstanding").getAsBigDecimal(), balance.get("principalOutstanding"));
        assertEqualBigDecimal(balanceBeforeRefund.get("totalPaidPrincipal").getAsBigDecimal(), balance.get("totalPaidPrincipal"));
        assertEqualBigDecimal(balanceBeforeRefund.get("totalPayment").getAsBigDecimal(), balance.get("totalPayment"));
        assertEqualBigDecimal(balanceBeforeRefund.get("realizedIncome").getAsBigDecimal(), balance.get("realizedIncome"));
        assertEqualBigDecimal(balanceBeforeRefund.get("unrealizedIncome").getAsBigDecimal(), balance.get("unrealizedIncome"));
        assertEqualBigDecimal(balanceBeforeRefund.get("overpaymentAmount").getAsBigDecimal().subtract(BigDecimal.valueOf(150)),
                balance.get("overpaymentAmount"));
        final JsonArray content = JsonParser.parseString(loanHelper.retrieveTransactionsByLoanIdRaw(loanId)).getAsJsonObject()
                .getAsJsonArray("content");
        assertEquals(3, content.size());
    }

    @Test
    public void testCreditBalanceRefundCanCloseLoanWhenRefundedFully() {
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createOverpaidLoan(BigDecimal.valueOf(100), disbursementDate);
        final JsonObject balanceBeforeRefund = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject()
                .getAsJsonObject("balance");
        final LocalDate refundDate = disbursementDate.plusDays(2);
        BusinessDateHelper.runAt(refundDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeCreditBalanceRefundByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildCreditBalanceRefundJson(refundDate, BigDecimal.valueOf(100), null, "cbr-close", 1, "cbr-account")));

        final JsonObject loanData = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject();

        assertTrue(loanData.has("status") && !loanData.get("status").isJsonNull());
        assertEquals("loanStatusType.closed.obligations.met", loanData.getAsJsonObject("status").get("code").getAsString());

        final JsonObject balance = loanData.getAsJsonObject("balance");
        assertNotNull(balance);
        assertEqualBigDecimal(balanceBeforeRefund.get("principalOutstanding").getAsBigDecimal(), balance.get("principalOutstanding"));
        assertEqualBigDecimal(balanceBeforeRefund.get("totalPaidPrincipal").getAsBigDecimal(), balance.get("totalPaidPrincipal"));
        assertEqualBigDecimal(balanceBeforeRefund.get("totalPayment").getAsBigDecimal(), balance.get("totalPayment"));
        assertEqualBigDecimal(balanceBeforeRefund.get("realizedIncome").getAsBigDecimal(), balance.get("realizedIncome"));
        assertEqualBigDecimal(balanceBeforeRefund.get("unrealizedIncome").getAsBigDecimal(), balance.get("unrealizedIncome"));
        assertEqualBigDecimal(BigDecimal.ZERO, balance.get("overpaymentAmount"));
    }

    @Test
    public void testCreditBalanceRefundWhenLoanNotOverpaidFails() {
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException ex = loanHelper.runCreditBalanceRefundByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildCreditBalanceRefundJson(approvedOnDate.plusDays(1), BigDecimal.valueOf(50),
                        null, null, null, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testCreditBalanceRefundAmountExceedingOverpaymentFails() {
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createOverpaidLoan(BigDecimal.valueOf(100), disbursementDate);
        final CallFailedRuntimeException ex = loanHelper.runCreditBalanceRefundByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildCreditBalanceRefundJson(disbursementDate.plusDays(2),
                        BigDecimal.valueOf(101), null, null, null, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testCreditBalanceRefundRaisesExternalBusinessEvents() {
        externalEventHelper.enableBusinessEvent(WC_CBR_TXN_EVENT);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createOverpaidLoan(BigDecimal.valueOf(100), disbursementDate);
        final LocalDate refundDate = disbursementDate.plusDays(2);
        BusinessDateHelper.runAt(refundDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            externalEventHelper.deleteAllExternalEvents();
            loanHelper.makeCreditBalanceRefundByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                    .buildCreditBalanceRefundJson(refundDate, BigDecimal.valueOf(100), null, "cbr-events", 1, "cbr-account"));
        });

        assertFalse(externalEventHelper.getExternalEventsByType(WC_CBR_TXN_EVENT).isEmpty());

        externalEventHelper.disableBusinessEvent(WC_CBR_TXN_EVENT);
    }

    @Test
    public void testCreditBalanceRefundWithNullPaymentDetailsDoesNotFailWith500() {
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createOverpaidLoan(BigDecimal.valueOf(100), disbursementDate);
        final JsonObject balanceBeforeRefund = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject()
                .getAsJsonObject("balance");
        final JsonObject payload = JsonParser
                .parseString(WorkingCapitalLoanDisbursementTestBuilder.buildCreditBalanceRefundJson(disbursementDate.plusDays(2),
                        BigDecimal.valueOf(50), null, "cbr-null-payment-details", null, null))
                .getAsJsonObject();
        payload.add("paymentDetails", null);

        BusinessDateHelper.runAt(disbursementDate.plusDays(2).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeCreditBalanceRefundByLoanId(loanId, payload.toString()));

        final JsonObject loanData = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject();

        assertTrue(loanData.has("status") && !loanData.get("status").isJsonNull());
        assertEquals("loanStatusType.overpaid", loanData.getAsJsonObject("status").get("code").getAsString());

        final JsonObject balance = loanData.getAsJsonObject("balance");
        assertNotNull(balance);
        assertEqualBigDecimal(balanceBeforeRefund.get("principalOutstanding").getAsBigDecimal(), balance.get("principalOutstanding"));
        assertEqualBigDecimal(balanceBeforeRefund.get("totalPaidPrincipal").getAsBigDecimal(), balance.get("totalPaidPrincipal"));
        assertEqualBigDecimal(balanceBeforeRefund.get("totalPayment").getAsBigDecimal(), balance.get("totalPayment"));
        assertEqualBigDecimal(balanceBeforeRefund.get("realizedIncome").getAsBigDecimal(), balance.get("realizedIncome"));
        assertEqualBigDecimal(balanceBeforeRefund.get("unrealizedIncome").getAsBigDecimal(), balance.get("unrealizedIncome"));
        assertEqualBigDecimal(balanceBeforeRefund.get("overpaymentAmount").getAsBigDecimal().subtract(BigDecimal.valueOf(50)),
                balance.get("overpaymentAmount"));
    }

    @Test
    public void testCreditBalanceRefundWithDuplicateTransactionExternalIdFails() {
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        final String sharedExternalId = "wcl-cbr-ext-" + UUID.randomUUID();
        final Long loanId1 = createOverpaidLoan(BigDecimal.valueOf(100), disbursementDate);
        final Long loanId2 = createOverpaidLoan(BigDecimal.valueOf(150), disbursementDate);
        final LocalDate refundDate = disbursementDate.plusDays(2);

        BusinessDateHelper
                .runAt(refundDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        () -> loanHelper.makeCreditBalanceRefundByLoanId(loanId1,
                                WorkingCapitalLoanDisbursementTestBuilder.buildTransactionJson(refundDate, BigDecimal.valueOf(50), null,
                                        "cbr-ext-1", 1, "cbr-account-1", sharedExternalId)));
        final CallFailedRuntimeException ex = loanHelper.runCreditBalanceRefundByLoanIdExpectingFailure(loanId2,
                WorkingCapitalLoanDisbursementTestBuilder.buildTransactionJson(refundDate, BigDecimal.valueOf(50), null, "cbr-ext-2", 1,
                        "cbr-account-2", sharedExternalId));
        assertEquals(400, ex.getStatus());
    }

    private Long createOverpaidLoan(final BigDecimal overpaymentAmount, final LocalDate disbursementDate) {
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                disbursementDate);
        final LocalDate repaymentDate = disbursementDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(repaymentDate,
                        BigDecimal.valueOf(5000).add(overpaymentAmount), null, "repayment-for-cbr", 1, "repayment-account")));
        return loanId;
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final BigDecimal disburseAmount,
            final LocalDate approvedOnDate) {
        final Long loanId = submitAndTrackLoan(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(principal).withPeriodPaymentRate(BigDecimal.ONE).buildSubmitJson());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, principal, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(approvedOnDate, disburseAmount));
        return loanId;
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

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private Long submitAndTrackLoan(final String submitJson) {
        final Long loanId = loanHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final JsonElement actual) {
        assertNotNull(actual);
        assertFalse(actual.isJsonNull());
        assertEquals(0, expected.compareTo(actual.getAsJsonPrimitive().getAsBigDecimal()));
    }
}
