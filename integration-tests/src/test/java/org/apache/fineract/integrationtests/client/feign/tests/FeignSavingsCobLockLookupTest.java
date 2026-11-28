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

import static org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DATETIME_PATTERN;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.JobExecuteRequest;
import org.apache.fineract.client.models.JobExecuteResponse;
import org.apache.fineract.client.models.JobParameterDTO;
import org.apache.fineract.client.models.SavingsAccountStatusEnumData;
import org.apache.fineract.client.models.SavingsAccountSubStatusEnumData;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

// Runs early in its shard, before other test classes leave savings accounts behind for the COB jobs to process.
@Order(1)
class FeignSavingsCobLockLookupTest extends FeignSavingsTestBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH);

    // The catch-up run picks the accounts whose last COB date is the day before its own. Fixed dates keep that to the
    // account this test brought to SEED_COB_DATE.
    private static final LocalDate ACTIVATION_DATE = LocalDate.of(2025, 2, 17);
    private static final LocalDate SEED_COB_DATE = ACTIVATION_DATE.plusDays(1);
    private static final LocalDate CATCH_UP_COB_DATE = SEED_COB_DATE.plusDays(1);

    private final List<Executable> savingsCleanups = new ArrayList<>();

    @Test
    void catchUpJobAdvancesAPreviouslyProcessedSavingsAccountAndSkipsANeverProcessedAccount() {
        runAt(DATE_FORMAT.format(SEED_COB_DATE.plusDays(1)), () -> withPausedScheduler(() -> {
            moveCobDateTo(SEED_COB_DATE);
            final Long productId = createDormancyTrackingProduct();
            final Long previouslyProcessed = createSavingsAccount(productId);
            // Inline COB, not the batch job: a regular (not catch-up) batch run also processes every never-processed
            // savings account that other test classes left active in the shared database, which can take minutes.
            executeInlineCob(previouslyProcessed);
            assertSubStatus(previouslyProcessed, "SavingsAccountSubStatusEnum.inactive");

            final Long neverProcessed = createSavingsAccount(productId);
            moveCobDateTo(CATCH_UP_COB_DATE);
            executeCatchUpCob(CATCH_UP_COB_DATE);

            assertSubStatus(previouslyProcessed, "SavingsAccountSubStatusEnum.dormant");
            assertSubStatus(neverProcessed, "SavingsAccountSubStatusEnum.none");
        }));
    }

    private void withPausedScheduler(final Runnable scenario) {
        final boolean wasActive = schedulerHelper.getSchedulerStatus();
        try {
            schedulerHelper.stopScheduler();
            scenario.run();
        } finally {
            try {
                assertAll(savingsCleanups);
            } finally {
                savingsCleanups.clear();
                schedulerHelper.updateSchedulerStatus(wasActive);
            }
        }
    }

    private void moveCobDateTo(final LocalDate cobDate) {
        businessDateHelper.updateBusinessDate("BUSINESS_DATE", DATE_FORMAT.format(cobDate.plusDays(1)), DATETIME_PATTERN);
        businessDateHelper.updateBusinessDate("COB_DATE", DATE_FORMAT.format(cobDate), DATETIME_PATTERN);
    }

    private Long createDormancyTrackingProduct() {
        final Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0)
                .isDormancyTrackingActive(true).daysToInactive(1L).daysToDormancy(2L).daysToEscheat(30L)).getResourceId();
        assertNotNull(productId);
        return productId;
    }

    private Long createSavingsAccount(final Long productId) {
        final String date = DATE_FORMAT.format(ACTIVATION_DATE);
        final Long clientId = createClient(date);
        assertNotNull(clientId);
        final Long savingsId = submitSavingsApplication(clientId, productId, date).getSavingsId();
        assertNotNull(savingsId);
        // Valid in both phases of the test: the business date is never before CATCH_UP_COB_DATE.
        savingsCleanups.add(() -> cleanupSavings(savingsId, CATCH_UP_COB_DATE));
        approveSavings(savingsId, date);
        activateSavings(savingsId, date);
        assertSubStatus(savingsId, "SavingsAccountSubStatusEnum.none");
        return savingsId;
    }

    private void executeInlineCob(final Long savingsId) {
        ok(() -> fineractClient().inlineJob().executeInlineJob(JobName.SAVINGS_COB.name(),
                new InlineJobRequest().savingsIds(List.of(savingsId))));
    }

    private void executeCatchUpCob(final LocalDate cobDate) {
        schedulerHelper.executeAndAwaitJob(JobName.SAVINGS_COB.toString(), job -> {
            final JobExecuteRequest request = new JobExecuteRequest()
                    .addJobParametersItem(new JobParameterDTO().parameterName("BusinessDate").parameterValue(cobDate.toString()))
                    .addJobParametersItem(new JobParameterDTO().parameterName("IS_CATCH_UP").parameterValue("true"));
            final Long jobId = job.getJobId();
            assertNotNull(jobId);
            final ApiResponse<JobExecuteResponse> response = ok(
                    () -> fineractClient().schedulerJob().executeJobWithHttpInfo(jobId, "executeJob", request, Map.of()));
            assertEquals(202, response.getStatusCode());
        });
    }

    private void assertSubStatus(final Long savingsId, final String expectedCode) {
        final SavingsAccountSubStatusEnumData subStatus = ok(
                () -> fineractClient().savingsAccount().retrieveSavingsAccount(savingsId, Map.of("fields", "subStatus"))).getSubStatus();
        assertNotNull(subStatus);
        assertEquals(expectedCode, subStatus.getCode());
    }

    private void cleanupSavings(final Long savingsId, final LocalDate cleanupDate) {
        final SavingsAccountStatusEnumData status = getSavingsDetails(savingsId).getStatus();
        assertNotNull(status);
        final String date = DATE_FORMAT.format(cleanupDate);
        if (Boolean.TRUE.equals(status.getActive())) {
            closeSavings(savingsId, date, true);
            final SavingsAccountStatusEnumData closedStatus = getSavingsDetails(savingsId).getStatus();
            assertNotNull(closedStatus);
            assertEquals(Boolean.TRUE, closedStatus.getClosed());
            return;
        }
        if (Boolean.TRUE.equals(status.getApproved())) {
            savingsHelper.undoApproval(savingsId);
        }
        deleteSavingsApplication(savingsId);
    }
}
