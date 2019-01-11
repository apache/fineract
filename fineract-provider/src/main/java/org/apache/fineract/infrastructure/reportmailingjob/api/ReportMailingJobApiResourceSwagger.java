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
package org.apache.fineract.infrastructure.reportmailingjob.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobTimelineData;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sanyam on 13/8/17.
 */
final class ReportMailingJobApiResourceSwagger {
    private ReportMailingJobApiResourceSwagger(){

    }

    @ApiModel(value = "GetReportMailingJobsTemplate")
    public static final class GetReportMailingJobsTemplate {
        private GetReportMailingJobsTemplate(){

        }
        @ApiModelProperty(example = "true")
        public boolean isActive;
        public List<EnumOptionData> emailAttachmentFileFormatOptions;
        public List<EnumOptionData> stretchyReportParamDateOptions;
    }

    @ApiModel(value = "GetReportMailingJobsResponse")
    public static final class GetReportMailingJobsResponse {
        private GetReportMailingJobsResponse(){

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Client Numbers Report")
        public String name;
        @ApiModelProperty(example = "Client Numbers Report")
        public String description;
        @ApiModelProperty(example = "1469627093000")
        public DateTime startDateTime;
        @ApiModelProperty(example = "")
        public String recurrence;
        public ReportMailingJobTimelineData timeline;
        @ApiModelProperty(example = "info@musonisystem.com")
        public String emailRecipients;
        @ApiModelProperty(example = "Client Numbers Report")
        public String emailSubject;
        @ApiModelProperty(example = "Client Numbers Report")
        public String emailMessage;
        @ApiModelProperty(example = "")
        public EnumOptionData emailAttachmentFileFormat;
        @ApiModelProperty(example = "")
        public ReportData stretchyReport;
        @ApiModelProperty(example = "{\"startDate\":\"2016-07-01\",\"endDate\":\"2016-08-02\",\"selectOffice\":\"1\",\"environementUrl\":\"environementUrl\"}")
        public String stretchyReportParamMap;
        @ApiModelProperty(example = "1469627093000")
        public DateTime nextRunDateTime;
        @ApiModelProperty(example = "0")
        public Integer numberOfRuns;
        @ApiModelProperty(example = "true")
        public boolean isActive;
        @ApiModelProperty(example = "1")
        public Long runAsUserId;
    }

    @ApiModel(value = "PostReportMailingJobsRequest")
    public static final class PostReportMailingJobsRequest {
        private PostReportMailingJobsRequest(){

        }
        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy HH:mm:ss")
        public String dateFormat;
        @ApiModelProperty(example = "Client Numbers Report")
        public String name;
        @ApiModelProperty(example = "Client Numbers Report")
        public String description;
        @ApiModelProperty(example = "1469627093000")
        public DateTime startDateTime;
        @ApiModelProperty(example = "120")
        public Long stretchyReportId;
        @ApiModelProperty(example = "info@musonisystem.com")
        public String emailRecipients;
        @ApiModelProperty(example = "Client Numbers Report")
        public String emailSubject;
        @ApiModelProperty(example = "Client Numbers Report")
        public String emailMessage;
        @ApiModelProperty(example = "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE,FR")
        public String recurrence;
        @ApiModelProperty(example = "true")
        public boolean isActive;
        @ApiModelProperty(example = "{\"startDate\":\"2016-07-01\",\"endDate\":\"2016-08-02\",\"selectOffice\":\"1\",\"environementUrl\":\"environementUrl\"}")
        public String stretchyReportParamMap;

    }

    @ApiModel(value = "PostReportMailingJobsResponse")
    public static final class PostReportMailingJobsResponse {
        private PostReportMailingJobsResponse(){

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "PutReportMailingJobsRequest")
    public static final class PutReportMailingJobsRequest {
        private PutReportMailingJobsRequest(){

        }
        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy HH:mm:ss")
        public String dateFormat;
        @ApiModelProperty(example = "10-08-2016 23:30:00")
        public DateTime startDateTime;
    }

    @ApiModel(value = "PutReportMailingJobsResponse")
    public static final class PutReportMailingJobsResponse {
        private PutReportMailingJobsResponse(){

        }
        final class PutReportMailingJobsResponseChanges{
            private PutReportMailingJobsResponseChanges(){}
            @ApiModelProperty(example = "10-08-2016 23:30:00")
            public DateTime startDateTime;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        public PutReportMailingJobsResponseChanges changes;
    }

    @ApiModel(value = "DeleteReportMailingJobsRequest ")
    public static final class DeleteReportMailingJobsRequest {
        private DeleteReportMailingJobsRequest(){

        }
    }

    @ApiModel(value = "DeleteReportMailingJobsResponse")
    public static final class DeleteReportMailingJobsResponse {
        private DeleteReportMailingJobsResponse(){

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

}
