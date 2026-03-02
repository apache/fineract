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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.test.data.job.DefaultJob;
import org.apache.fineract.test.service.JobService;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SchedulerStepDef extends AbstractStepDef {

    @Autowired
    private JobService jobService;

    @Autowired
    private FineractFeignClient fineractClient;

    @And("Admin runs the Add Accrual Transactions job")
    public void runAccrualTransaction() {
        jobService.executeAndWait(DefaultJob.ADD_ACCRUAL_TRANSACTIONS);
    }

    @And("Admin runs the Add Periodic Accrual Transactions job")
    public void runPeriodicAccrualTransaction() {
        jobService.executeAndWait(DefaultJob.ADD_PERIODIC_ACCRUAL_TRANSACTIONS);
    }

    @And("Admin runs the Increase Business Date by 1 day job")
    public void runIncreaseBusinessDate() {
        jobService.executeAndWait(DefaultJob.INCREASE_BUSINESS_DAY);
    }

    @And("Admin runs the Loan Delinquency Classification job")
    public void runLoanDelinquencyClassification() {
        jobService.executeAndWait(DefaultJob.LOAN_DELINQUENCY_CLASSIFICATION);
    }

    @When("Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job")
    public void runAddAccrualTransactionsForLoansWithIncomePostedAsTransactions() {
        jobService.executeAndWait(DefaultJob.ADD_ACCRUAL_TRANSACTIONS_FOR_LOANS_WITH_INCOME_POSTED_AS_TRANSACTIONS);
    }

    @When("Admin runs the Recalculate Interest for Loans job")
    public void runRecalculateInterestForLoans() {
        jobService.executeAndWait(DefaultJob.RECALCULATE_INTEREST_FOR_LOANS);
    }

    @When("Admin runs COB job")
    public void runCOB() {
        jobService.executeAndWait(DefaultJob.LOAN_COB);
    }

    @When("Admin runs the Accrual Activity Posting job")
    public void runAccrualActivityPosting() {
        jobService.executeAndWait(DefaultJob.ACCRUAL_ACTIVITY_POSTING);
    }

    @When("Admin runs WC COB job")
    public void runWorkingCapitalLoanCOB() {
        jobService.executeAndWait(DefaultJob.WORKING_CAPITAL_LOAN_COB);
    }

    @Then("Admin verifies scheduler job {string} has display name {string}")
    public void verifyJobDisplayName(String shortName, String expectedDisplayName) {
        GetJobsResponse response = ok(() -> fineractClient.schedulerJob().retrieveByShortName(shortName, Map.of()));
        assertThat(response.getDisplayName())//
                .as("Job '%s' display name — expected '%s' but got '%s'", shortName, expectedDisplayName, response.getDisplayName())//
                .isEqualTo(expectedDisplayName);
    }

    @Then("Admin verifies scheduler job {string} has active status {string}")
    public void verifyJobActiveStatus(String shortName, String expectedActive) {
        assertThat(expectedActive)//
                .as("Parameter must be 'true' or 'false' but got '%s'", expectedActive)//
                .isIn("true", "false");
        GetJobsResponse response = ok(() -> fineractClient.schedulerJob().retrieveByShortName(shortName, Map.of()));
        boolean expected = Boolean.parseBoolean(expectedActive);
        assertThat(response.getActive())//
                .as("Job '%s' active status — expected %s but got %s", shortName, expected, response.getActive())//
                .isEqualTo(expected);
    }
}
