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
import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.search.SearchConstants.SEARCH_RESPONSE_PARAMETERS;
import org.apache.fineract.portfolio.search.data.AdHocQueryDataValidator;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchConditions;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.apache.fineract.portfolio.search.data.SearchConditions;
import org.apache.fineract.portfolio.search.data.SearchData;
import org.apache.fineract.portfolio.search.service.SearchReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.search.SearchConstants.SEARCH_RESPONSE_PARAMETERS;
import org.apache.fineract.portfolio.search.data.AdHocQueryDataValidator;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchConditions;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.apache.fineract.portfolio.search.data.SearchConditions;
import org.apache.fineract.portfolio.search.data.SearchData;
import org.apache.fineract.portfolio.search.service.SearchReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
        private PostAdhocQuerySearchRequest(){

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
            // don't allow to instantiate; use only for live API documentation
        }
        ApiModelProperty(example = "HFC")
        public String officeName;
        ApiModelProperty(example = "01 BC3M")
        public String loanProductName;
        @ApiModelProperty(example = " 5692.41")
        public Long loanOutStanding;
        @ApiModelProperty(example = "76.4")
        public Long percentage;
    }

