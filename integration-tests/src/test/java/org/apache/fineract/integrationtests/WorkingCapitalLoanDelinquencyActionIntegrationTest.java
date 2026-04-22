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
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
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

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

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

        // then - range schedule periods should be shifted by 10 days
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterPause = getRangeSchedule(loanId);
        assertEquals(1, periodsAfterPause.size());

        final LocalDate newToDate = periodsAfterPause.getFirst().getToDate();
        assertEquals(expectedPeriodToDate.plusDays(10), newToDate, "Period toDate should be extended by 10 days (the pause duration)");

        // and - GET returns the saved action
        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(1, actions.size());
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.PAUSE, actions.getFirst().getAction());
        assertEquals(pauseStart, actions.getFirst().getStartDate());
        assertEquals(pauseEnd, actions.getFirst().getEndDate());
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

        // First period: fromDate unchanged (contains pauseStart), toDate extended by 7
        assertEquals(firstPeriodOriginalFromDate, periodsAfterPause.get(0).getFromDate(), "First period fromDate should stay unchanged");
        assertEquals(firstPeriodOriginalToDate.plusDays(7), periodsAfterPause.get(0).getToDate(),
                "First period toDate should be extended by 7 days");

        // Second period: both fromDate and toDate shifted by 7 (starts after pauseStart)
        assertEquals(secondPeriodOriginalFromDate.plusDays(7), periodsAfterPause.get(1).getFromDate(),
                "Second period fromDate should shift by 7 days");
        assertEquals(secondPeriodOriginalToDate.plusDays(7), periodsAfterPause.get(1).getToDate(),
                "Second period toDate should shift by 7 days");
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

        // when - send action type "resume" which is not supported yet
        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "resume", disbursementDate,
                        disbursementDate.plusDays(10)));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for unsupported action 'resume': {}", exception.getMessage());
    }

    /**
     * startDate >= endDate is rejected with 400.
     */
    @Test
    public void testStartDateNotBeforeEndDateIsRejected() {
        // given
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoan(clientId, productId);

        final LocalDate disbursementDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, disbursementDate);

        // when - startDate == endDate (not strictly before)
        // then - should fail with 400
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", disbursementDate,
                        disbursementDate));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for startDate == endDate: {}", exception.getMessage());
    }

    /**
     * Two consecutive (non-overlapping) pauses are accepted: first pause ends on day 10, second starts on day 10
     * (touching but not overlapping).
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

        // First pause: [disbursement, disbursement+10)
        final LocalDate pause1Start = disbursementDate;
        final LocalDate pause1End = disbursementDate.plusDays(10);
        WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", pause1Start, pause1End);

        // when - second pause starts exactly where first ends (touching, not overlapping)
        final LocalDate pause2Start = pause1End;
        final LocalDate pause2End = pause1End.plusDays(5);
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

    // ===================== Helper Methods =====================

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
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(10000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(10000));
        if (externalId != null) {
            builder.withExternalId(externalId);
        }
        final Long loanId = applicationHelper.submit(builder.buildSubmitJson());

        final LocalDate submittedOnDate = FeignCalls
                .ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId))
                .getSubmittedOnDate();
        applicationHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveJson(submittedOnDate));
        return loanId;
    }

    private List<WorkingCapitalLoanDelinquencyRangeScheduleData> getRangeSchedule(final Long loanId) {
        return WorkingCapitalLoanDelinquencyRangeScheduleHelper.getDelinquencyRangeSchedule(loanId);
    }
}
