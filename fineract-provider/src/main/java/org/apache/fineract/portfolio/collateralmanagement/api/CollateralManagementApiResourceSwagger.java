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

final class CollateralManagementApiResourceSwagger {

    private CollateralManagementApiResourceSwagger() {}

    @Schema(description = "GetCollateralManagementsResponse")
    public static final class GetCollateralManagementsResponse {

        private GetCollateralManagementsResponse() {}

        static final class GetCollateralTypeResponse {

            private GetCollateralTypeResponse() {}

            @Schema(example = "22kt")
            public String quality;

        }

        static final class GetCollateralCurrencyResponse {

            private GetCollateralCurrencyResponse() {}

            @Schema(example = "USD")
            public String code;
        }

        @Schema(example = "8")
        public Long id;
        @Schema(example = "Gold")
        public String name;
        private CollateralManagementApiResourceSwagger.GetCollateralManagementsResponse.GetCollateralTypeResponse quality;
        @Schema(example = "20000")
        public BigDecimal basePrice;
        @Schema(example = "gm")
        public String unitType;
        @Schema(example = "80")
        public BigDecimal pctToBase;
        public CollateralManagementApiResourceSwagger.GetCollateralManagementsResponse.GetCollateralCurrencyResponse currency;
    }

    @Schema(description = "GetCollateralProductTemplate")
    public static final class GetCollateralProductTemplate {

        private GetCollateralProductTemplate() {}

        static final class GetCurrencyData {

            private GetCurrencyData() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "Dollars")
            public String name;
            @Schema(example = "10")
            public int decimalPlaces;
            @Schema(example = "2")
            public Integer inMultiplesOf;
            @Schema(example = "USD")
            public String displaySymbol;
            @Schema(example = "Dollars")
            public String nameCode;
            @Schema(example = "Dollars")
            public String displayLabel;

        }

        public GetCurrencyData currency;
    }

    @Schema(description = "PostCollateralManagementProductRequest")
    public static final class PostCollateralManagementProductRequest {

        private PostCollateralManagementProductRequest() {}

        @Schema(example = "22kt")
        public String quality;
        @Schema(example = "4500")
        public BigDecimal basePrice;
        @Schema(example = "80")
        public BigDecimal pctToBase;
        @Schema(example = "gm")
        public String unitType;
        @Schema(example = "gold")
        public String name;
        @Schema(example = "USD")
        public String currency;
    }

    @Schema(description = "PostCollateralManagementProductResponse")
    public static final class PostCollateralManagementProductResponse {

        private PostCollateralManagementProductResponse() {}

        @Schema(example = "14")
        public Long resourceId;
    }

    @Schema(description = "PutCollateralProductRequest")
    public static final class PutCollateralProductRequest {

        private PutCollateralProductRequest() {}

        @Schema(example = "22kt")
        public String quality;
        @Schema(example = "4500")
        public BigDecimal basePrice;
        @Schema(example = "80")
        public BigDecimal pctToBase;
        @Schema(example = "gm")
        public String unitType;
        @Schema(example = "gold")
        public String name;
        @Schema(example = "USD")
        public String currency;

    }

    @Schema(description = "PutCollateralProductResponse")
    public static final class PutCollateralProductResponse {

        private PutCollateralProductResponse() {}

        @Schema(example = "12")
        public Long resourceId;
        public CollateralManagementApiResourceSwagger.PutCollateralProductRequest changes;
    }

    @Schema(description = "DeleteCollateralProductResponse")
    public static final class DeleteCollateralProductResponse {

        private DeleteCollateralProductResponse() {}

        @Schema(example = "12")
        public Long resourceId;
    }
}
