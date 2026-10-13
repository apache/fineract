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
package org.apache.fineract.infrastructure.jobs.service;

import static org.apache.fineract.infrastructure.jobs.service.MissedRunsFixture.DAILY_AT_MIDNIGHT;
import static org.apache.fineract.infrastructure.jobs.service.MissedRunsFixture.midnightDaysAgo;
import static org.apache.fineract.infrastructure.jobs.service.MissedRunsFixture.missedDailyRuns;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.junit.jupiter.api.Test;

class JobSchedulerServiceImplTest {

    private static final int DAYS_DOWN = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    void bootReplaysEveryMissedBusinessDateRunWhenEnabled() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            final LocalDate start = LocalDate.of(2025, 1, 1);
            final AtomicReference<LocalDate> businessDate = new AtomicReference<>(start);
            fixture.jobBody = job -> businessDate.updateAndGet(date -> date.plusDays(1));
            final ScheduledJobDetail job = fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT,
                    midnightDaysAgo(DAYS_DOWN));

            fixture.boot();

            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY))
                    .isEqualTo(missedDailyRuns(DAYS_DOWN)));
            assertThat(businessDate.get()).isEqualTo(start.plusDays(missedDailyRuns(DAYS_DOWN)));
            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(job.getNextRunTime()).isAfter(new Date()));
            assertThat(job.isCurrentlyRunning()).isFalse();
        }
    }

    @Test
    void bootFiresOnlyOnceForJobsWithTheDefaultMisfirePolicy() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            final ScheduledJobDetail ageing = fixture.addJob(JobName.UPDATE_LOAN_ARREARS_AGEING, DAILY_AT_MIDNIGHT,
                    midnightDaysAgo(DAYS_DOWN));
            fixture.addJob(JobName.INCREASE_COB_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT, midnightDaysAgo(DAYS_DOWN));

            fixture.boot();

            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(ageing.getNextRunTime()).isAfter(new Date()));
            // give a (wrong) second replay time to show up
            await().during(Duration.ofSeconds(1)).atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executionCount(JobName.UPDATE_LOAN_ARREARS_AGEING)).isEqualTo(1));
            assertThat(fixture.executionCount(JobName.INCREASE_COB_DATE_BY_1_DAY)).isEqualTo(1);
        }
    }

    @Test
    void bootDoesNotReplayAnythingWhenDisabled() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(false)) {
            final ScheduledJobDetail job = fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT,
                    midnightDaysAgo(DAYS_DOWN));

            fixture.boot();

            await().during(Duration.ofSeconds(2)).atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY)).isZero());
            assertThat(job.getNextRunTime()).isAfter(new Date());
        }
    }

    @Test
    void bootUnsuspendsTheTenantBeforeRegisteringSoReplayedRunsAreNotVetoed() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            fixture.schedulerDetail.setSuspended(true);
            fixture.schedulerDetail.setResetSchedulerOnBootup(true);
            fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT, midnightDaysAgo(DAYS_DOWN));

            fixture.boot();

            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY))
                    .isEqualTo(missedDailyRuns(DAYS_DOWN)));
            assertThat(fixture.schedulerDetail.isSuspended()).isFalse();
        }
    }

    @Test
    void aReplayedJobNeverRunsConcurrentlyWithItself() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            fixture.jobBody = job -> sleep(150);
            fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT, midnightDaysAgo(DAYS_DOWN));

            fixture.boot();

            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY))
                    .isEqualTo(missedDailyRuns(DAYS_DOWN)));
            assertThat(fixture.maxConcurrentExecutions.get()).isEqualTo(1);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
