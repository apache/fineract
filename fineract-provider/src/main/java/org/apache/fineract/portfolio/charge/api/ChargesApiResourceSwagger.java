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
package org.apache.fineract.portfolio.charge.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
final class ChargesApiResourceSwagger {

    @ApiModel(value = "GetChargesResponse")
    public static final class GetChargesResponse {
        private GetChargesResponse() {
        }

        final class GetChargesCurrencyResponse {
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

        final class GetChargesTimeTypeResponse {
            private GetChargesTimeTypeResponse() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeTimeType.disbursement")
            public String code;
            @ApiModelProperty(example = "Disbursement")
            public String value;
        }

        final class GetChargesAppliesToResponse {
            private GetChargesAppliesToResponse() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeAppliesTo.loan")
            public String code;
            @ApiModelProperty(example = "Loan")
            public String value;
        }

        final class GetChargesCalculationTypeResponse {
            private GetChargesCalculationTypeResponse() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeCalculationType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        final class GetChargesPaymentModeResponse {
            private GetChargesPaymentModeResponse() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargepaymentmode.accounttransfer")
            public String code;
            @ApiModelProperty(example = "Account Transfer")
            public String value;
        }

        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Loan Service fee")
        public String name;
        @ApiModelProperty(example = "true")
        public String active;
        @ApiModelProperty(example = "false")
        public String penalty;
        public GetChargesCurrencyResponse currency;
        @ApiModelProperty(example = "230.56")
        public Float amount;
        public GetChargesTimeTypeResponse chargeTimeType;
        public GetChargesAppliesToResponse chargeAppliesTo;
        public GetChargesCalculationTypeResponse chargeCalculationType;
        public GetChargesPaymentModeResponse chargePaymentMode;
    }

    @ApiModel(value = "PostChargesRequest")
    public static final class PostChargesRequest {
        private PostChargesRequest() {
        }

        @ApiModelProperty(example = "Loan Service fee")
        public String name;
        @ApiModelProperty(example = "1")
        public Integer chargeAppliesTo;
        @ApiModelProperty(example = "USD")
        public String currencyCode;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "230.56")
        public Float amount;
        @ApiModelProperty(example = "1")
        public Integer chargeTimeType;
        @ApiModelProperty(example = "1")
        public Integer chargeCalculationType;
        @ApiModelProperty(example = "1")
        public Integer chargePaymentMode;
        @ApiModelProperty(example = "true")
        public String active;
    }

    @ApiModel(value = "PostChargesResponse")
    public static final class PostChargesResponse {
        private PostChargesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutChargesChargeIdRequest")
    public static final class PutChargesChargeIdRequest {
        private PutChargesChargeIdRequest() {
        }

        @ApiModelProperty(example = "Loan service fee(changed)")
        public String name;
    }

    @ApiModel(value = "PutChargesChargeIdResponse")
    public static final class PutChargesChargeIdResponse {
        private PutChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
        public PutChargesChargeIdRequest changes;
    }

    @ApiModel(value = "DeleteChargesChargeIdResponse")
    public static final class DeleteChargesChargeIdResponse {
        private DeleteChargesChargeIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "GetChargesTemplateResponse")
    public static final class GetChargesTemplateResponse {
        private GetChargesTemplateResponse() {
        }

        final class GetChargesTemplateLoanChargeCalculationTypeOptions {
            private GetChargesTemplateLoanChargeCalculationTypeOptions() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "chargeCalculationType.flat")
            public String code;
            @ApiModelProperty(example = "Flat")
            public String value;
        }

        final class GetChargesTemplateLoanChargeTimeTypeOptions {
            private GetChargesTemplateLoanChargeTimeTypeOptions() {
            }

            @ApiModelProperty(example = "2")
            public Integer id;
            @ApiModelProperty(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @ApiModelProperty(example = "Specified due date")
            public String value;
        }
        final class GetChargesTemplateFeeFrequencyOptions{
            private GetChargesTemplateFeeFrequencyOptions(){}
            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "loanTermFrequency.periodFrequencyType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
        }
        @ApiModelProperty(example = "false")
        public String active;
        @ApiModelProperty(example = "false")
        public String penalty;
        public Set<GetChargesResponse.GetChargesCurrencyResponse> currencyOptions;
        public Set<GetChargesResponse.GetChargesCalculationTypeResponse> chargeCalculationTypeOptions;
        public Set<GetChargesResponse.GetChargesAppliesToResponse> chargeAppliesToOptions;
        public Set<GetChargesResponse.GetChargesTimeTypeResponse> chargeTimeTypeOptions;
        public Set<GetChargesResponse.GetChargesPaymentModeResponse> chargePaymentModeOptions;
        public Set<GetChargesTemplateLoanChargeCalculationTypeOptions> loanChargeCalculationTypeOptions;
        public Set<GetChargesTemplateLoanChargeTimeTypeOptions> loanChargeTimeTypeOptions;
        public Set<GetChargesTemplateLoanChargeCalculationTypeOptions> savingsChargeCalculationTypeOptions;
        public Set<GetChargesTemplateLoanChargeTimeTypeOptions> savingsChargeTimeTypeOptions;
        public Set<GetChargesTemplateFeeFrequencyOptions> feeFrequencyOptions;
    }
}
