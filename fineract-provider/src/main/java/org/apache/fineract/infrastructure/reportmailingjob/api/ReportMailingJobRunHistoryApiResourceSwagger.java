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
import io.swagger.annotations.ApiParam;
import org.joda.time.DateTime;

/**
 * Created by sanyam on 13/8/17.
 */
final class ReportMailingJobRunHistoryApiResourceSwagger {
    private ReportMailingJobRunHistoryApiResourceSwagger(){

    }

    @ApiModel(value = "GetReportMailingJobRunHistoryResponse")
    public static final class GetReportMailingJobRunHistoryResponse {
        private GetReportMailingJobRunHistoryResponse(){

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "1")
        public Long reportMailingJobId;
        @ApiModelProperty(example = "1469627093050")
        public DateTime startDateTime;
        @ApiModelProperty(example = "1469627093050")
        public DateTime endDateTime;
        @ApiModelProperty(example = "success")
        public String status;
        @ApiModelProperty(example = "")
        public String errorMessage;
        @ApiModelProperty(example = "")
        public String errorLog;
    }
}
