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
package org.apache.fineract.portfolio.collateral.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
final class CollateralsApiResourceSwagger {

    private CollateralsApiResourceSwagger() {}

    @Schema(description = "GetLoansLoanIdCollateralsResponse")
    public static final class GetLoansLoanIdCollateralsResponse {

        private GetLoansLoanIdCollateralsResponse() {}

        static final class GetCollateralTypeResponse {

            private GetCollateralTypeResponse() {}

            @Schema(example = "8")
            public Long id;
            @Schema(example = "Gold")
            public String name;
        }

        static final class GetCollateralCurrencyResponse {

            private GetCollateralCurrencyResponse() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        @Schema(example = "12")
        public Long id;
        public GetCollateralTypeResponse type;
        @Schema(example = "50000")
        public Long value;
        @Schema(example = "24 Carat Gold chain weighing 12 grams")
        public String description;
        public GetCollateralCurrencyResponse currency;
    }

    @Schema(description = "PostLoansLoanIdCollateralsRequest")
    public static final class PostLoansLoanIdCollateralsRequest {

        private PostLoansLoanIdCollateralsRequest() {}

        @Schema(example = "9")
        public Long collateralTypeId;
    }

    @Schema(description = "PostLoansLoanIdCollateralsResponse")
    public static final class PostLoansLoanIdCollateralsResponse {

        private PostLoansLoanIdCollateralsResponse() {}

        @Schema(example = "12")
        public Long resourceId;
    }

    @Schema(description = "PutLoansLoandIdCollateralsCollateralIdRequest")
    public static final class PutLoansLoandIdCollateralsCollateralIdRequest {

        private PutLoansLoandIdCollateralsCollateralIdRequest() {}

        @Schema(example = "22 Carat Gold chain weighing 12 grams")
        public String description;
    }

    @Schema(description = "PutLoansLoanIdCollateralsCollateralIdResponse")
    public static final class PutLoansLoanIdCollateralsCollateralIdResponse {

        private PutLoansLoanIdCollateralsCollateralIdResponse() {}

        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "12")
        public Long resourceId;
        public PutLoansLoandIdCollateralsCollateralIdRequest changes;
    }

    @Schema(description = "GetLoansLoanIdCollateralsTemplateResponse")
    public static final class GetLoansLoanIdCollateralsTemplateResponse {

        private GetLoansLoanIdCollateralsTemplateResponse() {}

        static final class GetCollateralsTemplateAllowedTypes {

            private GetCollateralsTemplateAllowedTypes() {}

            @Schema(example = "9")
            public Long id;
            @Schema(example = "Silver")
            public String name;
            @Schema(example = "0")
            public Integer position;
        }

        public Set<GetCollateralsTemplateAllowedTypes> allowedCollateralTypes;
    }

    @Schema(description = "DeleteLoansLoanIdCollateralsCollateralIdResponse")
    public static final class DeleteLoansLoanIdCollateralsCollateralIdResponse {

        private DeleteLoansLoanIdCollateralsCollateralIdResponse() {}

        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "12")
        public Long resourceId;
    }
}
