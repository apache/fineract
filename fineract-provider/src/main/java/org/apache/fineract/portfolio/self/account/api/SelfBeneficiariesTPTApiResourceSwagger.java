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
package org.apache.fineract.portfolio.self.account.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 12/22/17.
 */
final class SelfBeneficiariesTPTApiResourceSwagger {
    private SelfBeneficiariesTPTApiResourceSwagger() {
    }

    @ApiModel(value = "GetSelfBeneficiariesTPTTemplateResponse")
    public final static class GetSelfBeneficiariesTPTTemplateResponse {
        private GetSelfBeneficiariesTPTTemplateResponse() {
        }

        final class GetSelfBeneficiariesAccountOptions {
            private GetSelfBeneficiariesAccountOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        public Set<GetSelfBeneficiariesAccountOptions> accountTypeOptions;
    }

    @ApiModel(value = "PostSelfBeneficiariesTPTRequest")
    public final static class PostSelfBeneficiariesTPTRequest {
        private PostSelfBeneficiariesTPTRequest() {
        }

        @ApiModelProperty(example = "en_GB")
        public String locale;
        @ApiModelProperty(example = "beneficiary nick name")
        public String name;
        @ApiModelProperty(example = "HEAD OFFICE")
        public String officeName;
        @ApiModelProperty(example = "0000001")
        public Long accountNumber;
        @ApiModelProperty(example = "1")
        public Integer accountType;
        @ApiModelProperty(example = "1000")
        public Integer transferLimit;
    }

    @ApiModel(value = "PostSelfBeneficiariesTPTResponse")
    public final static class PostSelfBeneficiariesTPTResponse {
        private PostSelfBeneficiariesTPTResponse() {
        }

        @ApiModelProperty(example = "5")
        public Integer resourceId;
    }

    @ApiModel(value = "PutSelfBeneficiariesTPTBeneficiaryIdRequest")
    public final static class PutSelfBeneficiariesTPTBeneficiaryIdRequest {
        private PutSelfBeneficiariesTPTBeneficiaryIdRequest() {
        }

        @ApiModelProperty(example = "beneficiary nick name")
        public String name;
        @ApiModelProperty(example = "1000")
        public Integer transferLimit;
    }

    @ApiModel(value = "PutSelfBeneficiariesTPTBeneficiaryIdResponse")
    public final static class PutSelfBeneficiariesTPTBeneficiaryIdResponse {
        private PutSelfBeneficiariesTPTBeneficiaryIdResponse() {
        }

        final class PutSelfBeneficiariesChanges {
            private PutSelfBeneficiariesChanges() {
            }

            @ApiModelProperty(example = "1000")
            public Integer transferLimit;
            @ApiModelProperty(example = "Client22")
            public String name;
        }

        @ApiModelProperty(example = "5")
        public Integer resourceId;
        public PutSelfBeneficiariesChanges changes;
    }

    @ApiModel(value = "DeleteSelfBeneficiariesTPTBeneficiaryIdResponse")
    public final static class DeleteSelfBeneficiariesTPTBeneficiaryIdResponse {
        private DeleteSelfBeneficiariesTPTBeneficiaryIdResponse() {
        }

        @ApiModelProperty(example = "5")
        public Integer resourceId;
    }

    @ApiModel(value = "GetSelfBeneficiariesTPTResponse")
    public final static class GetSelfBeneficiariesTPTResponse {
        private GetSelfBeneficiariesTPTResponse() {
        }


        @ApiModelProperty(example = "1")
        public Integer id;
        @ApiModelProperty(example = "Client2Savings")
        public String name;
        @ApiModelProperty(example = "Test Office")
        public String officeName;
        @ApiModelProperty(example = "FN2 LN2")
        public String clientName;
        public GetSelfBeneficiariesTPTTemplateResponse.GetSelfBeneficiariesAccountOptions accountType;
        @ApiModelProperty(example = "000000002")
        public Long accountNumber;
        @ApiModelProperty(example = "0")
        public Integer transferLimit;
    }
}
