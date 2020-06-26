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
package org.apache.fineract.portfolio.floatingrates.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/17/17.
 */
final class FloatingRatesApiResourceSwagger {

    private FloatingRatesApiResourceSwagger() {}

    @Schema(description = "PostFloatingRatesRequest")
    public static final class PostFloatingRatesRequest {

        private PostFloatingRatesRequest() {}

        static final class PostFloatingRatesRatePeriods {

            private PostFloatingRatesRatePeriods() {}

            @Schema(example = "19 November 2015")
            public String fromDate;
            @Schema(example = "10")
            public Double interestRate;
            @Schema(example = "en")
            public String locale;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
        }

        @Schema(example = "Floating Rate 1")
        public String name;
        @Schema(example = "true")
        public Boolean isBaseLendingRate;
        @Schema(example = "true")
        public Boolean isActive;
        public Set<PostFloatingRatesRatePeriods> ratePeriods;
    }

    @Schema(description = "PostFloatingRatesResponse")
    public static final class PostFloatingRatesResponse {

        private PostFloatingRatesResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetFloatingRatesResponse")
    public static final class GetFloatingRatesResponse {

        private GetFloatingRatesResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Floating Rate 1")
        public String name;
        @Schema(example = "true")
        public Boolean isBaseLendingRate;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "mifos")
        public String createdBy;
        @Schema(example = "Nov 18, 2015")
        public String createdOn;
        @Schema(example = "mifos")
        public String modifiedBy;
        @Schema(example = "Nov 18, 2015")
        public String modifiedOn;
    }

    @Schema(description = "GetFloatingRatesFloatingRateIdResponse")
    public static final class GetFloatingRatesFloatingRateIdResponse {

        private GetFloatingRatesFloatingRateIdResponse() {}

        static final class GetFloatingRatesRatePeriods {

            private GetFloatingRatesRatePeriods() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Dec 15, 2015")
            public String fromDate;
            @Schema(example = "11")
            public Double interestRate;
            @Schema(example = "false")
            public Boolean isDifferentialToBaseLendingRate;
            @Schema(example = "true")
            public Boolean isActive;
            @Schema(example = "mifos")
            public String createdBy;
            @Schema(example = "Nov 18, 2015")
            public String createdOn;
            @Schema(example = "mifos")
            public String modifiedBy;
            @Schema(example = "Nov 18, 2015")
            public String modifiedOn;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Floating Rate 1")
        public String name;
        @Schema(example = "true")
        public Boolean isBaseLendingRate;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "mifos")
        public String createdBy;
        @Schema(example = "Nov 18, 2015")
        public String createdOn;
        @Schema(example = "mifos")
        public String modifiedBy;
        @Schema(example = "Nov 18, 2015")
        public String modifiedOn;
        public Set<GetFloatingRatesRatePeriods> ratePeriods;
    }

    @Schema(description = "PutFloatingRatesFloatingRateIdRequest")
    public static final class PutFloatingRatesFloatingRateIdRequest {

        private PutFloatingRatesFloatingRateIdRequest() {}

        @Schema(example = "Floating Rate 1")
        public String name;
        @Schema(example = "true")
        public Boolean isBaseLendingRate;
        @Schema(example = "true")
        public Boolean isActive;
        public Set<PostFloatingRatesRequest.PostFloatingRatesRatePeriods> ratePeriods;
    }

    @Schema(description = "PutFloatingRatesFloatingRateIdResponse")
    public static final class PutFloatingRatesFloatingRateIdResponse {

        private PutFloatingRatesFloatingRateIdResponse() {}

        static final class PutFloatingRatesChanges {

            private PutFloatingRatesChanges() {}

            public Set<PostFloatingRatesRequest.PostFloatingRatesRatePeriods> ratePeriods;
        }

        @Schema(example = "1")
        public Integer resourceId;
        public PutFloatingRatesChanges changes;
    }
}
