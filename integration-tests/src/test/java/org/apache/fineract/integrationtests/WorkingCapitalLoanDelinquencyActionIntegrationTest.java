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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

@Slf4j
public class WorkingCapitalLoanDelinquencyActionIntegrationTest {

    private static final int PERIOD_FREQUENCY_DAYS = 30;
    private static final String OVERLAP_ERROR_MESSAGE = "Delinquency pause period cannot overlap with another pause period";
    private static final String WC_DELINQUENCY_PAUSE_EVENT = "WorkingCapitalLoanDelinquencyPauseBusinessEvent";

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final FeignExternalEventHelper externalEventHelper = new FeignExternalEventHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    /**
     * Happy path: activate loan -> initial range period generated -> POST pause -> periods shifted by pause duration ->
     * GET returns saved action.
     */
    @Test
    public void testCreatePauseExtendsPeriods() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // verify initial period was generated
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterActivation = getRangeSchedule(loanId);
        assertEquals(1, periodsAfterActivation.size(), "Expected 1 initial period after activation");

        final LocalDate expectedPeriodToDate = periodsAfterActivation.getFirst().getToDate();
        log.info("Initial period toDate: {}", expectedPeriodToDate);

        // when - create a 10-day pause starting from disbursement date
        final LocalDate pauseStart = disbursementDate;
        final LocalDate pauseEnd = disbursementDate.plusDays(10);
        final PostWorkingCapitalLoansDelinquencyActionResponse createResult = WorkingCapitalLoanDelinquencyActionHelper
                .createDelinquencyAction(loanId, "pause", pauseStart, pauseEnd);
        assertNotNull(createResult);
        log.info("Create pause response resourceId={}", createResult.getResourceId());

        // then - range schedule periods should be shifted by 11 days (inclusive pause length)
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterPause = getRangeSchedule(loanId);
        assertEquals(1, periodsAfterPause.size());

        final LocalDate newToDate = periodsAfterPause.getFirst().getToDate();
        assert expectedPeriodToDate != null;
        assertEquals(expectedPeriodToDate.plusDays(11), newToDate,
                "Period toDate should be extended by 11 days (the inclusive pause duration)");

        // and - GET returns the saved action
        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(1, actions.size());
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.getFirst().getAction());
        assertEquals(pauseStart, actions.getFirst().getStartDate());
        assertEquals(pauseEnd, actions.getFirst().getEndDate());
    }

    /**
     * A pause action should publish a WorkingCapitalLoanDelinquencyPauseBusinessEvent for the loan.
     */
    @Test
    public void testCreatePausePublishesExternalBusinessEvent() {
        externalEventHelper.enableBusinessEvent(WC_DELINQUENCY_PAUSE_EVENT);
        try {
            final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
            final Long productId = createProduct(bucketId);
            final Long clientId = createClient();
            final Long loanId = submitAndApproveLoan(clientId, productId);

            final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
            WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

            externalEventHelper.deleteAllExternalEvents();
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", disbursementDate,
                    disbursementDate.plusDays(10));

            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(WC_DELINQUENCY_PAUSE_EVENT);
            final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(e.getAggregateRootId())).findFirst().orElse(null);
            assertNotNull(event, "Expected delinquency pause external event for loan");
            assertEquals(WC_DELINQUENCY_PAUSE_EVENT, event.getType());
            assertEquals(loanId, event.getAggregateRootId());
        } finally {
            externalEventHelper.disableBusinessEvent(WC_DELINQUENCY_PAUSE_EVENT);
        }
    }

    /**
     * Two periods present: pause extends the active period's toDate and also shifts the future period's fromDate and
     * toDate.
     */
    @Test
    public void testCreatePauseShiftsFuturePeriodsAlso() {
        // given - use short period so we can generate 2 periods
        final int periodDays = 15;
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(periodDays);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        // 20 days ago with 15-day frequency: period 1 [d0-d14], period 2 [d15-d29], period 2 toDate is still in future
        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(20);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // Generate next period(s) using the internal endpoint (simulates COB)
        WorkingCapitalLoanDelinquencyActionHelper.generateNextDelinquencyPeriod(loanId, LocalDate.now(ZoneId.systemDefault()));

        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods = getRangeSchedule(loanId);
        assertNotNull(periods);
        assertEquals(2, periods.size(), "Expected 2 periods after generation with 20 days history and 15-day frequency");

        final LocalDate firstPeriodOriginalFromDate = periods.get(0).getFromDate();
        final LocalDate firstPeriodOriginalToDate = periods.get(0).getToDate();
        final LocalDate secondPeriodOriginalFromDate = periods.get(1).getFromDate();
        final LocalDate secondPeriodOriginalToDate = periods.get(1).getToDate();

        // when - create a 7-day pause starting from disbursement date
        final LocalDate pauseStart = disbursementDate;
        final LocalDate pauseEnd = disbursementDate.plusDays(7);
        WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pauseStart, pauseEnd);

        // then - both periods are extended
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterPause = getRangeSchedule(loanId);
        assertEquals(2, periodsAfterPause.size());

        // First period: fromDate unchanged (contains pauseStart), toDate extended by 8 (inclusive)
        assertEquals(firstPeriodOriginalFromDate, periodsAfterPause.getFirst().getFromDate(),
                "First period fromDate should stay unchanged");
        assert firstPeriodOriginalToDate != null;
        assertEquals(firstPeriodOriginalToDate.plusDays(8), periodsAfterPause.get(0).getToDate(),
                "First period toDate should be extended by 8 days");

        // Second period: both fromDate and toDate shifted by 8 (starts after pauseStart)
        assert secondPeriodOriginalFromDate != null;
        assertEquals(secondPeriodOriginalFromDate.plusDays(8), periodsAfterPause.get(1).getFromDate(),
                "Second period fromDate should shift by 8 days");
        assertEquals(secondPeriodOriginalToDate.plusDays(8), periodsAfterPause.get(1).getToDate(),
                "Second period toDate should shift by 8 days");
    }

    /**
     * Overlapping pause is rejected with 400.
     */
    @Test
    public void testOverlappingPauseIsRejected() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // Create first pause
        final LocalDate pause1Start = disbursementDate;
        final LocalDate pause1End = disbursementDate.plusDays(10);
        WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pause1Start, pause1End);

        // when - try to create an overlapping pause (starts during the first pause)
        final LocalDate pause2Start = disbursementDate.plusDays(5);
        final LocalDate pause2End = disbursementDate.plusDays(15);

        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pause2Start, pause2End));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for overlapping pause: {}", exception.getMessage());
    }

    /**
     * Pause on a non-active (submitted/approved) WC loan is rejected with 400.
     */
    @Test
    public void testPauseOnNonActiveLoanIsRejected() {
        // given - loan is only approved, not active
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        // when - try to create a pause on an approved (not active) loan
        final LocalDate pauseStart = LocalDate.now(ZoneId.systemDefault());
        final LocalDate pauseEnd = LocalDate.now(ZoneId.systemDefault()).plusDays(10);

        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pauseStart, pauseEnd));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for pause on non-active loan: {}", exception.getMessage());
    }

    /**
     * Missing endDate is rejected with 400.
     */
    @Test
    public void testMissingEndDateIsRejected() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when - send pause without endDate
        final PostWorkingCapitalLoansDelinquencyActionRequest request = WorkingCapitalLoanDelinquencyActionHelper
                .buildActionRequest("pause", disbursementDate, null);

        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, request));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for missing endDate: {}", exception.getMessage());
    }

    /**
     * Invalid action type (non-PAUSE) is rejected with 400.
     */
    @Test
    public void testInvalidActionTypeIsRejected() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when - send unsupported action type
        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "invalid", disbursementDate,
                        disbursementDate.plusDays(10)));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for unsupported action 'invalid': {}", exception.getMessage());
    }

    /**
     * startDate after endDate is rejected with 400.
     */
    @Test
    public void testStartDateAfterEndDateIsRejected() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when - startDate is after endDate
        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", disbursementDate.plusDays(5),
                        disbursementDate));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for startDate after endDate: {}", exception.getMessage());
    }

    /**
     * Two consecutive inclusive pauses are accepted when the next pause starts the day after the previous one ends.
     */
    @Test
    public void testConsecutiveNonOverlappingPausesAreAccepted() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        final LocalDate pause1Start = disbursementDate;
        final LocalDate pause1End = disbursementDate.plusDays(9);
        WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pause1Start, pause1End);

        // when - second pause starts the day after the first pause ends
        final LocalDate pause2Start = pause1End.plusDays(1);
        final LocalDate pause2End = pause2Start.plusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pause2Start, pause2End);

        // then - both actions saved
        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(2, actions.size(), "Both consecutive pauses should be saved");
    }

    /**
     * Pause with startDate == disbursementDate is accepted (boundary case).
     */
    @Test
    public void testPauseStartingAtDisbursementDateIsAccepted() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when - pause starts exactly on disbursement date
        final LocalDate pauseStart = disbursementDate;
        final LocalDate pauseEnd = disbursementDate.plusDays(3);
        final PostWorkingCapitalLoansDelinquencyActionResponse result = WorkingCapitalLoanDelinquencyActionHelper
                .createDelinquencyAction(loanId, "pause", pauseStart, pauseEnd);

        // then
        assertNotNull(result, "Pause starting at disbursement date should be accepted");
    }

    /**
     * A pause whose startDate falls inside a delinquency range period that has already been evaluated by COB
     * (minPaymentCriteriaMet set) must be accepted, shift the period boundaries, and reprocess/re-evaluate the schedule
     * so the period is no longer prematurely closed.
     */
    @Test
    public void testBackdatedPauseIntoEvaluatedPeriodShiftsAndReevaluatesSchedule() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate[] originalPeriodToDateHolder = new LocalDate[1];

        BusinessDateHelper.runAt("01 June 2026", () -> {
            final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
            final Long productId = createProduct(bucketId);
            final Long clientId = createClient();
            loanIdHolder[0] = submitAndApproveLoan(clientId, productId);

            WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanIdHolder[0], LocalDate.of(2026, 6, 1));

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> initialPeriods = getRangeSchedule(loanIdHolder[0]);
            assertEquals(1, initialPeriods.size());
            originalPeriodToDateHolder[0] = initialPeriods.getFirst().getToDate();
        });

        // advance business date past the first period's toDate and run COB so the period gets evaluated
        BusinessDateHelper.runAt("01 July 2026", () -> {
            FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanIdHolder[0])));

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterCob = getRangeSchedule(loanIdHolder[0]);
            assertEquals(2, periodsAfterCob.size(), "COB should generate the next period once the first one has expired");
            assertNotNull(periodsAfterCob.getFirst().getMinPaymentCriteriaMet(),
                    "First period must have been evaluated by COB before the backdated pause is created");

            final LocalDate secondPeriodOriginalFromDate = periodsAfterCob.get(1).getFromDate();
            final LocalDate secondPeriodOriginalToDate = periodsAfterCob.get(1).getToDate();
            assertNotNull(secondPeriodOriginalFromDate);
            assertNotNull(secondPeriodOriginalToDate);

            // when - a backdated pause is created starting inside the already-evaluated first period
            final LocalDate pauseStart = LocalDate.of(2026, 6, 5);
            final LocalDate pauseEnd = LocalDate.of(2026, 6, 15);
            final PostWorkingCapitalLoansDelinquencyActionResponse result = WorkingCapitalLoanDelinquencyActionHelper
                    .createDelinquencyAction(loanIdHolder[0], "pause", pauseStart, pauseEnd);
            assertNotNull(result, "Backdated pause into an already-evaluated period should now be accepted");

            // then - the evaluated period's boundaries shift by the pause length and it is no longer closed
            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterPause = getRangeSchedule(loanIdHolder[0]);
            assertEquals(2, periodsAfterPause.size());

            final WorkingCapitalLoanDelinquencyRangeScheduleData firstPeriod = periodsAfterPause.getFirst();
            assertEquals(originalPeriodToDateHolder[0].plusDays(11), firstPeriod.getToDate(),
                    "First period toDate should be extended by the 11-day inclusive pause duration");
            assertNull(firstPeriod.getMinPaymentCriteriaMet(),
                    "First period must be reopened (unevaluated) once its due date moves past the current business date");

            final WorkingCapitalLoanDelinquencyRangeScheduleData secondPeriod = periodsAfterPause.get(1);
            assertEquals(secondPeriodOriginalFromDate.plusDays(11), secondPeriod.getFromDate(),
                    "Second period fromDate should also shift by the pause duration");
            assertEquals(secondPeriodOriginalToDate.plusDays(11), secondPeriod.getToDate(),
                    "Second period toDate should also shift by the pause duration");
        });
    }

    /**
     * GET delinquency actions returns empty list for a loan with no actions.
     */
    @Test
    public void testGetActionsReturnsEmptyListWhenNoneExist() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when
        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);

        // then
        assertNotNull(actions);
        assertTrue(actions.isEmpty(), "No actions should exist for a fresh loan");
    }

    /**
     * Create a pause via external ID and retrieve via external ID — verifies both external ID endpoints work correctly.
     */
    @Test
    public void testCreateAndRetrieveDelinquencyActionByExternalId() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final String externalId = Utils.randomStringGenerator("WCL_EXT_", 12);
        final Long loanId = submitAndApproveLoanWithExternalId(clientId, productId, externalId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when - create pause via external ID
        final LocalDate pauseStart = disbursementDate;
        final LocalDate pauseEnd = disbursementDate.plusDays(10);
        final PostWorkingCapitalLoansDelinquencyActionResponse createResult = WorkingCapitalLoanDelinquencyActionHelper
                .createDelinquencyActionByExternalId(externalId, "pause", pauseStart, pauseEnd);
        assertNotNull(createResult);
        assertNotNull(createResult.getResourceId());

        // then - retrieve via external ID should return the action
        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActionsByExternalId(externalId);
        assertEquals(1, actions.size());
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.getFirst().getAction());
        assertEquals(pauseStart, actions.getFirst().getStartDate());
        assertEquals(pauseEnd, actions.getFirst().getEndDate());

        // and - retrieve via loanId should return the same action (cross-check)
        final List<WorkingCapitalLoanDelinquencyActionData> actionsById = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(1, actionsById.size());
        assertEquals(actions.getFirst().getId(), actionsById.getFirst().getId());
    }

    /**
     * External ID that does not exist should return 404.
     */
    @Test
    public void testDelinquencyActionWithNonExistentExternalIdReturns404() {
        final String nonExistentExternalId = Utils.randomStringGenerator("NON_EXISTENT_", 12);

        // POST with non-existent external ID
        CallFailedRuntimeException postException = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyActionByExternalId(nonExistentExternalId, "pause",
                        LocalDate.now(ZoneId.systemDefault()), LocalDate.now(ZoneId.systemDefault()).plusDays(5)));
        assertEquals(404, postException.getStatus());

        // GET with non-existent external ID
        CallFailedRuntimeException getException = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.retrieveDelinquencyActionsByExternalId(nonExistentExternalId));
        assertEquals(404, getException.getStatus());
    }

    /**
     * With an existing future-dated pause, a second future-dated pause sharing the same startDate but a longer endDate
     * must be rejected with 400 and the overlap message.
     */
    @Test
    public void testFutureDatedOverlappingPauseSameStartLongerEndIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given - an existing pause entirely in the future relative to the business date
            final LocalDate pause1Start = businessDate.plusDays(61);
            final LocalDate pause1End = businessDate.plusDays(68);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when - a second future-dated pause with the same start but a longer end
            // then - rejected with 400 + overlap message
            assertPauseRejectedAsOverlapping(loanIdHolder[0], pause1Start, businessDate.plusDays(74));

            // and - only the original pause is persisted, with its dates unchanged
            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected overlapping pause must not be persisted");
            assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.getFirst().getAction());
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * A future-dated pause identical to an existing one (same startDate and endDate) must be rejected with 400 and the
     * overlap message.
     */
    @Test
    public void testFutureDatedIdenticalDuplicatePauseIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given
            final LocalDate pause1Start = businessDate.plusDays(61);
            final LocalDate pause1End = businessDate.plusDays(68);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when / then - an identical duplicate pause is rejected
            assertPauseRejectedAsOverlapping(loanIdHolder[0], pause1Start, pause1End);

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected duplicate pause must not be persisted");
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * With an existing backdated pause, a second backdated pause sharing the same startDate but a different endDate
     * must be rejected with 400 and the overlap message.
     */
    @Test
    public void testBackdatedOverlappingPauseSameStartDifferentEndIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given - an existing pause entirely in the past relative to the business date
            final LocalDate pause1Start = businessDate.minusDays(10);
            final LocalDate pause1End = businessDate.minusDays(5);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when / then - a backdated pause with the same start but a different end is rejected
            assertPauseRejectedAsOverlapping(loanIdHolder[0], pause1Start, businessDate.minusDays(1));

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected overlapping backdated pause must not be persisted");
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * A new pause starting exactly on an existing pause's endDate (inclusive boundary) must be rejected with 400 and
     * the overlap message.
     */
    @Test
    public void testBackdatedPauseStartingOnExistingPauseEndIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given
            final LocalDate pause1Start = businessDate.minusDays(10);
            final LocalDate pause1End = businessDate.minusDays(5);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when / then - a new pause starting on the existing pause's endDate is rejected (inclusive intervals)
            assertPauseRejectedAsOverlapping(loanIdHolder[0], pause1End, businessDate.plusDays(3));

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected boundary-overlapping pause must not be persisted");
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * A new pause ending exactly on an existing pause's startDate (inclusive boundary) must be rejected with 400 and
     * the overlap message.
     */
    @Test
    public void testPauseEndingOnExistingPauseStartIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given
            final LocalDate pause1Start = businessDate.plusDays(10);
            final LocalDate pause1End = businessDate.plusDays(15);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when / then - a new pause ending on the existing pause's startDate is rejected (inclusive intervals)
            assertPauseRejectedAsOverlapping(loanIdHolder[0], businessDate.plusDays(5), pause1Start);

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected boundary-overlapping pause must not be persisted");
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * A new pause fully containing an existing pause must be rejected with 400 and the overlap message.
     */
    @Test
    public void testPauseFullyContainingExistingPauseIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given
            final LocalDate pause1Start = businessDate.plusDays(10);
            final LocalDate pause1End = businessDate.plusDays(15);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when / then - a new pause that fully contains the existing one is rejected
            assertPauseRejectedAsOverlapping(loanIdHolder[0], businessDate.plusDays(5), businessDate.plusDays(20));

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected containing pause must not be persisted");
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * A new pause fully inside an existing pause must be rejected with 400 and the overlap message.
     */
    @Test
    public void testPauseFullyInsideExistingPauseIsRejected() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given
            final LocalDate pause1Start = businessDate.plusDays(5);
            final LocalDate pause1End = businessDate.plusDays(20);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when / then - a new pause entirely inside the existing one is rejected
            assertPauseRejectedAsOverlapping(loanIdHolder[0], businessDate.plusDays(10), businessDate.plusDays(15));

            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(1, actions.size(), "The rejected contained pause must not be persisted");
            assertEquals(pause1Start, actions.getFirst().getStartDate(), "Existing pause startDate must be unchanged");
            assertEquals(pause1End, actions.getFirst().getEndDate(), "Existing pause endDate must be unchanged");
        });
    }

    /**
     * Two future-dated pauses with a 1-day gap between the inclusive periods must both be accepted and persisted with
     * their exact dates.
     */
    @Test
    public void testAdjacentFutureDatedPausesAreAccepted() {
        final Long[] loanIdHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);

        BusinessDateHelper.runAt("17 June 2026", () -> loanIdHolder[0] = createActiveLoan(businessDate.minusDays(14)));

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // given
            final LocalDate pause1Start = businessDate.plusDays(10);
            final LocalDate pause1End = businessDate.plusDays(15);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanIdHolder[0], "pause", pause1Start, pause1End);

            // when - the second pause starts the day after the first pause ends
            final LocalDate pause2Start = businessDate.plusDays(16);
            final LocalDate pause2End = businessDate.plusDays(20);
            final PostWorkingCapitalLoansDelinquencyActionResponse result = WorkingCapitalLoanDelinquencyActionHelper
                    .createDelinquencyAction(loanIdHolder[0], "pause", pause2Start, pause2End);
            assertNotNull(result, "Adjacent non-overlapping future-dated pause must be accepted");

            // then - both pauses persisted with their exact dates
            final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanIdHolder[0]);
            assertEquals(2, actions.size(), "Both adjacent future-dated pauses must be persisted");
            assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.get(0).getAction());
            assertEquals(pause1Start, actions.get(0).getStartDate());
            assertEquals(pause1End, actions.get(0).getEndDate());
            assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.get(1).getAction());
            assertEquals(pause2Start, actions.get(1).getStartDate());
            assertEquals(pause2End, actions.get(1).getEndDate());
        });
    }

    /**
     * Once a pause is resumed, its blocked window ends at the RESUME-aware effective end date. A new pause starting the
     * day after the effective end must be accepted (loan A); a new pause starting exactly on the effective end must be
     * rejected as overlapping (loan B).
     */
    @Test
    public void testPauseAfterResumeRespectsEffectiveEndBoundary() {
        final Long[] loanAHolder = new Long[1];
        final Long[] loanBHolder = new Long[1];
        final LocalDate businessDate = LocalDate.of(2026, 7, 1);
        final LocalDate pause1Start = businessDate.minusDays(5);
        final LocalDate pause1End = businessDate.plusDays(10);

        // given - two loans, each with an active pause [D-5, D+10] created before the resume date
        BusinessDateHelper.runAt("26 June 2026", () -> {
            loanAHolder[0] = createActiveLoan(businessDate.minusDays(14));
            loanBHolder[0] = createActiveLoan(businessDate.minusDays(14));
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanAHolder[0], "pause", pause1Start, pause1End);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanBHolder[0], "pause", pause1Start, pause1End);
        });

        BusinessDateHelper.runAt("01 July 2026", () -> {
            // and - both pauses are resumed on the business date D, shortening the blocked window to [D-5, D]
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanAHolder[0], "resume", businessDate, null);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanBHolder[0], "resume", businessDate, null);

            // when (a) - loan A: a new pause starting the day after the effective end (D+1) is accepted
            final LocalDate pause2Start = businessDate.plusDays(1);
            final LocalDate pause2End = businessDate.plusDays(8);
            final PostWorkingCapitalLoansDelinquencyActionResponse result = WorkingCapitalLoanDelinquencyActionHelper
                    .createDelinquencyAction(loanAHolder[0], "pause", pause2Start, pause2End);
            assertNotNull(result, "Pause starting the day after the resumed pause's effective end must be accepted");

            // then (a) - loan A has 2 PAUSE rows with exact dates, first one shortened via effectiveEndDate = D
            final List<WorkingCapitalLoanDelinquencyActionData> loanAActions = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanAHolder[0]);
            final List<WorkingCapitalLoanDelinquencyActionData> loanAPauses = loanAActions.stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE == a.getAction()).toList();
            assertEquals(2, loanAPauses.size(), "Loan A must have exactly 2 persisted PAUSE rows");
            assertEquals(pause1Start, loanAPauses.get(0).getStartDate());
            assertEquals(pause1End, loanAPauses.get(0).getEndDate(), "Original endDate of the resumed pause must be unchanged");
            assertEquals(businessDate, loanAPauses.get(0).getEffectiveEndDate(),
                    "Resumed pause's effectiveEndDate must equal the resume date D");
            assertEquals(pause2Start, loanAPauses.get(1).getStartDate());
            assertEquals(pause2End, loanAPauses.get(1).getEndDate());

            // when/then (b) - loan B: a new pause starting exactly on the effective end (D) is rejected as overlap
            assertPauseRejectedAsOverlapping(loanBHolder[0], businessDate, businessDate.plusDays(8));

            final List<WorkingCapitalLoanDelinquencyActionData> loanBPauses = WorkingCapitalLoanDelinquencyActionHelper
                    .retrieveDelinquencyActions(loanBHolder[0]).stream()
                    .filter(a -> WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE == a.getAction()).toList();
            assertEquals(1, loanBPauses.size(), "Loan B must still have only the original PAUSE row");
        });
    }

    // ===================== Helper Methods =====================

    private void assertPauseRejectedAsOverlapping(final Long loanId, final LocalDate startDate, final LocalDate endDate) {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", startDate, endDate));
        assertEquals(400, exception.getStatus(), "Overlapping pause must be rejected with HTTP 400");
        assertNotNull(exception.getResponseBody(), "Overlap rejection must carry an error body");
        assertTrue(exception.getResponseBody().contains(OVERLAP_ERROR_MESSAGE),
                "Error body must contain '" + OVERLAP_ERROR_MESSAGE + "' but was: " + exception.getResponseBody());
    }

    private Long createActiveLoan(final LocalDate disbursementDate) {
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);
        return loanId;
    }

    private Long createWorkingCapitalLoanDelinquencyBucket(final int frequencyDays) {
        final PostDelinquencyRangeResponse range1 = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(30).locale("en"));
        assertNotNull(range1);

        final List<Integer> rangeIds = new ArrayList<>();
        rangeIds.add(Math.toIntExact(range1.getResourceId()));

        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(rangeIds.stream().map(Long::valueOf).toList(), frequencyDays, 0,
                        new BigDecimal("3"), 1);
        assertNotNull(bucket);
        log.info("Created WC delinquency bucket id={}", bucket.getResourceId());
        return bucket.getResourceId();
    }

    private Long createProduct(final Long delinquencyBucketId) {
        final String uniqueName = "WCL Product " + Utils.randomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDelinquencyBucketId(delinquencyBucketId).build()).getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private Long submitAndApproveLoan(final Long clientId, final Long productId) {
        return submitAndApproveLoanWithExternalId(clientId, productId, null);
    }

    private Long submitAndApproveLoanWithExternalId(final Long clientId, final Long productId, final String externalId) {
        final WorkingCapitalLoanApplicationTestBuilder builder = new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(10000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000));
        if (externalId != null) {
            builder.withExternalId(externalId);
        }
        final Long loanId = applicationHelper.submit(builder.buildSubmitRequest());

        final LocalDate submittedOnDate = FeignCalls
                .ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId))
                .getTimeline().getSubmittedOnDate();
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate));
        return loanId;
    }

    private List<WorkingCapitalLoanDelinquencyRangeScheduleData> getRangeSchedule(final Long loanId) {
        return WorkingCapitalLoanDelinquencyRangeScheduleHelper.getDelinquencyRangeSchedule(loanId);
    }
}
