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

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.data.job.DefaultJob;
import org.apache.fineract.test.service.JobService;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SchedulerStepDef extends AbstractStepDef {

    @Autowired
    private JobService jobService;

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

    @When("Admin runs COB job")
    public void runCOB() {
        jobService.executeAndWait(DefaultJob.LOAN_COB);
    }
}
