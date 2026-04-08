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
package org.apache.fineract.portfolio.collateralmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.apache.fineract.portfolio.collateralmanagement.data.UpdateClientCollateralRequest;

final class ClientCollateralManagementApiResourceSwagger {

    private ClientCollateralManagementApiResourceSwagger() {}

    @Schema(description = "PostClientCollateralRequest")
    public static final class PostClientCollateralRequest {

        private PostClientCollateralRequest() {}

        @Schema(example = "10")
        public BigDecimal quantity;
        @Schema(example = "1")
        public Long collateralId;
        @Schema(example = "en")
        public String locale;

    }

    @Schema(description = "PostClientCollateralResponse")
    public static final class PostClientCollateralResponse {

        private PostClientCollateralResponse() {}

        @Schema(example = "14")
        public Long resourceId;
        @Schema(example = "1")
        public Long clientId;

    }

    @Schema(description = "PutClientCollateralResponse")
    public static final class PutClientCollateralResponse {

        private PutClientCollateralResponse() {}

        @Schema(example = "12")
        public Long resourceId;
        @Schema(example = "1")
        public Long clientId;
        public UpdateClientCollateralRequest changes;
    }

    @Schema(description = "DeleteClientCollateralResponse")
    public static final class DeleteClientCollateralResponse {

        private DeleteClientCollateralResponse() {}

        @Schema(example = "12")
        public Long resourceId;

    }

}
