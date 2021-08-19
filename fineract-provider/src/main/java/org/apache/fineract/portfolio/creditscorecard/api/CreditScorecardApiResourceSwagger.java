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
package org.apache.fineract.portfolio.creditscorecard.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public class CreditScorecardApiResourceSwagger {

    @Schema(description = "GetScorecardFeatureResponse")
    public static final class GetScorecardFeatureResponse {

        private GetScorecardFeatureResponse() {
            //
        }

        static final class GetScorecardFeatureValueTypeResponse {

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "valueType.nominal")
            public String code;
            @Schema(example = "Nominal")
            public String description;
        }

        static final class GetScorecardFeatureDataTypeResponse {

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "dataType.string")
            public String code;
            @Schema(example = "String")
            public String description;
        }

        static final class GetScorecardFeatureCategoryResponse {

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "category.individual")
            public String code;
            @Schema(example = "Individual")
            public String description;
        }
    }

    @Schema(description = "PostScorecardFeatureRequest")
    public static final class PostScorecardFeatureRequest {

        private PostScorecardFeatureRequest() {}

        @Schema(example = "Gender")
        public String name;
        @Schema(example = "1")
        public Integer valueType;
        @Schema(example = "1")
        public String dataType;
        @Schema(example = "1")
        public Integer category;
        @Schema(example = "true")
        public String active;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostScorecardFeatureResponse")
    public static final class PostScorecardFeatureResponse {

        private PostScorecardFeatureResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutScorecardFeaturesFeatureIdRequest")
    public static final class PutScorecardFeaturesFeatureIdRequest {

        private PutScorecardFeaturesFeatureIdRequest() {}

        @Schema(example = "Loan service fee(changed)")
        public String name;
    }

    @Schema(description = "PutScorecardFeaturesFeatureIdResponse")
    public static final class PutScorecardFeaturesFeatureIdResponse {

        private PutScorecardFeaturesFeatureIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public PutScorecardFeaturesFeatureIdResponse changes;
    }

    @Schema(description = "DeleteScorecardFeaturesFeatureIdResponse")
    public static final class DeleteScorecardFeaturesFeatureIdResponse {

        private DeleteScorecardFeaturesFeatureIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetScorecardFeaturesTemplateResponse")
    public static final class GetScorecardFeaturesTemplateResponse {

        private GetScorecardFeaturesTemplateResponse() {}

        @Schema(example = "false")
        public String active;
        @Schema(example = "false")
        public String penalty;
        public Set<GetScorecardFeatureResponse.GetScorecardFeatureValueTypeResponse> valueTypeOptions;
        public Set<GetScorecardFeatureResponse.GetScorecardFeatureDataTypeResponse> dataTypeOptions;
        public Set<GetScorecardFeatureResponse.GetScorecardFeatureCategoryResponse> categoryOptions;
    }
}
