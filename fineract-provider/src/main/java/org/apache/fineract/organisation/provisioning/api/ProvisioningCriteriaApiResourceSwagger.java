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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

import java.util.Collection;

/**
 * Created by sanyam on 18/8/17.
 */
final class ProvisioningCriteriaApiResourceSwagger {
    private ProvisioningCriteriaApiResourceSwagger() {

    }

    @ApiModel(value = "PostProvisioningCriteriaRequest")
    public static final class PostProvisioningCriteriaRequest {
        private PostProvisioningCriteriaRequest() {

        }
        @ApiModelProperty(example = "High Risk Products Criteria")
        public String criteriaName;
        public Collection<LoanProductData> loanProducts;
        public Collection<ProvisioningCriteriaDefinitionData> provisioningcriteria;
    }

    @ApiModel(value = "PostProvisioningCriteriaResponse")
    public static final class PostProvisioningCriteriaResponse {
        private PostProvisioningCriteriaResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "GetProvisioningCriteriaResponse")
    public static final class GetProvisioningCriteriaResponse {
        private GetProvisioningCriteriaResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long criteriaId;
        @ApiModelProperty(example = "High Risk Products Criteria")
        public String criteriaName;
        @ApiModelProperty(example = "mifos")
        public String createdBy;
    }

    @ApiModel(value = "GetProvisioningCriteriaCriteriaIdResponse")
    public static final class GetProvisioningCriteriaCriteriaIdResponse {
        private GetProvisioningCriteriaCriteriaIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long criteriaId;
        @ApiModelProperty(example = "High Risk Products Criteria")
        public String criteriaName;
        @ApiModelProperty(example = "mifos")
        public String createdBy;
        public Collection<LoanProductData> loanProducts;
        public Collection<ProvisioningCriteriaDefinitionData> provisioningcriteria;
    }

    @ApiModel(value = "PutProvisioningCriteriaRequest")
    public static final class PutProvisioningCriteriaRequest {
        private PutProvisioningCriteriaRequest() {

        }
        @ApiModelProperty(example = "High Risk Products Criteria")
        public String criteriaName;
        public Collection<LoanProductData> loanProducts;
        public Collection<ProvisioningCriteriaDefinitionData> provisioningcriteria;
    }

    @ApiModel(value = "PutProvisioningCriteriaResponse")
    public static final class PutProvisioningCriteriaResponse {
        private PutProvisioningCriteriaResponse() {

        }
        final class PutProvisioningCriteriaResponseChanges{
            private PutProvisioningCriteriaResponseChanges(){}
            @ApiModelProperty(example = "High Risk Products Criteria")
            public String criteriaName;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        public PutProvisioningCriteriaResponseChanges changes;
    }

    @ApiModel(value = "DeleteProvisioningCriteriaResponse")
    public static final class DeleteProvisioningCriteriaResponse {
        private DeleteProvisioningCriteriaResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }
}
