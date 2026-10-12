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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.LoanAccountLock;
import org.apache.fineract.client.models.LoanAccountLockResponseDTO;
import org.apache.fineract.client.models.LockRequest;
import org.apache.fineract.client.models.OldestCOBProcessedLoanDTO;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class LoanCOBStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;

    @Then("The cobProcessedDate of the oldest loan processed by COB is more than 1 day earlier than cobBusinessDate")
    public void checkOldestCOBProcessed() {
        OldestCOBProcessedLoanDTO response = ok(() -> fineractClient.loanCobCatchUp().getOldestCOBProcessedLoan());

        LocalDate cobDate = response.getCobBusinessDate();
        Assertions.assertNotNull(cobDate);
        LocalDate cobDateMinusOne = cobDate.minusDays(1);
        LocalDate cobProcessedDate = response.getCobProcessedDate();
        log.debug("cobDateMinusOne: {}", cobDateMinusOne);
        log.debug("cobProcessedDate: {}", cobProcessedDate);

        boolean result = cobDateMinusOne.isAfter(cobProcessedDate);
        assertThat(result).as(ErrorMessageHelper.wrongLastCOBProcessedLoanDate(cobProcessedDate, cobDateMinusOne)).isTrue();
    }

    @Then("There are no locked loan accounts")
    public void listOfLockedLoansEmpty() {
        LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        int size = response.getContent().size();
        assertThat(size).as(ErrorMessageHelper.listOfLockedLoansNotEmpty(response)).isEqualTo(0);
        log.debug("Size of List of the locked loans: {}", size);
    }

    @Then("The loan account is not locked")
    public void loanIsNotInListOfLockedLoans() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long targetLoanId = loanResponse.getLoanId();

        LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        List<LoanAccountLock> content = response.getContent();
        boolean contains = content.stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);//

        assertThat(contains).as(ErrorMessageHelper.listOfLockedLoansContainsLoan(targetLoanId, response)).isFalse();
    }

    @Then("The loan account is locked by chunk processing")
    public void loanIsLockedByChunkProcessing() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long targetLoanId = loanResponse.getLoanId();

        final LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        final boolean stillLocked = response.getContent().stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);//

        assertThat(stillLocked).as(ErrorMessageHelper.expectedLoanToRemainLocked(targetLoanId, response)).isTrue();
    }

    @When("Admin places a lock on loan account with an error message")
    public void placeLockOnLoanAccountWithErrorMessage() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @When("Admin places a lock on loan account WITHOUT an error message")
    public void placeLockOnLoanAccountNoErrorMessage() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
    }

    @When("Admin places an inline COB lock on loan account WITHOUT an error message")
    public void placeInlineLockOnLoanAccountNoErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_INLINE_COB_PROCESSING", new LockRequest()));
    }

    @When("Admin places an inline COB lock on loan account with an error message")
    public void placeInlineLockOnLoanAccountWithErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_INLINE_COB_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @When("Admin places a lock on loan account WITHOUT an error message and null cob business date")
    public void placeLockOnLoanAccountWithNullCobBusinessDate() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().nullCobBusinessDate(true)));
    }

    @When("Admin places a lock on loan account WITHOUT an error message and cob business date {string}")
    public void placeLockOnLoanAccountWithExplicitCobBusinessDate(final String cobBusinessDate) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        final LocalDate parsed = LocalDate.parse(cobBusinessDate, DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().cobBusinessDate(parsed)));
    }

    @When("Admin places a lock on second loan account WITHOUT an error message")
    public void placeLockOnSecondLoanAccountNoErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
    }

    @When("Admin places a lock on second loan account with an error message")
    public void placeLockOnSecondLoanAccountWithErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @Then("The second loan account is not locked")
    public void secondLoanIsNotInListOfLockedLoans() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long targetLoanId = loanResponse.getLoanId();

        final LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        final boolean contains = response.getContent().stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);

        assertThat(contains).as(ErrorMessageHelper.listOfLockedLoansContainsLoan(targetLoanId, response)).isFalse();
    }

    @Then("The second loan account is locked by chunk processing")
    public void secondLoanIsLockedByChunkProcessing() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long targetLoanId = loanResponse.getLoanId();

        final LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        final boolean stillLocked = response.getContent().stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);

        assertThat(stillLocked).as(ErrorMessageHelper.expectedLoanToRemainLocked(targetLoanId, response)).isTrue();
    }
}
