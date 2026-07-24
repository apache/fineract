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
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * Integration tests (via the Fineract Feign Client) for the Working Capital loan "Delinquency Disable" feature and its
 * reversal (undo). Covers: disable applied as of the current date, singleton-until-reversed, blocking of
 * pause/resume/reschedule while disabled, and reversal re-enabling the account.
 */
@Slf4j
public class WorkingCapitalLoanDelinquencyDisableIntegrationTest {

    private static final int PERIOD_FREQUENCY_DAYS = 30;
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(10000);

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    /**
     * Disable is applied as of the current business date, has no end date while active and is visible via GET.
     */
    @Test
    public void testDisableIsAppliedAsOfCurrentDate() {
        final Long loanId = setupActiveLoan();

        final PostWorkingCapitalLoansDelinquencyActionResponse result = WorkingCapitalLoanDelinquencyActionHelper
                .disableDelinquency(loanId);
        assertNotNull(result);
        assertNotNull(result.getResourceId());

        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(1, actions.size());
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.DISABLE, actions.getFirst().getAction());
        assertEquals(LocalDate.now(ZoneId.systemDefault()), actions.getFirst().getStartDate(),
                "Disable must be applied as of the current business date");
        assertNull(actions.getFirst().getEndDate(), "An active disable must not have an end date");
    }

    /**
     * A second disable is rejected while a disable is already active (only once until reversed).
     */
    @Test
    public void testDisableIsSingletonUntilReversed() {
        final Long loanId = setupActiveLoan();
        WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for a second disable while one is active: {}", exception.getMessage());
    }

    /**
     * While disabled, pause, resume and reschedule actions are all rejected with 400.
     */
    @Test
    public void testPauseResumeRescheduleBlockedWhileDisabled() {
        final Long loanId = setupActiveLoan();
        final LocalDate today = LocalDate.now(ZoneId.systemDefault());
        WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId);

        final CallFailedRuntimeException pauseException = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "pause", today, today.plusDays(5)));
        assertEquals(400, pauseException.getStatus());

        final PostWorkingCapitalLoansDelinquencyActionRequest resumeRequest = WorkingCapitalLoanDelinquencyActionHelper
                .buildActionRequest("resume", today, null);
        final CallFailedRuntimeException resumeException = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, resumeRequest));
        assertEquals(400, resumeException.getStatus());

        final PostWorkingCapitalLoansDelinquencyActionRequest rescheduleRequest = new PostWorkingCapitalLoansDelinquencyActionRequest();
        rescheduleRequest.setAction("reschedule");
        rescheduleRequest.setMinimumPayment(new BigDecimal("5"));
        rescheduleRequest.setMinimumPaymentType("PERCENTAGE");
        rescheduleRequest.setDateFormat("yyyy-MM-dd");
        rescheduleRequest.setLocale("en");
        final CallFailedRuntimeException rescheduleException = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, rescheduleRequest));
        assertEquals(400, rescheduleException.getStatus());
    }

    /**
     * An enable persists its own ENABLE row and closes the active disable (as of the day before the enable date, like
     * breach); a new disable is allowed again afterwards.
     */
    @Test
    public void testEnableClosesDisableAndAllowsDisablingAgain() {
        final Long loanId = setupActiveLoan();
        WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId);

        final PostWorkingCapitalLoansDelinquencyActionResponse enableResult = WorkingCapitalLoanDelinquencyActionHelper
                .enableDelinquency(loanId);
        assertNotNull(enableResult.getResourceId());

        final List<WorkingCapitalLoanDelinquencyActionData> actionsAfterEnable = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(2, actionsAfterEnable.size());
        final WorkingCapitalLoanDelinquencyActionData closedDisable = actionsAfterEnable.getFirst();
        final WorkingCapitalLoanDelinquencyActionData enableRow = actionsAfterEnable.get(1);
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.DISABLE, closedDisable.getAction());
        assertEquals(LocalDate.now(ZoneId.systemDefault()).minusDays(1), closedDisable.getEndDate(),
                "The disable must be closed as of the day before the enable date");
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.ENABLE, enableRow.getAction());
        assertEquals(LocalDate.now(ZoneId.systemDefault()), enableRow.getStartDate(),
                "The enable row is stamped with the enable (current) date");
        assertNull(enableRow.getEndDate(), "The enable row has no end date");

        // once enabled, disabling again is allowed
        final PostWorkingCapitalLoansDelinquencyActionResponse secondDisable = WorkingCapitalLoanDelinquencyActionHelper
                .disableDelinquency(loanId);
        assertNotNull(secondDisable.getResourceId());
        final List<WorkingCapitalLoanDelinquencyActionData> actionsAfterSecondDisable = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActions(loanId);
        assertEquals(3, actionsAfterSecondDisable.size());
    }

    /**
     * Undo without an active disable is rejected with 400.
     */
    @Test
    public void testUndoWithoutActiveDisableIsRejected() {
        final Long loanId = setupActiveLoan();

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.enableDelinquency(loanId));
        assertEquals(400, exception.getStatus());
        log.info("Expected 400 for undo with no active disable: {}", exception.getMessage());
    }

    /**
     * Disable is rejected on a non-active (only approved) WC loan.
     */
    @Test
    public void testDisableOnNonActiveLoanIsRejected() {
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoanWithExternalId(clientId, productId, null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId));
        assertEquals(400, exception.getStatus());
    }

    /**
     * Disable and enable work through the external-id endpoints.
     */
    @Test
    public void testDisableAndEnableByExternalId() {
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final String externalId = Utils.randomStringGenerator("WCL_DIS_", 12);
        final Long loanId = submitAndApproveLoanWithExternalId(clientId, productId, externalId);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, LocalDate.now(ZoneId.systemDefault()).minusDays(5));

        final PostWorkingCapitalLoansDelinquencyActionResponse disableResult = WorkingCapitalLoanDelinquencyActionHelper
                .disableDelinquencyByExternalId(externalId);
        assertNotNull(disableResult.getResourceId());

        final PostWorkingCapitalLoansDelinquencyActionResponse enableResult = WorkingCapitalLoanDelinquencyActionHelper
                .enableDelinquencyByExternalId(externalId);
        assertNotNull(enableResult.getResourceId());

        final List<WorkingCapitalLoanDelinquencyActionData> actions = WorkingCapitalLoanDelinquencyActionHelper
                .retrieveDelinquencyActionsByExternalId(externalId);
        assertEquals(2, actions.size());
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.DISABLE, actions.getFirst().getAction());
        assertNotNull(actions.getFirst().getEndDate(), "The closed disable must have an end date");
        assertEquals(WorkingCapitalLoanDelinquencyActionData.ActionEnum.ENABLE, actions.get(1).getAction());
    }

    /**
     * While disabled, a repayment undo must not write any delinquency data on the range schedule periods (the
     * delinquency data is frozen); once the disable is reversed, delinquency is recomputed as of the undo date.
     */
    @Test
    public void testRepaymentUndoWhileDisabledWritesNoDelinquencyData() {
        // Activated 40 days ago with a 30-day frequency: the first period is already expired (delinquent-eligible).
        final Long loanId = setupActiveLoan(40);
        WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId);

        applicationHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                .buildRepaymentRequest(LocalDate.now(ZoneId.systemDefault()), new BigDecimal("100"), null, "repayment", 1, null));
        final Long repaymentTransactionId = findLatestTransactionId(loanId);
        applicationHelper.undoTransactionByLoanId(loanId, repaymentTransactionId);

        // The undo happened while disabled: no delinquency data may have been written and no tag applied.
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsWhileDisabled = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .getDelinquencyRangeSchedule(loanId);
        for (final WorkingCapitalLoanDelinquencyRangeScheduleData period : periodsWhileDisabled) {
            assertNull(period.getDelinquentAmount(), "No delinquent amount may be written while delinquency is disabled");
            assertNull(period.getDelinquentDays(), "No delinquent days may be written while delinquency is disabled");
        }
        assertTrue(retrieveTagHistory(loanId).isEmpty(), "No delinquency classification may be applied while disabled");

        // Reversing the disable recomputes the delinquency data as of the undo date.
        WorkingCapitalLoanDelinquencyActionHelper.enableDelinquency(loanId);
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfterUndo = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .getDelinquencyRangeSchedule(loanId);
        final WorkingCapitalLoanDelinquencyRangeScheduleData expiredPeriod = periodsAfterUndo.getFirst();
        assertNotNull(expiredPeriod.getDelinquentAmount(), "Delinquency must be recomputed once the disable is reversed");
        assertEquals(0, expiredPeriod.getDelinquentAmount().compareTo(expiredPeriod.getOutstandingAmount()));
        assertTrue(expiredPeriod.getDelinquentDays() > 0);
    }

    /**
     * Disabling delinquency lifts any active delinquency classification tag as of the disable date.
     */
    @Test
    public void testDisableLiftsActiveDelinquencyClassification() {
        final GlobalConfigurationHelper globalConfigurationHelper = new GlobalConfigurationHelper();
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            final Long loanId = setupActiveLoan(40);
            final LocalDate today = LocalDate.now(ZoneId.systemDefault());

            // The repayment triggers the instant classification and the expired unpaid period gets tagged.
            applicationHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(today,
                    new BigDecimal("10"), null, "repayment", 1, null));
            final List<GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse> tagsBeforeDisable = retrieveTagHistory(loanId);
            assertEquals(1, tagsBeforeDisable.size());
            assertNull(tagsBeforeDisable.getFirst().getLiftedOnDate(), "The classification tag must be active before the disable");

            WorkingCapitalLoanDelinquencyActionHelper.disableDelinquency(loanId);

            final List<GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse> tagsAfterDisable = retrieveTagHistory(loanId);
            assertEquals(1, tagsAfterDisable.size());
            assertEquals(today, tagsAfterDisable.getFirst().getLiftedOnDate(),
                    "The active classification tag must be lifted as of the disable date");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    /**
     * The disabled window behaves like a pause: reversing the disable extends the open periods by the (inclusive)
     * disabled days, so the window does not count towards delinquency.
     */
    @Test
    public void testUndoDisableDoesNotShiftPeriods() {
        final Long loanId = setupActiveLoan();
        final LocalDate disableDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate enableDate = disableDate.plusDays(3);

        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsBefore = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .getDelinquencyRangeSchedule(loanId);
        final LocalDate toDateBefore = periodsBefore.getFirst().getToDate();

        final GlobalConfigurationHelper globalConfigurationHelper = new GlobalConfigurationHelper();
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            // Disable on the current business date, advance three days, then enable.
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, disableDate);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "disable", disableDate, null);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, enableDate);
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, "enable", enableDate, null);

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periodsAfter = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                    .getDelinquencyRangeSchedule(loanId);
            // Matching breach: a disable is NOT treated as a pause, so re-enabling must not shift the period dates -
            // the
            // reprocess only re-evaluates and reclassifies delinquency.
            assertEquals(toDateBefore, periodsAfter.getFirst().getToDate(),
                    "Re-enabling delinquency must not shift period dates (a disable is not a pause)");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    // ===================== Helper Methods =====================

    private Long setupActiveLoan() {
        return setupActiveLoan(5);
    }

    private Long setupActiveLoan(final int activatedDaysAgo) {
        final Long bucketId = createWorkingCapitalLoanDelinquencyBucket(PERIOD_FREQUENCY_DAYS);
        final Long productId = createProduct(bucketId);
        final Long clientId = createClient();
        final Long loanId = submitAndApproveLoanWithExternalId(clientId, productId, null);
        WorkingCapitalLoanDelinquencyActionHelper.activateLoan(loanId, LocalDate.now(ZoneId.systemDefault()).minusDays(activatedDaysAgo));
        return loanId;
    }

    private Long findLatestTransactionId(final Long loanId) {
        return applicationHelper.retrieveTransactionsByLoanId(loanId).getContent().stream()
                .map(GetWorkingCapitalLoanTransactionIdResponse::getId).max(Comparator.naturalOrder()).orElseThrow();
    }

    private static List<GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse> retrieveTagHistory(final Long loanId) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans()
                .getDelinquencyRangeScheduleTagHistoryById(loanId));
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

    private Long submitAndApproveLoanWithExternalId(final Long clientId, final Long productId, final String externalId) {
        final WorkingCapitalLoanApplicationTestBuilder builder = new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(PRINCIPAL)
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000));
        if (externalId != null) {
            builder.withExternalId(externalId);
        }
        final Long loanId = applicationHelper.submit(builder.buildSubmitRequest());

        final LocalDate submittedOnDate = FeignCalls
                .ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId))
                .getTimeline().getSubmittedOnDate();
        // Approve explicitly with the submitted principal so the approved amount never relies on server-side defaults.
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(submittedOnDate, PRINCIPAL, null));
        return loanId;
    }
}
