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

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/04/17.
 */
final class InterestRateChartsApiResourceSwagger {
    private InterestRateChartsApiResourceSwagger() {
    }

    @ApiModel(value = "GetInterestRateChartsTemplateResponse")
    public final static class GetInterestRateChartsTemplateResponse {
        private GetInterestRateChartsTemplateResponse() {
        }

        final class GetInterestRateChartsTemplatePeriodTypes {
            private GetInterestRateChartsTemplatePeriodTypes() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "interestChartPeriodType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
        }

        public Set<GetInterestRateChartsTemplatePeriodTypes> periodTypes;
    }

    @ApiModel(value = "GetInterestRateChartsResponse")
    public static final class GetInterestRateChartsResponse {
        private GetInterestRateChartsResponse() {
        }

        final class GetInterestRateChartsChartSlabs {
            private GetInterestRateChartsChartSlabs() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            public GetInterestRateChartsTemplateResponse.GetInterestRateChartsTemplatePeriodTypes periodTypes;
            @ApiModelProperty(example = "1")
            public Integer fromPeriod;
            @ApiModelProperty(example = "6")
            public Integer annualInterestRate;
            public GetInterestRateChartsCurrency currency;
        }

        final class GetInterestRateChartsCurrency {
            private GetInterestRateChartsCurrency() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "[2014, 1, 1]")
        public LocalDate fromDate;
        @ApiModelProperty(example = "1")
        public Integer savingsProductId;
        @ApiModelProperty(example = "Fixed Deposit Product 001")
        public String savingsProductName;
        public Set<GetInterestRateChartsChartSlabs> chartSlabs;
    }

    @ApiModel(value = "PostInterestRateChartsRequest")
    public static final class PostInterestRateChartsRequest {
        private PostInterestRateChartsRequest() {
        }

        @ApiModelProperty(example = "Chart - 2014")
        public String name;
        @ApiModelProperty(example = "This chart is applicable for year 2014")
        public String description;
        @ApiModelProperty(example = "Document")
        public String type;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 Jan 2014")
        public String fromDate;
    }

    @ApiModel(value = "PostInterestRateChartsResponse")
    public static final class PostInterestRateChartsResponse {
        private PostInterestRateChartsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutInterestRateChartsChartIdRequest")
    public static final class PutInterestRateChartsChartIdRequest {
        private PutInterestRateChartsChartIdRequest() {
        }
        @ApiModelProperty(example = "Interest rate chart for 2014")
        public String name;
        @ApiModelProperty(example = "Interest rate chart for 2014")
        public String description;
    }

    @ApiModel(value = "PutInterestRateChartsChartIdResponse")
    public static final class PutInterestRateChartsChartIdResponse {
        private PutInterestRateChartsChartIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "DeleteInterestRateChartsChartIdResponse")
    public static final class DeleteInterestRateChartsChartIdResponse {
        private DeleteInterestRateChartsChartIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }
}
