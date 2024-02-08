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
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/04/17.
 */
final class InterestRateChartsApiResourceSwagger {

    private InterestRateChartsApiResourceSwagger() {}

    @Schema(description = "GetInterestRateChartsTemplateResponse")
    public static final class GetInterestRateChartsTemplateResponse {

        private GetInterestRateChartsTemplateResponse() {}

        static final class GetInterestRateChartsTemplatePeriodTypes {

            private GetInterestRateChartsTemplatePeriodTypes() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "interestChartPeriodType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        public Set<GetInterestRateChartsTemplatePeriodTypes> periodTypes;
    }

    @Schema(description = "GetInterestRateChartsResponse")
    public static final class GetInterestRateChartsResponse {

        private GetInterestRateChartsResponse() {}

        static final class GetInterestRateChartsChartSlabs {

            private GetInterestRateChartsChartSlabs() {}

            @Schema(example = "1")
            public Long id;
            public GetInterestRateChartsTemplateResponse.GetInterestRateChartsTemplatePeriodTypes periodTypes;
            @Schema(example = "1")
            public Integer fromPeriod;
            @Schema(example = "6")
            public Integer annualInterestRate;
            public GetInterestRateChartsCurrency currency;
        }

        static final class GetInterestRateChartsCurrency {

            private GetInterestRateChartsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "[2014, 1, 1]")
        public LocalDate fromDate;
        @Schema(example = "1")
        public Long savingsProductId;
        @Schema(example = "Fixed Deposit Product 001")
        public String savingsProductName;
        public Set<GetInterestRateChartsChartSlabs> chartSlabs;
    }

    @Schema(description = "PostInterestRateChartsRequest")
    public static final class PostInterestRateChartsRequest {

        private PostInterestRateChartsRequest() {}

        @Schema(example = "Chart - 2014")
        public String name;
        @Schema(example = "This chart is applicable for year 2014")
        public String description;
        @Schema(example = "Document")
        public String type;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "01 Jan 2014")
        public String fromDate;
    }

    @Schema(description = "PostInterestRateChartsResponse")
    public static final class PostInterestRateChartsResponse {

        private PostInterestRateChartsResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PutInterestRateChartsChartIdRequest")
    public static final class PutInterestRateChartsChartIdRequest {

        private PutInterestRateChartsChartIdRequest() {}

        @Schema(example = "Interest rate chart for 2014")
        public String name;
        @Schema(example = "Interest rate chart for 2014")
        public String description;
    }

    @Schema(description = "PutInterestRateChartsChartIdResponse")
    public static final class PutInterestRateChartsChartIdResponse {

        private PutInterestRateChartsChartIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "DeleteInterestRateChartsChartIdResponse")
    public static final class DeleteInterestRateChartsChartIdResponse {

        private DeleteInterestRateChartsChartIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }
}
