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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalLoanBreachActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanBreachPastDueAmountTest {

    private static final BigDecimal BREACH_AMOUNT = BigDecimal.valueOf(100);

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();
    private final WorkingCapitalLoanBreachActionHelper breachActionHelper = new WorkingCapitalLoanBreachActionHelper();

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdBreachIds = new ArrayList<>();
    private final Long createdClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

    @AfterEach
    void cleanup() {
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
            if (breachId == null) {
                continue;
            }
            try {
                breachHelper.delete(breachId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdBreachIds.clear();
    }

    @Test
    public void testBreachPastDueAmountAccumulatesWhenPeriodUnpaid() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));

        BusinessDateHelper.runAt("09 January 2026", () -> {
            runInlineCob(loanIdHolder[0]);

            final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanIdHolder[0]);
            assertNotNull(loanData.getBalance());
            assertEquals(0, BREACH_AMOUNT.compareTo(loanData.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testBreachPastDueAmountReducedByPartialRepayment() {
        final Long[] loanIdHolder = new Long[1];
        BigDecimal partialRepayment = BigDecimal.valueOf(40);
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));

        BusinessDateHelper.runAt("05 January 2026", () -> {
            runInlineCob(loanIdHolder[0]);
            loanHelper.makeRepaymentByLoanId(loanIdHolder[0], WorkingCapitalLoanDisbursementTestBuilder
                    .buildRepaymentRequest(LocalDate.of(2026, 1, 5), partialRepayment, null, null, null, null));
        });

        BusinessDateHelper.runAt("09 January 2026", () -> {
            runInlineCob(loanIdHolder[0]);

            final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanIdHolder[0]);
            assertNotNull(loanData.getBalance());
            assertEquals(0, BREACH_AMOUNT.subtract(partialRepayment).compareTo(loanData.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testBreachPastDueAmountZeroWhenPeriodFullyPaid() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));

        BusinessDateHelper.runAt("05 January 2026", () -> {
            runInlineCob(loanIdHolder[0]);
            loanHelper.makeRepaymentByLoanId(loanIdHolder[0], WorkingCapitalLoanDisbursementTestBuilder
                    .buildRepaymentRequest(LocalDate.of(2026, 1, 5), BREACH_AMOUNT, null, null, null, null));
        });

        BusinessDateHelper.runAt("09 January 2026", () -> {
            runInlineCob(loanIdHolder[0]);

            final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanIdHolder[0]);
            assertNotNull(loanData.getBalance());
            assertEquals(0, BigDecimal.ZERO.compareTo(loanData.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testBreachPastDueAmountAccumulatesAcrossMultipleUnpaidPeriods() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));

        BusinessDateHelper.runAt("16 January 2026", () -> {
            runInlineCob(loanIdHolder[0]);

            final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanIdHolder[0]);
            assertNotNull(loanData.getBalance());
            assertEquals(0, BREACH_AMOUNT.multiply(BigDecimal.TWO).compareTo(loanData.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testBreachPastDueAmountUnaffectedByBreachPause() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));
        final Long loanId = loanIdHolder[0];

        BusinessDateHelper.runAt("09 January 2026", () -> {
            runInlineCob(loanId);
            breachActionHelper.pause(loanId, "2026-01-09", "2026-01-20");
        });

        BusinessDateHelper.runAt("12 January 2026", () -> {
            runInlineCob(loanId);
            final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId);
            assertNotNull(loanData.getBalance());
            assertEquals(0, BREACH_AMOUNT.compareTo(loanData.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testBackdatedRepaymentRecalculatesPastDueAmountImmediately() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));
        final Long loanId = loanIdHolder[0];

        BusinessDateHelper.runAt("09 January 2026", () -> {
            // Period 1 (Jan 1 - Jan 7) has ended unpaid; period 2 (Jan 8 - Jan 14) is now open.
            runInlineCob(loanId);
            final GetWorkingCapitalLoansLoanIdResponse beforeAnyRepayment = loanHelper.retrieveById(loanId);
            assertNotNull(beforeAnyRepayment.getBalance());
            assertEquals(0, BREACH_AMOUNT.compareTo(beforeAnyRepayment.getBalance().getBreachPastDueAmount()));

            // Forward repayment lands in the still-open period 2 (today, Jan 9); not backdated, no effect on past due.
            loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(
                    LocalDate.of(2026, 1, 9), BigDecimal.valueOf(30), null, "forward repayment into period 2", null, null));
            final GetWorkingCapitalLoansLoanIdResponse afterForwardRepayment = loanHelper.retrieveById(loanId);
            assertNotNull(afterForwardRepayment.getBalance());
            assertEquals(0, BREACH_AMOUNT.compareTo(afterForwardRepayment.getBalance().getBreachPastDueAmount()));

            loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(
                    LocalDate.of(2026, 1, 5), BREACH_AMOUNT, null, "backdated repayment settling period 1", null, null));

            final GetWorkingCapitalLoansLoanIdResponse afterBackdatedRepayment = loanHelper.retrieveById(loanId);
            assertNotNull(afterBackdatedRepayment.getBalance());
            assertEquals(0, BigDecimal.ZERO.compareTo(afterBackdatedRepayment.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testUndoingRepaymentRecalculatesPastDueAmount() {
        final Long[] loanIdHolder = new Long[1];
        final Long[] transactionIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> loanIdHolder[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1)));
        final Long loanId = loanIdHolder[0];

        BusinessDateHelper.runAt("09 January 2026", () -> {
            // Period 1 (Jan 1 - Jan 7) has ended unpaid.
            runInlineCob(loanId);

            transactionIdHolder[0] = loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                    .buildRepaymentRequest(LocalDate.of(2026, 1, 5), BREACH_AMOUNT, null, "repayment settling period 1", null, null));

            final GetWorkingCapitalLoansLoanIdResponse afterRepayment = loanHelper.retrieveById(loanId);
            assertNotNull(afterRepayment.getBalance());
            assertEquals(0, BigDecimal.ZERO.compareTo(afterRepayment.getBalance().getBreachPastDueAmount()));

            loanHelper.undoTransactionById(loanId, transactionIdHolder[0]);

            final GetWorkingCapitalLoansLoanIdResponse afterUndo = loanHelper.retrieveById(loanId);
            assertNotNull(afterUndo.getBalance());
            assertEquals(0, BREACH_AMOUNT.compareTo(afterUndo.getBalance().getBreachPastDueAmount()));
        });
    }

    @Test
    public void testRealtimeAndBackdatedTransactionsProduceTheSamePastDueAmount() {
        final Long[] realtimeLoanId = new Long[1];
        final Long[] backdatedLoanId = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> {
            realtimeLoanId[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1));
            backdatedLoanId[0] = createActiveLoanWithBreach(LocalDate.of(2026, 1, 1));
            runInlineCob(realtimeLoanId[0]);
            runInlineCob(backdatedLoanId[0]);
        });

        BusinessDateHelper
                .runAt("05 January 2026",
                        () -> loanHelper.makeRepaymentByLoanId(realtimeLoanId[0],
                                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(LocalDate.of(2026, 1, 5), BigDecimal.ONE,
                                        null, "realtime payment into period 1", null, null)));
        BusinessDateHelper.runAt("09 January 2026", () -> {
            runInlineCob(realtimeLoanId[0]);
            loanHelper.makeRepaymentByLoanId(realtimeLoanId[0], WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(
                    LocalDate.of(2026, 1, 9), BigDecimal.valueOf(150), null, "realtime payment into period 2", null, null));
        });

        BusinessDateHelper.runAt("10 January 2026", () -> {
            runInlineCob(backdatedLoanId[0]);
            loanHelper.makeRepaymentByLoanId(backdatedLoanId[0], WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(
                    LocalDate.of(2026, 1, 9), BigDecimal.valueOf(150), null, "backdated-loan payment into period 2", null, null));
            loanHelper.makeRepaymentByLoanId(backdatedLoanId[0], WorkingCapitalLoanDisbursementTestBuilder
                    .buildRepaymentRequest(LocalDate.of(2026, 1, 5), BigDecimal.ONE, null, "backdated payment into period 1", null, null));
        });

        BusinessDateHelper.runAt("11 January 2026", () -> {
            runInlineCob(backdatedLoanId[0]);
            final GetWorkingCapitalLoansLoanIdResponse realtimeLoanData = loanHelper.retrieveById(realtimeLoanId[0]);
            final GetWorkingCapitalLoansLoanIdResponse backdatedLoanData = loanHelper.retrieveById(backdatedLoanId[0]);
            assertNotNull(realtimeLoanData.getBalance());
            assertNotNull(realtimeLoanData.getBalance().getBreachPastDueAmount());
            assertNotNull(backdatedLoanData.getBalance());

            final BigDecimal expectedPastDue = BREACH_AMOUNT.subtract(BigDecimal.ONE);
            assertEquals(0, expectedPastDue.compareTo(realtimeLoanData.getBalance().getBreachPastDueAmount()),
                    "Realtime loan past due amount");
            assertEquals(0, expectedPastDue.compareTo(backdatedLoanData.getBalance().getBreachPastDueAmount()),
                    "Backdated loan past due amount");
            assertEquals(0,
                    realtimeLoanData.getBalance().getBreachPastDueAmount()
                            .compareTo(backdatedLoanData.getBalance().getBreachPastDueAmount()),
                    "Realtime and backdated loans must produce the identical past due amount");

            final List<WorkingCapitalLoanBreachScheduleData> realtimeSchedule = getBreachSchedule(realtimeLoanId[0]);
            final List<WorkingCapitalLoanBreachScheduleData> backdatedSchedule = getBreachSchedule(backdatedLoanId[0]);
            assertEquals(0, expectedPastDue.compareTo(realtimeSchedule.get(0).getOutstandingAmount()),
                    "Realtime loan period 1 outstanding");
            assertEquals(0, BigDecimal.ZERO.compareTo(realtimeSchedule.get(1).getOutstandingAmount()),
                    "Realtime loan period 2 outstanding");
            assertEquals(0, expectedPastDue.compareTo(backdatedSchedule.get(0).getOutstandingAmount()),
                    "Backdated loan period 1 outstanding");
            assertEquals(0, BigDecimal.ZERO.compareTo(backdatedSchedule.get(1).getOutstandingAmount()),
                    "Backdated loan period 2 outstanding");
        });
    }

    private List<WorkingCapitalLoanBreachScheduleData> getBreachSchedule(final Long loanId) {
        return FeignCalls.ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanBreachSchedule().retrieveBreachSchedule(loanId));
    }

    private void runInlineCob(final Long loanId) {
        FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                new InlineJobRequest().addLoanIdsItem(loanId)));
    }

    private Long createActiveLoanWithBreach(final LocalDate approvalAndDisbursementDate) {
        final Long breachId = breachHelper.create(new WorkingCapitalBreachRequest().name(Utils.randomStringGenerator("Breach", 12))
                .breachFrequency(7).breachFrequencyType("DAYS").breachAmountCalculationType("FLAT").breachAmount(BREACH_AMOUNT));
        createdBreachIds.add(breachId);

        final String uniqueName = "WCL Breach PastDue Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withBreachId(breachId).build()).getResourceId();
        createdProductIds.add(productId);

        final PostWorkingCapitalLoansRequest submitRequest = new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).withSubmittedOnDate(approvalAndDisbursementDate).buildSubmitRequest();
        final Long loanId = loanHelper.submit(submitRequest);
        createdLoanIds.add(loanId);

        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvalAndDisbursementDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvalAndDisbursementDate, BigDecimal.valueOf(5000)));
        return loanId;
    }
}
