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
package org.apache.fineract.infrastructure.jobs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "job", uniqueConstraints = { @UniqueConstraint(columnNames = { "short_name" }, name = "job_short_name_key") })
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ScheduledJobDetail extends AbstractPersistableCustom<Long> {

    @Column(name = "name")
    private String jobName;

    @Column(name = "display_name")
    private String jobDisplayName;

    @Column(name = "node_id")
    private Integer nodeId;

    @Column(name = "is_mismatched_job")
    private boolean isMismatchedJob;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "task_priority")
    private Short taskPriority;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "previous_run_start_time")
    private Instant previousRunStartTime;

    @Column(name = "next_run_time")
    private Instant nextRunTime;

    @Column(name = "job_key")
    private String jobKey;

    @Column(name = "initializing_errorlog")
    private String errorLog;

    @Column(name = "is_active")
    private boolean activeSchedular;

    @Column(name = "currently_running")
    private boolean currentlyRunning;

    @Column(name = "updates_allowed")
    private boolean updatesAllowed;

    @Column(name = "scheduler_group")
    private Short schedulerGroup;

    @Column(name = "is_misfired")
    private boolean triggerMisfired;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    public void updateDisplayName(final String displayName) {
        this.jobDisplayName = StringUtils.defaultIfEmpty(displayName, null);
    }

    public void updateCronExpression(final String cronExpression) {
        this.cronExpression = StringUtils.defaultIfEmpty(cronExpression, null);
    }

    public void updateActive(final boolean active) {
        this.activeSchedular = active;
    }
}
