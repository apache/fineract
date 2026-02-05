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
package org.apache.fineract.portfolio.loanorigination.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.codes.api.CodeValuesApiResourceSwagger.GetCodeValuesDataResponse;

final class LoanOriginatorApiResourceSwagger {

    private LoanOriginatorApiResourceSwagger() {}

    @Schema(description = "GetLoanOriginatorsResponse")
    public static final class GetLoanOriginatorsResponse {

        private GetLoanOriginatorsResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "EXT-001")
        public String externalId;
        @Schema(example = "Main Branch Originator")
        public String name;
        @Schema(example = "ACTIVE")
        public String status;
        @Schema(example = "1")
        public GetCodeValuesDataResponse originatorType;
        @Schema(example = "1")
        public GetCodeValuesDataResponse channelType;
    }

    @Schema(description = "GetLoanOriginatorTemplateResponse")
    public static final class GetLoanOriginatorTemplateResponse {

        private GetLoanOriginatorTemplateResponse() {}

        @Schema(example = "EXT-001")
        public String externalId;
        public Set<String> statusOptions;
        public List<GetCodeValuesDataResponse> originatorTypeOptions;
        public List<GetCodeValuesDataResponse> channelTypeOptions;
    }

    @Schema(description = "PostLoanOriginatorsRequest")
    public static final class PostLoanOriginatorsRequest {

        private PostLoanOriginatorsRequest() {}

        @Schema(example = "EXT-001")
        public String externalId;
        @Schema(example = "Main Branch Originator", requiredMode = Schema.RequiredMode.REQUIRED)
        public String name;
        @Schema(example = "ACTIVE")
        public String status;
        @Schema(example = "1")
        public Long originatorTypeId;
        @Schema(example = "1")
        public Long channelTypeId;
    }

    @Schema(description = "PostLoanOriginatorsResponse")
    public static final class PostLoanOriginatorsResponse {

        private PostLoanOriginatorsResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PutLoanOriginatorsRequest")
    public static final class PutLoanOriginatorsRequest {

        private PutLoanOriginatorsRequest() {}

        @Schema(example = "Updated Name")
        public String name;
        @Schema(example = "INACTIVE")
        public String status;
        @Schema(example = "2")
        public Long originatorTypeId;
        @Schema(example = "2")
        public Long channelTypeId;
    }

    @Schema(description = "PutLoanOriginatorsResponse")
    public static final class PutLoanOriginatorsResponse {

        private PutLoanOriginatorsResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "DeleteLoanOriginatorsResponse")
    public static final class DeleteLoanOriginatorsResponse {

        private DeleteLoanOriginatorsResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }
}
