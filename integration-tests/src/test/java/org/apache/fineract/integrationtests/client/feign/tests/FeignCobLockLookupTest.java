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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.models.LoanAccountLock.LockOwnerEnum.LOAN_COB_CHUNK_PROCESSING;
import static org.apache.fineract.client.models.LoanAccountLock.LockOwnerEnum.LOAN_INLINE_COB_PROCESSING;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.firstError;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DATETIME_PATTERN;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.feign.services.LoansApi.RetrieveOneLoanQueryParams;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdStatus;
import org.apache.fineract.client.models.JobParameterDTO;
import org.apache.fineract.client.models.LoanAccountLock;
import org.apache.fineract.client.models.LoanAccountLock.LockOwnerEnum;
import org.apache.fineract.client.models.LockRequest;
import org.apache.fineract.client.models.OldestCOBProcessedLoanDTO;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FeignCobLockLookupTest extends FeignIntegrationTest {

    private static final double LOAN_PRINCIPAL = 1000.0;
    private static final BigDecimal WORKING_CAPITAL_PRINCIPAL = BigDecimal.valueOf(10000);
    private static final BigDecimal PERIOD_PAYMENT_RATE = BigDecimal.valueOf(18);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH);

    private final FeignClientHelper clients = new FeignClientHelper(fineractClient());
    private final FeignLoanHelper loans = new FeignLoanHelper(fineractClient());
    private final FeignTransactionHelper transactions = new FeignTransactionHelper(fineractClient());
    private final FeignSchedulerHelper schedulerHelper = new FeignSchedulerHelper(fineractClient());
    private final FeignWorkingCapitalLoanHelper workingCapitalLoans = new FeignWorkingCapitalLoanHelper(fineractClient());
    private final List<Executable> loanCleanups = new ArrayList<>();

    @ParameterizedTest
    @EnumSource(LoanType.class)
    void scheduledCobByShortNameSkipsInlineLocksAndProcessesChunkLocksAndUnlockedLoans(final LoanType loanType) {
        withPausedScheduler(() -> {
            final LocalDate cobDate = getCobDate(loanType);
            final Long productId = createProduct(loanType);
            final LocalDate disbursementDate = cobDate.minusDays(1);
            final Long inlineLocked = disburseLoan(loanType, productId, disbursementDate);
            final Long chunkLocked = disburseLoan(loanType, productId, disbursementDate);
            final Long unlocked = disburseLoan(loanType, productId, disbursementDate);

            placeLock(loanType, inlineLocked, LOAN_INLINE_COB_PROCESSING,
                    new LockRequest().cobBusinessDate(cobDate).error("Retryable inline lock"));
            try {
                placeLock(loanType, chunkLocked, LOAN_COB_CHUNK_PROCESSING, new LockRequest().cobBusinessDate(cobDate));
                executeCob(loanType, cobDate, CobExecution.BY_SHORT_NAME);

                assertNull(getLastClosedDate(loanType, inlineLocked));
                assertEquals(cobDate, getLastClosedDate(loanType, chunkLocked));
                assertEquals(cobDate, getLastClosedDate(loanType, unlocked));
                if (loanType == LoanType.LOAN) {
                    assertRemainingLock(inlineLocked, List.of(chunkLocked, unlocked));
                }
            } finally {
                runInlineCob(loanType, inlineLocked);
            }
        });
    }

    @ParameterizedTest
    @EnumSource(LoanType.class)
    void catchUpJobProcessesALoanClosedOnThePreviousDayAndSkipsANeverClosedLoan(final LoanType loanType) {
        withPausedScheduler(() -> {
            final LocalDate cobDate = getCobDate(loanType);
            final LocalDate earlierCobDate = cobDate.minusDays(1);
            final Long productId = createProduct(loanType);
            final Long loanId = prepareLoanClosedOn(loanType, productId, earlierCobDate);
            final Long neverClosedLoanId = disburseLoan(loanType, productId, earlierCobDate);
            assertNull(getLastClosedDate(loanType, neverClosedLoanId));

            executeCob(loanType, cobDate, CobExecution.CATCH_UP);

            assertEquals(cobDate, getLastClosedDate(loanType, loanId));
            assertNull(getLastClosedDate(loanType, neverClosedLoanId));
        });
    }

    private void withPausedScheduler(final Runnable scenario) {
        final boolean wasActive = schedulerHelper.getSchedulerStatus();
        try {
            schedulerHelper.stopScheduler();
            scenario.run();
        } finally {
            try {
                assertAll(loanCleanups);
            } finally {
                loanCleanups.clear();
                schedulerHelper.updateSchedulerStatus(wasActive);
            }
        }
    }

    private LocalDate getCobDate(final LoanType loanType) {
        final OldestCOBProcessedLoanDTO oldestLoan = switch (loanType) {
            case LOAN -> ok(() -> fineractClient().loanCobCatchUp().getOldestCOBProcessedLoan());
            case WORKING_CAPITAL -> ok(() -> fineractClient().workingCapitalLoanCobCatchUpApi().getOldestCOBProcessedLoan1());
        };
        final LocalDate cobDate = oldestLoan.getCobBusinessDate();
        assertNotNull(cobDate);
        return cobDate;
    }

    private Long createProduct(final LoanType loanType) {
        final Long productId = switch (loanType) {
            case LOAN -> loans
                    .createLoanProduct(new LoanProductTestBuilder().withPrincipal(Double.toString(LOAN_PRINCIPAL))
                            .withNumberOfRepayments("1").withinterestRatePerPeriod("0").withInterestTypeAsDecliningBalance().buildRequest())
                    .getResourceId();
            case WORKING_CAPITAL -> new WorkingCapitalLoanProductHelper().createWorkingCapitalLoanProduct(
                    new WorkingCapitalLoanProductTestBuilder().withName(Utils.uniqueRandomStringGenerator("WC COB ", 8))
                            .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                    .getResourceId();
        };
        assertNotNull(productId);
        return productId;
    }

    private Long disburseLoan(final LoanType loanType, final Long productId, final LocalDate disbursementDate) {
        final String date = DATE_FORMAT.format(disbursementDate);
        final Long clientId = clients.createClient(date);
        assertNotNull(clientId);
        if (loanType == LoanType.LOAN) {
            final Long loanId = loans.applyForLoan(LoanRequestBuilders.applyLoan(clientId, productId, date, LOAN_PRINCIPAL, 1)).getLoanId();
            assertNotNull(loanId);
            loanCleanups.add(() -> cleanupLoan(loanId));
            loans.approveLoan(loanId, LoanRequestBuilders.approveLoan(LOAN_PRINCIPAL, date));
            loans.disburseLoan(loanId, LoanRequestBuilders.disburseLoan(LOAN_PRINCIPAL, date));
            return loanId;
        }

        final Long loanId = workingCapitalLoans.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                WORKING_CAPITAL_PRINCIPAL, PERIOD_PAYMENT_RATE, date, date));
        assertNotNull(loanId);
        loanCleanups.add(() -> cleanupWorkingCapitalLoan(loanId));
        workingCapitalLoans.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, WORKING_CAPITAL_PRINCIPAL, date));
        workingCapitalLoans.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, WORKING_CAPITAL_PRINCIPAL));
        return loanId;
    }

    private Long prepareLoanClosedOn(final LoanType loanType, final Long productId, final LocalDate closingDate) {
        final Long loanId = disburseLoan(loanType, productId, closingDate);
        assertNull(getLastClosedDate(loanType, loanId));
        executeCob(loanType, closingDate, CobExecution.BY_ID);
        assertEquals(closingDate, getLastClosedDate(loanType, loanId));
        return loanId;
    }

    private void executeCob(final LoanType loanType, final LocalDate cobDate, final CobExecution execution) {
        schedulerHelper.executeAndAwaitJob(loanType.cobJob.toString(), job -> {
            final ExecuteJobRequest request = new ExecuteJobRequest()
                    .addJobParametersItem(new JobParameterDTO().parameterName("BusinessDate").parameterValue(cobDate.toString()));
            if (execution == CobExecution.CATCH_UP) {
                request.addJobParametersItem(new JobParameterDTO().parameterName("IS_CATCH_UP").parameterValue("true"));
            }
            final ApiResponse<Void> response;
            if (execution == CobExecution.BY_SHORT_NAME) {
                final String shortName = job.getShortName();
                assertNotNull(shortName);
                response = ok(() -> fineractClient().schedulerJob().executeJobByShortNameWithHttpInfo(shortName, "executeJob", request,
                        Map.of()));
            } else {
                final Long jobId = job.getJobId();
                assertNotNull(jobId);
                response = ok(() -> fineractClient().schedulerJob().executeJobWithHttpInfo(jobId, "executeJob", request, Map.of()));
            }
            assertEquals(202, response.getStatusCode());
        });
    }

    private void placeLock(final LoanType loanType, final Long loanId, final LockOwnerEnum owner, final LockRequest request) {
        final ApiResponse<Void> response = switch (loanType) {
            case LOAN -> ok(() -> fineractClient().loanAccountLock().placeLockOnLoanAccountWithHttpInfo(loanId, owner.getValue(), request,
                    Map.of()));
            case WORKING_CAPITAL -> ok(() -> fineractClient().workingCapitalLoanAccountLock()
                    .placeLockOnWorkingCapitalLoanAccountWithHttpInfo(loanId, owner.getValue(), request, Map.of()));
        };
        assertEquals(202, response.getStatusCode());
    }

    private LocalDate getLastClosedDate(final LoanType loanType, final Long loanId) {
        return switch (loanType) {
            case LOAN -> ok(() -> fineractClient().loans().retrieveOneLoan(loanId,
                    new RetrieveOneLoanQueryParams().fields("lastClosedBusinessDate"))).getLastClosedBusinessDate();
            case WORKING_CAPITAL -> workingCapitalLoans.getLoanDetails(loanId).getLastClosedBusinessDate();
        };
    }

    private void assertRemainingLock(final Long inlineLocked, final List<Long> processedLoanIds) {
        final List<LoanAccountLock> locks = ok(() -> fineractClient().loanAccountLock().retrieveLockedAccounts(0, 1000)).getContent();
        assertNotNull(locks);
        final List<LoanAccountLock> matchingLocks = locks.stream().filter(lock -> inlineLocked.equals(lock.getLoanId())).toList();
        assertEquals(1, matchingLocks.size());
        assertEquals(LOAN_INLINE_COB_PROCESSING, matchingLocks.getFirst().getLockOwner());
        assertFalse(locks.stream().anyMatch(lock -> processedLoanIds.contains(lock.getLoanId())));
    }

    private void runInlineCob(final LoanType loanType, final Long loanId) {
        switch (loanType) {
            case LOAN -> transactions.executeInlineCOB(loanId);
            case WORKING_CAPITAL -> workingCapitalLoans.executeInlineWCCOB(loanId);
        }
    }

    private void cleanupLoan(final Long loanId) {
        final GetLoansLoanIdStatus status = loans.getLoanDetails(loanId).getStatus();
        assertNotNull(status);
        final boolean active = Boolean.TRUE.equals(status.getActive());
        if (active) {
            loans.undoDisbursement(loanId);
        }
        if (active || Boolean.TRUE.equals(status.getWaitingForDisbursal())) {
            loans.undoApproval(loanId);
        }
        ok(() -> fineractClient().loans().deleteLoanApplication(loanId));
    }

    private void cleanupWorkingCapitalLoan(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loan = workingCapitalLoans.getLoanDetails(loanId);
        final GetWorkingCapitalLoansLoanIdStatus status = loan.getStatus();
        assertNotNull(status);
        final boolean active = Boolean.TRUE.equals(status.getActive());
        if (active) {
            workingCapitalLoans.undoDisbursal(loanId, WorkingCapitalLoanRequestBuilders.undoDisbursal());
        }
        if (active || Boolean.TRUE.equals(status.getWaitingForDisbursal())) {
            workingCapitalLoans.undoApproval(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
        }

        if (!workingCapitalLoans.getTransactions(loanId).isEmpty()) {
            assertNotNull(loan.getTimeline());
            final LocalDate submittedOnDate = loan.getTimeline().getSubmittedOnDate();
            ok(() -> fineractClient().workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, "reject",
                    WorkingCapitalLoanApplicationTestBuilder.buildRejectRequest(submittedOnDate)));
            final GetWorkingCapitalLoansLoanIdStatus rejectedStatus = workingCapitalLoans.getLoanDetails(loanId).getStatus();
            assertNotNull(rejectedStatus);
            assertEquals(LoanStatus.REJECTED.getCode(), rejectedStatus.getCode());
            return;
        }

        workingCapitalLoans.delete(loanId);
        final CallFailedRuntimeException failure = fail(
                () -> fineractClient().workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        assertEquals(404, failure.getStatus());
        assertEquals("error.msg.resource.not.found", failure.getUserMessageGlobalisationCode());
        assertEquals("error.msg.wc.loan.id.invalid", firstError(failure).userMessageGlobalisationCode());
    }

    private enum LoanType {

        LOAN(JobName.LOAN_COB), WORKING_CAPITAL(JobName.WORKING_CAPITAL_LOAN_COB_JOB);

        private final JobName cobJob;

        LoanType(final JobName cobJob) {
            this.cobJob = cobJob;
        }
    }

    private enum CobExecution {

        BY_ID, BY_SHORT_NAME, CATCH_UP
    }
}
