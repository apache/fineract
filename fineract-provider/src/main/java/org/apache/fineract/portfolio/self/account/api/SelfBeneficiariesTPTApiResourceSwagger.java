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
package org.apache.fineract.portfolio.self.account.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/22/17.
 */
final class SelfBeneficiariesTPTApiResourceSwagger {

    private SelfBeneficiariesTPTApiResourceSwagger() {}

    @Schema(description = "GetSelfBeneficiariesTPTTemplateResponse")
    public static final class GetSelfBeneficiariesTPTTemplateResponse {

        private GetSelfBeneficiariesTPTTemplateResponse() {}

        static final class GetSelfBeneficiariesAccountOptions {

            private GetSelfBeneficiariesAccountOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        public Set<GetSelfBeneficiariesAccountOptions> accountTypeOptions;
    }

    @Schema(description = "PostSelfBeneficiariesTPTRequest")
    public static final class PostSelfBeneficiariesTPTRequest {

        private PostSelfBeneficiariesTPTRequest() {}

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "beneficiary nick name")
        public String name;
        @Schema(example = "HEAD OFFICE")
        public String officeName;
        @Schema(example = "0000001")
        public Long accountNumber;
        @Schema(example = "1")
        public Integer accountType;
        @Schema(example = "1000")
        public Integer transferLimit;
    }

    @Schema(description = "PostSelfBeneficiariesTPTResponse")
    public static final class PostSelfBeneficiariesTPTResponse {

        private PostSelfBeneficiariesTPTResponse() {}

        @Schema(example = "5")
        public Integer resourceId;
    }

    @Schema(description = "PutSelfBeneficiariesTPTBeneficiaryIdRequest")
    public static final class PutSelfBeneficiariesTPTBeneficiaryIdRequest {

        private PutSelfBeneficiariesTPTBeneficiaryIdRequest() {}

        @Schema(example = "beneficiary nick name")
        public String name;
        @Schema(example = "1000")
        public Integer transferLimit;
    }

    @Schema(description = "PutSelfBeneficiariesTPTBeneficiaryIdResponse")
    public static final class PutSelfBeneficiariesTPTBeneficiaryIdResponse {

        private PutSelfBeneficiariesTPTBeneficiaryIdResponse() {}

        static final class PutSelfBeneficiariesChanges {

            private PutSelfBeneficiariesChanges() {}

            @Schema(example = "1000")
            public Integer transferLimit;
            @Schema(example = "Client22")
            public String name;
        }

        @Schema(example = "5")
        public Integer resourceId;
        public PutSelfBeneficiariesChanges changes;
    }

    @Schema(description = "DeleteSelfBeneficiariesTPTBeneficiaryIdResponse")
    public static final class DeleteSelfBeneficiariesTPTBeneficiaryIdResponse {

        private DeleteSelfBeneficiariesTPTBeneficiaryIdResponse() {}

        @Schema(example = "5")
        public Integer resourceId;
    }

    @Schema(description = "GetSelfBeneficiariesTPTResponse")
    public static final class GetSelfBeneficiariesTPTResponse {

        private GetSelfBeneficiariesTPTResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Client2Savings")
        public String name;
        @Schema(example = "Test Office")
        public String officeName;
        @Schema(example = "FN2 LN2")
        public String clientName;
        public GetSelfBeneficiariesTPTTemplateResponse.GetSelfBeneficiariesAccountOptions accountType;
        @Schema(example = "000000002")
        public Long accountNumber;
        @Schema(example = "0")
        public Integer transferLimit;
    }
}
