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
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateResponse.TypeEnum;
import org.apache.fineract.client.models.JobExecuteRequest;
import org.apache.fineract.client.models.JobExecuteResponse;
import org.apache.fineract.client.models.JobParameterDTO;
import org.apache.fineract.client.models.SavingsAccountStatusEnumData;
import org.apache.fineract.client.models.SavingsAccountSubStatusEnumData;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class FeignSavingsCobLockLookupTest extends FeignSavingsTestBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH);

    private final List<Executable> savingsCleanups = new ArrayList<>();

    @Test
    void catchUpJobAdvancesAPreviouslyProcessedSavingsAccountAndSkipsANeverProcessedAccount() {
        withPausedScheduler(() -> {
            final LocalDate cobDate = getCobDate();
            final LocalDate activationDate = cobDate.minusDays(2);
            final Long productId = createDormancyTrackingProduct();
            final Long previouslyProcessed = createSavingsAccount(productId, activationDate, cobDate);
            executeCob(cobDate.minusDays(1), false);
            assertSubStatus(previouslyProcessed, "SavingsAccountSubStatusEnum.inactive");

            final Long neverProcessed = createSavingsAccount(productId, activationDate, cobDate);
            executeCob(cobDate, true);

            assertSubStatus(previouslyProcessed, "SavingsAccountSubStatusEnum.dormant");
            assertSubStatus(neverProcessed, "SavingsAccountSubStatusEnum.none");
        });
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

    private LocalDate getCobDate() {
        if (!Boolean.TRUE.equals(globalConfigurationHelper.retrieveConfigurationByName("enable-business-date").getEnabled())) {
            return Utils.getLocalDateOfTenant();
        }
        final List<BusinessDateResponse> dates = ok(() -> fineractClient().businessDateManagement().getBusinessDates());
        final LocalDate businessDate = dateOrTenantDate(dates, TypeEnum.BUSINESS_DATE);
        final LocalDate cobDate = dateOrTenantDate(dates, TypeEnum.COB_DATE);
        return cobDate.isAfter(businessDate) ? businessDate : cobDate;
    }

    private LocalDate dateOrTenantDate(final List<BusinessDateResponse> dates, final TypeEnum type) {
        return dates.stream().filter(date -> date.getType() == type && date.getDate() != null).map(BusinessDateResponse::getDate)
                .findFirst().orElseGet(Utils::getLocalDateOfTenant);
    }

    private Long createDormancyTrackingProduct() {
        final Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0)
                .isDormancyTrackingActive(true).daysToInactive(1L).daysToDormancy(2L).daysToEscheat(30L)).getResourceId();
        assertNotNull(productId);
        return productId;
    }

    private Long createSavingsAccount(final Long productId, final LocalDate activationDate, final LocalDate cleanupDate) {
        final String date = DATE_FORMAT.format(activationDate);
        final Long clientId = createClient(date);
        assertNotNull(clientId);
        final Long savingsId = submitSavingsApplication(clientId, productId, date).getSavingsId();
        assertNotNull(savingsId);
        savingsCleanups.add(() -> cleanupSavings(savingsId, cleanupDate));
        approveSavings(savingsId, date);
        activateSavings(savingsId, date);
        assertSubStatus(savingsId, "SavingsAccountSubStatusEnum.none");
        return savingsId;
    }

    private void executeCob(final LocalDate cobDate, final boolean catchUp) {
        schedulerHelper.executeAndAwaitJob(JobName.SAVINGS_COB.toString(), job -> {
            final JobExecuteRequest request = new JobExecuteRequest()
                    .addJobParametersItem(new JobParameterDTO().parameterName("BusinessDate").parameterValue(cobDate.toString()));
            if (catchUp) {
                request.addJobParametersItem(new JobParameterDTO().parameterName("IS_CATCH_UP").parameterValue("true"));
            }
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
