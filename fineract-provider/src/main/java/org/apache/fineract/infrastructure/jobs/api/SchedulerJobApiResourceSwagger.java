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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.jobs.data.JobDetailHistoryData;

import java.util.Date;
import java.util.List;

/**
 * Created by sanyam on 12/8/17.
 */
final class SchedulerJobApiResourceSwagger {
    private SchedulerJobApiResourceSwagger() {

    }

    @ApiModel(value = "GetJobsResponse")
    public static final class GetJobsResponse {
        private GetJobsResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long jobId;
        @ApiModelProperty(example = "Update loan Summary")
        public String displayName;
        @ApiModelProperty(example = "")
        public Date nextRunTime;
        @ApiModelProperty(example = "")
        public String initializingError;
        @ApiModelProperty(example = "0 0 22 1/1 * ? *")
        public String cronExpression;
        @ApiModelProperty(example = "false")
        public boolean active;
        @ApiModelProperty(example = "false")
        public boolean currentlyRunning;
        public JobDetailHistoryData lastRunHistory;
    }

    @ApiModel(value = "PutJobsJobsIDRequest")
    public static final class PutJobsJobIDRequest {
        private PutJobsJobIDRequest() {

        }
        @ApiModelProperty(example = "Update loan Summary")
        public String displayName;
        @ApiModelProperty(example = "0 0 22 1/1 * ? *")
        public String cronExpression;
        @ApiModelProperty(example = "false")
        public boolean active;

    }

    @ApiModel(value = "GetJobsJobIDJobRunHistoryResponse")
    public static final class GetJobsJobIDJobRunHistoryResponse {
        private GetJobsJobIDJobRunHistoryResponse() {

        }

        final class JobDetailHistoryDataSwagger {
            private JobDetailHistoryDataSwagger(){}
            @ApiModelProperty(example = "1")
            public Long version;
            @ApiModelProperty(example = "Jul 16, 2013 12:00:00 PM")
            public Date jobRunStartTime;
            @ApiModelProperty(example = "Jul 16, 2013 12:00:00 PM")
            public Date jobRunEndTime;
            @ApiModelProperty(example = "success")
            public String status;
            @ApiModelProperty(example = "cron")
            public String triggerType;
        }
        @ApiModelProperty(example = "8")
        public int totalFilteredRecords;
        public List<JobDetailHistoryDataSwagger> pageItems;

    }
}
