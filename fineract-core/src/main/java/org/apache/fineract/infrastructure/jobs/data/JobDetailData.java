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
package org.apache.fineract.infrastructure.jobs.data;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class JobDetailData {

    @SuppressWarnings("unused")
    private Long jobId;

    @SuppressWarnings("unused")
    private String displayName;

    @SuppressWarnings("unused")
    private String shortName;

    @SuppressWarnings("unused")
    private Date nextRunTime;

    @SuppressWarnings("unused")
    private String initializingError;

    private String cronExpression;

    @SuppressWarnings("unused")
    private boolean active;

    @SuppressWarnings("unused")
    private boolean currentlyRunning;

    @SuppressWarnings("unused")
    private JobDetailHistoryData lastRunHistory;

    public JobDetailData(Long jobId, String displayName, String shortName, Date nextRunTime, String initializingError,
            String cronExpression, boolean active, boolean currentlyRunning, Long version, Date jobRunStartTime, Date jobRunEndTime,
            String status, String jobRunErrorMessage, String triggerType, String jobRunErrorLog) {
        this.jobId = jobId;
        this.displayName = displayName;
        this.shortName = shortName;
        this.nextRunTime = nextRunTime;
        this.initializingError = initializingError;
        this.cronExpression = cronExpression;
        this.active = active;
        this.currentlyRunning = currentlyRunning;
        if (version != null) {
            this.lastRunHistory = new JobDetailHistoryData().setVersion(version).setJobRunStartTime(jobRunStartTime)
                    .setJobRunEndTime(jobRunEndTime).setStatus(status).setJobRunErrorMessage(jobRunErrorMessage).setTriggerType(triggerType)
                    .setJobRunErrorLog(jobRunErrorLog);
        }
    }
}
