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
package org.apache.fineract.adhocquery.service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.adhocquery.domain.ReportRunFrequency;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "adHocScheduledJobRunnerService")
public class AdHocScheduledJobRunnerServiceImpl implements AdHocScheduledJobRunnerService {

    private static final Logger LOG = LoggerFactory.getLogger(AdHocScheduledJobRunnerServiceImpl.class);
    private final AdHocReadPlatformService adHocReadPlatformService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AdHocScheduledJobRunnerServiceImpl(final JdbcTemplate jdbcTemplate, final AdHocReadPlatformService adHocReadPlatformService) {
        this.jdbcTemplate = jdbcTemplate;
        this.adHocReadPlatformService = adHocReadPlatformService;

    }

    @Transactional
    @Override
    @CronTarget(jobName = JobName.GENERATE_ADHOCCLIENT_SCEHDULE)
    @SuppressWarnings("UnnecessaryDefaultInEnumSwitch")
    public void generateClientSchedule() {
        final Collection<AdHocData> adhocs = this.adHocReadPlatformService.retrieveAllActiveAdHocQuery();
        if (adhocs.size() > 0) {
            adhocs.forEach(adhoc -> {
                boolean run = true;
                LocalDate next = null;
                if (adhoc.getReportRunFrequency() != null) {
                    if (adhoc.getLastRun() != null) {
                        LocalDate start = adhoc.getLastRun().toLocalDate();
                        LocalDate end = ZonedDateTime.now(DateUtils.getDateTimeZoneOfTenant()).toLocalDate();
                        switch (ReportRunFrequency.fromId(adhoc.getReportRunFrequency())) {
                            case DAILY:
                                next = start.plusDays(1);
                                run = Math.toIntExact(ChronoUnit.DAYS.between(start, end)) >= 1;
                            break;
                            case WEEKLY:
                                next = start.plusDays(7);
                                run = Math.toIntExact(ChronoUnit.DAYS.between(start, end)) >= 7;
                            break;
                            case MONTHLY:
                                next = start.plusMonths(1);
                                run = Math.toIntExact(ChronoUnit.MONTHS.between(start, end)) >= 1;
                            break;
                            case YEARLY:
                                next = start.plusYears(1);
                                run = Math.toIntExact(ChronoUnit.YEARS.between(start, end)) >= 1;
                            break;
                            case CUSTOM:
                                next = start.plusDays((int) (long) adhoc.getReportRunEvery());
                                run = Math.toIntExact(ChronoUnit.DAYS.between(start, end)) >= adhoc.getReportRunEvery();
                            break;
                            default:
                                throw new IllegalStateException();
                        }
                    }
                }

                if (run) {
                    // jdbcTemplate.execute("truncate table
                    // "+adhoc.getTableName());
                    final StringBuilder insertSqlBuilder = new StringBuilder(900);
                    insertSqlBuilder.append("INSERT INTO ").append(adhoc.getTableName() + "(").append(adhoc.getTableFields() + ") ")
                            .append(adhoc.getQuery());
                    if (insertSqlBuilder.length() > 0) {
                        final int result = this.jdbcTemplate.update(insertSqlBuilder.toString());
                        LOG.info("{}: Records affected by generateClientSchedule: {}", ThreadLocalContextUtil.getTenant().getName(),
                                result);

                        this.jdbcTemplate.update("UPDATE m_adhoc SET last_run=? WHERE id=?", new Date(), adhoc.getId());
                    }
                } else {
                    LOG.info("{}: Skipping execution of {}, scheduled for execution on {}",
                            new Object[] { ThreadLocalContextUtil.getTenant().getName(), adhoc.getName(), next });
                }
            });
        } else {
            LOG.info("{}: Nothing to update by generateClientSchedule", ThreadLocalContextUtil.getTenant().getName());
        }

    }

}
