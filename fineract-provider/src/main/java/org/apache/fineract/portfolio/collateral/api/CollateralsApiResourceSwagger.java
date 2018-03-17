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
package org.apache.fineract.portfolio.collateral.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
final class CollateralsApiResourceSwagger {
    private CollateralsApiResourceSwagger() {
    }

    @ApiModel(value = "GetLoansLoanIdCollateralsResponse")
    public static final class GetLoansLoanIdCollateralsResponse {
        private GetLoansLoanIdCollateralsResponse() {
        }

        final class GetCollateralTypeResponse {
            private GetCollateralTypeResponse() {
            }

            @ApiModelProperty(example = "8")
            public Integer id;
            @ApiModelProperty(example = "Gold")
            public String name;
        }

        final class GetCollateralCurrencyResponse {
            private GetCollateralCurrencyResponse() {
            }

            @ApiModelProperty(example = "USD")
            public String code;
            @ApiModelProperty(example = "US Dollar")
            public String name;
            @ApiModelProperty(example = "2")
            public Integer decimalPlaces;
            @ApiModelProperty(example = "$")
            public String displaySymbol;
            @ApiModelProperty(example = "currency.USD")
            public String nameCode;
            @ApiModelProperty(example = "US Dollar ($)")
            public String displayLabel;
        }

        @ApiModelProperty(example = "12")
        public Integer id;
        public GetCollateralTypeResponse type;
        @ApiModelProperty(example = "50000")
        public Long value;
        @ApiModelProperty(example = "24 Carat Gold chain weighing 12 grams")
        public String description;
        public GetCollateralCurrencyResponse currency;
    }

    @ApiModel(value = "PostLoansLoanIdCollateralsRequest")
    public static final class PostLoansLoanIdCollateralsRequest {
        private PostLoansLoanIdCollateralsRequest() {
        }

        @ApiModelProperty(example = "9")
        public Integer collateralTypeId;
    }

    @ApiModel(value = "PostLoansLoanIdCollateralsResponse")
    public static final class PostLoansLoanIdCollateralsResponse {
        private PostLoansLoanIdCollateralsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer loanId;
        @ApiModelProperty(example = "12")
        public Integer resourceId;
    }

    @ApiModel(value = "PutLoansLoandIdCollateralsCollateralIdRequest")
    public static final class PutLoansLoandIdCollateralsCollateralIdRequest {
        private PutLoansLoandIdCollateralsCollateralIdRequest() {
        }

        @ApiModelProperty(example = "22 Carat Gold chain weighing 12 grams")
        public String description;
    }

    @ApiModel(value = "PutLoansLoanIdCollateralsCollateralIdResponse")
    public static final class PutLoansLoanIdCollateralsCollateralIdResponse {
        private PutLoansLoanIdCollateralsCollateralIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer loanId;
        @ApiModelProperty(example = "12")
        public Integer resourceId;
        public PutLoansLoandIdCollateralsCollateralIdRequest changes;
    }

    @ApiModel(value = "GetLoansLoanIdCollateralsTemplateResponse")
    public static final class GetLoansLoanIdCollateralsTemplateResponse {
        private GetLoansLoanIdCollateralsTemplateResponse() {
        }

        final class GetCollateralsTemplateAllowedTypes {
            private GetCollateralsTemplateAllowedTypes() {
            }

            @ApiModelProperty(example = "9")
            public Integer id;
            @ApiModelProperty(example = "Silver")
            public String name;
            @ApiModelProperty(example = "0")
            public Integer position;
        }

        public Set<GetCollateralsTemplateAllowedTypes> allowedCollateralTypes;
    }

    @ApiModel(value = "DeleteLoansLoanIdCollateralsCollateralIdResponse")
    public static final class DeleteLoansLoanIdCollateralsCollateralIdResponse {
        private DeleteLoansLoanIdCollateralsCollateralIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer loanId;
        @ApiModelProperty(example = "12")
        public Integer resourceId;
    }
}
