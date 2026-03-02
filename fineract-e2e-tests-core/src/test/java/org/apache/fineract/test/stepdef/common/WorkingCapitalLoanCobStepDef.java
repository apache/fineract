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

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.helper.WorkingCapitalLoanTestHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class WorkingCapitalLoanCobStepDef extends AbstractStepDef {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Autowired
    private WorkingCapitalLoanTestHelper wcLoanHelper;

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

    @SuppressWarnings("unchecked")
    private List<Long> getTrackedLoanIds() {
        return testContext().get(TestContextKey.WC_LOAN_IDS);
    }
}
