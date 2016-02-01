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

public class JobDetailHistoryData {

    @SuppressWarnings("unused")
    private final Long version;

    @SuppressWarnings("unused")
    private final Date jobRunStartTime;

    @SuppressWarnings("unused")
    private final Date jobRunEndTime;

    @SuppressWarnings("unused")
    private final String status;

    @SuppressWarnings("unused")
    private final String jobRunErrorMessage;

    @SuppressWarnings("unused")
    private final String triggerType;

    @SuppressWarnings("unused")
    private final String jobRunErrorLog;

    public JobDetailHistoryData(final Long version, final Date jobRunStartTime, final Date jobRunEndTime, final String status,
            final String jobRunErrorMessage, final String triggerType, final String jobRunErrorLog) {
        this.version = version;
        this.jobRunStartTime = jobRunStartTime;
        this.jobRunEndTime = jobRunEndTime;
        this.status = status;
        this.jobRunErrorMessage = jobRunErrorMessage;
        this.triggerType = triggerType;
        this.jobRunErrorLog = jobRunErrorLog;
    }
}