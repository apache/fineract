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
package org.apache.fineract.portfolio.interestratechart.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 08/12/17.
 */
final class InterestRateChartSlabsApiResourceSwagger {

    private InterestRateChartSlabsApiResourceSwagger() {}

    @Schema(description = "GetInterestRateChartsChartIdChartSlabsResponse")
    public static final class GetInterestRateChartsChartIdChartSlabsResponse {

        private GetInterestRateChartsChartIdChartSlabsResponse() {}

        static final class GetInterestRateChartsChartIdChartSlabsIncentives {

            private GetInterestRateChartsChartIdChartSlabsIncentives() {}

            static final class GetInterestRateChartsChartIdChartSlabsEntityType {

                private GetInterestRateChartsChartIdChartSlabsEntityType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "InterestIncentiveEntityType.customer")
                public Integer code;
                @Schema(example = "Customer")
                public Integer description;
            }

            static final class GetInterestRateChartsChartIdChartSlabsAttributeName {

                private GetInterestRateChartsChartIdChartSlabsAttributeName() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "InterestIncentiveAttributeName.gender")
                public Integer code;
                @Schema(example = "Gender")
                public Integer description;
            }

            static final class GetInterestRateChartsChartIdChartSlabsConditionType {

                private GetInterestRateChartsChartIdChartSlabsConditionType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "incentiveConditionType.equal")
                public Integer code;
                @Schema(example = "equal")
                public Integer description;
            }

            static final class GetInterestRateChartsChartIdChartSlabsIncentiveType {

                private GetInterestRateChartsChartIdChartSlabsIncentiveType() {}

                @Schema(example = "3")
                public Integer id;
                @Schema(example = "InterestIncentiveType.incentive")
                public Integer code;
                @Schema(example = "Incentive")
                public Integer description;
            }

            @Schema(example = "1")
            public Integer id;
            public GetInterestRateChartsChartIdChartSlabsEntityType entityType;
            public GetInterestRateChartsChartIdChartSlabsAttributeName attributeName;
            public GetInterestRateChartsChartIdChartSlabsConditionType conditionType;
            @Schema(example = "11")
            public Integer attributeValue;
            @Schema(example = "FEMALE")
            public String attributeValueDesc;
            public GetInterestRateChartsChartIdChartSlabsIncentiveType incentiveType;
            @Schema(example = "-1.000000")
            public Float amount;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "5% interest from 1 day till 180 days of deposit")
        public String description;
        public InterestRateChartsApiResourceSwagger.GetInterestRateChartsTemplateResponse.GetInterestRateChartsTemplatePeriodTypes periodTypes;
        @Schema(example = "1")
        public Integer fromPeriod;
        @Schema(example = "180")
        public Integer toPeriod;
        @Schema(example = "5")
        public Double annualInterestRate;
        public InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.GetInterestRateChartsCurrency currency;
        public Set<GetInterestRateChartsChartIdChartSlabsIncentives> incentives;
    }

    @Schema(description = "PostInterestRateChartsChartIdChartSlabsRequest")
    public static final class PostInterestRateChartsChartIdChartSlabsRequest {

        private PostInterestRateChartsChartIdChartSlabsRequest() {}

        static final class PostInterestRateChartsChartIdChartSlabsIncentives {

            private PostInterestRateChartsChartIdChartSlabsIncentives() {}

            @Schema(example = "2")
            public Integer entityType;
            @Schema(example = "2")
            public Integer attributeName;
            @Schema(example = "2")
            public Integer conditionType;
            @Schema(example = "11")
            public Integer attributeValue;
            @Schema(example = "2")
            public Integer incentiveType;
            @Schema(example = "-1")
            public Float amount;
        }

        @Schema(example = "0")
        public Integer periodType;
        @Schema(example = "1")
        public Integer fromPeriod;
        @Schema(example = "180")
        public Integer toPeriod;
        @Schema(example = "5")
        public Double annualInterestRate;
        @Schema(example = "5% interest from 1 day till 180 days of deposit")
        public String description;
        @Schema(example = "en")
        public String locale;
        public Set<PostInterestRateChartsChartIdChartSlabsIncentives> incentives;
    }

    @Schema(description = "PostInterestRateChartsChartIdChartSlabsResponse")
    public static final class PostInterestRateChartsChartIdChartSlabsResponse {

        private PostInterestRateChartsChartIdChartSlabsResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest")
    public static final class PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest {

        private PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest() {}

        @Schema(example = "6")
        public Double annualInterestRate;
        @Schema(example = "Interest rate changed to 6%")
        public String description;
    }

    @Schema(description = "PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse")
    public static final class PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse {

        private PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest changes;
    }

    @Schema(description = "DeleteInterestRateChartsChartIdChartSlabsResponse")
    public static final class DeleteInterestRateChartsChartIdChartSlabsResponse {

        private DeleteInterestRateChartsChartIdChartSlabsResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }
}
