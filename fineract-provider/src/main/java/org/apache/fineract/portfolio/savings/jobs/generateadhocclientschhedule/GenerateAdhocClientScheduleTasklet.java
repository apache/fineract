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
package org.apache.fineract.portfolio.savings.jobs.generateadhocclientschhedule;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.adhocquery.domain.ReportRunFrequency;
import org.apache.fineract.adhocquery.service.AdHocReadPlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class GenerateAdhocClientScheduleTasklet implements Tasklet {

    private final AdHocReadPlatformService adHocReadPlatformService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Collection<AdHocData> adhocs = adHocReadPlatformService.retrieveAllActiveAdHocQuery();
        if (adhocs.size() > 0) {
            adhocs.forEach(adhoc -> {
                boolean run = true;
                LocalDate next = null;
                if (adhoc.getReportRunFrequency() != null) {
                    if (adhoc.getLastRun() != null) {
                        LocalDate start = adhoc.getLastRun().toLocalDate();
                        LocalDate end = ZonedDateTime.now(DateUtils.getDateTimeZoneOfTenant()).toLocalDate();
                        switch (ReportRunFrequency.fromId(adhoc.getReportRunFrequency())) {
                            case DAILY -> {
                                next = start.plusDays(1);
                                run = Math.toIntExact(ChronoUnit.DAYS.between(start, end)) >= 1;
                            }
                            case WEEKLY -> {
                                next = start.plusDays(7);
                                run = Math.toIntExact(ChronoUnit.DAYS.between(start, end)) >= 7;
                            }
                            case MONTHLY -> {
                                next = start.plusMonths(1);
                                run = Math.toIntExact(ChronoUnit.MONTHS.between(start, end)) >= 1;
                            }
                            case YEARLY -> {
                                next = start.plusYears(1);
                                run = Math.toIntExact(ChronoUnit.YEARS.between(start, end)) >= 1;
                            }
                            case CUSTOM -> {
                                next = start.plusDays((long) adhoc.getReportRunEvery());
                                run = Math.toIntExact(ChronoUnit.DAYS.between(start, end)) >= adhoc.getReportRunEvery();
                            }
                        }
                    }
                }

                if (run) {
                    final StringBuilder insertSqlBuilder = new StringBuilder(900);
                    insertSqlBuilder.append("INSERT INTO ").append(adhoc.getTableName()).append("(").append(adhoc.getTableFields())
                            .append(") ").append(adhoc.getQuery());
                    if (insertSqlBuilder.length() > 0) {
                        final int result = jdbcTemplate.update(insertSqlBuilder.toString());
                        log.debug("{}: Records affected by generateClientSchedule: {}", ThreadLocalContextUtil.getTenant().getName(),
                                result);

                        jdbcTemplate.update("UPDATE m_adhoc SET last_run=? WHERE id=?", new Date(), adhoc.getId());
                    }
                } else {
                    log.debug("{}: Skipping execution of {}, scheduled for execution on {}", ThreadLocalContextUtil.getTenant().getName(),
                            adhoc.getName(), next);
                }
            });
        } else {
            log.debug("{}: Nothing to update by generateClientSchedule", ThreadLocalContextUtil.getTenant().getName());
        }
        return RepeatStatus.FINISHED;
    }
}
