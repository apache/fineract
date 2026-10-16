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
package org.apache.fineract.infrastructure.creditbureau.api;

import io.swagger.v3.oas.annotations.media.Schema;

final class CreditBureauConfigurationApiResourceSwagger {

    private CreditBureauConfigurationApiResourceSwagger() {}

    @Schema(description = "PostOrganisationCreditBureauRequest")
    public static final class PostOrganisationCreditBureauRequest {

        private PostOrganisationCreditBureauRequest() {}

        @Schema(example = "ThitsaWorks Myanmar")
        public String alias;
        @Schema(example = "true")
        public Boolean isActive;
    }

    @Schema(description = "PutOrganisationCreditBureauRequest")
    public static final class PutOrganisationCreditBureauRequest {

        private PutOrganisationCreditBureauRequest() {}

        @Schema(example = "1", description = "The organisation credit bureau to update")
        public Long creditBureauId;
        @Schema(example = "true")
        public Boolean isActive;
    }

    @Schema(description = "PostCreditBureauConfigurationRequest")
    public static final class PostCreditBureauConfigurationRequest {

        private PostCreditBureauConfigurationRequest() {}

        @Schema(example = "USERNAME")
        public String configkey;
        @Schema(example = "testUser")
        public String value;
        @Schema(example = "The user name for the credit bureau")
        public String description;
    }

    @Schema(description = "PutCreditBureauConfigurationRequest")
    public static final class PutCreditBureauConfigurationRequest {

        private PutCreditBureauConfigurationRequest() {}

        @Schema(example = "USERNAME")
        public String configkey;
        @Schema(example = "testUser")
        public String value;
    }

    @Schema(description = "PostCreditBureauLoanProductMappingRequest")
    public static final class PostCreditBureauLoanProductMappingRequest {

        private PostCreditBureauLoanProductMappingRequest() {}

        @Schema(example = "1")
        public Long loanProductId;
        @Schema(example = "true")
        public Boolean isCreditcheckMandatory;
        @Schema(example = "false")
        public Boolean skipCreditcheckInFailure;
        @Schema(example = "30")
        public Integer stalePeriod;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "en", description = "Required together with stalePeriod")
        public String locale;
    }
}
