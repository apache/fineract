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
import java.util.Date;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.junit.jupiter.api.Test;

class JobRegisterServiceImplTest {

    private static final int DAYS_DOWN = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    void resumeReplaysTheRunsSkippedWhileSuspendedWhenEnabled() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            final ScheduledJobDetail job = fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT,
                    new Date(System.currentTimeMillis() + 3_600_000));
            fixture.boot();
            fixture.asRequest(fixture.jobRegisterService::pauseScheduler);
            // what the fires vetoed during the suspension leave behind (see the restart test for the veto itself)
            job.setNextRunTime(midnightDaysAgo(DAYS_DOWN));
            job.setTriggerMisfired(true);

            fixture.asRequest(fixture.jobRegisterService::startScheduler);

            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY))
                    .isEqualTo(missedDailyRuns(DAYS_DOWN)));
            assertThat(job.isTriggerMisfired()).isFalse();
            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(job.getNextRunTime()).isAfter(new Date()));
        }
    }

    @Test
    void restartWhileSuspendedKeepsTheMissedRunsPendingUntilResume() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            fixture.schedulerDetail.setSuspended(true);
            fixture.schedulerDetail.setResetSchedulerOnBootup(false);
            final Date missedSince = midnightDaysAgo(DAYS_DOWN);
            final ScheduledJobDetail job = fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, DAILY_AT_MIDNIGHT, missedSince);
            fixture.addJob(JobName.UPDATE_LOAN_ARREARS_AGEING, DAILY_AT_MIDNIGHT, missedSince);

            fixture.boot();

            await().atMost(TIMEOUT).until(job::isTriggerMisfired);
            await().during(Duration.ofSeconds(1)).atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executions).as("everything is vetoed while suspended").isEmpty());
            assertThat(job.getNextRunTime()).as("a vetoed fire does not advance next_run_time").isEqualTo(missedSince);

            fixture.asRequest(fixture.jobRegisterService::startScheduler);

            await().atMost(TIMEOUT).untilAsserted(() -> {
                assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY)).isEqualTo(missedDailyRuns(DAYS_DOWN));
                assertThat(fixture.executionCount(JobName.UPDATE_LOAN_ARREARS_AGEING)).isEqualTo(1);
            });
            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(job.getNextRunTime()).isAfter(new Date()));
            assertThat(job.isTriggerMisfired()).isFalse();
        }
    }

    @Test
    void resumeDoesNotRegisterAJobAgainThatHasNothingMissed() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true)) {
            fixture.schedulerDetail.setSuspended(true);
            final ScheduledJobDetail job = fixture.addJob(JobName.UPDATE_LOAN_ARREARS_AGEING, DAILY_AT_MIDNIGHT,
                    new Date(System.currentTimeMillis() + 3_600_000));
            fixture.boot();
            final Date nextRunTime = job.getNextRunTime();

            fixture.asRequest(fixture.jobRegisterService::startScheduler);

            await().during(Duration.ofSeconds(1)).atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executionCount(JobName.UPDATE_LOAN_ARREARS_AGEING)).isZero());
            assertThat(job.getNextRunTime()).isEqualTo(nextRunTime);
        }
    }

    @Test
    void resumeKeepsTheLegacyMisfiredReplayWhenDisabled() throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(false)) {
            fixture.schedulerDetail.setExecuteInstructionForMisfiredJobs(true);
            final ScheduledJobDetail job = fixture.addJob(JobName.UPDATE_LOAN_ARREARS_AGEING, DAILY_AT_MIDNIGHT,
                    midnightDaysAgo(DAYS_DOWN));
            fixture.boot();
            fixture.schedulerDetail.setSuspended(true);
            job.setNextRunTime(midnightDaysAgo(DAYS_DOWN));
            job.setTriggerMisfired(true);

            fixture.asRequest(fixture.jobRegisterService::startScheduler);

            // The legacy replay is asynchronous and moves next_run_time before the replayed fire is vetoed or not
            // (FINERACT-2918), so only its synchronous effects are asserted.
            assertThat(fixture.schedulerDetail.isSuspended()).isFalse();
            assertThat(job.isTriggerMisfired()).isFalse();
            assertThat(job.getNextRunTime()).as("legacy resume takes the trigger's next fire time").isAfter(new Date());
        }
    }
}
