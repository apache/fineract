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
package org.apache.fineract.organisation.provisioning.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

/**
 * Created by sanyam on 18/8/17.
 */
final class ProvisioningCriteriaApiResourceSwagger {

    private ProvisioningCriteriaApiResourceSwagger() {

    }

    @Schema(description = "PostProvisioningCriteriaRequest")
    public static final class PostProvisioningCriteriaRequest {

        private PostProvisioningCriteriaRequest() {

        }

        @Schema(example = "High Risk Products Criteria")
        public String criteriaName;
        public Collection<LoanProductData> loanProducts;
        public Collection<ProvisioningCriteriaDefinitionData> provisioningcriteria;
    }

    @Schema(description = "PostProvisioningCriteriaResponse")
    public static final class PostProvisioningCriteriaResponse {

        private PostProvisioningCriteriaResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "GetProvisioningCriteriaResponse")
    public static final class GetProvisioningCriteriaResponse {

        private GetProvisioningCriteriaResponse() {

        }

        @Schema(example = "1")
        public Long criteriaId;
        @Schema(example = "High Risk Products Criteria")
        public String criteriaName;
        @Schema(example = "mifos")
        public String createdBy;
    }

    @Schema(description = "GetProvisioningCriteriaCriteriaIdResponse")
    public static final class GetProvisioningCriteriaCriteriaIdResponse {

        private GetProvisioningCriteriaCriteriaIdResponse() {

        }

        @Schema(example = "1")
        public Long criteriaId;
        @Schema(example = "High Risk Products Criteria")
        public String criteriaName;
        @Schema(example = "mifos")
        public String createdBy;
        public Collection<LoanProductData> loanProducts;
        public Collection<ProvisioningCriteriaDefinitionData> provisioningcriteria;
    }

    @Schema(description = "PutProvisioningCriteriaRequest")
    public static final class PutProvisioningCriteriaRequest {

        private PutProvisioningCriteriaRequest() {

        }

        @Schema(example = "High Risk Products Criteria")
        public String criteriaName;
        public Collection<LoanProductData> loanProducts;
        public Collection<ProvisioningCriteriaDefinitionData> provisioningcriteria;
    }

    @Schema(description = "PutProvisioningCriteriaResponse")
    public static final class PutProvisioningCriteriaResponse {

        private PutProvisioningCriteriaResponse() {

        }

        static final class PutProvisioningCriteriaResponseChanges {

            private PutProvisioningCriteriaResponseChanges() {}

            @Schema(example = "High Risk Products Criteria")
            public String criteriaName;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutProvisioningCriteriaResponseChanges changes;
    }

    @Schema(description = "DeleteProvisioningCriteriaResponse")
    public static final class DeleteProvisioningCriteriaResponse {

        private DeleteProvisioningCriteriaResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }
}
