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
package org.apache.fineract.infrastructure.dataqueries.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.dataqueries.data.ReportParameterData;

import java.util.Collection;
import java.util.List;

/**
 * Created by sanyam on 4/8/17.
 */
final class ReportsApiResourceSwagger {
    private ReportsApiResourceSwagger() {

    }

    @ApiModel(value = "GetReportsResponse")
    public static final class GetReportsResponse {
        private GetReportsResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Client Listing")
        public String reportName;
        @ApiModelProperty(example = "Table")
        public String reportType;
        public String reportSubType;
        @ApiModelProperty(example = "Client")
        public String reportCategory;
        @ApiModelProperty(example = "Individual Client Report Lists the small number of defined fields on the client table.  Would expect to copy this report and add any one to one additional data for specific tenant needs. Can be run for any size MFI but you expect it only to be run within a branch for larger ones.  Depending on how many columns are displayed, there is probably is a limit of about 20/50k clients returned for html display (export to excel doesnt have that client browser/memory impact).")
        public String description;
        @ApiModelProperty(example = "")
        public String reportSql;
        @ApiModelProperty(example = "true")
        public Boolean coreReport;
        @ApiModelProperty(example = "true")
        public Boolean useReport;
        public Collection<ReportParameterData> reportParameters;

    }

    @ApiModel(value = "GetReportsTemplateResponse")
    public static final class GetReportsTemplateResponse {
        private GetReportsTemplateResponse(){

        }

        public List<String> allowedReportTypes;
        public List<String> allowedReportSubTypes;
        public Collection<ReportParameterData> allowedParameters;
    }

    @ApiModel(value = "PostRepostRequest")
    public static final class PostRepostRequest {
        private PostRepostRequest() {

        }
        @ApiModelProperty(example = "Completely New Report")
        public String reportName;
        @ApiModelProperty(example = "Table")
        public String reportType;
        @ApiModelProperty(example = "")
        public String reportSubType;
        @ApiModelProperty(example = "Loan")
        public String reportCategory;
        @ApiModelProperty(example = "Just An Example")
        public String description;
        @ApiModelProperty(example = "select 'very good sql' as AComment")
        public String reportSql;
        public Collection<ReportParameterData> reportParameters;
    }

    @ApiModel(value = "PostReportsResponse")
    public static final class PostReportsResponse {
        private PostReportsResponse(){

        }
        @ApiModelProperty(example = "132")
        public long resourceId;
    }

    @ApiModel(value = "PutReportRequest")
    public static final class PutReportRequest {
        private PutReportRequest() {

        }
        @ApiModelProperty(example = "Completely New Report")
        public String reportName;
        public Collection<ReportParameterData> reportParameters;

    }

    @ApiModel(value = "PutReportResponse")
    public static final class PutReportResponse {
        private PutReportResponse() {

        }
        final class PutReportResponseChanges{
            private PutReportResponseChanges() {

            }
            @ApiModelProperty(example = "Changed New Report")
            public String reportName;
            public Collection<ReportParameterData> reportParameters;
        }
        @ApiModelProperty(example = "132")
        public long resourceId;
        public PutReportResponseChanges changes;
    }

    @ApiModel(value = "DeleteReportsResponse")
    public static final class DeleteReportsResponse {
        private DeleteReportsResponse(){

        }
        @ApiModelProperty(example = "132")
        public long resourceId;
    }
}
