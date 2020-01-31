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
package org.apache.fineract.portfolio.search.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.LocalDate;



/**
 * Created by Kang Breder on 13/06/19.
 */
final class SearchApiResourceSwagger {
    private SearchApiResourceSwagger() {

    }

    @ApiModel(value = "GetSearchResponse")
    public static final class GetSearchResponse {
        private GetSearchResponse() {

        }

        @ApiModelProperty(example = "1")
        public Long entityId;
        @ApiModelProperty(example = "000000001")
        public Long entityAccountNo;
        @ApiModelProperty(example = "ID_JKZGEXF")
        public String entityExternalId;
        @ApiModelProperty(example = "Group_Name_HVCU5")
        public String entityName;
        @ApiModelProperty(example = "GROUP")
        public String entityType;
        @ApiModelProperty(example = "1")
        public Long parentId;
        @ApiModelProperty(example = "Head Office")
        public String parentName;
        public EnumOptionData entityStatus;
    }

    @ApiModel(value = "PostAdhocQuerySearchRequest")
    public static final class PostAdhocQuerySearchRequest {
        private PostAdhocQuerySearchRequest() {

        }


        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "approvalDate")
        public String loanDateOption;
        @ApiModelProperty(example = "2013-01-01")
        public LocalDate loanFromDate;
        @ApiModelProperty(example = "2014-01-27")
        public LocalDate loanToDate;
        @ApiModelProperty(example = "true")
        public boolean includeOutStandingAmountPercentage;
        @ApiModelProperty(example = "<=")
        public String outStandingAmountPercentageCondition;
        @ApiModelProperty(example = "80")
        public Long outStandingAmountPercentage;
        @ApiModelProperty(example = "true")
        public boolean includeOutstandingAmount;
        @ApiModelProperty(example = "between")
        public String outstandingAmountCondition;
        @ApiModelProperty(example = "100")
        public Long minOutstandingAmount;
        @ApiModelProperty(example = "10000")
        public Long maxOutstandingAmount;
    }

    @ApiModel(value = "PostAdhocQuerySearchResponse")
    public static final class PostAdhocQuerySearchResponse {
        private PostAdhocQuerySearchResponse() {

        }

        @ApiModelProperty(example = "HFC")
        public String officeName;
        @ApiModelProperty(example = "01 BC3M")
        public String loanProductName;
        @ApiModelProperty(example = " 5692.41")
        public Long loanOutStanding;
        @ApiModelProperty(example = "76.4")
        public Long percentage;
    }
}

