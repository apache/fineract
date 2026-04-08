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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.jobs.api.SchedulerJobApiConstants;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "task_priority")
    private Short taskPriority;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "previous_run_start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date previousRunStartTime;

    @Column(name = "next_run_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextRunTime;

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

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(SchedulerJobApiConstants.displayNameParamName, this.jobDisplayName)) {
            final String newValue = command.stringValueOfParameterNamed(SchedulerJobApiConstants.displayNameParamName).trim();
            actualChanges.put(SchedulerJobApiConstants.displayNameParamName, newValue);
            this.jobDisplayName = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(SchedulerJobApiConstants.cronExpressionParamName, this.cronExpression)) {
            final String newValue = command.stringValueOfParameterNamed(SchedulerJobApiConstants.cronExpressionParamName).trim();
            actualChanges.put(SchedulerJobApiConstants.cronExpressionParamName, newValue);
            this.cronExpression = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInBooleanParameterNamed(SchedulerJobApiConstants.jobActiveStatusParamName, this.activeSchedular)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(SchedulerJobApiConstants.jobActiveStatusParamName);
            actualChanges.put(SchedulerJobApiConstants.jobActiveStatusParamName, newValue);
            this.activeSchedular = newValue;
        }

        return actualChanges;
    }

}
