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
package org.apache.fineract.infrastructure.security.api;

import io.swagger.v3.oas.annotations.media.Schema;

public final class TwoFactorApiResourceSwagger {

    private TwoFactorApiResourceSwagger() {

    }

    @Schema(description = "GetTwoFactorDeliveryMethodsResponse")
    public static final class GetTwoFactorDeliveryMethodsResponse {

        private GetTwoFactorDeliveryMethodsResponse() {

        }

        @Schema(example = "email")
        public String name;
        @Schema(example = "demomfi@mifos.org")
        public String target;
    }

    @Schema(description = "PostTwoFactorRequestTokenResponse")
    public static final class PostTwoFactorRequestTokenResponse {

        private PostTwoFactorRequestTokenResponse() {

        }

        @Schema(description = "Epoch milliseconds", example = "1752236400000")
        public Long requestTime;
        @Schema(example = "300")
        public Integer tokenLiveTimeInSec;
        @Schema(example = "false")
        public Boolean extendedAccessToken;
        public GetTwoFactorDeliveryMethodsResponse deliveryMethod;
    }

    @Schema(description = "PostTwoFactorValidateResponse")
    public static final class PostTwoFactorValidateResponse {

        private PostTwoFactorValidateResponse() {

        }

        @Schema(example = "aa3d4cfd-4bd8-4e65-97c3-2d1f5b1c7f9a")
        public String token;
        @Schema(description = "Epoch milliseconds", example = "1752236400000")
        public Long validFrom;
        @Schema(description = "Epoch milliseconds", example = "1752322800000")
        public Long validTo;
    }

    @Schema(description = "PostTwoFactorInvalidateRequest")
    public static final class PostTwoFactorInvalidateRequest {

        private PostTwoFactorInvalidateRequest() {

        }

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "aa3d4cfd-4bd8-4e65-97c3-2d1f5b1c7f9a")
        public String token;
    }

    @Schema(description = "PostTwoFactorInvalidateResponse")
    public static final class PostTwoFactorInvalidateResponse {

        private PostTwoFactorInvalidateResponse() {

        }

        @Schema(description = "The invalidated access token", example = "aa3d4cfd-4bd8-4e65-97c3-2d1f5b1c7f9a")
        public String resourceIdentifier;
    }
}
