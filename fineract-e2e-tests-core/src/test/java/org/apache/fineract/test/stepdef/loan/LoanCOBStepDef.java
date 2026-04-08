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
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanCOBStepDef extends AbstractStepDef {

    @Autowired
    private FineractFeignClient fineractClient;

    @Then("The cobProcessedDate of the oldest loan processed by COB is more than 1 day earlier than cobBusinessDate")
    public void checkOldestCOBProcessed() throws IOException {
        OldestCOBProcessedLoanDTO response = ok(() -> fineractClient.loanCobCatchUp().getOldestCOBProcessedLoan());

        LocalDate cobDate = response.getCobBusinessDate();
        LocalDate cobDateMinusOne = cobDate.minusDays(1);
        LocalDate cobProcessedDate = response.getCobProcessedDate();
        log.debug("cobDateMinusOne: {}", cobDateMinusOne);
        log.debug("cobProcessedDate: {}", cobProcessedDate);

        boolean result = cobDateMinusOne.isAfter(cobProcessedDate);
        assertThat(result).as(ErrorMessageHelper.wrongLastCOBProcessedLoanDate(cobProcessedDate, cobDateMinusOne)).isTrue();
    }

    @Then("There are no locked loan accounts")
    public void listOfLockedLoansEmpty() throws IOException {
        LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.<String, Object>of("page", 0, "size", 1000)));

        int size = response.getContent().size();
        assertThat(size).as(ErrorMessageHelper.listOfLockedLoansNotEmpty(response)).isEqualTo(0);
        log.debug("Size of List of the locked loans: {}", size);
    }

    @Then("The loan account is not locked")
    public void loanIsNotInListOfLockedLoans() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long targetLoanId = loanResponse.getLoanId();

        LoanAccountLockResponseDTO response = ok(
                () -> fineractClient.loanAccountLock().retrieveLockedAccounts(Map.<String, Object>of("page", 0, "size", 1000)));

        List<LoanAccountLock> content = response.getContent();
        boolean contains = content.stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);//

        assertThat(contains).as(ErrorMessageHelper.listOfLockedLoansContainsLoan(targetLoanId, response)).isFalse();
    }

    @When("Admin places a lock on loan account with an error message")
    public void placeLockOnLoanAccountWithErrorMessage() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.defaultApi().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @When("Admin places a lock on loan account WITHOUT an error message")
    public void placeLockOnLoanAccountNoErrorMessage() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        executeVoid(() -> fineractClient.defaultApi().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
    }
}
