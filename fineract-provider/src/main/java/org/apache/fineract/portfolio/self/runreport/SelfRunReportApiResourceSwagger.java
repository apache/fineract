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
package org.apache.fineract.portfolio.self.runreport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;


/**
 * Created by Kang Breder on 07/08/19.
 */

final class SelfRunReportApiResourceSwagger {
    private SelfRunReportApiResourceSwagger() {
    }
    @ApiModel(value = "GetRunReportResponse")
    public final static class GetRunReportResponse {
        private GetRunReportResponse() {
    }
        final class GetRunReportColumnHeaders {
            private GetRunReportColumnHeaders() {
            }
            @ApiModelProperty(example = "Office/Branch")
            public String columnName;
            @ApiModelProperty(example = "VARCHAR")
            public String columnType;
            @ApiModelProperty(example = "false")
            public Boolean isColumnNullable;
            @ApiModelProperty(example = "false")
            public Boolean isColumnPrimaryKey;
            @ApiModelProperty(example = "[]")
            public String columnValues;

        }
        final class GetPocketData {
            private GetPocketData() {
            }

                @ApiModelProperty(example = "[\"Head Office\", \"000000001\", \"John Doe\"  \"2017-03-04\", \"786YYH7\"")
                public String row;
            }
        public Set<GetRunReportColumnHeaders> columnHeaders;
        public Set<GetPocketData> data;

        }
    }
