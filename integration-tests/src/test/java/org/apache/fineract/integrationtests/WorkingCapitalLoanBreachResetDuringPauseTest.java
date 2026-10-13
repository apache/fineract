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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
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

public class WorkingCapitalLoanBreachResetDuringPauseTest {

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(800);
    private static final BigDecimal TOTAL_PAYMENT_VOLUME = BigDecimal.valueOf(10000);
    private static final BigDecimal BREACHED_AMOUNT = BigDecimal.valueOf(400);

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
    public void testResetInsideActivePauseStartsRestartedPeriodOnResetDate() {
        final Long loanId = pauseThenResetInsidePause();

        BusinessDateHelper.runAt("12 January 2026", () -> {
            final List<WorkingCapitalLoanBreachScheduleData> schedule = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(3, schedule.size(), describe(schedule));
            assertPeriod(schedule, 0, "2026-01-01", "2026-01-06", 6, true, false);
            assertPeriod(schedule, 1, "2026-01-07", "2026-01-11", 5, true, false);
            // 6-day period restarted on the reset date, extended by the two pause days (12, 13 Jan) that follow the
            // reset
            assertPeriod(schedule, 2, "2026-01-12", "2026-01-19", 8, null, true);
            assertContiguous(schedule);

            assertBreachPastDueAmount(loanId, BigDecimal.ZERO);
        });
    }

    @Test
    public void testLaterPauseAfterResetInsidePauseDoesNotReopenGap() {
        final Long loanId = pauseThenResetInsidePause();

        BusinessDateHelper.runAt("14 January 2026", () -> {
            breachActionHelper.pause(loanId, "2026-01-14", "2026-01-14");

            final List<WorkingCapitalLoanBreachScheduleData> schedule = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(3, schedule.size(), describe(schedule));
            assertPeriod(schedule, 0, "2026-01-01", "2026-01-06", 6, true, false);
            assertPeriod(schedule, 1, "2026-01-07", "2026-01-11", 5, true, false);
            // restarted period extended by 12, 13 Jan (first pause) and 14 Jan (second pause)
            assertPeriod(schedule, 2, "2026-01-12", "2026-01-20", 9, null, true);
            assertContiguous(schedule);

            assertBreachPastDueAmount(loanId, BigDecimal.ZERO);
        });
    }

    @Test
    public void testUndoOfResetInsidePauseRestoresPrePauseSchedule() {
        final Long loanId = pauseThenResetInsidePause();

        BusinessDateHelper.runAt("12 January 2026", () -> {
            breachActionHelper.undoReset(loanId, "2026-01-12");

            final List<WorkingCapitalLoanBreachScheduleData> schedule = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(2, schedule.size(), describe(schedule));
            assertPeriod(schedule, 0, "2026-01-01", "2026-01-06", 6, true, false);
            assertPeriod(schedule, 1, "2026-01-07", "2026-01-15", 9, null, false);
            assertContiguous(schedule);

            assertBreachPastDueAmount(loanId, BREACHED_AMOUNT);
        });
    }

    @Test
    public void testUndoOfStackedResetRestoresRestartedPeriodWithoutGap() {
        final Long loanId = pauseThenResetInsidePause();

        // Only one reset is allowed per period, so the second reset lands on the first day of the next period. The undo
        // then restores the period holding the day before it: the one the 12 Jan reset started.
        BusinessDateHelper.runAt("20 January 2026", () -> {
            runInlineCob(loanId);
            breachActionHelper.resetRestartingFromResetDate(loanId, "2026-01-20");

            final List<WorkingCapitalLoanBreachScheduleData> schedule = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(4, schedule.size(), describe(schedule));
            assertPeriod(schedule, 2, "2026-01-12", "2026-01-19", 8, true, true);
            assertPeriod(schedule, 3, "2026-01-20", "2026-01-25", 6, null, true);
            assertContiguous(schedule);

            breachActionHelper.undoReset(loanId, "2026-01-20");

            final List<WorkingCapitalLoanBreachScheduleData> restored = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(4, restored.size(), describe(restored));
            assertPeriod(restored, 0, "2026-01-01", "2026-01-06", 6, true, false);
            assertPeriod(restored, 1, "2026-01-07", "2026-01-11", 5, true, false);
            // the 12 Jan reset is still active, so its period keeps the 6 days plus the pause days 12, 13 Jan
            assertPeriod(restored, 2, "2026-01-12", "2026-01-19", 8, true, true);
            assertPeriod(restored, 3, "2026-01-20", "2026-01-25", 6, null, false);
            assertContiguous(restored);

            // past due counts from the period of the 12 Jan reset, which has now expired unpaid
            assertBreachPastDueAmount(loanId, BREACHED_AMOUNT);
        });
    }

    /**
     * 01 Jan: disburse, COB. 08 Jan: COB (period 1 breached). 11 Jan: pause 11-13 Jan. 12 Jan: restart reset while the
     * pause is still active.
     */
    private Long pauseThenResetInsidePause() {
        final Long[] loanIdHolder = new Long[1];
        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdHolder[0] = createActiveLoan(LocalDate.of(2026, 1, 1));
            runInlineCob(loanIdHolder[0]);
        });
        final Long loanId = loanIdHolder[0];

        BusinessDateHelper.runAt("08 January 2026", () -> {
            runInlineCob(loanId);

            final List<WorkingCapitalLoanBreachScheduleData> schedule = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(2, schedule.size(), describe(schedule));
            assertPeriod(schedule, 0, "2026-01-01", "2026-01-06", 6, true, false);
            assertEquals(0, BREACHED_AMOUNT.compareTo(schedule.getFirst().getOutstandingAmount()));
            assertPeriod(schedule, 1, "2026-01-07", "2026-01-12", 6, null, false);
        });

        BusinessDateHelper.runAt("11 January 2026", () -> {
            breachActionHelper.pause(loanId, "2026-01-11", "2026-01-13");

            final List<WorkingCapitalLoanBreachScheduleData> schedule = breachActionHelper.retrieveBreachSchedule(loanId);
            assertEquals(2, schedule.size(), describe(schedule));
            assertPeriod(schedule, 1, "2026-01-07", "2026-01-15", 9, null, false);
        });

        BusinessDateHelper.runAt("12 January 2026", () -> breachActionHelper.resetRestartingFromResetDate(loanId, "2026-01-12"));
        return loanId;
    }

    private void assertPeriod(final List<WorkingCapitalLoanBreachScheduleData> schedule, final int index, final String fromDate,
            final String toDate, final int numberOfDays, final Boolean breach, final boolean reset) {
        final WorkingCapitalLoanBreachScheduleData period = schedule.get(index);
        final String label = describe(schedule) + "\n=> period " + period.getPeriodNumber();
        assertEquals(LocalDate.parse(fromDate), period.getFromDate(), label + " fromDate");
        assertEquals(LocalDate.parse(toDate), period.getToDate(), label + " toDate");
        assertEquals(numberOfDays, period.getNumberOfDays(), label + " numberOfDays");
        assertEquals(breach, period.getBreach(), label + " breach");
        assertEquals(reset, Boolean.TRUE.equals(period.getReset()), label + " reset");
    }

    private void assertContiguous(final List<WorkingCapitalLoanBreachScheduleData> schedule) {
        for (int i = 1; i < schedule.size(); i++) {
            final LocalDate previousTo = schedule.get(i - 1).getToDate();
            final LocalDate from = schedule.get(i).getFromDate();
            assertEquals(previousTo.plusDays(1), from, "gap or overlap between periods " + (i - 1) + " and " + i + ": "
                    + ChronoUnit.DAYS.between(previousTo, from) + " days apart; " + describe(schedule));
        }
    }

    private void assertBreachPastDueAmount(final Long loanId, final BigDecimal expected) {
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId);
        assertNotNull(loanData.getBalance());
        assertNotNull(loanData.getBalance().getBreachPastDueAmount());
        assertEquals(0, expected.compareTo(loanData.getBalance().getBreachPastDueAmount()),
                "breachPastDueAmount expected " + expected + " but was " + loanData.getBalance().getBreachPastDueAmount());
    }

    private static String describe(final List<WorkingCapitalLoanBreachScheduleData> schedule) {
        final StringBuilder sb = new StringBuilder("schedule:");
        for (final WorkingCapitalLoanBreachScheduleData p : schedule) {
            sb.append("\n  #").append(p.getPeriodNumber()).append(' ').append(p.getFromDate()).append("..").append(p.getToDate())
                    .append(" days=").append(p.getNumberOfDays()).append(" outstanding=").append(p.getOutstandingAmount())
                    .append(" breach=").append(p.getBreach()).append(" reset=").append(p.getReset());
        }
        return sb.toString();
    }

    private void runInlineCob(final Long loanId) {
        FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                new InlineJobRequest().addLoanIdsItem(loanId)));
    }

    private Long createActiveLoan(final LocalDate approvalAndDisbursementDate) {
        final Long breachId = breachHelper
                .create(new WorkingCapitalBreachRequest().name(Utils.randomStringGenerator("Breach", 12)).breachFrequency(6)
                        .breachFrequencyType("DAYS").breachAmountCalculationType("PERCENTAGE").breachAmount(BigDecimal.valueOf(50)));
        createdBreachIds.add(breachId);

        final String uniqueName = "WCL Breach Reset Pause " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withBreachId(breachId).build()).getResourceId();
        createdProductIds.add(productId);

        final Long loanId = loanHelper.submit(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(PRINCIPAL)
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(TOTAL_PAYMENT_VOLUME).withSubmittedOnDate(approvalAndDisbursementDate).buildSubmitRequest());
        createdLoanIds.add(loanId);

        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvalAndDisbursementDate, PRINCIPAL, null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvalAndDisbursementDate, PRINCIPAL));
        return loanId;
    }
}
