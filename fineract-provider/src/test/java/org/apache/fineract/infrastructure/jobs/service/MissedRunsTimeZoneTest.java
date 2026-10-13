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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * next_run_time is an instant: whether it is "in the past" and where the trigger starts do not depend on the tenant's
 * or the JVM's zone, while the cron runs in the tenant's zone.
 */
class MissedRunsTimeZoneTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @ParameterizedTest
    @CsvSource({ "America/New_York,Pacific/Auckland", "Pacific/Auckland,America/Los_Angeles", "Asia/Kolkata,UTC" })
    void aDailyCronOfTheTenantIsReplayedPerTenantMidnightWhateverTheSystemZone(String tenantZone, String systemZone) throws Exception {
        try (MissedRunsFixture fixture = new MissedRunsFixture(true, tenantZone)) {
            fixture.setSystemZone(systemZone);
            final ZoneId tenant = ZoneId.of(tenantZone);
            final ZonedDateTime firstMissed = ZonedDateTime.now(tenant).truncatedTo(ChronoUnit.DAYS).minusDays(3);
            final ScheduledJobDetail job = fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, "0 0 0 * * ?",
                    Date.from(firstMissed.toInstant()));

            fixture.boot();

            await().atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY)).isEqualTo(4));
            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(job.getNextRunTime()).isAfter(new Date()));
            assertThat(job.getNextRunTime().toInstant().atZone(tenant).toLocalTime()).as("next run is the tenant's midnight")
                    .isEqualTo(java.time.LocalTime.MIDNIGHT);
        }
    }

    @Test
    void theRepeatedHourOfADaylightSavingEndFiresOnce() throws Exception {
        final ZoneId newYork = ZoneId.of("America/New_York");
        try (MissedRunsFixture fixture = new MissedRunsFixture(true, "America/New_York")) {
            fixture.setSystemZone("Pacific/Auckland");
            // 2025-11-02 01:30 happens twice in New York
            fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, "0 30 1 1,2 11 ? 2025",
                    Date.from(ZonedDateTime.of(2025, 11, 1, 1, 30, 0, 0, newYork).toInstant()));

            fixture.boot();

            await().during(Duration.ofSeconds(1)).atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY)).isEqualTo(2));
        }
    }

    @Test
    void aLocalTimeThatDoesNotExistOnTheDaylightSavingStartDayWasNeverScheduledSoItIsNotReplayed() throws Exception {
        final ZoneId newYork = ZoneId.of("America/New_York");
        try (MissedRunsFixture fixture = new MissedRunsFixture(true, "America/New_York")) {
            fixture.setSystemZone("Pacific/Auckland");
            // 2026-03-08 02:30 does not exist in New York: Quartz never fires the cron that day, so only 03-09 is
            // missed
            fixture.addJob(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY, "0 30 2 8,9 3 ? 2026",
                    Date.from(ZonedDateTime.of(2026, 3, 8, 0, 0, 0, 0, newYork).toInstant()));

            fixture.boot();

            await().during(Duration.ofSeconds(1)).atMost(TIMEOUT)
                    .untilAsserted(() -> assertThat(fixture.executionCount(JobName.INCREASE_BUSINESS_DATE_BY_1_DAY)).isEqualTo(1));
        }
    }
}
