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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.IsCatchUpRunningDTO;
import org.apache.fineract.client.models.LockRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.WorkingCapitalLoanTestHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalLoanCobStepDef extends AbstractStepDef {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final WorkingCapitalLoanTestHelper wcLoanHelper;
    private final FineractFeignClient fineractClient;

    @Before(value = "@WCCOBFeature")
    public void beforeWcCobScenario() {
        testContext().set(TestContextKey.WC_LOAN_IDS, new ArrayList<Long>());
    }

    // order > 10000 (default) so this cleanup runs before other @After hooks that may depend on DB state
    @After(value = "@WCCOBFeature", order = 10001)
    public void afterWcCobScenario() {
        List<Long> loanIds = getTrackedLoanIds();
        if (!loanIds.isEmpty()) {
            log.debug("After hook: cleaning up {} WC loan(s)", loanIds.size());
            for (Long loanId : loanIds) {
                try {
                    wcLoanHelper.deleteById(loanId);
                    log.debug("After hook: deleted WC loan id={}", loanId);
                } catch (Exception e) {
                    log.warn("After hook: failed to delete WC loan id={}: {}", loanId, e.getMessage());
                }
            }
            loanIds.clear();
        }
    }

    @When("Admin runs inline COB job for Working Capital Loan")
    public void runWorkingCapitalInlineCOB() {
        InlineJobRequest inlineJobRequest = new InlineJobRequest().addLoanIdsItem(getTrackedLoanIds().getLast());
        ok(() -> fineractClient.inlineJob().executeInlineJob("WC_LOAN_COB", inlineJobRequest));
    }

    @When("Admin runs inline COB job for Working Capital Loan by loanId")
    public void runWorkingCapitalInlineCOBByLoanId() {
        PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse);
        Long loanId = loanResponse.getLoanId();

        InlineJobRequest inlineJobRequest = new InlineJobRequest().addLoanIdsItem(loanId);

        ok(() -> fineractClient.inlineJob().executeInlineJob("WC_LOAN_COB", inlineJobRequest));
    }

    @When("Admin runs inline COB job for all Working Capital Loans")
    public void runWorkingCapitalInlineCOBForAll() {
        InlineJobRequest inlineJobRequest = new InlineJobRequest();
        for (Long loanId : getTrackedLoanIds()) {
            inlineJobRequest.addLoanIdsItem(loanId);
        }
        ok(() -> fineractClient.inlineJob().executeInlineJob("WC_LOAN_COB", inlineJobRequest));
    }

    @Given("Admin inserts an active WC loan into the database")
    public void insertActiveWcLoan() {
        Long loanId = wcLoanHelper.insertActiveLoan(getClientId(), getProductId());
        log.debug("Inserted WC loan with id={}", loanId);
        getTrackedLoanIds().add(loanId);
    }

    @Given("Admin inserts {int} active WC loans into the database")
    public void insertMultipleActiveWcLoans(int count) {
        final Long clientId = getClientId();
        final Long productId = getProductId();
        for (int i = 0; i < count; i++) {
            Long loanId = wcLoanHelper.insertActiveLoan(clientId, productId);
            log.debug("Inserted WC loan with id={}", loanId);
            getTrackedLoanIds().add(loanId);
        }
    }

    @Then("Admin verifies all inserted WC loans have lastClosedBusinessDate {string}")
    public void verifyAllLoansHaveLastClosedBusinessDate(String expectedDate) {
        LocalDate expected = LocalDate.parse(expectedDate, DATE_FORMAT);
        List<Long> loanIds = getTrackedLoanIds();
        assertThat(loanIds).as("No WC loan IDs tracked in test context").isNotEmpty();
        for (Long loanId : loanIds) {
            LocalDate actual = wcLoanHelper.getLastClosedBusinessDate(loanId);
            log.debug("WC loan id={} lastClosedBusinessDate={}", loanId, actual);
            assertThat(actual)//
                    .as("WC loan id=%d — expected lastClosedBusinessDate '%s' but got '%s'", loanId, expected, actual)//
                    .isEqualTo(expected);
        }
    }

    @Given("Admin inserts a WC loan with status {string} into the database")
    public void insertWcLoanWithStatus(String statusName) {
        final LoanStatus status = LoanStatus.valueOf(statusName);
        Long loanId = wcLoanHelper.insertLoan(status, null, getClientId(), getProductId());
        log.debug("Inserted WC loan with id={}, status={}", loanId, statusName);
        getTrackedLoanIds().add(loanId);
    }

    @Given("Admin inserts a WC loan with status {string} and lastClosedBusinessDate {string} into the database")
    public void insertWcLoanWithStatusAndDate(String statusName, String dateStr) {
        final LoanStatus status = LoanStatus.valueOf(statusName);
        final LocalDate lastClosedBusinessDate = LocalDate.parse(dateStr, DATE_FORMAT);
        Long loanId = wcLoanHelper.insertLoan(status, lastClosedBusinessDate, getClientId(), getProductId());
        log.debug("Inserted WC loan with id={}, status={}, lastClosedBusinessDate={}", loanId, statusName, lastClosedBusinessDate);
        getTrackedLoanIds().add(loanId);
    }

    @Then("Admin verifies all inserted WC loans have null lastClosedBusinessDate")
    public void verifyAllLoansHaveNullLastClosedBusinessDate() {
        List<Long> loanIds = getTrackedLoanIds();
        assertThat(loanIds).as("No WC loan IDs tracked in test context").isNotEmpty();
        for (Long loanId : loanIds) {
            LocalDate actual = wcLoanHelper.getLastClosedBusinessDate(loanId);
            log.debug("WC loan id={} lastClosedBusinessDate={}", loanId, actual);
            assertThat(actual)//
                    .as("WC loan id=%d — expected null lastClosedBusinessDate but got '%s'", loanId, actual)//
                    .isNull();
        }
    }

    @Then("Admin verifies all inserted WC loans have version {int}")
    public void verifyAllLoansHaveVersion(int expectedVersion) {
        List<Long> loanIds = getTrackedLoanIds();
        assertThat(loanIds).as("No WC loan IDs tracked in test context").isNotEmpty();
        for (Long loanId : loanIds) {
            int actual = wcLoanHelper.getVersion(loanId);
            log.debug("WC loan id={} version={}", loanId, actual);
            assertThat(actual)//
                    .as("WC loan id=%d — expected version %d but got %d", loanId, expectedVersion, actual)//
                    .isEqualTo(expectedVersion);
        }
    }

    @Then("Admin verifies all inserted WC loans have no account locks")
    public void verifyAllLoansHaveNoAccountLocks() {
        List<Long> loanIds = getTrackedLoanIds();
        assertThat(loanIds).as("No WC loan IDs tracked in test context").isNotEmpty();
        for (Long loanId : loanIds) {
            int lockCount = wcLoanHelper.countLocksByLoanId(loanId);
            log.debug("WC loan id={} lock count={}", loanId, lockCount);
            assertThat(lockCount)//
                    .as("WC loan id=%d — expected 0 account locks but got %d", loanId, lockCount)//
                    .isZero();
        }
    }

    @Then("Admin verifies all inserted WC loans have at least one account lock")
    public void verifyAllLoansHaveAtLeastOneAccountLock() {
        final List<Long> loanIds = getTrackedLoanIds();
        assertThat(loanIds).as("No WC loan IDs tracked in test context").isNotEmpty();
        for (final Long loanId : loanIds) {
            final int lockCount = wcLoanHelper.countLocksByLoanId(loanId);
            log.debug("WC loan id={} lock count={}", loanId, lockCount);
            assertThat(lockCount)//
                    .as("WC loan id=%d — expected at least one account lock but got %d", loanId, lockCount)//
                    .isPositive();
        }
    }

    @When("Admin places a chunk-processing lock without an error message on the last inserted WC loan")
    public void placeChunkLockWithoutErrorOnLastWcLoan() {
        final Long loanId = getTrackedLoanIds().getLast();
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
        log.debug("Placed chunk-processing lock without error on WC loan id={}", loanId);
    }

    @When("Admin places a chunk-processing lock with error {string} on the last inserted WC loan")
    public void placeChunkLockWithErrorOnLastWcLoan(final String error) {
        final Long loanId = getTrackedLoanIds().getLast();
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_COB_CHUNK_PROCESSING", new LockRequest().error(error)));
        log.debug("Placed chunk-processing lock with error '{}' on WC loan id={}", error, loanId);
    }

    @When("Admin places an inline-COB lock without an error message on the last inserted WC loan")
    public void placeInlineLockWithoutErrorOnLastWcLoan() {
        final Long loanId = getTrackedLoanIds().getLast();
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_INLINE_COB_PROCESSING", new LockRequest()));
        log.debug("Placed inline-COB lock without error on WC loan id={}", loanId);
    }

    @When("Admin places an inline-COB lock with error {string} on the last inserted WC loan")
    public void placeInlineLockWithErrorOnLastWcLoan(final String error) {
        final Long loanId = getTrackedLoanIds().getLast();
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_INLINE_COB_PROCESSING", new LockRequest().error(error)));
        log.debug("Placed inline-COB lock with error '{}' on WC loan id={}", error, loanId);
    }

    @When("Admin places a chunk-processing lock without an error message and null cob business date on the last inserted WC loan")
    public void placeChunkLockWithNullCobDateOnLastWcLoan() {
        final Long loanId = getTrackedLoanIds().getLast();
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_COB_CHUNK_PROCESSING", new LockRequest().nullCobBusinessDate(true)));
        log.debug("Placed chunk-processing lock with null cob date on WC loan id={}", loanId);
    }

    @When("Admin places a chunk-processing lock without an error message and cob business date {string} on the last inserted WC loan")
    public void placeChunkLockWithExplicitCobDateOnLastWcLoan(final String cobBusinessDate) {
        final Long loanId = getTrackedLoanIds().getLast();
        final LocalDate parsed = LocalDate.parse(cobBusinessDate, DATE_FORMAT);
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_COB_CHUNK_PROCESSING", new LockRequest().cobBusinessDate(parsed)));
        log.debug("Placed chunk-processing lock with explicit cob date {} on WC loan id={}", parsed, loanId);
    }

    @When("Admin places a chunk-processing lock without an error message on WC loan {int}")
    public void placeChunkLockWithoutErrorOnWcLoanAtIndex(final int index) {
        final Long loanId = loanAtIndex(index);
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
        log.debug("Placed chunk-processing lock without error on WC loan index={} id={}", index, loanId);
    }

    @When("Admin places a chunk-processing lock with error {string} on WC loan {int}")
    public void placeChunkLockWithErrorOnWcLoanAtIndex(final String error, final int index) {
        final Long loanId = loanAtIndex(index);
        executeVoid(() -> fineractClient.workingCapitalLoanAccountLock().placeLockOnWorkingCapitalLoanAccount(loanId,
                "LOAN_COB_CHUNK_PROCESSING", new LockRequest().error(error)));
        log.debug("Placed chunk-processing lock with error '{}' on WC loan index={} id={}", error, index, loanId);
    }

    @Then("Admin verifies inserted WC loan {int} has no account locks")
    public void verifyLoanAtIndexHasNoLocks(final int index) {
        final Long loanId = loanAtIndex(index);
        final int lockCount = wcLoanHelper.countLocksByLoanId(loanId);
        log.debug("WC loan index={} id={} lock count={}", index, loanId, lockCount);
        assertThat(lockCount)//
                .as("WC loan index=%d id=%d — expected 0 account locks but got %d", index, loanId, lockCount)//
                .isZero();
    }

    @Then("Admin verifies inserted WC loan {int} has at least one account lock")
    public void verifyLoanAtIndexHasAtLeastOneLock(final int index) {
        final Long loanId = loanAtIndex(index);
        final int lockCount = wcLoanHelper.countLocksByLoanId(loanId);
        log.debug("WC loan index={} id={} lock count={}", index, loanId, lockCount);
        assertThat(lockCount)//
                .as("WC loan index=%d id=%d — expected at least one account lock but got %d", index, loanId, lockCount)//
                .isPositive();
    }

    private Long loanAtIndex(final int index) {
        final List<Long> loanIds = getTrackedLoanIds();
        assertThat(index).as("Loan index %d out of range (1..%d)", index, loanIds.size()).isBetween(1, loanIds.size());
        return loanIds.get(index - 1);
    }

    @Then("Admin verifies inserted WC loan {int} has lastClosedBusinessDate {string}")
    public void verifyLoanAtIndexHasLastClosedBusinessDate(int index, String expectedDate) {
        LocalDate expected = LocalDate.parse(expectedDate, DATE_FORMAT);
        List<Long> loanIds = getTrackedLoanIds();
        assertThat(index).as("Loan index %d out of range (1..%d)", index, loanIds.size()).isBetween(1, loanIds.size());
        Long loanId = loanIds.get(index - 1);
        LocalDate actual = wcLoanHelper.getLastClosedBusinessDate(loanId);
        log.debug("WC loan index={} id={} lastClosedBusinessDate={}", index, loanId, actual);
        assertThat(actual)//
                .as("WC loan index=%d id=%d — expected lastClosedBusinessDate '%s' but got '%s'", index, loanId, expected, actual)//
                .isEqualTo(expected);
    }

    @Then("Admin verifies inserted WC loan {int} has null lastClosedBusinessDate")
    public void verifyLoanAtIndexHasNullLastClosedBusinessDate(int index) {
        List<Long> loanIds = getTrackedLoanIds();
        assertThat(index).as("Loan index %d out of range (1..%d)", index, loanIds.size()).isBetween(1, loanIds.size());
        Long loanId = loanIds.get(index - 1);
        LocalDate actual = wcLoanHelper.getLastClosedBusinessDate(loanId);
        log.debug("WC loan index={} id={} lastClosedBusinessDate={}", index, loanId, actual);
        assertThat(actual)//
                .as("WC loan index=%d id=%d — expected null lastClosedBusinessDate but got '%s'", index, loanId, actual)//
                .isNull();
    }

    @When("Admin runs Working Capital COB catch up")
    public void runWorkingCapitalLoanCOBCatchUp() {
        // Catch-up is a tenant-wide singleton, so a run still in flight from an earlier scenario rejects this one.
        // Firing regardless and swallowing the rejection leaves the scenario waiting on a catch-up that was never
        // accepted.
        await() //
                .atMost(Duration.ofMinutes(2)) //
                .pollInterval(Duration.ofSeconds(2)) //
                .pollDelay(Duration.ZERO) //
                .until(() -> Boolean.FALSE
                        .equals(ok(() -> fineractClient.workingCapitalLoanCobCatchUpApi().isCatchUpRunning1()).getCatchUpRunning()));

        try {
            executeVoid(() -> fineractClient.workingCapitalLoanCobCatchUpApi().executeLoanCOBCatchUp1());
        } catch (CallFailedRuntimeException e) {
            rethrowUnlessCatchUpUnavailable(e.getStatus(), e);
        } catch (feign.FeignException e) {
            // A 400 carrying no body never reaches the decoder that produces CallFailedRuntimeException, so it arrives
            // as a raw Feign exception and slips past the catch above. Same meaning, so handle it the same way.
            rethrowUnlessCatchUpUnavailable(e.status(), e);
        }
    }

    /**
     * A 400 means catch-up would not start - already running, or nothing left to catch up. Neither is a test failure.
     */
    private void rethrowUnlessCatchUpUnavailable(final int status, final RuntimeException e) {
        if (status != 400) {
            throw e;
        }
        log.info("WC COB catch-up was not started (400 response), continuing with test");
    }

    @When("Admin checks that WC Loan COB is running until the current business date")
    public void checkWCLoanCOBCatchUpRunningUntilCOBBusinessDate() {
        // Resolve the expected completion date upfront, before the async job potentially finishes.
        // COB catch-up processes every day from the oldest lastClosedBusinessDate up to cobBusinessDate.
        // When complete, cobProcessedDate (the oldest loan's lastClosedBusinessDate) will equal cobBusinessDate.
        BusinessDateResponse businessDateResponse = ok(
                () -> fineractClient.businessDateManagement().getBusinessDate(BusinessDateHelper.COB, Map.of()));
        LocalDate expectedCompletionDate = businessDateResponse.getDate();

        // Single-phase polling: handles both the case where the job is still running AND where it
        // already finished before this polling loop started (race condition with async execution).
        // Bug fix #1: removed Phase 1 "wait until running" which timed out when the job completed
        // too quickly for the poll to catch isCatchUpRunning = true.
        // Bug fix #2: use cobProcessedDate (oldest loan's lastClosedBusinessDate) instead of
        // cobBusinessDate (which is always == current COB date, making the check vacuous).
        // Bug fix #3: Scoped to the loans this scenario created, not to the tenant. The catch-up API reports the oldest
        // lastClosedBusinessDate across every eligible loan, and this same feature deliberately leaves loans locked
        // with an error message - a lock COB is designed never to clear, so those loans never advance again. Once one
        // exists, a tenant-wide reading of "caught up" is pinned to its date forever and this step can only time out,
        // however long it waits. The tracked loans still make the check meaningful: catch-up has to walk them across
        // the skipped business dates to satisfy it.
        final List<Long> loanIds = getTrackedLoanIds();
        assertThat(loanIds).as("No WC loan IDs tracked in test context").isNotEmpty();
        await() //
                .atMost(Duration.ofMinutes(4)) //
                .pollInterval(Duration.ofSeconds(5)) //
                .pollDelay(Duration.ofSeconds(2)) //
                .until(() -> {
                    IsCatchUpRunningDTO statusResponse = ok(() -> fineractClient.workingCapitalLoanCobCatchUpApi().isCatchUpRunning1());

                    if (statusResponse.getCatchUpRunning()) {
                        log.debug("WC COB catch-up still running, waiting...");
                        return false;
                    }

                    for (final Long loanId : loanIds) {
                        final LocalDate lastClosed = wcLoanHelper.getLastClosedBusinessDate(loanId);
                        if (lastClosed == null || lastClosed.isBefore(expectedCompletionDate)) {
                            log.debug("WC COB catch-up incomplete: loan {} lastClosedBusinessDate={}, expected at least {}", loanId,
                                    lastClosed, expectedCompletionDate);
                            return false;
                        }
                    }
                    return true;
                });
    }

    @Then("Admin verifies internal working capital cob last run data values are empty {string}")
    public void adminClearsInternalWorkingCapitalCOBLastRunData(String isEmpty) {
        Map<String, Object> ok = ok(() -> fineractClient.workingCapitalLoanInternalCobApi().getLastCobRun());
        log.debug("internal working capital cob last run data values : {}", ok);
        Assertions.assertNotNull(ok);
        Boolean shouldContainValues = isEmpty.equals("false");
        Assertions.assertEquals(shouldContainValues, ok.containsKey("cob-job-after-listener"));
        Assertions.assertEquals(shouldContainValues, ok.containsKey("cob-job-before-listener"));
    }

    @When("Admin clears internal working capital cob last run data")
    public void adminClearsInternalWorkingCapitalCOBLastRunData() {
        ok(() -> fineractClient.workingCapitalLoanInternalCobApi().deleteLastCobRun());
    }

    @SuppressWarnings("unchecked")
    private List<Long> getTrackedLoanIds() {
        return testContext().get(TestContextKey.WC_LOAN_IDS);
    }

    private Long getClientId() {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        return clientResponse.getClientId();
    }

    private Long getProductId() {
        final PostWorkingCapitalLoanProductsResponse workingCapitalLoanProductsResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        return workingCapitalLoanProductsResponse.getResourceId();
    }
}
