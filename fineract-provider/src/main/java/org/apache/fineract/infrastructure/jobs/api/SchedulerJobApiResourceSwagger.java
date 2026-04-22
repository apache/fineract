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
package org.apache.fineract.infrastructure.jobs.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import org.apache.fineract.infrastructure.jobs.data.JobDetailHistoryData;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;

/**
 * Created by sanyam on 12/8/17.
 */
final class SchedulerJobApiResourceSwagger {

    private SchedulerJobApiResourceSwagger() {

    }

    @Schema(description = "GetJobsResponse")
    public static final class GetJobsResponse {

        private GetJobsResponse() {

        }

        @Schema(example = "1")
        public Long jobId;
        @Schema(example = "Update loan Summary")
        public String displayName;
        @Schema(example = "LA_USUM")
        public String shortName;
        @Schema(example = "")
        public Date nextRunTime;
        @Schema(example = "")
        public String initializingError;
        @Schema(example = "0 0 22 1/1 * ? *")
        public String cronExpression;
        @Schema(example = "false")
        public boolean active;
        @Schema(example = "false")
        public boolean currentlyRunning;
        public JobDetailHistoryData lastRunHistory;
    }

    @Schema(description = "PutJobsJobsIDRequest")
    public static final class PutJobsJobIDRequest {

        private PutJobsJobIDRequest() {

        }

        @Schema(example = "Update loan Summary")
        public String displayName;
        @Schema(example = "0 0 22 1/1 * ? *")
        public String cronExpression;
        @Schema(example = "false")
        public boolean active;

    }

    @Schema(description = "GetJobsJobIDJobRunHistoryResponse")
    public static final class GetJobsJobIDJobRunHistoryResponse {

        private GetJobsJobIDJobRunHistoryResponse() {

        }

        static final class JobDetailHistoryDataSwagger {

            private JobDetailHistoryDataSwagger() {}

            @Schema(example = "1")
            public Long version;
            @Schema(example = "Jul 16, 2013 12:00:00 PM")
            public Date jobRunStartTime;
            @Schema(example = "Jul 16, 2013 12:00:00 PM")
            public Date jobRunEndTime;
            @Schema(example = "success")
            public String status;
            @Schema(example = "cron")
            public String triggerType;
        }

        @Schema(example = "8")
        public int totalFilteredRecords;
        public List<JobDetailHistoryDataSwagger> pageItems;

    }

    @Schema(description = "ExecuteJobRequest")
    public static final class ExecuteJobRequest {

        private ExecuteJobRequest() {

        }

        @Schema(example = "Update loan Summary")
        public List<JobParameterDTO> jobParameters;

    }
}
