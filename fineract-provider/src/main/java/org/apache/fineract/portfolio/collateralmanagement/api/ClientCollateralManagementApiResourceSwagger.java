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
import java.time.LocalDateTime;
import java.util.Set;

final class ClientCollateralManagementApiResourceSwagger {

    private ClientCollateralManagementApiResourceSwagger() {}

    @Schema(description = "GetClientCollateralManagementsResponse")
    public static final class GetClientCollateralManagementsResponse {

        private GetClientCollateralManagementsResponse() {}

        static final class GetClientCollateralDataResponse {

            private GetClientCollateralDataResponse() {}

            @Schema(example = "Gold")
            public String name;
            @Schema(example = "1")
            public Long id;
            @Schema(example = "10")
            public BigDecimal quantity;
            @Schema(example = "10000.00")
            public BigDecimal total;
            @Schema(example = "9000.00")
            public BigDecimal totalCollateral;
            public GetClientIdResponse client;
            public Set<GetTransactionDataResponse> transaction;
        }

        static final class GetTransactionDataResponse {

            private GetTransactionDataResponse() {}

            @Schema(example = "5000.00")
            public BigDecimal lastRepayment;
            @Schema(example = "3000.00")
            public BigDecimal remainingAmount;
            @Schema(example = "1")
            public Long loanId;
            @Schema(example = "[2021, 6, 19]")
            public LocalDateTime lastRepaymentDate;
        }

        static final class GetClientIdResponse {

            private GetClientIdResponse() {}

            @Schema(example = "1")
            public Long clientId;
        }

    }

    @Schema(description = "GetLoanCollateralManagementTemplate")
    public static final class GetLoanCollateralManagementTemplate {

        private GetLoanCollateralManagementTemplate() {}

        @Schema(example = "1")
        public Long collateralId;
        @Schema(example = "10000.00")
        public BigDecimal basePrice;
        @Schema(example = "40")
        public BigDecimal pctToBase;
        @Schema(example = "10")
        public BigDecimal quantity;
        @Schema(example = "Vehicle")
        public String name;

    }

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
        public Integer resourceId;
        @Schema(example = "1")
        public Integer clientId;

    }

    @Schema(description = "PutClientCollateralRequest")
    public static final class PutClientCollateralRequest {

        private PutClientCollateralRequest() {}

        @Schema(example = "14")
        public BigDecimal quantity;
        @Schema(example = "en")
        public String locale;

    }

    @Schema(description = "PutClientCollateralResponse")
    public static final class PutClientCollateralResponse {

        private PutClientCollateralResponse() {}

        @Schema(example = "12")
        public Integer resourceId;
        @Schema(example = "1")
        public Integer clientId;
        public PutClientCollateralRequest changes;
    }

    @Schema(description = "DeleteClientCollateralResponse")
    public static final class DeleteClientCollateralResponse {

        private DeleteClientCollateralResponse() {}

        @Schema(example = "12")
        public Integer resourceId;

    }

}
