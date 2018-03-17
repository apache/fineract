/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.floatingrates.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 12/17/17.
 */
final class FloatingRatesApiResourceSwagger {
    private FloatingRatesApiResourceSwagger() {
    }

    @ApiModel(value = "PostFloatingRatesRequest")
    public final static class PostFloatingRatesRequest {
        private PostFloatingRatesRequest() {
        }

        final class PostFloatingRatesRatePeriods {
            private PostFloatingRatesRatePeriods() {
            }

            @ApiModelProperty(example = "19 November 2015")
            public String fromDate;
            @ApiModelProperty(example = "10")
            public Double interestRate;
            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd MMMM yyyy")
            public String dateFormat;
        }

        @ApiModelProperty(example = "Floating Rate 1")
        public String name;
        @ApiModelProperty(example = "true")
        public Boolean isBaseLendingRate;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        public Set<PostFloatingRatesRatePeriods> ratePeriods;
    }

    @ApiModel(value = "PostFloatingRatesResponse")
    public final static class PostFloatingRatesResponse {
        private PostFloatingRatesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "GetFloatingRatesResponse")
    public final static class GetFloatingRatesResponse {
        private GetFloatingRatesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "Floating Rate 1")
        public String name;
        @ApiModelProperty(example = "true")
        public Boolean isBaseLendingRate;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "mifos")
        public String createdBy;
        @ApiModelProperty(example = "Nov 18, 2015")
        public String createdOn;
        @ApiModelProperty(example = "mifos")
        public String modifiedBy;
        @ApiModelProperty(example = "Nov 18, 2015")
        public String modifiedOn;
    }

    @ApiModel(value = "GetFloatingRatesFloatingRateIdResponse")
    public final static class GetFloatingRatesFloatingRateIdResponse {
        private GetFloatingRatesFloatingRateIdResponse() {
        }

        final class GetFloatingRatesRatePeriods {
            private GetFloatingRatesRatePeriods() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Dec 15, 2015")
            public String fromDate;
            @ApiModelProperty(example = "11")
            public Double interestRate;
            @ApiModelProperty(example = "false")
            public Boolean isDifferentialToBaseLendingRate;
            @ApiModelProperty(example = "true")
            public Boolean isActive;
            @ApiModelProperty(example = "mifos")
            public String createdBy;
            @ApiModelProperty(example = "Nov 18, 2015")
            public String createdOn;
            @ApiModelProperty(example = "mifos")
            public String modifiedBy;
            @ApiModelProperty(example = "Nov 18, 2015")
            public String modifiedOn;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "Floating Rate 1")
        public String name;
        @ApiModelProperty(example = "true")
        public Boolean isBaseLendingRate;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "mifos")
        public String createdBy;
        @ApiModelProperty(example = "Nov 18, 2015")
        public String createdOn;
        @ApiModelProperty(example = "mifos")
        public String modifiedBy;
        @ApiModelProperty(example = "Nov 18, 2015")
        public String modifiedOn;
        public Set<GetFloatingRatesRatePeriods> ratePeriods;
    }

    @ApiModel(value = "PutFloatingRatesFloatingRateIdRequest")
    public final static class PutFloatingRatesFloatingRateIdRequest {
        private PutFloatingRatesFloatingRateIdRequest() {
        }

        @ApiModelProperty(example = "Floating Rate 1")
        public String name;
        @ApiModelProperty(example = "true")
        public Boolean isBaseLendingRate;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        public Set<PostFloatingRatesRequest.PostFloatingRatesRatePeriods> ratePeriods;
    }

    @ApiModel(value = "PutFloatingRatesFloatingRateIdResponse")
    public final static class PutFloatingRatesFloatingRateIdResponse {
        private PutFloatingRatesFloatingRateIdResponse() {
        }

        final class PutFloatingRatesChanges {
            private PutFloatingRatesChanges() {
            }

            public Set<PostFloatingRatesRequest.PostFloatingRatesRatePeriods> ratePeriods;
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutFloatingRatesChanges changes;
    }
}
