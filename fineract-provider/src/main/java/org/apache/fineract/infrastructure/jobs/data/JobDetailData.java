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

public class JobDetailData {

    @SuppressWarnings("unused")
    private final Long jobId;

    @SuppressWarnings("unused")
    private final String displayName;

    @SuppressWarnings("unused")
    private final Date nextRunTime;

    @SuppressWarnings("unused")
    private final String initializingError;

    @SuppressWarnings("unused")
    private final String cronExpression;

    @SuppressWarnings("unused")
    private final boolean active;

    @SuppressWarnings("unused")
    private final boolean currentlyRunning;

    @SuppressWarnings("unused")
    private final JobDetailHistoryData lastRunHistory;

    public JobDetailData(final Long jobId, final String displayName, final Date nextRunTime, final String initializingError,
            final String cronExpression, final boolean active, final boolean currentlyRunning, final JobDetailHistoryData lastRunHistory) {
        this.jobId = jobId;
        this.displayName = displayName;
        this.nextRunTime = nextRunTime;
        this.initializingError = initializingError;
        this.cronExpression = cronExpression;
        this.active = active;
        this.lastRunHistory = lastRunHistory;
        this.currentlyRunning = currentlyRunning;
    }
}
