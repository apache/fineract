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
package org.apache.fineract.portfolio.interestratechart.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 08/12/17.
 */
final class InterestRateChartSlabsApiResourceSwagger {
    private InterestRateChartSlabsApiResourceSwagger() {
    }

    @ApiModel(value = "GetInterestRateChartsChartIdChartSlabsResponse")
    public final static class GetInterestRateChartsChartIdChartSlabsResponse {
        private GetInterestRateChartsChartIdChartSlabsResponse() {
        }

        final class GetInterestRateChartsChartIdChartSlabsIncentives {
            private GetInterestRateChartsChartIdChartSlabsIncentives() {
            }

            final class GetInterestRateChartsChartIdChartSlabsEntityType {
                private GetInterestRateChartsChartIdChartSlabsEntityType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "InterestIncentiveEntityType.customer")
                public Integer code;
                @ApiModelProperty(example = "Customer")
                public Integer value;
            }

            final class GetInterestRateChartsChartIdChartSlabsAttributeName {
                private GetInterestRateChartsChartIdChartSlabsAttributeName() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "InterestIncentiveAttributeName.gender")
                public Integer code;
                @ApiModelProperty(example = "Gender")
                public Integer value;
            }

            final class GetInterestRateChartsChartIdChartSlabsConditionType {
                private GetInterestRateChartsChartIdChartSlabsConditionType() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "incentiveConditionType.equal")
                public Integer code;
                @ApiModelProperty(example = "equal")
                public Integer value;
            }

            final class GetInterestRateChartsChartIdChartSlabsIncentiveType {
                private GetInterestRateChartsChartIdChartSlabsIncentiveType() {
                }

                @ApiModelProperty(example = "3")
                public Integer id;
                @ApiModelProperty(example = "InterestIncentiveType.incentive")
                public Integer code;
                @ApiModelProperty(example = "Incentive")
                public Integer value;
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            public GetInterestRateChartsChartIdChartSlabsEntityType entityType;
            public GetInterestRateChartsChartIdChartSlabsAttributeName attributeName;
            public GetInterestRateChartsChartIdChartSlabsConditionType conditionType;
            @ApiModelProperty(example = "11")
            public Integer attributeValue;
            @ApiModelProperty(example = "FEMALE")
            public String attributeValueDesc;
            public GetInterestRateChartsChartIdChartSlabsIncentiveType incentiveType;
            @ApiModelProperty(example = "-1.000000")
            public Float amount;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "5% interest from 1 day till 180 days of deposit")
        public String description;
        public InterestRateChartsApiResourceSwagger.GetInterestRateChartsTemplateResponse.GetInterestRateChartsTemplatePeriodTypes periodTypes;
        @ApiModelProperty(example = "1")
        public Integer fromPeriod;
        @ApiModelProperty(example = "180")
        public Integer toPeriod;
        @ApiModelProperty(example = "5")
        public Double annualInterestRate;
        public InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.GetInterestRateChartsCurrency currency;
        public Set<GetInterestRateChartsChartIdChartSlabsIncentives> incentives;
    }

    @ApiModel(value = "PostInterestRateChartsChartIdChartSlabsRequest")
    public static final class PostInterestRateChartsChartIdChartSlabsRequest {
        private PostInterestRateChartsChartIdChartSlabsRequest() {
        }

        final class PostInterestRateChartsChartIdChartSlabsIncentives {
            private PostInterestRateChartsChartIdChartSlabsIncentives() {
            }

            @ApiModelProperty(example = "2")
            public Integer entityType;
            @ApiModelProperty(example = "2")
            public Integer attributeName;
            @ApiModelProperty(example = "2")
            public Integer conditionType;
            @ApiModelProperty(example = "11")
            public Integer attributeValue;
            @ApiModelProperty(example = "2")
            public Integer incentiveType;
            @ApiModelProperty(example = "-1")
            public Float amount;
        }

        @ApiModelProperty(example = "0")
        public Integer periodType;
        @ApiModelProperty(example = "1")
        public Integer fromPeriod;
        @ApiModelProperty(example = "180")
        public Integer toPeriod;
        @ApiModelProperty(example = "5")
        public Double annualInterestRate;
        @ApiModelProperty(example = "5% interest from 1 day till 180 days of deposit")
        public String description;
        @ApiModelProperty(example = "en")
        public String locale;
        public Set<PostInterestRateChartsChartIdChartSlabsIncentives> incentives;
    }

    @ApiModel(value = "PostInterestRateChartsChartIdChartSlabsResponse")
    public final static class PostInterestRateChartsChartIdChartSlabsResponse {
        private PostInterestRateChartsChartIdChartSlabsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest")
    public static final class PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest {
        private PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest() {
        }

        @ApiModelProperty(example = "6")
        public Double annualInterestRate;
        @ApiModelProperty(example = "Interest rate changed to 6%")
        public String description;
    }

    @ApiModel(value = "PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse")
    public static final class PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse {
        private PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest changes;
    }

    @ApiModel(value = "DeleteInterestRateChartsChartIdChartSlabsResponse")
    public final static class DeleteInterestRateChartsChartIdChartSlabsResponse {
        private DeleteInterestRateChartsChartIdChartSlabsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
