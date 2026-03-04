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
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.IsCatchUpRunningDTO;
import org.apache.fineract.client.models.OldestCOBProcessedLoanDTO;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.WorkingCapitalLoanTestHelper;
import org.apache.fineract.test.messaging.config.JobPollingProperties;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class WorkingCapitalLoanCobStepDef extends AbstractStepDef {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Autowired
    private WorkingCapitalLoanTestHelper wcLoanHelper;
    @Autowired
    private FineractFeignClient fineractClient;
    @Autowired
    private JobPollingProperties jobPollingProperties;

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
    public void runWorkingCapitalInlineCOB() throws IOException {
        InlineJobRequest inlineJobRequest = new InlineJobRequest().addLoanIdsItem(getTrackedLoanIds().getLast());

        ok(() -> fineractClient.inlineJob().executeInlineJob("WC_LOAN_COB", inlineJobRequest));
    }

    @Given("Admin inserts an active WC loan into the database")
    public void insertActiveWcLoan() {
        Long loanId = wcLoanHelper.insertActiveLoan();
        log.debug("Inserted WC loan with id={}", loanId);
        getTrackedLoanIds().add(loanId);
    }

    @Given("Admin inserts {int} active WC loans into the database")
    public void insertMultipleActiveWcLoans(int count) {
        for (int i = 0; i < count; i++) {
            Long loanId = wcLoanHelper.insertActiveLoan();
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
        LoanStatus status = LoanStatus.valueOf(statusName);
        Long loanId = wcLoanHelper.insertLoan(status.getValue(), null);
        log.debug("Inserted WC loan with id={}, status={}({})", loanId, statusName, status.getValue());
        getTrackedLoanIds().add(loanId);
    }

    @Given("Admin inserts a WC loan with status {string} and lastClosedBusinessDate {string} into the database")
    public void insertWcLoanWithStatusAndDate(String statusName, String dateStr) {
        LoanStatus status = LoanStatus.valueOf(statusName);
        LocalDate lastClosedBusinessDate = LocalDate.parse(dateStr, DATE_FORMAT);
        Long loanId = wcLoanHelper.insertLoan(status.getValue(), lastClosedBusinessDate);
        log.debug("Inserted WC loan with id={}, status={}({}), lastClosedBusinessDate={}", loanId, statusName, status.getValue(),
                lastClosedBusinessDate);
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
        try {
            executeVoid(() -> fineractClient.workingCapitalLoanCobCatchUpApi().executeLoanCOBCatchUp1());
        } catch (CallFailedRuntimeException e) {
            if (e.getStatus() == 400) {
                log.info("COB catch-up is already running (400 response), continuing with test");
            } else {
                throw e;
            }
        }
    }

    @When("Admin checks that WC Loan COB is running until the current business date")
    public void checkWCLoanCOBCatchUpRunningUntilCOBBusinessDate() {
        await().atMost(Duration.ofMillis(jobPollingProperties.getTimeoutInMillis())) //
                .pollInterval(Duration.ofMillis(jobPollingProperties.getIntervalInMillis())) //
                .until(() -> {
                    IsCatchUpRunningDTO isCatchUpRunningResponse = ok(
                            () -> fineractClient.workingCapitalLoanCobCatchUpApi().isCatchUpRunning1());
                    IsCatchUpRunningDTO isCatchUpRunning = isCatchUpRunningResponse;
                    return isCatchUpRunning.getCatchUpRunning();
                });
        // Then wait for catch-up to complete
        await().atMost(Duration.ofMinutes(4)).pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5)).until(() -> {
            // Check if catch-up is still running
            IsCatchUpRunningDTO statusResponse = ok(() -> fineractClient.workingCapitalLoanCobCatchUpApi().isCatchUpRunning1());
            // Only proceed with date check if catch-up is not running
            if (!statusResponse.getCatchUpRunning()) {
                // Get the current business date
                BusinessDateResponse businessDateResponse = ok(
                        () -> fineractClient.businessDateManagement().getBusinessDate(BusinessDateHelper.COB, Map.of()));
                LocalDate currentBusinessDate = businessDateResponse.getDate();

                // Get the last closed business date
                OldestCOBProcessedLoanDTO catchUpResponse = ok(() -> fineractClient.loanCobCatchUp().getOldestCOBProcessedLoan());
                LocalDate lastClosedDate = catchUpResponse.getCobBusinessDate();

                // Verify that the last closed date is not before the current business date
                return !lastClosedDate.isBefore(currentBusinessDate);
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    private List<Long> getTrackedLoanIds() {
        return testContext().get(TestContextKey.WC_LOAN_IDS);
    }
}
