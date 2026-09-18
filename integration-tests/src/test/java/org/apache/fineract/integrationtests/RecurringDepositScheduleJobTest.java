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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositAccountHelper;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositAccountStatusChecker;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Regression coverage for GenerateRdScheduleTasklet: its INSERT into m_mandatory_savings_schedule only ever executes
 * when the "Generate Mandatory Savings Schedule" job finds an active recurring deposit account with no fixed deposit
 * period (a term-less/open-ended account still needing future installments generated). This test reproduces that exact
 * scenario end-to-end against the real job/scheduler.
 */
@ExtendWith({ SavingsTestLifecycleExtension.class })
public class RecurringDepositScheduleJobTest {

    private static final String WHOLE_TERM = "1";
    private static final String GENERATE_RD_SCHEDULE_JOB_NAME = "Generate Mandatory Savings Schedule";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void generateRdScheduleJobSucceedsForOpenEndedRecurringDepositAccount() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar validFromCalendar = Calendar.getInstance();
        validFromCalendar.add(Calendar.MONTH, -3);
        final String validFrom = dateFormat.format(validFromCalendar.getTime());
        Calendar validToCalendar = Calendar.getInstance();
        validToCalendar.add(Calendar.YEAR, 10);
        final String validTo = dateFormat.format(validToCalendar.getTime());

        Calendar oneMonthAgo = Calendar.getInstance();
        oneMonthAgo.add(Calendar.MONTH, -1);
        final String submittedOnDate = dateFormat.format(oneMonthAgo.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);

        // Product must also be term-less: DepositProductAssembler falls back to the product's maxDepositTerm when
        // the account omits it, which would otherwise force depositPeriod to be mandatory again.
        String productJson = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec).withPeriodRangeChart()
                .withoutMaxDepositTerm().build(validFrom, validTo);
        Integer productId = RecurringDepositProductHelper.createRecurringDepositProduct(productJson, this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(productId);

        RecurringDepositAccountHelper recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec,
                this.responseSpec);
        // Omitting depositPeriod creates an open-ended RD account: the only scenario where
        // DepositAccountReadPlatformServiceImpl.retriveDataForRDScheduleCreation() returns rows for the job to insert.
        String accountJson = recurringDepositAccountHelper.withSubmittedOnDate(submittedOnDate)
                .withExpectedFirstDepositOnDate(submittedOnDate).withDepositPeriod(null).withoutMaxDepositTerm()
                .build(clientId.toString(), productId.toString(), WHOLE_TERM);
        Integer accountId = RecurringDepositAccountHelper.applyRecurringDepositApplication(accountJson, this.requestSpec,
                this.responseSpec);
        Assertions.assertNotNull(accountId);

        recurringDepositAccountHelper.approveRecurringDeposit(accountId, submittedOnDate);
        HashMap statusAfterActivation = recurringDepositAccountHelper.activateRecurringDeposit(accountId, submittedOnDate);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(statusAfterActivation);

        // GenerateRdScheduleTasklet's INSERT had 9 columns but only 7 '?' placeholders, so it throws as soon as it
        // is asked to insert a row. executeAndAwaitJob fails the test when the job run history status isn't
        // "success", which is exactly what happens for this account before the fix.
        SchedulerJobHelper.executeAndAwaitJob(GENERATE_RD_SCHEDULE_JOB_NAME);
    }
}
