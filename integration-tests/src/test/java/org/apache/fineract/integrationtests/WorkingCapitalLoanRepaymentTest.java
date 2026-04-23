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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
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

public class WorkingCapitalLoanRepaymentTest {

    private static final String WC_REPAYMENT_TXN_EVENT = "WorkingCapitalLoanRepaymentTransactionBusinessEvent";
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
    public void testRepaymentUpdatesTransactionAllocationBalanceAndStatus() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).withDiscount(BigDecimal.valueOf(100)).buildSubmitJson());
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000),
                BigDecimal.valueOf(100)));
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault());
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(5000)));
        final LocalDate repaymentDate = disbursementDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(repaymentDate,
                        BigDecimal.valueOf(5200), null, "repayment", 1, "repayment-account")));

        final JsonObject loanData = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject();
        assertStatus(loanData, "loanStatusType.overpaid");
        assertEqualBigDecimal(BigDecimal.ZERO, loanData.getAsJsonObject("balance").get("principalOutstanding"));
        assertEqualBigDecimal(BigDecimal.valueOf(100), loanData.getAsJsonObject("balance").get("overpaymentAmount"));
        final JsonArray content = JsonParser.parseString(loanHelper.retrieveTransactionsByLoanIdRaw(loanId)).getAsJsonObject()
                .getAsJsonArray("content");
        assertEquals(2, content.size());
    }

    @Test
    public void testRepaymentRaisesExternalBusinessEvent() {
        externalEventHelper.enableBusinessEvent(WC_REPAYMENT_TXN_EVENT);
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson());
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(approvedOnDate, BigDecimal.valueOf(5000)));
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            externalEventHelper.deleteAllExternalEvents();
            loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(repaymentDate,
                    BigDecimal.valueOf(100), null, "repayment", 1, "repayment-account"));
        });
        final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(WC_REPAYMENT_TXN_EVENT);
        assertFalse(events.isEmpty());
        final Map<?, ?> payload = events.getFirst().getPayLoad();
        assertEquals(loanId.doubleValue(), ((Number) payload.get("wcLoanId")).doubleValue());
        externalEventHelper.disableBusinessEvent(WC_REPAYMENT_TXN_EVENT);
    }

    @Test
    public void testRepaymentWithMissingTransactionDateFails() {
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(null, BigDecimal.valueOf(100), null, null, null, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentWithMissingTransactionAmountFails() {
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentJson(approvedOnDate.plusDays(1), null, null, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentWithFutureDateFails() {
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(LocalDate.now(ZoneId.systemDefault()).plusDays(30),
                        BigDecimal.valueOf(100), null, null, null, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentWithInvalidClassificationIdFails() {
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentJson(approvedOnDate.plusDays(1), BigDecimal.valueOf(100), 0L, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentWhenLoanNotDisbursedFails() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE).buildSubmitJson());
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(approvedOnDate, BigDecimal.valueOf(100), null, null, null,
                        null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentWithDateBeforeDisbursementFails() {
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE).buildSubmitJson());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(approvedOnDate, BigDecimal.valueOf(5000)));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentJson(approvedOnDate.minusDays(1), BigDecimal.valueOf(100), null, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentWithNegativeAmountFails() {
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentJson(approvedOnDate.plusDays(1), BigDecimal.valueOf(-100), null, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentByExternalId() {
        final Long productId = createProduct();
        final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withExternalId(loanExternalId).buildSubmitJson());
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(approvedOnDate, BigDecimal.valueOf(5000)));
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanExternalId(loanExternalId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentJson(repaymentDate, BigDecimal.valueOf(100), null, "repayment", 1, "repayment-account")));
        final JsonArray content = JsonParser.parseString(loanHelper.retrieveTransactionsByLoanIdRaw(loanId)).getAsJsonObject()
                .getAsJsonArray("content");
        assertEquals(2, content.size());
    }

    @Test
    public void testRepaymentWithDuplicateTransactionExternalIdFails() {
        final Long productId = createProduct();
        final String sharedExternalId = "wcl-repay-ext-" + UUID.randomUUID();
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());

        final Long loanId1 = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), BigDecimal.valueOf(5000), approvedOnDate);
        final Long loanId2 = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(3000), BigDecimal.valueOf(3000), approvedOnDate);
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);

        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId1, WorkingCapitalLoanDisbursementTestBuilder
                        .buildTransactionJson(repaymentDate, BigDecimal.valueOf(100), null, null, null, null, sharedExternalId)));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId2,
                WorkingCapitalLoanDisbursementTestBuilder.buildTransactionJson(repaymentDate, BigDecimal.valueOf(100), null, null, null,
                        null, sharedExternalId));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentExactPayoffSetsClosedObligationsMet() {
        final LocalDate approvedOnDate = LocalDate.now(ZoneId.systemDefault());
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(repaymentDate,
                        BigDecimal.valueOf(5000), null, "full payoff", 1, "repayment-account")));
        final JsonObject loanData = JsonParser.parseString(loanHelper.retrieveById(loanId)).getAsJsonObject();
        assertStatus(loanData, "loanStatusType.closed.obligations.met");
        assertEqualBigDecimal(BigDecimal.ZERO, loanData.getAsJsonObject("balance").get("principalOutstanding"));
    }

    @Test
    public void testRepaymentAmortizationMatchesProvidedReferenceSchedule() {
        final Long productId = createProductForReferenceSchedule();
        final LocalDate disbursementDate = LocalDate.of(2019, 1, 1);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(9000)).withPeriodPaymentRate(new BigDecimal("0.18"))
                .withTotalPayment(BigDecimal.valueOf(100000)).withDiscount(BigDecimal.valueOf(1000)).withSubmittedOnDate(disbursementDate)
                .buildSubmitJson());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(disbursementDate, BigDecimal.valueOf(9000),
                BigDecimal.valueOf(1000)));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(disbursementDate, BigDecimal.valueOf(9000)));

        for (int day = 1; day <= 3; day++) {
            final int repaymentDay = day;
            final LocalDate repaymentDate = disbursementDate.plusDays(day);
            BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    () -> loanHelper.makeRepaymentByLoanId(loanId,
                            WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentJson(repaymentDate, BigDecimal.valueOf(50), null,
                                    "reference-schedule-day-" + repaymentDay, 1, "repayment-account")));
        }

        final JsonObject schedule = JsonParser.parseString(loanHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId)).getAsJsonObject();
        assertEqualBigDecimal(BigDecimal.valueOf(1000), schedule.get("originationFeeAmount"));
        assertEqualBigDecimal(BigDecimal.valueOf(9000), schedule.get("netDisbursementAmount"));
        assertEqualBigDecimal(BigDecimal.valueOf(100000), schedule.get("totalPaymentValue"));
        assertEqualBigDecimal(new BigDecimal("0.18"), schedule.get("periodPaymentRate"));
        assertEquals(360, schedule.get("npvDayCount").getAsInt());
        assertTrue(schedule.get("expectedPaymentAmount").getAsBigDecimal().compareTo(BigDecimal.ZERO) > 0,
                "expectedPaymentAmount should be positive");

        final JsonArray payments = schedule.getAsJsonArray("payments");
        final Map<LocalDate, JsonObject> paymentByDate = new HashMap<>();
        for (final JsonElement paymentElement : payments) {
            final JsonObject payment = paymentElement.getAsJsonObject();
            paymentByDate.put(parseDate(payment.get("paymentDate")), payment);
        }
        final List<ExpectedScheduleRow> expectedRows = buildExpectedScheduleRows();
        assertEquals(expectedRows.size(), paymentByDate.size(), "Fixture and API schedule row count differ");
        for (ExpectedScheduleRow row : expectedRows) {
            assertScheduleRow(paymentByDate.get(row.paymentDate()), row.count(), row.paymentsLeft(), row.expectedPaymentAmount(),
                    row.forecastPaymentAmount(), row.actualPaymentAmount(), row.discountFactor(), row.npvValue(), row.balance(),
                    row.expectedAmortization(), row.netAmortization(), row.actualAmortization(), row.incomeModification(),
                    row.deferredBalance(), row.paymentDate().toString());
        }
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final BigDecimal disburseAmount) {
        return createApprovedAndDisbursedLoan(productId, principal, disburseAmount, LocalDate.now(ZoneId.systemDefault()));
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final BigDecimal disburseAmount,
            final LocalDate approvedOnDate) {
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(principal).withPeriodPaymentRate(BigDecimal.ONE).buildSubmitJson());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(approvedOnDate, principal, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseJson(approvedOnDate, disburseAmount));
        return loanId;
    }

    private static void assertStatus(final JsonObject data, final String expectedStatusCode) {
        assertTrue(data.has("status") && !data.get("status").isJsonNull());
        assertEquals(expectedStatusCode, data.getAsJsonObject("status").get("code").getAsString());
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final JsonElement actual) {
        assertNotNull(actual);
        assertFalse(actual.isJsonNull());
        assertEquals(0, expected.compareTo(actual.getAsJsonPrimitive().getAsBigDecimal()));
    }

    private static void assertScheduleRow(final JsonObject payment, final String expectedCount, final String expectedPaymentsLeft,
            final String expectedPaymentAmount, final String expectedForecastPaymentAmount, final String expectedActualPaymentAmount,
            final String expectedDiscountFactor, final String expectedNpvValue, final String expectedBalance,
            final String expectedExpectedAmortization, final String expectedNetAmortization, final String expectedActualAmortization,
            final String expectedIncomeModification, final String expectedDeferredBalance, final String rowDateLabel) {
        assertNotNull(payment, "Expected payment row to exist for date " + rowDateLabel);
        assertEquals(Long.parseLong(expectedCount), payment.get("count").getAsLong(), "Mismatch for count at " + rowDateLabel);
        assertEquals(Long.parseLong(expectedPaymentsLeft), payment.get("paymentsLeft").getAsLong(),
                "Mismatch for paymentsLeft at " + rowDateLabel);
        assertAmount(payment, "expectedPaymentAmount", expectedPaymentAmount, rowDateLabel);
        assertAmount(payment, "forecastPaymentAmount", expectedForecastPaymentAmount, rowDateLabel);
        assertAmountOrNull(payment, "actualPaymentAmount", expectedActualPaymentAmount, rowDateLabel);
        assertDiscountFactor(payment, expectedDiscountFactor, rowDateLabel);
        assertAmount(payment, "npvValue", expectedNpvValue, rowDateLabel);
        assertAmount(payment, "balance", expectedBalance, rowDateLabel);
        assertAmount(payment, "expectedAmortizationAmount", expectedExpectedAmortization, rowDateLabel);
        assertAmountOrNull(payment, "actualAmortizationAmount", expectedActualAmortization, rowDateLabel);
        assertAmount(payment, "netAmortizationAmount", expectedNetAmortization, rowDateLabel);
        assertAmount(payment, "incomeModification", expectedIncomeModification, rowDateLabel);
        assertAmount(payment, "deferredBalance", expectedDeferredBalance, rowDateLabel);
    }

    private static void assertDiscountFactor(final JsonObject payment, final String expectedDiscountFactor, final String rowDateLabel) {
        final BigDecimal actual = payment.get("discountFactor").getAsBigDecimal();
        final BigDecimal expected = new BigDecimal(expectedDiscountFactor);
        assertTrue(actual.subtract(expected).abs().compareTo(new BigDecimal("0.00000002")) <= 0,
                "Unexpected discountFactor at " + rowDateLabel + ". expected=" + expected + ", actual=" + actual);
    }

    private static void assertAmount(final JsonObject payment, final String field, final String expectedValue, final String rowDateLabel) {
        if (expectedValue == null) {
            assertTrue(!payment.has(field) || payment.get(field).isJsonNull(), "Expected null for " + field + " at " + rowDateLabel);
            return;
        }
        assertTrue(payment.has(field) && !payment.get(field).isJsonNull(), "Expected non-null for " + field + " at " + rowDateLabel);
        final BigDecimal actual = payment.get(field).getAsBigDecimal().setScale(2, RoundingMode.HALF_UP);
        final BigDecimal expected = new BigDecimal(expectedValue).setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, expected.compareTo(actual),
                "Mismatch for " + field + " at " + rowDateLabel + ": expected=" + expected + ", actual=" + actual);
    }

    private static void assertAmountOrNull(final JsonObject payment, final String field, final String expectedValue,
            final String rowDateLabel) {
        if (expectedValue == null) {
            assertTrue(!payment.has(field) || payment.get(field).isJsonNull(), "Expected null for " + field + " at " + rowDateLabel);
            return;
        }
        assertAmount(payment, field, expectedValue, rowDateLabel);
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

    private static List<ExpectedScheduleRow> buildExpectedScheduleRows() {
        return List.of(expectedRow("1/1/2019", "203", "0", "-9000", null, null, "1", "-9000", "9000", null, null, null, null, "1000"),
                expectedRow("1/2/2019", "202", "0", "50", "50", "50", "1", "50", "8959.61", "9.61", "28.70", "9.61", "0.00", "990.39"),
                expectedRow("1/3/2019", "201", "0", "50", "50", "50", "1", "50", "8919.18", "9.57", "19.09", "9.57", "0.00", "980.82"),
                expectedRow("1/4/2019", "200", "0", "50", "50", "50", "1", "50", "8878.70", "9.52", "9.52", "9.52", "0.00", "971.30"),
                expectedRow("1/5/2019", "199", "1", "50", "50", null, "0.9989333245", "49.94666623", "8838.18", "9.48", "9.52", null,
                        "-9.48", "971.30"),
                expectedRow("1/6/2019", "198", "2", "50", "50", null, "0.9978677868", "49.89338934", "8797.62", "9.44", "9.52", null,
                        "-9.44", "971.30"),
                expectedRow("1/7/2019", "197", "3", "50", "50", null, "0.9968033857", "49.84016929", "8757.01", "9.39", "9.52", null,
                        "-9.39", "971.30"),
                expectedRow("1/8/2019", "196", "4", "50", "50", null, "0.99574012", "49.787006", "8716.36", "9.35", "9.52", null, "-9.35",
                        "971.30"),
                expectedRow("1/9/2019", "195", "5", "50", "50", null, "0.9946779885", "49.73389942", "8675.67", "9.31", "9.52", null,
                        "-9.31", "971.30"),
                expectedRow("1/10/2019", "194", "6", "50", "50", null, "0.9936169898", "49.68084949", "8634.94", "9.26", "9.52", null,
                        "-9.26", "971.30"),
                expectedRow("1/11/2019", "193", "7", "50", "50", null, "0.992557123", "49.62785615", "8594.16", "9.22", "9.52", null,
                        "-9.22", "971.30"),
                expectedRow("1/12/2019", "192", "8", "50", "50", null, "0.9914983866", "49.57491933", "8553.33", "9.18", "9.52", null,
                        "-9.18", "971.30"),
                expectedRow("1/13/2019", "191", "9", "50", "50", null, "0.9904407796", "49.52203898", "8512.47", "9.13", "9.52", null,
                        "-9.13", "971.30"),
                expectedRow("1/14/2019", "190", "10", "50", "50", null, "0.9893843007", "49.46921504", "8471.56", "9.09", "9.52", null,
                        "-9.09", "971.30"),
                expectedRow("1/15/2019", "189", "11", "50", "50", null, "0.9883289487", "49.41644744", "8430.60", "9.05", "9.52", null,
                        "-9.05", "971.30"),
                expectedRow("1/16/2019", "188", "12", "50", "50", null, "0.9872747225", "49.36373612", "8389.61", "9.00", "9.52", null,
                        "-9.00", "971.30"),
                expectedRow("1/17/2019", "187", "13", "50", "50", null, "0.9862216208", "49.31108104", "8348.56", "8.96", "9.52", null,
                        "-8.96", "971.30"),
                expectedRow("1/18/2019", "186", "14", "50", "50", null, "0.9851696423", "49.25848212", "8307.48", "8.91", "9.52", null,
                        "-8.91", "971.30"),
                expectedRow("1/19/2019", "185", "15", "50", "50", null, "0.984118786", "49.2059393", "8266.35", "8.87", "9.52", null,
                        "-8.87", "971.30"),
                expectedRow("1/20/2019", "184", "16", "50", "50", null, "0.9830690507", "49.15345253", "8225.18", "8.83", "9.52", null,
                        "-8.83", "971.30"),
                expectedRow("1/21/2019", "183", "17", "50", "50", null, "0.982020435", "49.10102175", "8183.96", "8.78", "9.52", null,
                        "-8.78", "971.30"),
                expectedRow("1/22/2019", "182", "18", "50", "50", null, "0.9809729379", "49.0486469", "8142.70", "8.74", "9.52", null,
                        "-8.74", "971.30"),
                expectedRow("1/23/2019", "181", "19", "50", "50", null, "0.9799265581", "48.99632791", "8101.39", "8.69", "9.52", null,
                        "-8.69", "971.30"),
                expectedRow("1/24/2019", "180", "20", "50", "50", null, "0.9788812945", "48.94406473", "8060.04", "8.65", "9.52", null,
                        "-8.65", "971.30"),
                expectedRow("1/25/2019", "179", "21", "50", "50", null, "0.9778371458", "48.89185729", "8018.65", "8.61", "9.52", null,
                        "-8.61", "971.30"),
                expectedRow("1/26/2019", "178", "22", "50", "50", null, "0.9767941109", "48.83970555", "7977.21", "8.56", "9.52", null,
                        "-8.56", "971.30"),
                expectedRow("1/27/2019", "177", "23", "50", "50", null, "0.9757521886", "48.78760943", "7935.73", "8.52", "9.52", null,
                        "-8.52", "971.30"),
                expectedRow("1/28/2019", "176", "24", "50", "50", null, "0.9747113777", "48.73556888", "7894.21", "8.47", "9.52", null,
                        "-8.47", "971.30"),
                expectedRow("1/29/2019", "175", "25", "50", "50", null, "0.973671677", "48.68358385", "7852.63", "8.43", "9.52", null,
                        "-8.43", "971.30"),
                expectedRow("1/30/2019", "174", "26", "50", "50", null, "0.9726330853", "48.63165426", "7811.02", "8.39", "9.52", null,
                        "-8.39", "971.30"),
                expectedRow("1/31/2019", "173", "27", "50", "50", null, "0.9715956014", "48.57978007", "7769.36", "8.34", "9.52", null,
                        "-8.34", "971.30"),
                expectedRow("2/1/2019", "172", "28", "50", "50", null, "0.9705592242", "48.52796121", "7727.66", "8.30", "9.52", null,
                        "-8.30", "971.30"),
                expectedRow("2/2/2019", "171", "29", "50", "50", null, "0.9695239525", "48.47619762", "7685.91", "8.25", "9.52", null,
                        "-8.25", "971.30"),
                expectedRow("2/3/2019", "170", "30", "50", "50", null, "0.968489785", "48.42448925", "7644.12", "8.21", "9.52", null,
                        "-8.21", "971.30"),
                expectedRow("2/4/2019", "169", "31", "50", "50", null, "0.9674567207", "48.37283604", "7602.28", "8.16", "9.52", null,
                        "-8.16", "971.30"),
                expectedRow("2/5/2019", "168", "32", "50", "50", null, "0.9664247584", "48.32123792", "7560.40", "8.12", "9.52", null,
                        "-8.12", "971.30"),
                expectedRow("2/6/2019", "167", "33", "50", "50", null, "0.9653938968", "48.26969484", "7518.47", "8.07", "9.52", null,
                        "-8.07", "971.30"),
                expectedRow("2/7/2019", "166", "34", "50", "50", null, "0.9643641348", "48.21820674", "7476.50", "8.03", "9.52", null,
                        "-8.03", "971.30"),
                expectedRow("2/8/2019", "165", "35", "50", "50", null, "0.9633354712", "48.16677356", "7434.48", "7.98", "9.52", null,
                        "-7.98", "971.30"),
                expectedRow("2/9/2019", "164", "36", "50", "50", null, "0.9623079049", "48.11539525", "7392.42", "7.94", "9.52", null,
                        "-7.94", "971.30"),
                expectedRow("2/10/2019", "163", "37", "50", "50", null, "0.9612814347", "48.06407173", "7350.31", "7.89", "9.52", null,
                        "-7.89", "971.30"),
                expectedRow("2/11/2019", "162", "38", "50", "50", null, "0.9602560593", "48.01280297", "7308.16", "7.85", "9.52", null,
                        "-7.85", "971.30"),
                expectedRow("2/12/2019", "161", "39", "50", "50", null, "0.9592317777", "47.96158889", "7265.97", "7.80", "9.52", null,
                        "-7.80", "971.30"),
                expectedRow("2/13/2019", "160", "40", "50", "50", null, "0.9582085887", "47.91042944", "7223.72", "7.76", "9.52", null,
                        "-7.76", "971.30"),
                expectedRow("2/14/2019", "159", "41", "50", "50", null, "0.9571864911", "47.85932456", "7181.44", "7.71", "9.52", null,
                        "-7.71", "971.30"),
                expectedRow("2/15/2019", "158", "42", "50", "50", null, "0.9561654838", "47.80827419", "7139.11", "7.67", "9.52", null,
                        "-7.67", "971.30"),
                expectedRow("2/16/2019", "157", "43", "50", "50", null, "0.9551455655", "47.75727827", "7096.73", "7.62", "9.52", null,
                        "-7.62", "971.30"),
                expectedRow("2/17/2019", "156", "44", "50", "50", null, "0.9541267351", "47.70633676", "7054.31", "7.58", "9.52", null,
                        "-7.58", "971.30"),
                expectedRow("2/18/2019", "155", "45", "50", "50", null, "0.9531089916", "47.65544958", "7011.84", "7.53", "9.52", null,
                        "-7.53", "971.30"),
                expectedRow("2/19/2019", "154", "46", "50", "50", null, "0.9520923336", "47.60461668", "6969.33", "7.49", "9.52", null,
                        "-7.49", "971.30"),
                expectedRow("2/20/2019", "153", "47", "50", "50", null, "0.95107676", "47.553838", "6926.77", "7.44", "9.52", null, "-7.44",
                        "971.30"),
                expectedRow("2/21/2019", "152", "48", "50", "50", null, "0.9500622698", "47.50311349", "6884.17", "7.40", "9.52", null,
                        "-7.40", "971.30"),
                expectedRow("2/22/2019", "151", "49", "50", "50", null, "0.9490488616", "47.45244308", "6841.52", "7.35", "9.52", null,
                        "-7.35", "971.30"),
                expectedRow("2/23/2019", "150", "50", "50", "50", null, "0.9480365345", "47.40182672", "6798.82", "7.31", "9.52", null,
                        "-7.31", "971.30"),
                expectedRow("2/24/2019", "149", "51", "50", "50", null, "0.9470252872", "47.35126436", "6756.08", "7.26", "9.52", null,
                        "-7.26", "971.30"),
                expectedRow("2/25/2019", "148", "52", "50", "50", null, "0.9460151185", "47.30075593", "6713.30", "7.21", "9.52", null,
                        "-7.21", "971.30"),
                expectedRow("2/26/2019", "147", "53", "50", "50", null, "0.9450060274", "47.25030137", "6670.47", "7.17", "9.52", null,
                        "-7.17", "971.30"),
                expectedRow("2/27/2019", "146", "54", "50", "50", null, "0.9439980126", "47.19990063", "6627.59", "7.12", "9.52", null,
                        "-7.12", "971.30"),
                expectedRow("2/28/2019", "145", "55", "50", "50", null, "0.9429910731", "47.14955366", "6584.67", "7.08", "9.52", null,
                        "-7.08", "971.30"),
                expectedRow("3/1/2019", "144", "56", "50", "50", null, "0.9419852077", "47.09926038", "6541.70", "7.03", "9.52", null,
                        "-7.03", "971.30"),
                expectedRow("3/2/2019", "143", "57", "50", "50", null, "0.9409804151", "47.04902076", "6498.68", "6.99", "9.52", null,
                        "-6.99", "971.30"),
                expectedRow("3/3/2019", "142", "58", "50", "50", null, "0.9399766944", "46.99883472", "6455.62", "6.94", "9.52", null,
                        "-6.94", "971.30"),
                expectedRow("3/4/2019", "141", "59", "50", "50", null, "0.9389740443", "46.94870222", "6412.51", "6.89", "9.52", null,
                        "-6.89", "971.30"),
                expectedRow("3/5/2019", "140", "60", "50", "50", null, "0.9379724637", "46.89862319", "6369.36", "6.85", "9.52", null,
                        "-6.85", "971.30"),
                expectedRow("3/6/2019", "139", "61", "50", "50", null, "0.9369719515", "46.84859758", "6326.16", "6.80", "9.52", null,
                        "-6.80", "971.30"),
                expectedRow("3/7/2019", "138", "62", "50", "50", null, "0.9359725065", "46.79862533", "6282.92", "6.76", "9.52", null,
                        "-6.76", "971.30"),
                expectedRow("3/8/2019", "137", "63", "50", "50", null, "0.9349741276", "46.74870638", "6239.63", "6.71", "9.52", null,
                        "-6.71", "971.30"),
                expectedRow("3/9/2019", "136", "64", "50", "50", null, "0.9339768136", "46.69884068", "6196.29", "6.66", "9.52", null,
                        "-6.66", "971.30"),
                expectedRow("3/10/2019", "135", "65", "50", "50", null, "0.9329805635", "46.64902817", "6152.91", "6.62", "9.52", null,
                        "-6.62", "971.30"),
                expectedRow("3/11/2019", "134", "66", "50", "50", null, "0.931985376", "46.5992688", "6109.48", "6.57", "9.52", null,
                        "-6.57", "971.30"),
                expectedRow("3/12/2019", "133", "67", "50", "50", null, "0.93099125", "46.5495625", "6066.00", "6.52", "9.52", null,
                        "-6.52", "971.30"),
                expectedRow("3/13/2019", "132", "68", "50", "50", null, "0.9299981845", "46.49990922", "6022.48", "6.48", "9.52", null,
                        "-6.48", "971.30"),
                expectedRow("3/14/2019", "131", "69", "50", "50", null, "0.9290061782", "46.45030891", "5978.91", "6.43", "9.52", null,
                        "-6.43", "971.30"),
                expectedRow("3/15/2019", "130", "70", "50", "50", null, "0.9280152301", "46.40076151", "5935.29", "6.38", "9.52", null,
                        "-6.38", "971.30"),
                expectedRow("3/16/2019", "129", "71", "50", "50", null, "0.927025339", "46.35126695", "5891.63", "6.34", "9.52", null,
                        "-6.34", "971.30"),
                expectedRow("3/17/2019", "128", "72", "50", "50", null, "0.9260365038", "46.30182519", "5847.92", "6.29", "9.52", null,
                        "-6.29", "971.30"),
                expectedRow("3/18/2019", "127", "73", "50", "50", null, "0.9250487234", "46.25243617", "5804.17", "6.24", "9.52", null,
                        "-6.24", "971.30"),
                expectedRow("3/19/2019", "126", "74", "50", "50", null, "0.9240619966", "46.20309983", "5760.36", "6.20", "9.52", null,
                        "-6.20", "971.30"),
                expectedRow("3/20/2019", "125", "75", "50", "50", null, "0.9230763224", "46.15381612", "5716.52", "6.15", "9.52", null,
                        "-6.15", "971.30"),
                expectedRow("3/21/2019", "124", "76", "50", "50", null, "0.9220916995", "46.10458497", "5672.62", "6.10", "9.52", null,
                        "-6.10", "971.30"),
                expectedRow("3/22/2019", "123", "77", "50", "50", null, "0.9211081269", "46.05540634", "5628.68", "6.06", "9.52", null,
                        "-6.06", "971.30"),
                expectedRow("3/23/2019", "122", "78", "50", "50", null, "0.9201256034", "46.00628017", "5584.69", "6.01", "9.52", null,
                        "-6.01", "971.30"),
                expectedRow("3/24/2019", "121", "79", "50", "50", null, "0.919144128", "45.9572064", "5540.65", "5.96", "9.52", null,
                        "-5.96", "971.30"),
                expectedRow("3/25/2019", "120", "80", "50", "50", null, "0.9181636995", "45.90818498", "5496.57", "5.92", "9.52", null,
                        "-5.92", "971.30"),
                expectedRow("3/26/2019", "119", "81", "50", "50", null, "0.9171843168", "45.85921584", "5452.44", "5.87", "9.52", null,
                        "-5.87", "971.30"),
                expectedRow("3/27/2019", "118", "82", "50", "50", null, "0.9162059788", "45.81029894", "5408.26", "5.82", "9.52", null,
                        "-5.82", "971.30"),
                expectedRow("3/28/2019", "117", "83", "50", "50", null, "0.9152286843", "45.76143422", "5364.03", "5.78", "9.52", null,
                        "-5.78", "971.30"),
                expectedRow("3/29/2019", "116", "84", "50", "50", null, "0.9142524323", "45.71262162", "5319.76", "5.73", "9.52", null,
                        "-5.73", "971.30"),
                expectedRow("3/30/2019", "115", "85", "50", "50", null, "0.9132772217", "45.66386108", "5275.44", "5.68", "9.52", null,
                        "-5.68", "971.30"),
                expectedRow("3/31/2019", "114", "86", "50", "50", null, "0.9123030513", "45.61515256", "5231.08", "5.63", "9.52", null,
                        "-5.63", "971.30"),
                expectedRow("4/1/2019", "113", "87", "50", "50", null, "0.91132992", "45.566496", "5186.66", "5.59", "9.52", null, "-5.59",
                        "971.30"),
                expectedRow("4/2/2019", "112", "88", "50", "50", null, "0.9103578267", "45.51789134", "5142.20", "5.54", "9.52", null,
                        "-5.54", "971.30"),
                expectedRow("4/3/2019", "111", "89", "50", "50", null, "0.9093867703", "45.46933852", "5097.69", "5.49", "9.52", null,
                        "-5.49", "971.30"),
                expectedRow("4/4/2019", "110", "90", "50", "50", null, "0.9084167498", "45.42083749", "5053.13", "5.44", "9.52", null,
                        "-5.44", "971.30"),
                expectedRow("4/5/2019", "109", "91", "50", "50", null, "0.9074477639", "45.3723882", "5008.53", "5.40", "9.52", null,
                        "-5.40", "971.30"),
                expectedRow("4/6/2019", "108", "92", "50", "50", null, "0.9064798116", "45.32399058", "4963.88", "5.35", "9.52", null,
                        "-5.35", "971.30"),
                expectedRow("4/7/2019", "107", "93", "50", "50", null, "0.9055128918", "45.27564459", "4919.18", "5.30", "9.52", null,
                        "-5.30", "971.30"),
                expectedRow("4/8/2019", "106", "94", "50", "50", null, "0.9045470035", "45.22735017", "4874.43", "5.25", "9.52", null,
                        "-5.25", "971.30"),
                expectedRow("4/9/2019", "105", "95", "50", "50", null, "0.9035821453", "45.17910727", "4829.64", "5.20", "9.52", null,
                        "-5.20", "971.30"),
                expectedRow("4/10/2019", "104", "96", "50", "50", null, "0.9026183164", "45.13091582", "4784.79", "5.16", "9.52", null,
                        "-5.16", "971.30"),
                expectedRow("4/11/2019", "103", "97", "50", "50", null, "0.9016555156", "45.08277578", "4739.90", "5.11", "9.52", null,
                        "-5.11", "971.30"),
                expectedRow("4/12/2019", "102", "98", "50", "50", null, "0.9006937418", "45.03468709", "4694.96", "5.06", "9.52", null,
                        "-5.06", "971.30"),
                expectedRow("4/13/2019", "101", "99", "50", "50", null, "0.8997329939", "44.98664969", "4649.98", "5.01", "9.52", null,
                        "-5.01", "971.30"),
                expectedRow("4/14/2019", "100", "100", "50", "50", null, "0.8987732707", "44.93866354", "4604.94", "4.97", "9.52", null,
                        "-4.97", "971.30"),
                expectedRow("4/15/2019", "99", "101", "50", "50", null, "0.8978145713", "44.89072857", "4559.86", "4.92", "9.52", null,
                        "-4.92", "971.30"),
                expectedRow("4/16/2019", "98", "102", "50", "50", null, "0.8968568945", "44.84284473", "4514.73", "4.87", "9.52", null,
                        "-4.87", "971.30"),
                expectedRow("4/17/2019", "97", "103", "50", "50", null, "0.8959002393", "44.79501196", "4469.55", "4.82", "9.52", null,
                        "-4.82", "971.30"),
                expectedRow("4/18/2019", "96", "104", "50", "50", null, "0.8949446045", "44.74723022", "4424.32", "4.77", "9.52", null,
                        "-4.77", "971.30"),
                expectedRow("4/19/2019", "95", "105", "50", "50", null, "0.893989989", "44.69949945", "4379.05", "4.72", "9.52", null,
                        "-4.72", "971.30"),
                expectedRow("4/20/2019", "94", "106", "50", "50", null, "0.8930363918", "44.65181959", "4333.72", "4.68", "9.52", null,
                        "-4.68", "971.30"),
                expectedRow("4/21/2019", "93", "107", "50", "50", null, "0.8920838118", "44.60419059", "4288.35", "4.63", "9.52", null,
                        "-4.63", "971.30"),
                expectedRow("4/22/2019", "92", "108", "50", "50", null, "0.8911322479", "44.55661239", "4242.93", "4.58", "9.52", null,
                        "-4.58", "971.30"),
                expectedRow("4/23/2019", "91", "109", "50", "50", null, "0.890181699", "44.50908495", "4197.46", "4.53", "9.52", null,
                        "-4.53", "971.30"),
                expectedRow("4/24/2019", "90", "110", "50", "50", null, "0.889232164", "44.4616082", "4151.94", "4.48", "9.52", null,
                        "-4.48", "971.30"),
                expectedRow("4/25/2019", "89", "111", "50", "50", null, "0.8882836418", "44.41418209", "4106.38", "4.43", "9.52", null,
                        "-4.43", "971.30"),
                expectedRow("4/26/2019", "88", "112", "50", "50", null, "0.8873361315", "44.36680657", "4060.76", "4.38", "9.52", null,
                        "-4.38", "971.30"),
                expectedRow("4/27/2019", "87", "113", "50", "50", null, "0.8863896318", "44.31948159", "4015.10", "4.34", "9.52", null,
                        "-4.34", "971.30"),
                expectedRow("4/28/2019", "86", "114", "50", "50", null, "0.8854441417", "44.27220708", "3969.38", "4.29", "9.52", null,
                        "-4.29", "971.30"),
                expectedRow("4/29/2019", "85", "115", "50", "50", null, "0.8844996601", "44.22498301", "3923.62", "4.24", "9.52", null,
                        "-4.24", "971.30"),
                expectedRow("4/30/2019", "84", "116", "50", "50", null, "0.883556186", "44.1778093", "3877.81", "4.19", "9.52", null,
                        "-4.19", "971.30"),
                expectedRow("5/1/2019", "83", "117", "50", "50", null, "0.8826137183", "44.13068592", "3831.95", "4.14", "9.52", null,
                        "-4.14", "971.30"),
                expectedRow("5/2/2019", "82", "118", "50", "50", null, "0.8816722559", "44.0836128", "3786.04", "4.09", "9.52", null,
                        "-4.09", "971.30"),
                expectedRow("5/3/2019", "81", "119", "50", "50", null, "0.8807317977", "44.03658989", "3740.09", "4.04", "9.52", null,
                        "-4.04", "971.30"),
                expectedRow("5/4/2019", "80", "120", "50", "50", null, "0.8797923427", "43.98961714", "3694.08", "3.99", "9.52", null,
                        "-3.99", "971.30"),
                expectedRow("5/5/2019", "79", "121", "50", "50", null, "0.8788538898", "43.94269449", "3648.03", "3.94", "9.52", null,
                        "-3.94", "971.30"),
                expectedRow("5/6/2019", "78", "122", "50", "50", null, "0.8779164379", "43.8958219", "3601.92", "3.90", "9.52", null,
                        "-3.90", "971.30"),
                expectedRow("5/7/2019", "77", "123", "50", "50", null, "0.876979986", "43.8489993", "3555.77", "3.85", "9.52", null,
                        "-3.85", "971.30"),
                expectedRow("5/8/2019", "76", "124", "50", "50", null, "0.8760445329", "43.80222665", "3509.56", "3.80", "9.52", null,
                        "-3.80", "971.30"),
                expectedRow("5/9/2019", "75", "125", "50", "50", null, "0.8751100777", "43.75550389", "3463.31", "3.75", "9.52", null,
                        "-3.75", "971.30"),
                expectedRow("5/10/2019", "74", "126", "50", "50", null, "0.8741766193", "43.70883096", "3417.01", "3.70", "9.52", null,
                        "-3.70", "971.30"),
                expectedRow("5/11/2019", "73", "127", "50", "50", null, "0.8732441565", "43.66220782", "3370.66", "3.65", "9.52", null,
                        "-3.65", "971.30"),
                expectedRow("5/12/2019", "72", "128", "50", "50", null, "0.8723126884", "43.61563442", "3324.26", "3.60", "9.52", null,
                        "-3.60", "971.30"),
                expectedRow("5/13/2019", "71", "129", "50", "50", null, "0.8713822138", "43.56911069", "3277.81", "3.55", "9.52", null,
                        "-3.55", "971.30"),
                expectedRow("5/14/2019", "70", "130", "50", "50", null, "0.8704527318", "43.52263659", "3231.31", "3.50", "9.52", null,
                        "-3.50", "971.30"),
                expectedRow("5/15/2019", "69", "131", "50", "50", null, "0.8695242412", "43.47621206", "3184.76", "3.45", "9.52", null,
                        "-3.45", "971.30"),
                expectedRow("5/16/2019", "68", "132", "50", "50", null, "0.868596741", "43.42983705", "3138.16", "3.40", "9.52", null,
                        "-3.40", "971.30"),
                expectedRow("5/17/2019", "67", "133", "50", "50", null, "0.8676702302", "43.38351151", "3091.51", "3.35", "9.52", null,
                        "-3.35", "971.30"),
                expectedRow("5/18/2019", "66", "134", "50", "50", null, "0.8667447076", "43.33723538", "3044.81", "3.30", "9.52", null,
                        "-3.30", "971.30"),
                expectedRow("5/19/2019", "65", "135", "50", "50", null, "0.8658201723", "43.29100861", "2998.06", "3.25", "9.52", null,
                        "-3.25", "971.30"),
                expectedRow("5/20/2019", "64", "136", "50", "50", null, "0.8648966231", "43.24483116", "2951.26", "3.20", "9.52", null,
                        "-3.20", "971.30"),
                expectedRow("5/21/2019", "63", "137", "50", "50", null, "0.8639740591", "43.19870296", "2904.42", "3.15", "9.52", null,
                        "-3.15", "971.30"),
                expectedRow("5/22/2019", "62", "138", "50", "50", null, "0.8630524792", "43.15262396", "2857.52", "3.10", "9.52", null,
                        "-3.10", "971.30"),
                expectedRow("5/23/2019", "61", "139", "50", "50", null, "0.8621318823", "43.10659411", "2810.57", "3.05", "9.52", null,
                        "-3.05", "971.30"),
                expectedRow("5/24/2019", "60", "140", "50", "50", null, "0.8612122673", "43.06061337", "2763.57", "3.00", "9.52", null,
                        "-3.00", "971.30"),
                expectedRow("5/25/2019", "59", "141", "50", "50", null, "0.8602936333", "43.01468167", "2716.52", "2.95", "9.52", null,
                        "-2.95", "971.30"),
                expectedRow("5/26/2019", "58", "142", "50", "50", null, "0.8593759792", "42.96879896", "2669.42", "2.90", "9.52", null,
                        "-2.90", "971.30"),
                expectedRow("5/27/2019", "57", "143", "50", "50", null, "0.8584593039", "42.9229652", "2622.27", "2.85", "9.52", null,
                        "-2.85", "971.30"),
                expectedRow("5/28/2019", "56", "144", "50", "50", null, "0.8575436064", "42.87718032", "2575.07", "2.80", "9.52", null,
                        "-2.80", "971.30"),
                expectedRow("5/29/2019", "55", "145", "50", "50", null, "0.8566288857", "42.83144429", "2527.82", "2.75", "9.52", null,
                        "-2.75", "971.30"),
                expectedRow("5/30/2019", "54", "146", "50", "50", null, "0.8557151407", "42.78575703", "2480.52", "2.70", "9.52", null,
                        "-2.70", "971.30"),
                expectedRow("5/31/2019", "53", "147", "50", "50", null, "0.8548023703", "42.74011852", "2433.17", "2.65", "9.52", null,
                        "-2.65", "971.30"),
                expectedRow("6/1/2019", "52", "148", "50", "50", null, "0.8538905736", "42.69452868", "2385.77", "2.60", "9.52", null,
                        "-2.60", "971.30"),
                expectedRow("6/2/2019", "51", "149", "50", "50", null, "0.8529797495", "42.64898747", "2338.31", "2.55", "9.52", null,
                        "-2.55", "971.30"),
                expectedRow("6/3/2019", "50", "150", "50", "50", null, "0.8520698969", "42.60349484", "2290.81", "2.50", "9.52", null,
                        "-2.50", "971.30"),
                expectedRow("6/4/2019", "49", "151", "50", "50", null, "0.8511610148", "42.55805074", "2243.26", "2.45", "9.52", null,
                        "-2.45", "971.30"),
                expectedRow("6/5/2019", "48", "152", "50", "50", null, "0.8502531022", "42.51265511", "2195.65", "2.40", "9.52", null,
                        "-2.40", "971.30"),
                expectedRow("6/6/2019", "47", "153", "50", "50", null, "0.8493461581", "42.46730791", "2148.00", "2.34", "9.52", null,
                        "-2.34", "971.30"),
                expectedRow("6/7/2019", "46", "154", "50", "50", null, "0.8484401814", "42.42200907", "2100.29", "2.29", "9.52", null,
                        "-2.29", "971.30"),
                expectedRow("6/8/2019", "45", "155", "50", "50", null, "0.8475351711", "42.37675855", "2052.53", "2.24", "9.52", null,
                        "-2.24", "971.30"),
                expectedRow("6/9/2019", "44", "156", "50", "50", null, "0.8466311261", "42.3315563", "2004.73", "2.19", "9.52", null,
                        "-2.19", "971.30"),
                expectedRow("6/10/2019", "43", "157", "50", "50", null, "0.8457280454", "42.28640227", "1956.87", "2.14", "9.52", null,
                        "-2.14", "971.30"),
                expectedRow("6/11/2019", "42", "158", "50", "50", null, "0.844825928", "42.2412964", "1908.96", "2.09", "9.52", null,
                        "-2.09", "971.30"),
                expectedRow("6/12/2019", "41", "159", "50", "50", null, "0.8439247729", "42.19623865", "1860.99", "2.04", "9.52", null,
                        "-2.04", "971.30"),
                expectedRow("6/13/2019", "40", "160", "50", "50", null, "0.8430245791", "42.15122895", "1812.98", "1.99", "9.52", null,
                        "-1.99", "971.30"),
                expectedRow("6/14/2019", "39", "161", "50", "50", null, "0.8421253454", "42.10626727", "1764.92", "1.94", "9.52", null,
                        "-1.94", "971.30"),
                expectedRow("6/15/2019", "38", "162", "50", "50", null, "0.841227071", "42.06135355", "1716.80", "1.88", "9.52", null,
                        "-1.88", "971.30"),
                expectedRow("6/16/2019", "37", "163", "50", "50", null, "0.8403297547", "42.01648774", "1668.64", "1.83", "9.52", null,
                        "-1.83", "971.30"),
                expectedRow("6/17/2019", "36", "164", "50", "50", null, "0.8394333956", "41.97166978", "1620.42", "1.78", "9.52", null,
                        "-1.78", "971.30"),
                expectedRow("6/18/2019", "35", "165", "50", "50", null, "0.8385379925", "41.92689963", "1572.15", "1.73", "9.52", null,
                        "-1.73", "971.30"),
                expectedRow("6/19/2019", "34", "166", "50", "50", null, "0.8376435446", "41.88217723", "1523.83", "1.68", "9.52", null,
                        "-1.68", "971.30"),
                expectedRow("6/20/2019", "33", "167", "50", "50", null, "0.8367500508", "41.83750254", "1475.45", "1.63", "9.52", null,
                        "-1.63", "971.30"),
                expectedRow("6/21/2019", "32", "168", "50", "50", null, "0.83585751", "41.7928755", "1427.03", "1.58", "9.52", null,
                        "-1.58", "971.30"),
                expectedRow("6/22/2019", "31", "169", "50", "50", null, "0.8349659213", "41.74829607", "1378.55", "1.52", "9.52", null,
                        "-1.52", "971.30"),
                expectedRow("6/23/2019", "30", "170", "50", "50", null, "0.8340752837", "41.70376418", "1330.02", "1.47", "9.52", null,
                        "-1.47", "971.30"),
                expectedRow("6/24/2019", "29", "171", "50", "50", null, "0.833185596", "41.6592798", "1281.45", "1.42", "9.52", null,
                        "-1.42", "971.30"),
                expectedRow("6/25/2019", "28", "172", "50", "50", null, "0.8322968574", "41.61484287", "1232.81", "1.37", "9.52", null,
                        "-1.37", "971.30"),
                expectedRow("6/26/2019", "27", "173", "50", "50", null, "0.8314090667", "41.57045334", "1184.13", "1.32", "9.52", null,
                        "-1.32", "971.30"),
                expectedRow("6/27/2019", "26", "174", "50", "50", null, "0.8305222231", "41.52611115", "1135.39", "1.26", "9.52", null,
                        "-1.26", "971.30"),
                expectedRow("6/28/2019", "25", "175", "50", "50", null, "0.8296363254", "41.48181627", "1086.61", "1.21", "9.52", null,
                        "-1.21", "971.30"),
                expectedRow("6/29/2019", "24", "176", "50", "50", null, "0.8287513727", "41.43756863", "1037.77", "1.16", "9.52", null,
                        "-1.16", "971.30"),
                expectedRow("6/30/2019", "23", "177", "50", "50", null, "0.8278673639", "41.39336819", "988.88", "1.11", "9.52", null,
                        "-1.11", "971.30"),
                expectedRow("7/1/2019", "22", "178", "50", "50", null, "0.8269842981", "41.3492149", "939.93", "1.06", "9.52", null,
                        "-1.06", "971.30"),
                expectedRow("7/2/2019", "21", "179", "50", "50", null, "0.8261021742", "41.30510871", "890.93", "1.00", "9.52", null,
                        "-1.00", "971.30"),
                expectedRow("7/3/2019", "20", "180", "50", "50", null, "0.8252209913", "41.26104956", "841.89", "0.95", "9.52", null,
                        "-0.95", "971.30"),
                expectedRow("7/4/2019", "19", "181", "50", "50", null, "0.8243407483", "41.21703741", "792.79", "0.90", "9.52", null,
                        "-0.90", "971.30"),
                expectedRow("7/5/2019", "18", "182", "50", "50", null, "0.8234614442", "41.17307221", "743.63", "0.85", "9.52", null,
                        "-0.85", "971.30"),
                expectedRow("7/6/2019", "17", "183", "50", "50", null, "0.8225830781", "41.1291539", "694.43", "0.79", "9.52", null,
                        "-0.79", "971.30"),
                expectedRow("7/7/2019", "16", "184", "50", "50", null, "0.8217056489", "41.08528244", "645.17", "0.74", "9.52", null,
                        "-0.74", "971.30"),
                expectedRow("7/8/2019", "15", "185", "50", "50", null, "0.8208291556", "41.04145778", "595.86", "0.69", "9.52", null,
                        "-0.69", "971.30"),
                expectedRow("7/9/2019", "14", "186", "50", "50", null, "0.8199535973", "40.99767987", "546.49", "0.64", "9.52", null,
                        "-0.64", "971.30"),
                expectedRow("7/10/2019", "13", "187", "50", "50", null, "0.8190789729", "40.95394865", "497.08", "0.58", "9.52", null,
                        "-0.58", "971.30"),
                expectedRow("7/11/2019", "12", "188", "50", "50", null, "0.8182052815", "40.91026407", "447.61", "0.53", "9.52", null,
                        "-0.53", "971.30"),
                expectedRow("7/12/2019", "11", "189", "50", "50", null, "0.8173325219", "40.8666261", "398.08", "0.48", "9.52", null,
                        "-0.48", "971.30"),
                expectedRow("7/13/2019", "10", "190", "50", "50", null, "0.8164606934", "40.82303467", "348.51", "0.43", "9.52", null,
                        "-0.43", "971.30"),
                expectedRow("7/14/2019", "9", "191", "50", "50", null, "0.8155897948", "40.77948974", "298.88", "0.37", "9.52", null,
                        "-0.37", "971.30"),
                expectedRow("7/15/2019", "8", "192", "50", "50", null, "0.8147198252", "40.73599126", "249.20", "0.32", "9.52", null,
                        "-0.32", "971.30"),
                expectedRow("7/16/2019", "7", "193", "50", "50", null, "0.8138507835", "40.69253918", "199.47", "0.27", "9.52", null,
                        "-0.27", "971.30"),
                expectedRow("7/17/2019", "6", "194", "50", "50", null, "0.8129826688", "40.64913344", "149.68", "0.21", "9.52", null,
                        "-0.21", "971.30"),
                expectedRow("7/18/2019", "5", "195", "50", "50", null, "0.8121154802", "40.60577401", "99.84", "0.16", "9.52", null,
                        "-0.16", "971.30"),
                expectedRow("7/19/2019", "4", "196", "50", "50", null, "0.8112492165", "40.56246082", "49.95", "0.11", "9.52", null,
                        "-0.11", "971.30"),
                expectedRow("7/20/2019", "3", "197", "50", "50", null, "0.8103838768", "40.51919384", "0.00", "0.05", "9.52", null, "-0.05",
                        "971.30"));
    }

    private static ExpectedScheduleRow expectedRow(final String paymentDate, final String count, final String paymentsLeft,
            final String expectedPaymentAmount, final String forecastPaymentAmount, final String actualPaymentAmount,
            final String discountFactor, final String npvValue, final String balance, final String expectedAmortization,
            final String netAmortization, final String actualAmortization, final String incomeModification, final String deferredBalance) {
        return new ExpectedScheduleRow(parseDateString(paymentDate), count, paymentsLeft, expectedPaymentAmount, forecastPaymentAmount,
                actualPaymentAmount, discountFactor, npvValue, balance, expectedAmortization, netAmortization, actualAmortization,
                incomeModification, deferredBalance);
    }

    private static LocalDate parseDateString(final String dateText) {
        return LocalDate.parse(dateText, DateTimeFormatter.ofPattern("M/d/yyyy"));
    }

    private record ExpectedScheduleRow(LocalDate paymentDate, String count, String paymentsLeft, String expectedPaymentAmount,
            String forecastPaymentAmount, String actualPaymentAmount, String discountFactor, String npvValue, String balance,
            String expectedAmortization, String netAmortization, String actualAmortization, String incomeModification,
            String deferredBalance) {
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
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withAllowAttributeOverrides(java.util.Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductForReferenceSchedule() {
        final String uniqueName = "WCL Excel Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withRepaymentEvery(1).withRepaymentFrequencyType("DAYS")
                .withAllowAttributeOverrides(java.util.Map.of("discountDefault", Boolean.TRUE)).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private Long submitAndTrack(final String submitJson) {
        final Long loanId = loanHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
